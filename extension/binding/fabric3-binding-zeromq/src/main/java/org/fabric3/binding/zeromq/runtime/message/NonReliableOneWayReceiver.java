/*
 * Fabric3 Copyright (c) 2009-2013 Metaform Systems
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

import java.nio.ByteBuffer;
import java.util.List;
import java.util.concurrent.ExecutorService;

import org.fabric3.api.annotation.management.Management;
import org.fabric3.binding.zeromq.common.ZeroMQMetadata;
import org.fabric3.binding.zeromq.runtime.MessagingMonitor;
import org.fabric3.binding.zeromq.runtime.SocketAddress;
import org.fabric3.binding.zeromq.runtime.context.ContextManager;
import org.fabric3.spi.invocation.Message;
import org.fabric3.spi.invocation.MessageCache;
import org.fabric3.spi.invocation.WorkContext;
import org.fabric3.spi.wire.Interceptor;
import org.fabric3.spi.wire.InvocationChain;
import org.zeromq.ZMQ;

/**
 * Implementation of a {@link Receiver} the implements one-way with no qualities of service.
 * <p/>
 * Since ZeroMQ requires the creating socket thread to receive messages, a polling thread is used for reading messages from the ZeroMQ socket. The receiver
 * listens for address updates (e.g. a sender coming online or going away). Since ZeroMQ does not implement disconnect semantics on a socket, if an update is
 * received the original socket will be closed and a new one created to connect to the updated set of addresses.
 */
@Management
public class NonReliableOneWayReceiver extends AbstractReceiver implements Thread.UncaughtExceptionHandler {
    private ExecutorService executorService;

    /**
     * Constructor.
     *
     * @param manager         the ZeroMQ Context manager
     * @param address         the address to receive messages on
     * @param chains          the invocation chains for dispatching invocations
     * @param executorService the runtime executor service
     * @param metadata        metadata
     * @param pollTimeout     timeout for polling operations in microseconds
     * @param monitor         the monitor
     */
    public NonReliableOneWayReceiver(ContextManager manager,
                                     SocketAddress address,
                                     List<InvocationChain> chains,
                                     ExecutorService executorService,
                                     ZeroMQMetadata metadata,
                                     long pollTimeout,
                                     MessagingMonitor monitor) {
        super(manager, address, chains, ZMQ.PULL, pollTimeout, metadata, monitor);
        this.executorService = executorService;
    }

    @Override
    protected boolean invoke(ZMQ.Socket socket) {
        final byte[][] frames = new byte[3][];
        int i = 1;
        frames[0] = socket.recv(0);
        while (socket.hasReceiveMore()) {
            if (i > 2) {
                monitor.error("Invalid message: received more than three frames");
                return false;
            }
            frames[i] = socket.recv(0);
            i++;
        }
        executorService.submit(new Runnable() {
            public void run() {
                Message request = MessageCache.getAndResetMessage();
                try {
                    request.setBody(frames[0]);
                    int methodIndex = ByteBuffer.wrap(frames[1]).getInt();
                    WorkContext context = setWorkContext(frames[2]);

                    request.setWorkContext(context);

                    Interceptor interceptor = interceptors[methodIndex];

                    interceptor.invoke(request);
                } finally {
                    request.reset();
                }
            }
        });
        return true;
    }

    @Override
    protected void response(ZMQ.Socket socket) {
        // no-op
    }
}


