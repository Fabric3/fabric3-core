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
package org.fabric3.binding.web.runtime;

import java.net.URI;
import javax.servlet.ServletException;

import org.atmosphere.cpr.AtmosphereServlet;
import org.atmosphere.cpr.Broadcaster;
import org.atmosphere.handler.ReflectorServletProcessor;
import org.osoa.sca.annotations.Destroy;
import org.osoa.sca.annotations.EagerInit;
import org.osoa.sca.annotations.Init;
import org.osoa.sca.annotations.Reference;

import org.fabric3.binding.web.common.OperationsAllowed;
import org.fabric3.binding.web.provision.WebConnectionSourceDefinition;
import org.fabric3.spi.builder.component.ConnectionAttachException;
import org.fabric3.spi.builder.component.SourceConnectionAttacher;
import org.fabric3.spi.channel.Channel;
import org.fabric3.spi.channel.ChannelConnection;
import org.fabric3.spi.channel.ChannelManager;
import org.fabric3.spi.channel.EventStream;
import org.fabric3.spi.host.ServletHost;
import org.fabric3.spi.model.physical.PhysicalConnectionTargetDefinition;
import org.fabric3.spi.util.UriHelper;

/**
 * Attaches a channel to a servlet that accepts incoming comet and websocket connections.
 *
 * @version $Rev$ $Date$
 */
@EagerInit
public class WebSourceConnectionAttacher implements SourceConnectionAttacher<WebConnectionSourceDefinition> {
    private static final String CONTEXT_PATH = "/channels/*";

    private ChannelManager channelManager;
    private BroadcasterManager broadcasterManager;
    private ServletHost servletHost;

    private AtmosphereServlet gatewayServlet;
    private ChannelRouter router;

    public WebSourceConnectionAttacher(@Reference ChannelManager channelManager,
                                              @Reference BroadcasterManager broadcasterManager,
                                              @Reference ServletHost servletHost) {
        this.channelManager = channelManager;
        this.broadcasterManager = broadcasterManager;
        this.servletHost = servletHost;
    }

    @Init
    public void init() throws ServletException, InstantiationException, IllegalAccessException, ClassNotFoundException {
        GatewayServletContext context = new GatewayServletContext(CONTEXT_PATH);
        // TODO support other configuration as specified in AtmosphereServlet init()
        context.setInitParameter(AtmosphereServlet.PROPERTY_SESSION_SUPPORT, "false");
        context.setInitParameter(AtmosphereServlet.WEBSOCKET_ATMOSPHEREHANDLER, "false");   // turn the handler off as it is overriden below
        context.setInitParameter(AtmosphereServlet.WEBSOCKET_SUPPORT, "true");

        GatewayServletConfig config = new GatewayServletConfig(context);

        gatewayServlet = new GatewayServlet(servletHost);
        gatewayServlet.init(config);

        router = new ChannelRouter();

        ReflectorServletProcessor processor = new ReflectorServletProcessor();
        processor.setServlet(router);
        processor.init(config);
        WebSocketHandler webSocketHandler = new WebSocketHandler(processor, broadcasterManager);
        gatewayServlet.addAtmosphereHandler("/*", webSocketHandler);
        servletHost.registerMapping(CONTEXT_PATH, gatewayServlet);
    }

    @Destroy
    public void destroy() {
        servletHost.unregisterMapping(CONTEXT_PATH);
        gatewayServlet.destroy();
    }

    public void attach(WebConnectionSourceDefinition source, PhysicalConnectionTargetDefinition target, ChannelConnection connection)
            throws ConnectionAttachException {
        URI sourceUri = source.getSourceUri();
        Channel channel = channelManager.getChannel(sourceUri);
        if (channel == null) {
            throw new ChannelNotFoundException("Channel not found: " + sourceUri);
        }

        String path = UriHelper.getBaseName(sourceUri);

        OperationsAllowed allowed = source.getAllowed();

        if (OperationsAllowed.SUBSCRIBE == allowed || OperationsAllowed.ALL == allowed) {
            // create the subscriber responsible for broadcasting channel events to suspended clients
            Broadcaster broadcaster = broadcasterManager.get(path);
            EventStream stream = new BroadcasterEventStream(broadcaster);
            ChannelSubscriber subscriber = new ChannelSubscriberImpl(stream);
            channel.subscribe(sourceUri, subscriber);
            router.register(path, subscriber);
        } else {
            // not allowed to subscribe
            DenyChannelSubscriber subscriber = new DenyChannelSubscriber();
            router.register(path, subscriber);
        }
        // create the publisher responsible for flowing events from clients to the channel
        if (OperationsAllowed.PUBLISH == allowed || OperationsAllowed.ALL == allowed) {
            ChannelPublisher publisher = new ChannelPublisherImpl();
            channel.attach(publisher);
            router.register(path, publisher);
        } else {
            // not allowed to publish
            DenyChannelPublisher publisher = new DenyChannelPublisher();
            router.register(path, publisher);
        }
        // TODO monitor
    }

    public void detach(WebConnectionSourceDefinition source, PhysicalConnectionTargetDefinition target) {
        String path = null;
        gatewayServlet.removeAtmosphereHandler(path);
    }

}