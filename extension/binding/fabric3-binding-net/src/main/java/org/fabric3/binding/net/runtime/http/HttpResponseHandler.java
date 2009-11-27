/*
* Fabric3
* Copyright (c) 2009 Metaform Systems
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
package org.fabric3.binding.net.runtime.http;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelPipelineCoverage;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelHandler;
import org.jboss.netty.handler.codec.http.HttpChunk;
import org.jboss.netty.handler.codec.http.HttpResponse;
import org.oasisopen.sca.ServiceRuntimeException;

import org.fabric3.binding.net.NetBindingMonitor;

/**
 * Handles responses over an HTTP channel on the client side for request-response style interactions. This handler is placed on the reference side of
 * an invocation chain.
 */
@ChannelPipelineCoverage("one")
public class HttpResponseHandler extends SimpleChannelHandler {
    private long responseWait;
    private NetBindingMonitor monitor;

    private volatile boolean readingChunks;
    private StringBuilder body = new StringBuilder();

    // queue used by clients to block on awaiting a response
    private BlockingQueue<Response> responseQueue = new LinkedBlockingQueue<Response>();

    public HttpResponseHandler(long responseWait, NetBindingMonitor monitor) {
        this.responseWait = responseWait;
        this.monitor = monitor;
    }

    @Override
    public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) throws Exception {
        if (!readingChunks) {
            HttpResponse response = (HttpResponse) e.getMessage();

            if (response.getStatus().getCode() == 200 && response.isChunked()) {
                readingChunks = true;
            } else {
                ChannelBuffer content = response.getContent();
                if (content.readable()) {
                    responseQueue.offer(new Response(response.getStatus().getCode(), content.toString("UTF-8")));
                }
            }
        } else {
            HttpChunk chunk = (HttpChunk) e.getMessage();
            if (chunk.isLast()) {
                readingChunks = false;
                responseQueue.offer(new Response(200, body.toString()));
            } else {
                body.append(chunk.getContent().toString("UTF-8"));
            }
        }
    }

    /**
     * Blocks on a response.
     *
     * @return the response or null
     * @throws ServiceRuntimeException if waiting on the response times out
     */
    public Response getResponse() throws ServiceRuntimeException {
        try {
            Response response = responseQueue.poll(responseWait, TimeUnit.MILLISECONDS);
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

}
