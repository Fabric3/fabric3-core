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
import org.fabric3.spi.model.physical.PhysicalChannel;

/**
 *
 */
public class ChannelBuilderRegistryImplTestCase extends TestCase {
    public static final URI URI = java.net.URI.create("test");
    private ChannelBuilderRegistryImpl registry;
    private ChannelBuilder builder;
    private PhysicalChannel physicalChannel;
    private Channel channel;
    private ChannelManager channelManager;

    public void testBuild() throws Exception {
        EasyMock.expect(channelManager.getAndIncrementChannel(URI, ChannelSide.CONSUMER)).andReturn(null);
        channelManager.register(channel);
        EasyMock.expect(builder.build(physicalChannel)).andReturn(channel);
        EasyMock.replay(channelManager, builder, channel);

        assertNotNull(registry.build(physicalChannel));

        EasyMock.verify(channelManager, builder, channel);
    }

    public void testBuildButAlreadyRegistered() throws Exception {
        EasyMock.expect(channelManager.getAndIncrementChannel(URI, ChannelSide.CONSUMER)).andReturn(channel);
        EasyMock.replay(channelManager, builder, channel);

        assertNotNull(registry.build(physicalChannel));

        EasyMock.verify(channelManager, builder, channel);
    }

    public void testDispose() throws Exception {
        EasyMock.expect(channelManager.getAndDecrementChannel(URI, ChannelSide.CONSUMER)).andReturn(channel);
        EasyMock.expect(channelManager.getCount(URI, ChannelSide.CONSUMER)).andReturn(0);
        EasyMock.expect(channelManager.unregister(URI, ChannelSide.CONSUMER)).andReturn(channel);
        builder.dispose(physicalChannel, channel);
        EasyMock.replay(channelManager, builder);

        registry.dispose(physicalChannel);

        EasyMock.verify(channelManager, builder);
    }

    public void testDisposeButInUse() throws Exception {
        EasyMock.expect(channelManager.getAndDecrementChannel(URI, ChannelSide.CONSUMER)).andReturn(channel);
        EasyMock.expect(channelManager.getCount(URI, ChannelSide.CONSUMER)).andReturn(1);
        EasyMock.replay(channelManager, builder);

        registry.dispose(physicalChannel);

        EasyMock.verify(channelManager, builder);
    }

    @Override
    public void setUp() throws Exception {
        super.setUp();
        channelManager = EasyMock.createMock(ChannelManager.class);
        registry = new ChannelBuilderRegistryImpl(channelManager);
        builder = EasyMock.createMock(ChannelBuilder.class);
        registry.builders = Collections.singletonMap("default", builder);

        channel = EasyMock.createMock(Channel.class);

        physicalChannel = new PhysicalChannel(URI, new QName("test", "bar"));
        physicalChannel.setChannelSide(ChannelSide.CONSUMER);
    }
}
