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
import java.nio.ByteBuffer;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import org.oasisopen.sca.ServiceRuntimeException;
import org.zeromq.ZMQ;
import org.zeromq.ZMQ.Context;
import org.zeromq.ZMQ.Socket;

import org.fabric3.binding.zeromq.runtime.SocketAddress;
import org.fabric3.spi.invocation.CallFrame;
import org.fabric3.spi.invocation.ConversationContext;
import org.fabric3.spi.invocation.F3Conversation;
import org.fabric3.spi.invocation.Message;
import org.fabric3.spi.invocation.MessageImpl;
import org.fabric3.spi.invocation.WorkContext;
import org.fabric3.spi.wire.Interceptor;

/**
 * Implementation of a {@link Receiver} with no qualities of service.
 * <p/>
 * Since ZeroMQ requires the creating socket thread to receive messages, a polling thread is used for reading messages from the ZeroMQ socket. The
 * receiver listens for address updates (e.g. a sender coming online or going away). Since ZeroMQ does not implement disconnect semantics on a socket,
 * if an update is received the original socket will be closed and a new one created to connect to the update set of addresses.
 *
 * @version $Revision: 10396 $ $Date: 2011-03-15 18:20:58 +0100 (Tue, 15 Mar 2011) $
 */
public class NonReliableRequestReplyReceiver implements Receiver, Thread.UncaughtExceptionHandler {

    private Context context;
    private SocketAddress address;
    private Interceptor singleInterceptor;
    private Interceptor[] interceptors;
    private MessagingMonitor monitor;


    private Receiver receiver;
    private String callbackUri;

    public NonReliableRequestReplyReceiver(Context context,
                                           SocketAddress address,
                                           List<Interceptor> interceptors,
                                           String callbackUri,
                                           MessagingMonitor monitor) {
        this.context = context;
        this.address = address;
        if (interceptors.size() == 1) {
            singleInterceptor = interceptors.get(0);
        } else {
            this.interceptors = interceptors.toArray(new Interceptor[interceptors.size()]);
        }
        this.callbackUri = callbackUri;
        this.monitor = monitor;
    }

    public void start() {
        receiver = new Receiver();
        schedule();

    }

    public void stop() {
        receiver.stop();
    }

    public void uncaughtException(Thread t, Throwable e) {
        monitor.error(e);
    }

    private void schedule() {
        // TODO use runtime thread pool
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
    private WorkContext createWorkContext(byte[] header) {
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
                CallFrame frame = new CallFrame(callbackUri, id, conversation, context);
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


    /**
     * The message receiver. Responsible for creating socket connections to publishers and polling for messages.
     */
    private class Receiver implements Runnable {
        private Socket socket;
        private ZMQ.Poller poller;
        private AtomicBoolean active = new AtomicBoolean(true);

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
                bind();

                while (active.get()) {
                    long val = poller.poll();
                    if (val > 0) {
                        byte[] clientId = socket.recv(0);

                        byte[] messageId = socket.recv(0);
                        byte[] contextHeader = socket.recv(0);
                        WorkContext context = createWorkContext(contextHeader);
                        Message request = new MessageImpl();
                        request.setWorkContext(context);
                        if (singleInterceptor != null) {
                            invokeAndReply(request, clientId, messageId, singleInterceptor);
                        } else {
                            ByteBuffer buffer = ByteBuffer.wrap(socket.recv(0));
                            int methodIndex = buffer.getInt();
                            Interceptor interceptor = interceptors[methodIndex];
                            invokeAndReply(request, clientId, messageId, interceptor);
                        }
                    }
                }
            } catch (RuntimeException e) {
                // exception, make sure the thread is rescheduled
                schedule();
                throw e;
            }

        }

        private void bind() {
            socket = context.socket(ZMQ.XREP);
            socket.bind(address.toProtocolString());
            poller = context.poller();
            poller.register(socket);
        }

        private void invokeAndReply(Message request, byte[] clientId, byte[] messageId, Interceptor interceptor) {
            byte[] body = socket.recv(0);
            request.setBody(body);
            Message response = interceptor.invoke(request);
            Object responseBody = response.getBody();
            if (!(responseBody instanceof byte[])) {
                throw new ServiceRuntimeException("Return value not serialized");
            }
            socket.send(clientId, ZMQ.SNDMORE);
            socket.send(messageId, ZMQ.SNDMORE);
            socket.send((byte[]) responseBody, 0);
        }
    }


}
