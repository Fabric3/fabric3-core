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
package org.fabric3.binding.net.runtime.http;

import java.net.SocketAddress;

import org.jboss.netty.bootstrap.ClientBootstrap;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.handler.codec.http.DefaultHttpRequest;
import org.jboss.netty.handler.codec.http.HttpMethod;
import org.jboss.netty.handler.codec.http.HttpRequest;
import org.jboss.netty.handler.codec.http.HttpVersion;
import org.oasisopen.sca.ServiceRuntimeException;

import org.fabric3.binding.net.NetBindingMonitor;
import org.fabric3.spi.binding.format.EncoderException;
import org.fabric3.spi.binding.format.MessageEncoder;
import org.fabric3.spi.binding.format.ParameterEncoder;
import org.fabric3.spi.invocation.Message;
import org.fabric3.spi.invocation.MessageImpl;
import org.fabric3.spi.wire.Interceptor;

/**
 * Propagates non-blocking invocations made by a client over an HTTP channel. This interceptor is placed on the reference side of an invocation
 * chain.
 *
 * @version $Rev$ $Date$
 */
public class HttpOneWayInterceptor implements Interceptor {
    private static final Message MESSAGE = new MessageImpl();
    private String operationName;
    private MessageEncoder messageEncoder;
    private ParameterEncoder parameterEncoder;
    private ClientBootstrap bootstrap;
    private SocketAddress address;
    private int retries;
    private NetBindingMonitor monitor;
    private String url;

    /**
     * Constructor.
     *
     * @param url              the target service URL
     * @param operationName    the name of the operation being invoked
     * @param address          the target service address
     * @param messageEncoder   encoder for message envelopers
     * @param parameterEncoder encoder for parameters
     * @param bootstrap        the Netty ClientBootstrap instance for sending invocations
     * @param retries          the number of times to retry failed communications operations
     * @param monitor          the event monitor
     */
    public HttpOneWayInterceptor(String url,
                                 String operationName,
                                 SocketAddress address,
                                 MessageEncoder messageEncoder,
                                 ParameterEncoder parameterEncoder,
                                 ClientBootstrap bootstrap,
                                 int retries,
                                 NetBindingMonitor monitor) {
        this.url = url;
        this.operationName = operationName;
        this.messageEncoder = messageEncoder;
        this.parameterEncoder = parameterEncoder;
        this.bootstrap = bootstrap;
        this.address = address;
        this.retries = retries;
        this.monitor = monitor;
    }

    public Message invoke(final Message msg) {
        try {
            // connection succeeded, write data
            HttpRequest request = new DefaultHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.POST, url);
            String serialized = parameterEncoder.encodeText(msg);
            if (msg.isFault()) {
                msg.setBodyWithFault(serialized);
            } else {
                msg.setBody(serialized);
            }
            HttpCallback callback = new HttpCallback(request);
            String encodedMessage = messageEncoder.encodeText(operationName, msg, callback);
            ChannelFuture future = bootstrap.connect(address);
            HttpRetryConnectListener listener = new HttpRetryConnectListener(request, encodedMessage, address, bootstrap, retries, monitor);
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
}
