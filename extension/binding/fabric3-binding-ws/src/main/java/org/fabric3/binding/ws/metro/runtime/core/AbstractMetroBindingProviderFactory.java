package org.fabric3.binding.ws.metro.runtime.core;

import javax.xml.ws.BindingProvider;
import javax.xml.ws.handler.Handler;
import java.util.List;
import java.util.Map;

import com.sun.xml.ws.developer.JAXWSProperties;
import org.fabric3.binding.ws.metro.provision.ConnectionConfiguration;
import org.fabric3.spi.container.objectfactory.ObjectFactory;

/**
 *
 */
public abstract class AbstractMetroBindingProviderFactory<T> implements ObjectFactory<T> {
    private ConnectionConfiguration connectionConfig;
    private List<Handler> handlers;

    /**
     * Constructor.
     *
     * @param connConfig the underlying HTTP connection configuration or null if defaults should be used
     * @param handlers   optional handlers, may be null.
     */
    public AbstractMetroBindingProviderFactory(ConnectionConfiguration connConfig, List<Handler> handlers) {
        this.connectionConfig = connConfig;
        this.handlers = handlers;
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
