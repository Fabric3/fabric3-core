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

import java.nio.ByteBuffer;
import java.util.List;

import org.zeromq.ZMQ;
import org.zeromq.ZMQ.Context;

import org.fabric3.binding.zeromq.runtime.SocketAddress;
import org.fabric3.spi.invocation.Message;
import org.fabric3.spi.invocation.MessageImpl;
import org.fabric3.spi.invocation.WorkContext;
import org.fabric3.spi.wire.Interceptor;
import org.fabric3.spi.wire.InvocationChain;

/**
 * Implementation of a {@link Receiver} the implements one-way with no qualities of service.
 * <p/>
 * Since ZeroMQ requires the creating socket thread to receive messages, a polling thread is used for reading messages from the ZeroMQ socket. The
 * receiver listens for address updates (e.g. a sender coming online or going away). Since ZeroMQ does not implement disconnect semantics on a socket,
 * if an update is received the original socket will be closed and a new one created to connect to the update set of addresses.
 *
 * @version $Revision: 10396 $ $Date: 2011-03-15 18:20:58 +0100 (Tue, 15 Mar 2011) $
 */
public class NonReliableOneWayReceiver extends AbstractReceiver implements Thread.UncaughtExceptionHandler {

    private Interceptor singleInterceptor;
    private Interceptor[] interceptors;


    public NonReliableOneWayReceiver(Context context,
                                     SocketAddress address,
                                     List<InvocationChain> chains,
                                     String callbackUri,
                                     MessagingMonitor monitor) {
        super(context, address, chains, callbackUri, ZMQ.PULL, monitor);
//        if (chains.size() == 1) {
//            singleInterceptor = chains.get(0).getHeadInterceptor();
//        } else {
        this.interceptors = new Interceptor[chains.size()];
        for (int i = 0, chainsSize = chains.size(); i < chainsSize; i++) {
            InvocationChain chain = chains.get(i);
            interceptors[i] = chain.getHeadInterceptor();
//            }
        }
    }


    @Override
    protected void invoke(ZMQ.Socket socket) {
        byte[] clientId = socket.recv(0);

        byte[] contextHeader = socket.recv(0);
        WorkContext context = createWorkContext(contextHeader);
        Message request = new MessageImpl();
        request.setWorkContext(context);
//                        if (singleInterceptor != null) {
//                            invokeAndReply(request, clientId, messageId, singleInterceptor);
//                        } else {
        ByteBuffer buffer = ByteBuffer.wrap(socket.recv(0));
        int methodIndex = buffer.getInt();
        Interceptor interceptor = interceptors[methodIndex];


        byte[] body = socket.recv(0);
        request.setBody(body);

        interceptor.invoke(request);
    }
}


