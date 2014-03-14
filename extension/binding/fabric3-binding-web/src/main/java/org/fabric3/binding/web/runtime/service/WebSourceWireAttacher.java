/*
* Fabric3
* Copyright (c) 2009-2013 Metaform Systems
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
package org.fabric3.binding.web.runtime.service;

import java.util.concurrent.ExecutorService;
import javax.servlet.ServletException;

import org.atmosphere.cache.HeaderBroadcasterCache;
import org.atmosphere.cpr.ApplicationConfig;
import org.atmosphere.cpr.AtmosphereConfig;
import org.atmosphere.cpr.AtmosphereFramework;
import org.oasisopen.sca.annotation.Destroy;
import org.oasisopen.sca.annotation.EagerInit;
import org.oasisopen.sca.annotation.Init;
import org.oasisopen.sca.annotation.Property;
import org.oasisopen.sca.annotation.Reference;

import org.fabric3.api.annotation.monitor.Monitor;
import org.fabric3.binding.web.provision.WebWireSourceDefinition;
import org.fabric3.binding.web.runtime.common.BroadcasterManager;
import org.fabric3.binding.web.runtime.common.GatewayServletConfig;
import org.fabric3.binding.web.runtime.common.GatewayServletContext;
import org.fabric3.binding.web.runtime.common.LongRunningExecutorService;
import org.fabric3.spi.container.builder.WiringException;
import org.fabric3.spi.container.builder.component.SourceWireAttacher;
import org.fabric3.spi.classloader.ClassLoaderRegistry;
import org.fabric3.spi.host.ServletHost;
import org.fabric3.spi.model.physical.PhysicalWireTargetDefinition;
import org.fabric3.spi.container.objectfactory.ObjectFactory;
import org.fabric3.spi.container.wire.InvocationChain;
import org.fabric3.spi.container.wire.Wire;

/**
 * Attaches a service to the gateway servlet that accepts incoming websocket connections using Atmosphere. The gateway servlet is responsible for
 * receiving invocations and routing them to the appropriate service based on the request path.
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
                                 @Reference ExecutorService threadPool,
                                 @Monitor ServiceMonitor monitor) {
        this.broadcasterManager = broadcasterManager;
        this.serviceManager = serviceManager;
        this.servletHost = servletHost;
        this.monitor = monitor;
        this.classLoaderRegistry = classLoaderRegistry;
        this.threadPool = new LongRunningExecutorService(threadPool);
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

    public void attach(WebWireSourceDefinition source, PhysicalWireTargetDefinition target, Wire wire) throws WiringException {
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

    public void detach(WebWireSourceDefinition source, PhysicalWireTargetDefinition target) throws WiringException {
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
