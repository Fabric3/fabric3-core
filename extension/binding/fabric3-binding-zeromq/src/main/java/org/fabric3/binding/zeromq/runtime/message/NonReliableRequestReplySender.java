/*
 * Fabric3 Copyright (c) 2009-2011 Metaform Systems
 * 
 * Fabric3 is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version, with the following exception:
 * 
 * Linking this software statically or dynamically with other modules is making
 * a combined work based on this software. Thus, the terms and conditions of the
 * GNU General Public License cover the whole combination.
 * 
 * As a special exception, the copyright holders of this software give you
 * permission to link this software with independent modules to produce an
 * executable, regardless of the license terms of these independent modules, and
 * to copy and distribute the resulting executable under terms of your choice,
 * provided that you also meet, for each linked independent module, the terms
 * and conditions of the license of that module. An independent module is a
 * module which is not derived from or based on this software. If you modify
 * this software, you may extend this exception to your version of the software,
 * but you are not obligated to do so. If you do not wish to do so, delete this
 * exception statement from your version.
 * 
 * Fabric3 is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * Fabric3. If not, see <http://www.gnu.org/licenses/>.
 */
package org.fabric3.binding.zeromq.runtime.message;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;

import org.oasisopen.sca.ServiceRuntimeException;
import org.oasisopen.sca.ServiceUnavailableException;
import org.zeromq.ZMQ;
import org.zeromq.ZMQ.Context;
import org.zeromq.ZMQ.Socket;

import org.fabric3.binding.zeromq.runtime.SocketAddress;

/**
 * A {@link RequestReplySender} that provides no qualities of service.
 * <p/>
 * Since ZeroMQ requires the creating socket thread to dispatch messages, a looping thread is used for sending messages. Messages are provided to the
 * thread via a queue.
 *
 * @version $Revision$ $Date$
 */
public class NonReliableRequestReplySender implements RequestReplySender, Thread.UncaughtExceptionHandler {
    private static final Callable<byte[]> CALLABLE = new Callable<byte[]>() {
        public byte[] call() throws Exception {
            return null;
        }
    };

    private Context context;
    private SocketAddress address;
    private MessagingMonitor monitor;

    private Socket socket;
    private Dispatcher dispatcher;
    private LinkedBlockingQueue<Request> queue;

    public NonReliableRequestReplySender(Context context, SocketAddress address, MessagingMonitor monitor) {
        this.address = address;
        this.context = context;
        this.monitor = monitor;
        queue = new LinkedBlockingQueue<Request>();
    }

    public void start() {
        dispatcher = new Dispatcher();
        // TODO use runtime thread pool
        Thread thread = new Thread(dispatcher);
        thread.setUncaughtExceptionHandler(this);
        thread.start();
    }

    public void stop() {
        dispatcher.stop();
    }

    public byte[] send(byte[] message) {
        try {
            Request future = new Request(message);
            return future.get(10000, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            Thread.interrupted();
            throw new ServiceRuntimeException(e);
        } catch (ExecutionException e) {
            throw new ServiceRuntimeException(e);
        } catch (TimeoutException e) {
            throw new ServiceUnavailableException(e);
        }
    }

    public void uncaughtException(Thread t, Throwable e) {
        monitor.error(e);
    }

    /**
     * Dispatches requests to the ZeroMQ socket.
     */
    private class Dispatcher implements Runnable {
        private AtomicBoolean active = new AtomicBoolean(true);

        public void stop() {
            active.set(false);
            if (socket != null) {
                socket.close();
            }
        }

        public void run() {
            socket = context.socket(ZMQ.PUB);
            socket.bind(address.toProtocolString());

            while (active.get()) {
                List<Request> drained = new ArrayList<Request>();
                queue.drainTo(drained);
                for (Request request : drained) {
                    socket.send(request.getPayload(), 0);
                    byte[] response = socket.recv(1);
                    request.set(response);
                    request.run();
                }
            }
        }
    }

    /**
     * A {@link Future} used to pass a request payload to the ZeroMQ socket thread and retrieve the invocation return value on completion.
     */
    private class Request extends FutureTask<byte[]> {
        private byte[] payload;

        public Request(byte[] payload) {
            super(CALLABLE);
            this.payload = payload;
        }

        public byte[] getPayload() {
            return payload;
        }

        @Override
        public void set(byte[] s) {
            super.set(s);
        }
    }


}
