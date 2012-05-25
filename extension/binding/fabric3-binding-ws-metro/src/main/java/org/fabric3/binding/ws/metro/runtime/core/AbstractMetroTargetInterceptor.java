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
package org.fabric3.binding.ws.metro.runtime.core;

import java.util.List;
import java.util.Map;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.handler.Handler;
import javax.xml.ws.handler.soap.SOAPHandler;

import com.sun.xml.ws.developer.JAXWSProperties;

import org.fabric3.binding.ws.metro.provision.ConnectionConfiguration;
import org.fabric3.binding.ws.metro.provision.SecurityConfiguration;
import org.fabric3.binding.ws.metro.runtime.MetroConstants;
import org.fabric3.spi.invocation.Message;
import org.fabric3.spi.invocation.MessageImpl;
import org.fabric3.spi.wire.Interceptor;

/**
 * Contains base functionality for Metro interceptors.
 *
 * @version $Rev: 7476 $ $Date: 2009-08-15 01:25:27 -0400 (Sat, 15 Aug 2009) $
 */
public abstract class AbstractMetroTargetInterceptor implements Interceptor {
    // blank response for one-way operations
    protected static final Message NULL_RESPONSE = new MessageImpl();

    private SecurityConfiguration securityConfig;
    private ConnectionConfiguration connectionConfig;
    private List<Handler> handlers;

    /**
     * Constructor.
     *
     * @param securityConfig the security configuration or null if security is not configured
     * @param connConfig     the underlying HTTP connection configuration or null if defaults should be used
     * @param handlers       optional handlers, may be null.
     */
    public AbstractMetroTargetInterceptor(SecurityConfiguration securityConfig, ConnectionConfiguration connConfig, List<Handler> handlers) {
        this.securityConfig = securityConfig;
        this.connectionConfig = connConfig;
        this.handlers = handlers;
    }

    public Interceptor getNext() {
        return null;
    }

    public void setNext(Interceptor next) {
        throw new IllegalStateException("This interceptor must be the last in the chain");
    }


    /**
     * Configures the outbound security context.
     *
     * @param provider the binding provider for the invocation
     */
    protected void configureSecurity(BindingProvider provider) {
        if (securityConfig == null) {
            // no security
            return;
        }
        // User authentication configured
        // Places authentication information in the invocation context, which is used by the Fabric3 security environment to include the
        // credentials in the message header.
        Map<String, Object> context = provider.getRequestContext();
        if (securityConfig.getUsername() != null) {
            context.put(MetroConstants.USERNAME, securityConfig.getUsername());
            context.put(MetroConstants.PASSWORD, securityConfig.getPassword());
        } else if (securityConfig.getAlias() != null) {
            context.put(MetroConstants.KEYSTORE_ALIAS, securityConfig.getAlias());
        }
    }

    /**
     * Configures the outbound HTTP connection.
     *
     * @param provider the binding provider for the invocation
     */
    protected void configureConnection(BindingProvider provider) {
        if (connectionConfig == null) {
            // use defaults
            return;
        }
        Map<String, Object> context = provider.getRequestContext();
        if (connectionConfig.getConnectTimeout() != ConnectionConfiguration.DEFAULT) {
            context.put(JAXWSProperties.CONNECT_TIMEOUT, connectionConfig.getConnectTimeout());
        }
        if (connectionConfig.getRequestTimeout() != ConnectionConfiguration.DEFAULT) {
            context.put(JAXWSProperties.REQUEST_TIMEOUT, connectionConfig.getRequestTimeout());
        }
        if (connectionConfig.getClientStreamingChunkSize() != ConnectionConfiguration.DEFAULT) {
            context.put(JAXWSProperties.HTTP_CLIENT_STREAMING_CHUNK_SIZE, connectionConfig.getClientStreamingChunkSize());
        }
    }

    /**
     * Configures the outbound HTTP connection.
     *
     * @param provider the binding provider for the invocation
     */
    protected void loadHandlers(BindingProvider provider) {
        if (handlers == null) {
            return;
        }
        String endpointPath = (String) provider.getRequestContext().get(BindingProvider.ENDPOINT_ADDRESS_PROPERTY);
        if (endpointPath == null) {
            // Nothing to bind
            return;
        }
        provider.getBinding().setHandlerChain(handlers);
    }

}