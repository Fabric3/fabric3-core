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
package org.fabric3.binding.web.runtime.channel;

import javax.servlet.ServletException;
import java.net.URI;
import java.util.concurrent.ExecutorService;

import org.atmosphere.cpr.ApplicationConfig;
import org.atmosphere.cpr.AtmosphereConfig;
import org.atmosphere.cpr.AtmosphereFramework;
import org.atmosphere.cpr.Broadcaster;
import org.fabric3.api.annotation.monitor.Monitor;
import org.fabric3.binding.web.common.OperationsAllowed;
import org.fabric3.binding.web.provision.WebChannelBindingDefinition;
import org.fabric3.binding.web.runtime.common.BroadcasterManager;
import org.fabric3.binding.web.runtime.common.GatewayServletConfig;
import org.fabric3.binding.web.runtime.common.GatewayServletContext;
import org.fabric3.binding.web.runtime.common.LongRunningExecutorService;
import org.fabric3.spi.classloader.ClassLoaderRegistry;
import org.fabric3.spi.container.ContainerException;
import org.fabric3.spi.container.builder.component.ChannelBindingBuilder;
import org.fabric3.spi.container.channel.Channel;
import org.fabric3.spi.container.channel.EventStream;
import org.fabric3.spi.host.ServletHost;
import org.fabric3.spi.util.UriHelper;
import org.oasisopen.sca.annotation.Destroy;
import org.oasisopen.sca.annotation.EagerInit;
import org.oasisopen.sca.annotation.Init;
import org.oasisopen.sca.annotation.Property;
import org.oasisopen.sca.annotation.Reference;

/**
 * Attaches a channel to the gateway servlet that accepts incoming comet and websocket connections using Atmosphere. The gateway servlet is responsible for
 * receiving events and routing them to the appropriate channel based on the request path.
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
                                    @Reference(name = "executorService") ExecutorService executorService,
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
     * Initializes the Atmosphere infrastructure, including the gateway servlet, websocket handler, and channel router. The gateway servlet is registered with
     * the runtime Servlet host to receive incoming comet and websocket requests.
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

        org.atmosphere.cpr.AtmosphereServlet atmosphereServlet = new org.atmosphere.cpr.AtmosphereServlet(false, false);
        atmosphereFramework = atmosphereServlet.framework();

        // Configure external thread pool
        AtmosphereConfig atmosphereConfig = atmosphereFramework.getAtmosphereConfig();
        atmosphereConfig.properties().put("executorService", threadPool);
        atmosphereConfig.properties().put("asyncWriteService", threadPool);

        atmosphereServlet.init(config);

        ChannelWebSocketHandler webSocketHandler = new ChannelWebSocketHandler(broadcasterManager, pubSubManager, monitor);
        atmosphereFramework.addAtmosphereHandler("/*", webSocketHandler);
        servletHost.registerMapping(CONTEXT_PATH, atmosphereServlet);
    }

    @Destroy
    public void destroy() {
        servletHost.unregisterMapping(CONTEXT_PATH);
        atmosphereFramework.destroy();
    }

    public void build(WebChannelBindingDefinition definition, Channel channel) throws ContainerException {
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

    public void dispose(WebChannelBindingDefinition definition, Channel channel) throws ContainerException {
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
