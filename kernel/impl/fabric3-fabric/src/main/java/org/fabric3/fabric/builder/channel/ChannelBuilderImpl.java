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
package org.fabric3.fabric.builder.channel;

import javax.xml.namespace.QName;
import java.net.URI;
import java.util.List;
import java.util.concurrent.ExecutorService;

import org.fabric3.api.annotation.monitor.Monitor;
import org.fabric3.fabric.channel.AsyncFanOutHandler;
import org.fabric3.fabric.channel.ChannelImpl;
import org.fabric3.fabric.channel.FanOutHandler;
import org.fabric3.fabric.channel.ReplicationHandler;
import org.fabric3.fabric.channel.ReplicationMonitor;
import org.fabric3.spi.builder.BuilderException;
import org.fabric3.spi.builder.channel.ChannelBuilder;
import org.fabric3.spi.channel.Channel;
import org.fabric3.spi.federation.ZoneChannelException;
import org.fabric3.spi.federation.ZoneTopologyService;
import org.fabric3.spi.model.physical.PhysicalChannelDefinition;
import org.oasisopen.sca.annotation.Reference;

/**
 *
 */
public class ChannelBuilderImpl implements ChannelBuilder {
    private ExecutorService executorService;
    private ReplicationMonitor monitor;
    private ZoneTopologyService topologyService;
    private boolean replicationCapable;

    public ChannelBuilderImpl(@Reference ExecutorService executorService, @Monitor ReplicationMonitor monitor) {
        this.executorService = executorService;
        this.monitor = monitor;
    }

    @Reference(required = false)
    public void setTopologyService(List<ZoneTopologyService> services) {
        // use a collection to force reinjection
        if (services != null && !services.isEmpty()) {
            this.topologyService = services.get(0);
            replicationCapable = topologyService.supportsDynamicChannels();
        }
    }

    public Channel build(PhysicalChannelDefinition definition) throws BuilderException {
        URI uri = definition.getUri();
        QName deployable = definition.getDeployable();
        FanOutHandler fanOutHandler = new AsyncFanOutHandler(executorService);
        Channel channel;
        if (definition.isReplicate() && replicationCapable) {
            String channelName = uri.toString();
            ReplicationHandler replicationHandler = new ReplicationHandler(channelName, topologyService, monitor);
            channel = new ChannelImpl(uri, deployable, replicationHandler, fanOutHandler);
            try {
                topologyService.openChannel(channelName, null, replicationHandler);
            } catch (ZoneChannelException e) {
                throw new BuilderException(e);
            }
        } else {
            channel = new ChannelImpl(uri, deployable, fanOutHandler);
        }
        return channel;
    }
}
