/*
 * Fabric3
 * Copyright (c) 2009-2012 Metaform Systems
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
 *
 * ----------------------------------------------------
 *
 * Portions originally based on Apache Tuscany 2007
 * licensed under the Apache 2.0 license.
 *
 */
package org.fabric3.fabric.executor;

import java.net.URI;
import java.util.Collections;
import java.util.Map;
import javax.xml.namespace.QName;

import junit.framework.TestCase;
import org.easymock.EasyMock;

import org.fabric3.fabric.command.DisposeChannelsCommand;
import org.fabric3.spi.builder.component.ChannelBindingBuilder;
import org.fabric3.spi.channel.Channel;
import org.fabric3.spi.channel.ChannelManager;
import org.fabric3.spi.executor.CommandExecutorRegistry;
import org.fabric3.spi.federation.ZoneTopologyService;
import org.fabric3.spi.model.physical.PhysicalChannelBindingDefinition;
import org.fabric3.spi.model.physical.PhysicalChannelDefinition;

/**
 *
 */
public class DisposeChannelsCommandExecutorTestCase extends TestCase {


    @SuppressWarnings({"unchecked"})
    public void testDisposeChannel() throws Exception {
        PhysicalChannelDefinition definition = new MockDefinition();
        definition.setBindingDefinition(new MockBindingDefinition());

        CommandExecutorRegistry registry = EasyMock.createMock(CommandExecutorRegistry.class);

        ChannelBindingBuilder builder = EasyMock.createMock(ChannelBindingBuilder.class);
        builder.dispose(EasyMock.isA(PhysicalChannelBindingDefinition.class), EasyMock.isA(Channel.class));

        Channel channel = EasyMock.createMock(Channel.class);
        ChannelManager channelManager = EasyMock.createMock(ChannelManager.class);
        EasyMock.expect(channelManager.unregister(EasyMock.isA(URI.class))).andReturn(channel);

        ZoneTopologyService topologyService = EasyMock.createMock(ZoneTopologyService.class);
        EasyMock.expect(topologyService.supportsDynamicChannels()).andReturn(true);
        topologyService.closeChannel(EasyMock.isA(String.class));
        EasyMock.replay(channelManager, builder, registry, topologyService, channel);

        DisposeChannelsCommandExecutor executor = new DisposeChannelsCommandExecutor(channelManager, null);

        executor.setTopologyService(Collections.singletonList(topologyService));
        Map<Class<? extends PhysicalChannelBindingDefinition>, ChannelBindingBuilder<? extends PhysicalChannelBindingDefinition>> map =
                Collections.<Class<? extends PhysicalChannelBindingDefinition>,
                        ChannelBindingBuilder<? extends PhysicalChannelBindingDefinition>>singletonMap(MockBindingDefinition.class, builder);
        executor.setBuilders(map);


        DisposeChannelsCommand command = new DisposeChannelsCommand(Collections.singletonList(definition));
        executor.execute(command);

        EasyMock.verify(channelManager, builder, registry, topologyService, channel);
    }

    private class MockDefinition extends PhysicalChannelDefinition {
        private static final long serialVersionUID = -809769047230911419L;

        private MockDefinition() {
            super(URI.create("test"), new QName("foo", "bar"), false, true);
        }
    }

    private class MockBindingDefinition extends PhysicalChannelBindingDefinition {
        private static final long serialVersionUID = -474926224717103363L;
    }
}
