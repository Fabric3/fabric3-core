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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

import org.oasisopen.sca.ServiceRuntimeException;
import org.zeromq.ZMQ;

import org.fabric3.binding.zeromq.runtime.SocketAddress;
import org.fabric3.spi.invocation.CallFrame;
import org.fabric3.spi.invocation.WorkContext;

/**
 * @version $Revision: 10212 $ $Date: 2011-03-15 18:20:58 +0100 (Tue, 15 Mar 2011) $
 */
public class NonReliableOneWaySender implements OneWaySender, Thread.UncaughtExceptionHandler {


    private String id;
    private ZMQ.Context context;
    private List<SocketAddress> addresses;
    private MessagingMonitor monitor;

    private SocketMultiplexer multiplexer;
    private Dispatcher dispatcher;

    private LinkedBlockingQueue<Request> queue;

    public NonReliableOneWaySender(String id, ZMQ.Context context, List<SocketAddress> addresses, MessagingMonitor monitor) {
        this.id = id;
        this.addresses = addresses;
        this.context = context;
        this.monitor = monitor;
        queue = new LinkedBlockingQueue<Request>();
        multiplexer = new RoundRobinSocketMultiplexer(context, ZMQ.PULL);
    }

    public void start() {
        dispatcher = new Dispatcher();
        schedule();
    }

    public void stop() {
        dispatcher.stop();
    }

    public String getId() {
        return id;
    }

    public void onUpdate(List<SocketAddress> addresses) {
        // refresh socket
        this.addresses = addresses;
        dispatcher.refresh();
    }

    public void send(byte[] message, int index, WorkContext workContext) {
        try {
            Request request = new Request(message, index, workContext);
            queue.put(request);
        } catch (InterruptedException e) {
            Thread.interrupted();
            throw new ServiceRuntimeException(e);
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

    /**
     * Dispatches requests to the ZeroMQ socket.
     */
    private class Dispatcher implements Runnable {
        private AtomicBoolean active = new AtomicBoolean(true);
        private AtomicBoolean doRefresh = new AtomicBoolean(true);

        /**
         * Signals to closes the old socket and establish a new one when publisher addresses have changed in the domain.
         */
        public void refresh() {
            doRefresh.set(true);
        }

        /**
         * Stops polling and closes the existing socket.
         */
        public void stop() {
            active.set(false);
            if (multiplexer != null) {
                multiplexer.close();
            }
        }

        public void run() {
            while (active.get()) {
                try {
                    reconnect();

                    // handle pending requests
                    List<Request> drained = new ArrayList<Request>();
                    queue.drainTo(drained);
                    for (Request request : drained) {
                        // serialize the work context as a header
                        byte[] serializedWork = serialize(request.getWorkContext());
                        ZMQ.Socket socket = multiplexer.get();
                        socket.send(serializedWork, ZMQ.SNDMORE);

                        // serialize the operation index
                        int index = request.getIndex();
                        if (index >= 0) {
                            byte[] serializedIndex = ByteBuffer.allocate(4).putInt(index).array();
                            socket.send(serializedIndex, ZMQ.SNDMORE);
                        }

                        // serialize the request payload
                        socket.send(request.getPayload(), 0);
                    }
                } catch (RuntimeException e) {
                    // exception, make sure the thread is rescheduled
                    schedule();
                    throw e;
                } catch (IOException e) {
                    monitor.error(e);
                }

            }
        }

        /**
         * Serializes the work context.
         *
         * @param workContext the work context
         * @return the serialized work context
         * @throws IOException if a serialization error is encountered
         */
        private byte[] serialize(WorkContext workContext) throws IOException {
            List<CallFrame> stack = workContext.getCallFrameStack();
            ByteArrayOutputStream bas = new ByteArrayOutputStream();
            ObjectOutputStream stream = new ObjectOutputStream(bas);
            stream.writeObject(stack);
            stream.close();
            return bas.toByteArray();
        }

        /**
         * Closes an existing socket and creates a new one, binding it to the list of active service endpoints.
         */
        private synchronized void reconnect() {
            if (!doRefresh.getAndSet(false)) {
                return;
            }

//            handle update better. Need to only close connections that are no longer active. This means have to cache address so a compare can be made abd the connections closed

            if (multiplexer != null) {
                multiplexer.close();
            }


            multiplexer.update(addresses);
        }
    }

    private class Request {
        private byte[] payload;
        private WorkContext workContext;
        private int index;

        public Request(byte[] payload, int index, WorkContext workContext) {
            this.payload = payload;
            this.index = index;
            this.workContext = workContext;
        }

        public byte[] getPayload() {
            return payload;
        }

        public int getIndex() {
            return index;
        }

        public WorkContext getWorkContext() {
            return workContext;
        }

    }


}
