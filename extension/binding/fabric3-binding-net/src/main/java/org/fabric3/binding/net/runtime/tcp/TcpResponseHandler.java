/*
* Fabric3
* Copyright (c) 2009-2011 Metaform Systems
*
* Fabric3 is free software: you can redistribute it and/or modify
* it under the terms of the GNU General Public License as
* published by the Free Software Foundation, either version 3 of
* the License, or (at your option) any later version, with the
* following exception:
*
* Linking this software statically or dynamically with other
* modules is making a combined work based on this software.
* Thus, the terms and conditions of the GNU General Public
* License cover the whole combination.
*
* As a special exception, the copyright holders of this software
* give you permission to link this software with independent
* modules to produce an executable, regardless of the license
* terms of these independent modules, and to copy and distribute
* the resulting executable under terms of your choice, provided
* that you also meet, for each linked independent module, the
* terms and conditions of the license of that module. An
* independent module is a module which is not derived from or
* based on this software. If you modify this software, you may
* extend this exception to your version of the software, but
* you are not obligated to do so. If you do not wish to do so,
* delete this exception statement from your version.
*
* Fabric3 is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty
* of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
* See the GNU General Public License for more details.
*
* You should have received a copy of the
* GNU General Public License along with Fabric3.
* If not, see <http://www.gnu.org/licenses/>.
*/
package org.fabric3.binding.net.runtime.tcp;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelPipelineCoverage;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelHandler;
import org.oasisopen.sca.ServiceRuntimeException;

import org.fabric3.binding.net.NetBindingMonitor;
import org.fabric3.binding.net.provision.NetConstants;
import org.fabric3.spi.binding.format.HeaderContext;
import org.fabric3.spi.binding.format.MessageEncoder;
import org.fabric3.spi.binding.format.ParameterEncoder;
import org.fabric3.spi.invocation.Message;

/**
 * Handles TCP responses on the client side for request-response style interactions. This handler is placed on the reference side of an invocation
 * chain.
 */
@ChannelPipelineCoverage("one")
public class TcpResponseHandler extends SimpleChannelHandler {
    private static final HeaderContext CONTEXT = new TcpResponseHeaderContext();
    private MessageEncoder messageEncoder;
    private ParameterEncoder parameterEncoder;
    private long responseWait;
    private NetBindingMonitor monitor;

    // queue used by clients to block on awaiting a response
    private BlockingQueue<Message> responseQueue = new LinkedBlockingQueue<Message>();

    public TcpResponseHandler(MessageEncoder messageEncoder, ParameterEncoder parameterEncoder, long responseWait, NetBindingMonitor monitor) {
        this.messageEncoder = messageEncoder;
        this.parameterEncoder = parameterEncoder;
        this.responseWait = responseWait;
        this.monitor = monitor;
    }

    @Override
    public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) throws Exception {
        ChannelBuffer buffer = (ChannelBuffer) e.getMessage();
        if (buffer.readable()) {
            byte[] bytes = buffer.toByteBuffer().array();
            Message message = messageEncoder.decode(bytes, CONTEXT);
            String operationName = message.getWorkContext().getHeader(String.class, NetConstants.OPERATION_NAME);
            if (message.isFault()) {
                Throwable fault = parameterEncoder.decodeFault(operationName, (byte[]) message.getBody());
                message.setBodyWithFault(fault);
            } else {
                Object deserialized = parameterEncoder.decodeResponse(operationName, (byte[]) message.getBody());
                message.setBody(deserialized);
            }
            responseQueue.offer(message);
        }
    }

    /**
     * Blocks on a response.
     *
     * @return the response or null
     * @throws ServiceRuntimeException if waiting on the response times out
     */
    public Message getResponse() throws ServiceRuntimeException {
        try {
            Message response = responseQueue.poll(responseWait, TimeUnit.MILLISECONDS);
            if (response == null) {
                // timed out waiting for a response, throw exception back to the client since this is a blocking operation
                throw new ServiceRuntimeException("Timeout waiting on response");
            }
            return response;
        } catch (InterruptedException e) {
            throw new ServiceRuntimeException(e);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e) throws Exception {
        ctx.getChannel().close();
        monitor.error(e.getCause());
    }

    private static class TcpResponseHeaderContext implements HeaderContext {

        public long getContentLength() {
            throw new UnsupportedOperationException();
        }

        public String getOperationName() {
            throw new UnsupportedOperationException();
        }

        public String getRoutingText() {
            throw new UnsupportedOperationException();
        }

        public byte[] getRoutingBytes() {
            throw new UnsupportedOperationException();
        }
    }

}