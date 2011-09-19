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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

import org.oasisopen.sca.ServiceRuntimeException;
import org.zeromq.ZMQ;
import org.zeromq.ZMQ.Socket;

import org.fabric3.binding.zeromq.common.ZeroMQMetadata;
import org.fabric3.binding.zeromq.runtime.MessagingMonitor;
import org.fabric3.binding.zeromq.runtime.SocketAddress;
import org.fabric3.binding.zeromq.runtime.context.ContextManager;
import org.fabric3.spi.host.Port;
import org.fabric3.spi.invocation.CallFrame;
import org.fabric3.spi.invocation.ConversationContext;
import org.fabric3.spi.invocation.F3Conversation;
import org.fabric3.spi.invocation.WorkContext;
import org.fabric3.spi.wire.Interceptor;
import org.fabric3.spi.wire.InvocationChain;

/**
 * @version $Revision: 10396 $ $Date: 2011-03-15 18:20:58 +0100 (Tue, 15 Mar 2011) $
 */
public abstract class AbstractReceiver extends AbstractStatistics implements Receiver, Thread.UncaughtExceptionHandler {

    protected ContextManager manager;
    protected SocketAddress address;
    protected int socketType;

    protected Interceptor[] interceptors;
    protected MessagingMonitor monitor;


    protected Receiver receiver;
    protected long pollTimeout;
    protected ZeroMQMetadata metadata;
    protected String id = getClass().getName() + ":" + UUID.randomUUID().toString();

    /**
     * Constructor.
     *
     * @param manager     the ZeroMQ Context manager
     * @param address     the address to receive messages on
     * @param chains      the invocation chains for dispatching invocations
     * @param metadata    metadata
     * @param socketType  the socket type as defined by ZeroMQ
     * @param pollTimeout timeout for polling operations in microseconds
     * @param monitor     the monitor
     */
    public AbstractReceiver(ContextManager manager,
                            SocketAddress address,
                            List<InvocationChain> chains,
                            int socketType,
                            long pollTimeout,
                            ZeroMQMetadata metadata,
                            MessagingMonitor monitor) {
        this.manager = manager;
        this.address = address;
        this.pollTimeout = pollTimeout;
        this.interceptors = new Interceptor[chains.size()];
        for (int i = 0, chainsSize = chains.size(); i < chainsSize; i++) {
            InvocationChain chain = chains.get(i);
            interceptors[i] = chain.getHeadInterceptor();
        }
        this.socketType = socketType;
        this.metadata = metadata;
        this.monitor = monitor;
    }

    public void start() {
        if (receiver == null) {
            receiver = new Receiver();
            schedule();
        }
    }

    public void stop() {
        try {
            receiver.stop();
        } finally {
            receiver = null;
        }
    }

    public void uncaughtException(Thread t, Throwable e) {
        monitor.error(e);
    }

    private void schedule() {
        Thread thread = new Thread(receiver);
        thread.setUncaughtExceptionHandler(this);
        thread.start();
    }

    /**
     * Creates a WorkContext for the request by deserializing the callframe stack
     * <p/>
     * client that is wired to it. Otherwise, it is null.
     *
     * @param header the serialized work context header
     * @return the work context
     */
    @SuppressWarnings({"unchecked"})
    protected WorkContext createWorkContext(byte[] header) {
        try {
            WorkContext workContext = new WorkContext();
            if (header == null) {
                // no callframe found, use a blank one
                return workContext;
            }
            ByteArrayInputStream bas = new ByteArrayInputStream(header);
            ObjectInputStream stream = new ObjectInputStream(bas);
            List<CallFrame> stack = (List<CallFrame>) stream.readObject();
            workContext.addCallFrames(stack);
            stream.close();
            CallFrame previous = workContext.peekCallFrame();
            if (previous != null) {
                // Copy correlation and conversation information from incoming frame to new frame
                // Note that the callback URI is set to the callback address of this service so its callback wire can be mapped in the case of a
                // bidirectional service
                Serializable id = previous.getCorrelationId(Serializable.class);
                ConversationContext context = previous.getConversationContext();
                F3Conversation conversation = previous.getConversation();
                String callback = previous.getCallbackUri();
                CallFrame frame = new CallFrame(callback, id, conversation, context);
                stack.add(frame);
            } else {
                workContext.addCallFrame(CallFrame.STATELESS_FRAME);
            }
            return workContext;
        } catch (IOException e) {
            throw new ServiceRuntimeException("Error deserializing callframe", e);
        } catch (ClassNotFoundException e) {
            throw new ServiceRuntimeException("Error deserializing callframe", e);
        }
    }

    protected abstract boolean invoke(Socket socket);

    protected abstract void response(Socket socket);

    /**
     * The message receiver. Responsible for creating socket connections to publishers and polling for messages.
     */
    private class Receiver implements Runnable {
        private Socket socket;
        private ZMQ.Poller poller;
        private AtomicBoolean active = new AtomicBoolean(true);

        /**
         * Signals to stops polling and close the receiver socket, if one is open. Note that the socket cannot be closed in this method, as it will be
         * called on a different thread than {@link #run()}, which opened the socket. ZeroMQ requires a socket only be accessed by the thread that
         * created it.
         */
        public synchronized void stop() {
            active.set(false);
        }

        public void run() {
            try {
                bind();
                startStatistics();
                while (active.get()) {
                    if (poller == null) {
                        // the socket or poller could not be created, abort
                        monitor.error("Failed to initialize ZeroMQ socket, aborting receiver");
                        return;
                    }
                    long val = poller.poll(pollTimeout);
                    if (val > 0) {
                        if (!invoke(socket)){
                            continue;
                        }
                        response(socket);
                        messagesProcessed.incrementAndGet();
                    }
                }
                // the socket must be closed here on this thread!
                if (socket != null) {
                    try {
                        socket.close();
                    } finally {
                        manager.release(id);
                    }
                    socket = null;
                }
            } catch (RuntimeException e) {
                // exception, make sure the thread is rescheduled
                manager.release(id);
                schedule();
                throw e;
            }

        }

        private void bind() {
            if (socket != null) {
                // Socket is still active, ignore. This can happen if bind is called after the receiver has been rescheduled
                return;
            }
            manager.reserve(id);
            socket = manager.getContext().socket(socketType);
            SocketHelper.configure(socket, metadata);
            address.getPort().bind(Port.TYPE.TCP);
            socket.bind(address.toProtocolString());
            poller = manager.getContext().poller();
            poller.register(socket, ZMQ.Poller.POLLIN);
        }

    }


}
