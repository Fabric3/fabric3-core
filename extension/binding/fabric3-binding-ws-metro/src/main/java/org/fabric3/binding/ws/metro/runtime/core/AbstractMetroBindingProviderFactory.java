package org.fabric3.binding.ws.metro.runtime.core;

import java.util.List;
import java.util.Map;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.handler.Handler;

import com.sun.xml.ws.developer.JAXWSProperties;

import org.fabric3.binding.ws.metro.provision.ConnectionConfiguration;
import org.fabric3.binding.ws.metro.provision.SecurityConfiguration;
import org.fabric3.binding.ws.metro.runtime.MetroConstants;
import org.fabric3.spi.container.objectfactory.ObjectFactory;

/**
 *
 */
public abstract class AbstractMetroBindingProviderFactory<T> implements ObjectFactory<T> {
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
    public AbstractMetroBindingProviderFactory(SecurityConfiguration securityConfig, ConnectionConfiguration connConfig, List<Handler> handlers) {
        this.securityConfig = securityConfig;
        this.connectionConfig = connConfig;
        this.handlers = handlers;
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
     * Configures binding handlers.
     *
     * @param provider the binding provider for the invocation
     */
    protected void configureHandlers(BindingProvider provider) {
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
