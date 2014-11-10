/*
 * Fabric3
 * Copyright (c) 2009-2015 Metaform Systems
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * Portions originally based on Apache Tuscany 2007
 * licensed under the Apache 2.0 license.
 */
package org.fabric3.binding.web.runtime.service;

import javax.servlet.ServletException;
import java.util.concurrent.ExecutorService;

import org.atmosphere.cache.HeaderBroadcasterCache;
import org.atmosphere.cpr.ApplicationConfig;
import org.atmosphere.cpr.AtmosphereConfig;
import org.atmosphere.cpr.AtmosphereFramework;
import org.fabric3.api.annotation.monitor.Monitor;
import org.fabric3.binding.web.provision.WebWireSourceDefinition;
import org.fabric3.binding.web.runtime.common.BroadcasterManager;
import org.fabric3.binding.web.runtime.common.GatewayServletConfig;
import org.fabric3.binding.web.runtime.common.GatewayServletContext;
import org.fabric3.binding.web.runtime.common.LongRunningExecutorService;
import org.fabric3.spi.classloader.ClassLoaderRegistry;
import org.fabric3.spi.container.ContainerException;
import org.fabric3.spi.container.builder.component.SourceWireAttacher;
import org.fabric3.spi.container.objectfactory.ObjectFactory;
import org.fabric3.spi.container.wire.InvocationChain;
import org.fabric3.spi.container.wire.Wire;
import org.fabric3.spi.host.ServletHost;
import org.fabric3.spi.model.physical.PhysicalWireTargetDefinition;
import org.oasisopen.sca.annotation.Destroy;
import org.oasisopen.sca.annotation.EagerInit;
import org.oasisopen.sca.annotation.Init;
import org.oasisopen.sca.annotation.Property;
import org.oasisopen.sca.annotation.Reference;

/**
 * Attaches a service to the gateway servlet that accepts incoming websocket connections using Atmosphere. The gateway servlet is responsible for receiving
 * invocations and routing them to the appropriate service based on the request path.
 */
@EagerInit
public class WebSourceWireAttacher implements SourceWireAttacher<WebWireSourceDefinition> {
    private static final String CONTEXT_PATH = "/web/*";

    private ServiceManager serviceManager;
    private BroadcasterManager broadcasterManager;
    private ServletHost servletHost;
    private long timeout = 1000 * 10 * 60;
    private AtmosphereFramework atmosphereFramework;
    private ServiceMonitor monitor;
    private ClassLoaderRegistry classLoaderRegistry;

    private ExecutorService threadPool;

    public WebSourceWireAttacher(@Reference ServiceManager serviceManager,
                                 @Reference BroadcasterManager broadcasterManager,
                                 @Reference ServletHost servletHost,
                                 @Reference ClassLoaderRegistry classLoaderRegistry,
                                 @Reference(name = "executorService") ExecutorService executorService,
                                 @Monitor ServiceMonitor monitor) {
        this.broadcasterManager = broadcasterManager;
        this.serviceManager = serviceManager;
        this.servletHost = servletHost;
        this.monitor = monitor;
        this.classLoaderRegistry = classLoaderRegistry;
        this.threadPool = new LongRunningExecutorService(executorService);
    }

    /**
     * Sets the client connection timeout
     *
     * @param timeout the timeout in milliseconds
     */
    @Property(required = false)
    public void setTimeout(long timeout) {
        this.timeout = timeout;
    }

    @Init
    public void init() throws ServletException {
        GatewayServletContext context = new GatewayServletContext(CONTEXT_PATH, classLoaderRegistry);
        // TODO support other configuration as specified in AtmosphereServlet init()
        context.setInitParameter(ApplicationConfig.PROPERTY_SESSION_SUPPORT, "false");
        context.setInitParameter(ApplicationConfig.WEBSOCKET_SUPPORT, "true");
        context.setInitParameter(ApplicationConfig.PROPERTY_NATIVE_COMETSUPPORT, "true");
        context.setInitParameter(ApplicationConfig.BROADCASTER_SHARABLE_THREAD_POOLS, "true");
        context.setInitParameter(ApplicationConfig.BROADCASTER_CACHE, HeaderBroadcasterCache.class.getName());
        GatewayServletConfig config = new GatewayServletConfig(context);

        org.atmosphere.cpr.AtmosphereServlet atmosphereServlet = new org.atmosphere.cpr.AtmosphereServlet(false, false);
        atmosphereFramework = atmosphereServlet.framework();

        // Configure external thread pool
        AtmosphereConfig atmosphereConfig = atmosphereFramework.getAtmosphereConfig();
        atmosphereConfig.properties().put("executorService", threadPool);
        atmosphereConfig.properties().put("asyncWriteService", threadPool);

        atmosphereServlet.init(config);

        ServiceWebSocketHandler webSocketHandler = new ServiceWebSocketHandler(serviceManager, broadcasterManager, monitor);
        atmosphereFramework.addAtmosphereHandler("/*", webSocketHandler);
        servletHost.registerMapping(CONTEXT_PATH, atmosphereServlet);
    }

    @Destroy
    public void destroy() {
        servletHost.unregisterMapping(CONTEXT_PATH);
        atmosphereFramework.destroy();
    }

    public void attach(WebWireSourceDefinition source, PhysicalWireTargetDefinition target, Wire wire) throws ContainerException {
        String path = getPath(source);
        if (wire.getInvocationChains().size() != 1) {
            // the websocket binding only supports service contracts with one operation
            throw new IllegalArgumentException("Invalid wire size");
        }
        InvocationChain chain = wire.getInvocationChains().get(0);

        // use the service URI as the callback id
        String callbackUri = source.getUri().toString();
        serviceManager.register(path, chain, callbackUri);
        String prefix = CONTEXT_PATH.substring(0, CONTEXT_PATH.length() - 1);
        monitor.provisionedEndpoint(prefix + path);
    }

    public void detach(WebWireSourceDefinition source, PhysicalWireTargetDefinition target) throws ContainerException {
        String path = getPath(source);
        serviceManager.unregister(path);
        String prefix = CONTEXT_PATH.substring(0, CONTEXT_PATH.length() - 1);
        monitor.removedEndpoint(prefix + path);
    }

    public void attachObjectFactory(WebWireSourceDefinition source, ObjectFactory<?> objectFactory, PhysicalWireTargetDefinition target) {
        throw new UnsupportedOperationException();
    }

    public void detachObjectFactory(WebWireSourceDefinition source, PhysicalWireTargetDefinition target) {
        throw new UnsupportedOperationException();
    }

    /**
     * Returns the service path by stripping the leading '/'.
     *
     * @param source the source metadata
     * @return the path
     */
    private String getPath(WebWireSourceDefinition source) {
        return source.getUri().getPath().substring(1);
    }

}
