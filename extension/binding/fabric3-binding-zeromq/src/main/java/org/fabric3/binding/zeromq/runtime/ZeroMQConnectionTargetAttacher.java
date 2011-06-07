/*
 * Fabric3 Copyright (c) 2009-2011 Metaform Systems
 * 
 * Fabric3 is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version, with the following exception:
 * 
 * Linking this software statically or dynamically with other modules is making
 * a combined work based on this software. Thus, the terms and conditions of the
 * GNU General Public License cover the whole combination.
 * 
 * As a special exception, the copyright holders of this software give you
 * permission to link this software with independent modules to produce an
 * executable, regardless of the license terms of these independent modules, and
 * to copy and distribute the resulting executable under terms of your choice,
 * provided that you also meet, for each linked independent module, the terms
 * and conditions of the license of that module. An independent module is a
 * module which is not derived from or based on this software. If you modify
 * this software, you may extend this exception to your version of the software,
 * but you are not obligated to do so. If you do not wish to do so, delete this
 * exception statement from your version.
 * 
 * Fabric3 is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * Fabric3. If not, see <http://www.gnu.org/licenses/>.
 */
package org.fabric3.binding.zeromq.runtime;

import java.net.InetAddress;
import java.net.UnknownHostException;

import org.osoa.sca.annotations.Reference;
import org.zeromq.ZMQ;

import org.fabric3.api.annotation.monitor.Monitor;
import org.fabric3.binding.zeromq.common.ZeroMQMetadata;
import org.fabric3.binding.zeromq.provision.ZeroMQConnectionTargetDefinition;
import org.fabric3.binding.zeromq.runtime.context.ContextManager;
import org.fabric3.binding.zeromq.runtime.federation.AddressAnnouncement;
import org.fabric3.binding.zeromq.runtime.federation.AddressCache;
import org.fabric3.binding.zeromq.runtime.handler.PublisherHandler;
import org.fabric3.binding.zeromq.runtime.handler.SerializingEventStreamHandler;
import org.fabric3.binding.zeromq.runtime.message.NonReliablePublisher;
import org.fabric3.binding.zeromq.runtime.message.MessagingMonitor;
import org.fabric3.host.runtime.HostInfo;
import org.fabric3.spi.builder.component.ConnectionAttachException;
import org.fabric3.spi.builder.component.TargetConnectionAttacher;
import org.fabric3.spi.channel.ChannelConnection;
import org.fabric3.spi.channel.EventStream;
import org.fabric3.spi.host.PortAllocationException;
import org.fabric3.spi.host.PortAllocator;
import org.fabric3.spi.model.physical.PhysicalConnectionSourceDefinition;

/**
 * @version $Revision$ $Date$
 */
public class ZeroMQConnectionTargetAttacher implements TargetConnectionAttacher<ZeroMQConnectionTargetDefinition> {
    private static final String ZMQ = "zmq";

    private ContextManager contextManager;
    private PortAllocator allocator;
    private AddressCache addressCache;
    private HostInfo info;
    private MessagingMonitor monitor;

    public ZeroMQConnectionTargetAttacher(@Reference ContextManager contextManager,
                                          @Reference PortAllocator allocator,
                                          @Reference AddressCache addressCache,
                                          @Reference HostInfo info,
                                          @Monitor MessagingMonitor monitor) {
        this.contextManager = contextManager;
        this.allocator = allocator;
        this.addressCache = addressCache;
        this.info = info;
        this.monitor = monitor;
    }

    public void attach(PhysicalConnectionSourceDefinition source, ZeroMQConnectionTargetDefinition target, ChannelConnection connection)
            throws ConnectionAttachException {
        ZeroMQMetadata metadata = target.getMetadata();
        try {
            String channelName = metadata.getChannelName();
            int port = allocator.allocate(channelName, ZMQ);
            // XCV FIXME localhost
            String runtimeName = info.getRuntimeName();
            SocketAddress address = new SocketAddress(runtimeName, "tcp", InetAddress.getLocalHost().getHostAddress(), port);
            ZMQ.Context context = contextManager.getContext();
            NonReliablePublisher publisher = new NonReliablePublisher(context, address, monitor);
            for (EventStream stream : connection.getEventStreams()) {
                stream.addHandler(new SerializingEventStreamHandler());
                stream.addHandler(new PublisherHandler(publisher));
            }

            AddressAnnouncement event = new AddressAnnouncement(channelName, AddressAnnouncement.Type.ACTIVATED, address);
            addressCache.publish(event);
            publisher.start();
        } catch (PortAllocationException e) {
            throw new ConnectionAttachException(e);
        } catch (UnknownHostException e) {
            throw new ConnectionAttachException(e);
        }
    }

    public void detach(PhysicalConnectionSourceDefinition source, ZeroMQConnectionTargetDefinition target) {
        throw new UnsupportedOperationException();
    }

}
