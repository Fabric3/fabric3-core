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
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

import org.zeromq.ZMQ;
import org.zeromq.ZMQ.Context;
import org.zeromq.ZMQ.Socket;

import org.fabric3.binding.zeromq.runtime.SocketAddress;

/**
 * Implements a basic PUB client with no qualities of service.
 * <p/>
 * Since ZeroMQ requires the creating socket thread to dispatch messages, a looping thread is used for publishing messages. Messages are provided to
 * the thread via a queue.
 *
 * @version $Revision$ $Date$
 */
public class NonReliablePublisher implements Publisher, Thread.UncaughtExceptionHandler {
    private Context context;
    private SocketAddress address;
    private MessagingMonitor monitor;

    private Socket socket;
    private Dispatcher dispatcher;

    private LinkedBlockingQueue<byte[]> queue;

    public void start() {
        dispatcher = new Dispatcher();
        schedule();
    }

    public void stop() {
        dispatcher.stop();
    }

    public NonReliablePublisher(Context context, SocketAddress address, MessagingMonitor monitor) {
        this.context = context;
        this.address = address;
        this.monitor = monitor;
        this.queue = new LinkedBlockingQueue<byte[]>();
    }

    public void publish(byte[] message) {
        try {
            queue.put(message);
        } catch (InterruptedException e) {
            Thread.interrupted();
        }
    }

    public void uncaughtException(Thread t, Throwable e) {
        monitor.error(e);
    }

    private void schedule() {
        // TODO use runtime thread pool
        Thread thread = new Thread(dispatcher);
        thread.setUncaughtExceptionHandler(this);
        thread.start();
    }

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
                try {
                    List<byte[]> drained = new ArrayList<byte[]>();
                    queue.drainTo(drained);
                    for (byte[] bytes : drained) {
                        socket.send(bytes, 0);
                    }
                } catch (RuntimeException e) {
                    // exception, make sure the thread is rescheduled
                    schedule();
                    throw e;
                }

            }

        }
    }


}
