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

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelFutureListener;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelPipelineCoverage;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelHandler;
import org.jboss.netty.handler.codec.http.DefaultHttpResponse;
import org.jboss.netty.handler.codec.http.HttpChunk;
import static org.jboss.netty.handler.codec.http.HttpHeaders.Names.CONNECTION;
import static org.jboss.netty.handler.codec.http.HttpHeaders.Values.CLOSE;
import static org.jboss.netty.handler.codec.http.HttpHeaders.Values.KEEP_ALIVE;
import org.jboss.netty.handler.codec.http.HttpRequest;
import org.jboss.netty.handler.codec.http.HttpResponse;
import org.jboss.netty.handler.codec.http.HttpResponseStatus;
import static org.jboss.netty.handler.codec.http.HttpVersion.HTTP_1_0;
import static org.jboss.netty.handler.codec.http.HttpVersion.HTTP_1_1;

import org.fabric3.binding.net.NetBindingMonitor;
import static org.fabric3.binding.net.provision.NetConstants.OPERATION_NAME;
import org.fabric3.binding.net.runtime.WireHolder;
import org.fabric3.spi.binding.format.EncoderException;
import org.fabric3.spi.binding.format.MessageEncoder;
import org.fabric3.spi.binding.format.ParameterEncoder;
import org.fabric3.spi.binding.format.ResponseEncodeCallback;
import org.fabric3.spi.invocation.F3Conversation;
import org.fabric3.spi.invocation.CallFrame;
import org.fabric3.spi.invocation.ConversationContext;
import org.fabric3.spi.invocation.Message;
import org.fabric3.spi.invocation.WorkContext;
import org.fabric3.spi.model.physical.PhysicalOperationDefinition;
import org.fabric3.spi.wire.Interceptor;
import org.fabric3.spi.wire.InvocationChain;

/**
 * Handles incoming requests from an HTTP channel. This is placed on the service side of an invocation chain.
 */
@ChannelPipelineCoverage("one")
public class HttpRequestHandler extends SimpleChannelHandler {
    private MessageEncoder messageEncoder;
    private NetBindingMonitor monitor;
    private volatile HttpRequest request;
    private volatile boolean readingChunks;
    private Map<String, WireHolder> pathToHolders = new ConcurrentHashMap<String, WireHolder>();
    private StringBuilder requestContent = new StringBuilder();

    /**
     * Constructor.
     *
     * @param messageEncoder the message encoder
     * @param monitor        event monitor
     */
    public HttpRequestHandler(MessageEncoder messageEncoder, NetBindingMonitor monitor) {
        this.messageEncoder = messageEncoder;
        this.monitor = monitor;
    }

    /**
     * Registers a wire for a request path, i.e. the path of the service URI.
     *
     * @param path   the path part of the service URI
     * @param holder the wire holder containing the wire and serializers to perform an invocation for the path
     */
    public void register(String path, WireHolder holder) {
        pathToHolders.put(path, holder);
    }

    /**
     * Unregisters a wire for a request path, i.e. the path of the service URI.
     *
     * @param path the path part of the service URI
     */
    public void unregister(String path) {
        pathToHolders.remove(path);
    }

    @Override
    public void messageReceived(ChannelHandlerContext ctx, MessageEvent event) throws Exception {
        if (!readingChunks) {
            HttpRequest request = this.request = (HttpRequest) event.getMessage();
            if (request.isChunked()) {
                readingChunks = true;
            } else {
                ChannelBuffer content = request.getContent();
                invoke(request, content.toString("UTF-8"), event);
                // TODO handle exceptions
            }
        } else {
            HttpChunk chunk = (HttpChunk) event.getMessage();
            if (chunk.isLast()) {
                // end of content
                readingChunks = false;
                requestContent.append(chunk.getContent().toString("UTF-8"));
                invoke(request, requestContent.toString(), event);
                // TODO handle exceptions
            } else {
                requestContent.append(chunk.getContent().toString("UTF-8"));
            }
        }
    }

    @SuppressWarnings({"unchecked"})
    private void invoke(HttpRequest request, String content, MessageEvent event) throws EncoderException, ClassNotFoundException {
        WireHolder wireHolder = pathToHolders.get(request.getUri());
        if (wireHolder == null) {
            throw new AssertionError("Holder not found for request:" + request.getUri());
        }

        HttpRequestContext context = new HttpRequestContext(request);
        Message message = messageEncoder.decode(content, context);

        WorkContext workContext = message.getWorkContext();
        String callbackUri = wireHolder.getCallbackUri();
        CallFrame previous = workContext.peekCallFrame();
        // Copy correlation and conversation information from incoming frame to new frame
        // Note that the callback URI is set to the callback address of this service so its callback wire can be mapped in the case of a
        // bidirectional service
        Serializable id = previous.getCorrelationId(Serializable.class);
        ConversationContext conversationContext = previous.getConversationContext();
        F3Conversation conversation = previous.getConversation();
        CallFrame frame = new CallFrame(callbackUri, id, conversation, conversationContext);
        workContext.addCallFrame(frame);

        ParameterEncoder parameterEncoder = wireHolder.getParameterEncoder();
        String operationName = request.getHeader(OPERATION_NAME);

        String header = request.getHeader(CONNECTION);

        Object body = parameterEncoder.decode(operationName, (String) message.getBody());
        if (body == null) {
            // no params
            message.setBody(null);
        } else {
            message.setBody(new Object[]{body});
        }

        InvocationChain chain = selectOperation(operationName, wireHolder);
        Interceptor interceptor = chain.getHeadInterceptor();
        Message response = interceptor.invoke(message);
        boolean close = CLOSE.equalsIgnoreCase(header) || request.getProtocolVersion().equals(HTTP_1_0) && !KEEP_ALIVE.equalsIgnoreCase(header);
        writeResponse(operationName, response, wireHolder, event, close);

        // reuse buffer
        requestContent.setLength(0);
    }


    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e) throws Exception {
        monitor.error(e.getCause());
        e.getChannel().close();
    }

    private InvocationChain selectOperation(String operationName, WireHolder wireHolder) {
        List<InvocationChain> chains = wireHolder.getInvocationChains();
        if (operationName != null) {
            for (InvocationChain chain : chains) {
                PhysicalOperationDefinition definition = chain.getPhysicalOperation();
                if (definition.getName().equals(operationName)) {
                    return chain;
                }
            }
        }
        // TODO should select from HTTP Method name
        throw new AssertionError();
    }

    private void writeResponse(String operationName, Message msg, WireHolder wireHolder, MessageEvent event, boolean close) {
        HttpResponseStatus status;
        if (msg.isFault()) {
            // Return a 500 response
            status = HttpResponseStatus.INTERNAL_SERVER_ERROR;
        } else {
            status = HttpResponseStatus.OK;
        }
        HttpResponse response = new DefaultHttpResponse(HTTP_1_1, status);
        try {
            ParameterEncoder parameterEncoder = wireHolder.getParameterEncoder();
            String serialized = parameterEncoder.encodeText(msg);
            msg.setBody(serialized);
            ResponseEncodeCallback callback = new HttpResponseCallback(response);
            String serializedMessage = messageEncoder.encodeResponseText(operationName, msg, callback);

            ChannelBuffer buf = ChannelBuffers.copiedBuffer(serializedMessage, "UTF-8");
            response.setContent(buf);

            // Write the response
            ChannelFuture future = event.getChannel().write(response);
            if (close) {
                future.addListener(ChannelFutureListener.CLOSE);
            }

        } catch (EncoderException e) {
            monitor.error(e);
        }
    }


}
