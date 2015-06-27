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
package org.fabric3.fabric.container.channel;

import java.net.URI;

import junit.framework.TestCase;
import org.easymock.EasyMock;
import org.fabric3.api.host.Fabric3Exception;
import org.fabric3.spi.container.channel.Channel;
import org.fabric3.spi.model.physical.ChannelSide;

/**
 *
 */
public class ChannelManagerImplTestCase extends TestCase {
    private static final URI CHANNEL_URI = URI.create("test");

    private Channel channel;
    private ChannelManagerImpl manager;

    public void testDuplicateRegistration() throws Exception {
        EasyMock.expect(channel.getUri()).andReturn(CHANNEL_URI).times(2);
        EasyMock.expect(channel.getChannelSide()).andReturn(ChannelSide.CONSUMER).times(2);
        EasyMock.replay(channel);

        manager.register(channel);
        try {
            manager.register(channel);
            fail();
        } catch (Fabric3Exception e) {
            // expected
        }

        EasyMock.verify(channel);
    }

    public void testGetChannel() throws Exception {
        EasyMock.expect(channel.getUri()).andReturn(CHANNEL_URI);
        EasyMock.expect(channel.getChannelSide()).andReturn(ChannelSide.CONSUMER);
        EasyMock.replay(channel);

        manager.register(channel);
        assertEquals(channel, manager.getChannel(CHANNEL_URI, ChannelSide.CONSUMER));

        EasyMock.verify(channel);
    }

    public void testCount() throws Exception {
        EasyMock.expect(channel.getUri()).andReturn(CHANNEL_URI);
        EasyMock.expect(channel.getChannelSide()).andReturn(ChannelSide.CONSUMER);
        EasyMock.replay(channel);

        manager.register(channel);
        assertEquals(1, manager.getCount(CHANNEL_URI, ChannelSide.CONSUMER));
        assertEquals(-1, manager.getCount(CHANNEL_URI, ChannelSide.PRODUCER));
        manager.getAndIncrementChannel(CHANNEL_URI, ChannelSide.CONSUMER);
        assertEquals(2, manager.getCount(CHANNEL_URI, ChannelSide.CONSUMER));
        manager.getAndDecrementChannel(CHANNEL_URI, ChannelSide.CONSUMER);
        manager.getAndDecrementChannel(CHANNEL_URI, ChannelSide.CONSUMER);
        assertEquals(0, manager.getCount(CHANNEL_URI, ChannelSide.CONSUMER));

        EasyMock.verify(channel);
    }

    public void testUnRegister() throws Exception {
        EasyMock.expect(channel.getUri()).andReturn(CHANNEL_URI).times(2);
        EasyMock.expect(channel.getChannelSide()).andReturn(ChannelSide.CONSUMER).times(2);
        EasyMock.replay(channel);

        manager.register(channel);
        manager.unregister(CHANNEL_URI, ChannelSide.CONSUMER);
        manager.register(channel);

        EasyMock.verify(channel);
    }

    public void testStartStopContext() throws Exception {
        EasyMock.expect(channel.getUri()).andReturn(CHANNEL_URI);
        EasyMock.expect(channel.getChannelSide()).andReturn(ChannelSide.CONSUMER);
        EasyMock.expect(channel.getContributionUri()).andReturn(CHANNEL_URI).times(2);
        channel.start();
        channel.stop();
        EasyMock.replay(channel);

        manager.register(channel);
        manager.startContext(CHANNEL_URI);
        manager.stopContext(CHANNEL_URI);

        EasyMock.verify(channel);
    }

    public void setUp() throws Exception {
        super.setUp();
        manager = new ChannelManagerImpl();

        channel = EasyMock.createMock(Channel.class);
    }
}