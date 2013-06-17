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
package org.fabric3.binding.web.runtime.channel;

import java.net.URI;
import java.util.concurrent.ExecutorService;

import javax.servlet.ServletException;

import org.atmosphere.cpr.ApplicationConfig;
import org.atmosphere.cpr.AtmosphereConfig;
import org.atmosphere.cpr.AtmosphereFramework;
import org.atmosphere.cpr.Broadcaster;
import org.oasisopen.sca.annotation.Destroy;
import org.oasisopen.sca.annotation.EagerInit;
import org.oasisopen.sca.annotation.Init;
import org.oasisopen.sca.annotation.Property;
import org.oasisopen.sca.annotation.Reference;

import org.fabric3.api.annotation.monitor.Monitor;
import org.fabric3.binding.web.common.OperationsAllowed;
import org.fabric3.binding.web.provision.WebChannelBindingDefinition;
import org.fabric3.binding.web.runtime.common.BroadcasterManager;
import org.fabric3.binding.web.runtime.common.GatewayServletConfig;
import org.fabric3.binding.web.runtime.common.GatewayServletContext;
import org.fabric3.binding.web.runtime.common.LongRunningExecutorService;
import org.fabric3.spi.builder.BuilderException;
import org.fabric3.spi.builder.component.ChannelBindingBuilder;
import org.fabric3.spi.channel.Channel;
import org.fabric3.spi.channel.EventStream;
import org.fabric3.spi.classloader.ClassLoaderRegistry;
import org.fabric3.spi.host.ServletHost;
import org.fabric3.spi.util.UriHelper;

/**
 * Attaches a channel to the gateway servlet that accepts incoming comet and websocket connections using Atmosphere. The gateway servlet is
 * responsible for receiving events and routing them to the appropriate channel based on the request path.
 */
@EagerInit
public class WebChannelBindingBuilder implements ChannelBindingBuilder<WebChannelBindingDefinition> {
    private static final String CONTEXT_PATH = "/channels/*";

    private BroadcasterManager broadcasterManager;
    private PubSubManager pubSubManager;
    private ServletHost servletHost;

    private AtmosphereFramework atmosphereFramework;
    private long timeout = 1000 * 10 * 60;
    private ChannelMonitor monitor;

    private ClassLoaderRegistry classLoaderRegistry;
    private ExecutorService threadPool;
    
    public WebChannelBindingBuilder(@Reference BroadcasterManager broadcasterManager,
                                    @Reference PubSubManager pubSubManager,
                                    @Reference ServletHost servletHost,
                                    @Reference ClassLoaderRegistry classLoaderRegistry,
                                    @Reference ExecutorService threadPool,
                                    @Monitor ChannelMonitor monitor) {
        this.broadcasterManager = broadcasterManager;
        this.pubSubManager = pubSubManager;
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

    /**
     * Initializes the Atmosphere infrastructure, including the gateway servlet, websocket handler, and channel router. The gateway servlet is
     * registered with the runtime Servlet host to receive incoming comet and websocket requests.
     *
     * @throws ServletException if an error initializing one of the Atmosphere servlets is encountered
     */
    @Init
    public void init() throws ServletException {
    	GatewayServletContext context = new GatewayServletContext(CONTEXT_PATH, classLoaderRegistry);
        // TODO support other configuration as specified in AtmosphereServlet init()
        context.setInitParameter(ApplicationConfig.PROPERTY_SESSION_SUPPORT, "false");
//        context.setInitParameter(AtmosphereServlet.WEBSOCKET_ATMOSPHEREHANDLER, "false");   // turn the handler off as it is overriden below
        
        context.setInitParameter(ApplicationConfig.WEBSOCKET_SUPPORT, "true");
        context.setInitParameter(ApplicationConfig.PROPERTY_NATIVE_COMETSUPPORT, "true");
        context.setInitParameter(ApplicationConfig.BROADCASTER_SHARABLE_THREAD_POOLS, "true");

        GatewayServletConfig config = new GatewayServletConfig(context);
        
        org.atmosphere.cpr.AtmosphereServlet atmosphereServlet = new org.atmosphere.cpr.AtmosphereServlet(false,false);
        atmosphereFramework = atmosphereServlet.framework();
        
        // Configure external thread pool
        AtmosphereConfig atmosphereConfig = atmosphereFramework.getAtmosphereConfig();
        atmosphereConfig.properties().put("executorService", threadPool);
        atmosphereConfig.properties().put("asyncWriteService", threadPool);
        
        atmosphereServlet.init(config);
        
        ChannelWebSocketHandler webSocketHandler = new ChannelWebSocketHandler( broadcasterManager, pubSubManager, monitor );
        atmosphereFramework.addAtmosphereHandler("/*", webSocketHandler);
        servletHost.registerMapping(CONTEXT_PATH, atmosphereServlet);
    }

    @Destroy
    public void destroy() {
        servletHost.unregisterMapping(CONTEXT_PATH);
        atmosphereFramework.destroy();
    }

    public void build(WebChannelBindingDefinition definition, Channel channel) throws BuilderException {
        URI sourceUri = channel.getUri();
        String path = UriHelper.getBaseName(sourceUri);
        OperationsAllowed allowed = definition.getAllowed();
        // setup the subscriber infrastructure
        if (OperationsAllowed.SUBSCRIBE == allowed || OperationsAllowed.ALL == allowed) {
            // create the subscriber responsible for broadcasting channel events to suspended clients
            Broadcaster broadcaster = broadcasterManager.getChannelBroadcaster(path, atmosphereFramework.getAtmosphereConfig());
            EventStream stream = new BroadcasterEventStream(broadcaster);
            ChannelSubscriber subscriber = new ChannelSubscriberImpl(stream, timeout);
            channel.subscribe(sourceUri, subscriber);
            pubSubManager.register(path, subscriber);
        } else {
            // clients are not not allowed to subscribe
            DenyChannelSubscriber subscriber = new DenyChannelSubscriber();
            pubSubManager.register(path, subscriber);
        }

        // create the publisher responsible for flowing events from clients to the channel
        if (OperationsAllowed.PUBLISH == allowed || OperationsAllowed.ALL == allowed) {
            DefaultChannelPublisher publisher = new DefaultChannelPublisher();
            channel.addHandler(publisher);
            pubSubManager.register(path, publisher);
        } else {
            // clients are not allowed to publish to the channel
            DenyChannelPublisher publisher = new DenyChannelPublisher();
            pubSubManager.register(path, publisher);
        }
        String prefix = CONTEXT_PATH.substring(0, CONTEXT_PATH.length() - 1);
        monitor.provisionedChannelEndpoint(prefix + path);

    }

    public void dispose(WebChannelBindingDefinition definition, Channel channel) throws BuilderException {
        URI sourceUri = channel.getUri();

        String path = UriHelper.getBaseName(sourceUri);
        OperationsAllowed allowed = definition.getAllowed();

        //   remove the subscriber infrastructure
        if (OperationsAllowed.SUBSCRIBE == allowed || OperationsAllowed.ALL == allowed) {
            pubSubManager.unregisterSubscriber(path);
            channel.unsubscribe(sourceUri);
            broadcasterManager.remove(path);
        } else {
            ChannelPublisher publisher = pubSubManager.unregisterPublisher(path);
            channel.removeHandler(publisher);
        }

        // detach publisher and close cluster channel
        pubSubManager.unregisterPublisher(path);
        String prefix = CONTEXT_PATH.substring(0, CONTEXT_PATH.length() - 1);
        monitor.removedChannelEndpoint(prefix + path);

    }    
    
}
