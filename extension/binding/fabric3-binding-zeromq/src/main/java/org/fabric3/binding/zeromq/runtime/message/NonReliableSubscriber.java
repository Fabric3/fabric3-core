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

import java.net.URI;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import org.zeromq.ZMQ;
import org.zeromq.ZMQ.Context;
import org.zeromq.ZMQ.Socket;

import org.fabric3.binding.zeromq.runtime.SocketAddress;
import org.fabric3.binding.zeromq.runtime.federation.AddressListener;
import org.fabric3.binding.zeromq.runtime.handler.AsyncFanOutHandler;
import org.fabric3.spi.channel.ChannelConnection;
import org.fabric3.spi.channel.EventStreamHandler;

/**
 * Implements a basic SUB server with no qualities of service.
 * <p/>
 * Since ZeroMQ requires the creating socket thread to receive messages, a polling thread is used for connecting to one or more publishers and
 * receiving messages. The subscriber listens for address updates (e.g. a publisher coming online or going away). Since ZeroMQ does not implement
 * disconnect semantics on a socket, if an update is received the original socket will be closed and a new one created to connect to the update set of
 * addresses.
 *
 * @version $Revision: 10396 $ $Date: 2011-03-15 18:20:58 +0100 (Tue, 15 Mar 2011) $
 */
public class NonReliableSubscriber implements Subscriber, AddressListener, Thread.UncaughtExceptionHandler {
    private static final byte[] EMPTY_BYTES = new byte[0];

    private String id;
    private Context context;
    private List<SocketAddress> addresses;
    private EventStreamHandler handler;
    private MessagingMonitor monitor;

    private AsyncFanOutHandler fanOutHandler;

    private AtomicInteger connectionCount = new AtomicInteger();

    private Receiver receiver;

    public NonReliableSubscriber(String id,
                                 Context context,
                                 List<SocketAddress> addresses,
                                 EventStreamHandler head,
                                 MessagingMonitor monitor) {
        this.id = id;
        this.context = context;
        this.addresses = addresses;
        this.handler = head;
        this.monitor = monitor;
        EventStreamHandler current = handler;
        setFanOutHandler(current);
    }

    public void start() {
        receiver = new Receiver();
        schedule();

    }

    public void stop() {
        receiver.stop();
    }

    public void addConnection(URI subscriberId, ChannelConnection connection) {
        fanOutHandler.addConnection(subscriberId, connection);
        connectionCount.incrementAndGet();
    }

    public void removeConnection(URI subscriberId) {
        fanOutHandler.removeConnection(subscriberId);
        connectionCount.decrementAndGet();
    }

    public boolean hasConnections() {
        return connectionCount.get() > 0;
    }

    public void uncaughtException(Thread t, Throwable e) {
        monitor.error(e);
    }

    public String getId() {
        return id;
    }

    public void onUpdate(List<SocketAddress> addresses) {
        // refresh socket
        this.addresses = addresses;
        receiver.refresh();
    }

    private void setFanOutHandler(EventStreamHandler current) {
        while (current != null) {
            if (current instanceof AsyncFanOutHandler) {
                fanOutHandler = (AsyncFanOutHandler) current;
                break;
            }
            current = current.getNext();
        }
        if (fanOutHandler == null) {
            throw new AssertionError("Fanout handler not added to subscriber");
        }
    }

    private void schedule() {
        // TODO use runtime thread pool
        Thread thread = new Thread(receiver);
        thread.setUncaughtExceptionHandler(this);
        thread.start();
    }


    /**
     * The message receiver. Responsible for creating socket connections to publishers and polling for messages.
     */
    private class Receiver implements Runnable {
        private Socket socket;
        private ZMQ.Poller poller;
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
        public synchronized void stop() {
            active.set(false);
            if (socket != null) {
                socket.close();
            }
        }

        public void run() {
            try {
                while (active.get()) {
                    reconnect();
                    long val = poller.poll();
                    if (val > 0) {
                        byte[] payload = socket.recv(0);
                        handler.handle(payload);
                    }
                }
            } catch (RuntimeException e) {
                // exception, make sure the thread is rescheduled
                schedule();
                throw e;
            }

        }

        /**
         * Closes an existing socket and creates a new one, binding it to the list of active publisher endpoints.
         */
        private synchronized void reconnect() {
            if (!doRefresh.getAndSet(false)) {
                return;
            }
            if (socket != null) {
                socket.close();
            }
            socket = context.socket(ZMQ.SUB);
            socket.subscribe(EMPTY_BYTES);    // receive all messages

            for (SocketAddress address : addresses) {
                socket.connect(address.toProtocolString());
            }
            poller = context.poller();
            poller.register(socket);
        }
    }


}
