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
package org.fabric3.binding.net.runtime.tcp;

import java.net.SocketAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jboss.netty.bootstrap.ClientBootstrap;
import org.jboss.netty.channel.ChannelFuture;
import org.oasisopen.sca.ServiceRuntimeException;

import org.fabric3.binding.net.NetBindingMonitor;
import org.fabric3.binding.net.provision.NetConstants;
import org.fabric3.spi.binding.format.EncodeCallback;
import org.fabric3.spi.binding.format.EncoderException;
import org.fabric3.spi.binding.format.MessageEncoder;
import org.fabric3.spi.binding.format.ParameterEncoder;
import org.fabric3.spi.invocation.CallFrame;
import org.fabric3.spi.invocation.Message;
import org.fabric3.spi.invocation.MessageImpl;
import org.fabric3.spi.invocation.WorkContext;
import org.fabric3.spi.wire.Interceptor;

/**
 * Propagates non-blocking invocations made by a client over a TCP channel. This interceptor is placed on the reference side of an invocation chain.
 *
 * @version $Rev$ $Date$
 */
public class TcpOneWayInterceptor implements Interceptor {
    private static final Message MESSAGE = new MessageImpl();
    private static final EncodeCallback CALLBACK = new TcpOneWayCallback();
    private String targetUri;
    private MessageEncoder messageEncoder;
    private ClientBootstrap boostrap;
    private SocketAddress address;
    private ParameterEncoder parameterEncoder;
    private NetBindingMonitor monitor;
    private String operationName;
    private int maxRetry;

    /**
     * Constructor.
     *
     * @param targetUri        the target service URI
     * @param operationName    the name of the operation being invoked
     * @param address          the target service address
     * @param messageEncoder   encodes the message envelope
     * @param parameterEncoder encodes the invocation paramters
     * @param boostrap         the Netty ClientBootstrap instance for sending invocations
     * @param maxRetry         the number of times to retry an operation
     * @param monitor          the event monitor
     */
    public TcpOneWayInterceptor(String targetUri,
                                String operationName,
                                SocketAddress address,
                                MessageEncoder messageEncoder,
                                ParameterEncoder parameterEncoder,
                                ClientBootstrap boostrap,
                                int maxRetry,
                                NetBindingMonitor monitor) {
        this.operationName = operationName;
        this.targetUri = targetUri;
        this.messageEncoder = messageEncoder;
        this.boostrap = boostrap;
        this.address = address;
        this.parameterEncoder = parameterEncoder;
        this.maxRetry = maxRetry;
        this.monitor = monitor;
    }

    public Message invoke(Message msg) {
        // Copy the work context since the binding write operation is performed asynchronously in a different thread and may occur after this
        // invocation has returned. Copying avoids the possibility of another operation modifying the work context before it is accessed by
        // this write.
        WorkContext oldWorkContext = msg.getWorkContext();
        List<CallFrame> newStack = null;
        List<CallFrame> stack = oldWorkContext.getCallFrameStack();
        if (stack != null && !stack.isEmpty()) {
            // clone the callstack to avoid multiple threads seeing changes
            newStack = new ArrayList<CallFrame>(stack);
        }
        msg.setWorkContext(null);
        Map<String, Object> newHeaders = null;
        Map<String, Object> headers = oldWorkContext.getHeaders();
        if (headers != null && !headers.isEmpty()) {
            // clone the headers to avoid multiple threads seeing changes
            newHeaders = new HashMap<String, Object>(headers);
        }
        WorkContext context = new WorkContext();
        context.addCallFrames(newStack);
        context.addHeaders(newHeaders);
        msg.setWorkContext(context);

        // set the target uri and operation names
        context.setHeader(NetConstants.TARGET_URI, targetUri);
        context.setHeader(NetConstants.OPERATION_NAME, operationName);

        try {
            byte[] serialized = parameterEncoder.encodeBytes(msg);
            if (msg.isFault()) {
                msg.setBodyWithFault(serialized);
            } else {
                msg.setBody(serialized);
            }
            byte[] serializedMessage = messageEncoder.encodeBytes(operationName, msg, CALLBACK);
            TcpRetryConnectListener listener = new TcpRetryConnectListener(serializedMessage, address, boostrap, maxRetry, monitor);
            ChannelFuture future = boostrap.connect(address);
            future.addListener(listener);
            return MESSAGE;
        } catch (EncoderException e) {
            throw new ServiceRuntimeException(e);
        }

    }

    public void setNext(Interceptor next) {
        throw new IllegalArgumentException("Interceptor must be the last in the chain");
    }

    public Interceptor getNext() {
        return null;
    }

    private static class TcpOneWayCallback implements EncodeCallback {

        public void encodeContentLengthHeader(long length) {
            // no-op
        }

        public void encodeOperationHeader(String name) {
            // no-op
        }

        public void encodeRoutingHeader(String header) {
            // no-op
        }

        public void encodeRoutingHeader(byte[] header) {
            // no-op
        }

    }

}