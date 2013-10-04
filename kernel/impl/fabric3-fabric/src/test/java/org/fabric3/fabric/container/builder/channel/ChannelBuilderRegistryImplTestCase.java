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
package org.fabric3.fabric.container.builder.channel;

import javax.xml.namespace.QName;
import java.net.URI;
import java.util.Collections;

import junit.framework.TestCase;
import org.easymock.EasyMock;
import org.fabric3.spi.container.builder.channel.ChannelBuilder;
import org.fabric3.spi.container.channel.Channel;
import org.fabric3.spi.container.channel.ChannelManager;
import org.fabric3.spi.model.physical.ChannelSide;
import org.fabric3.spi.model.physical.PhysicalChannelDefinition;

/**
 *
 */
public class ChannelBuilderRegistryImplTestCase extends TestCase {
    public static final URI URI = java.net.URI.create("test");
    private ChannelBuilderRegistryImpl registry;
    private ChannelBuilder builder;
    private PhysicalChannelDefinition definition;
    private Channel channel;
    private ChannelManager channelManager;

    public void testBuild() throws Exception {
        EasyMock.expect(channelManager.getAndIncrementChannel(URI, ChannelSide.CONSUMER)).andReturn(null);
        channelManager.register(channel);
        EasyMock.expect(builder.build(definition)).andReturn(channel);
        EasyMock.replay(channelManager, builder, channel);

        assertNotNull(registry.build(definition));

        EasyMock.verify(channelManager, builder, channel);
    }

    public void testBuildButAlreadyRegistered() throws Exception {
        EasyMock.expect(channelManager.getAndIncrementChannel(URI, ChannelSide.CONSUMER)).andReturn(channel);
        EasyMock.replay(channelManager, builder, channel);

        assertNotNull(registry.build(definition));

        EasyMock.verify(channelManager, builder, channel);
    }

    public void testDispose() throws Exception {
        EasyMock.expect(channelManager.getAndDecrementChannel(URI, ChannelSide.CONSUMER)).andReturn(channel);
        EasyMock.expect(channelManager.getCount(URI, ChannelSide.CONSUMER)).andReturn(0);
        EasyMock.expect(channelManager.unregister(URI, ChannelSide.CONSUMER)).andReturn(channel);
        builder.dispose(definition, channel);
        EasyMock.replay(channelManager, builder);

        registry.dispose(definition);

        EasyMock.verify(channelManager, builder);
    }

    public void testDisposeButInUse() throws Exception {
        EasyMock.expect(channelManager.getAndDecrementChannel(URI, ChannelSide.CONSUMER)).andReturn(channel);
        EasyMock.expect(channelManager.getCount(URI, ChannelSide.CONSUMER)).andReturn(1);
        EasyMock.replay(channelManager, builder);

        registry.dispose(definition);

        EasyMock.verify(channelManager, builder);
    }

    @Override
    public void setUp() throws Exception {
        super.setUp();
        channelManager = EasyMock.createMock(ChannelManager.class);
        registry = new ChannelBuilderRegistryImpl(channelManager);
        builder = EasyMock.createMock(ChannelBuilder.class);
        registry.setBuilders(Collections.singletonMap("default", builder));

        channel = EasyMock.createMock(Channel.class);

        definition = new PhysicalChannelDefinition(URI, new QName("test", "bar"), false);
        definition.setChannelSide(ChannelSide.CONSUMER);
    }
}
