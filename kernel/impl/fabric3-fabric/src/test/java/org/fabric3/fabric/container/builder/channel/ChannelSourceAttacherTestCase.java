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

import java.net.URI;

import junit.framework.TestCase;
import org.easymock.EasyMock;
import org.fabric3.fabric.model.physical.ChannelSourceDefinition;
import org.fabric3.spi.container.channel.Channel;
import org.fabric3.spi.container.channel.ChannelConnection;
import org.fabric3.spi.container.channel.ChannelManager;
import org.fabric3.spi.model.physical.ChannelSide;
import org.fabric3.spi.model.physical.PhysicalConnectionTargetDefinition;
import org.oasisopen.sca.annotation.EagerInit;

/**
 *
 */
@EagerInit
public class ChannelSourceAttacherTestCase extends TestCase {

    public void testAttachDetach() throws Exception {
        URI channelUri = URI.create("channel");
        URI targetUri = URI.create("target");

        ChannelManager channelManager = EasyMock.createMock(ChannelManager.class);
        ChannelConnection connection = EasyMock.createMock(ChannelConnection.class);

        Channel channel = EasyMock.createMock((Channel.class));
        channel.subscribe(targetUri, connection);
        EasyMock.expectLastCall();
        EasyMock.expect(channel.unsubscribe(targetUri)).andReturn(connection);
        EasyMock.expect(channelManager.getChannel(channelUri, ChannelSide.PRODUCER)).andReturn(channel).times(2);

        EasyMock.replay(channelManager, connection, channel);
        
        ChannelSourceAttacher attacher = new ChannelSourceAttacher(channelManager);
        ChannelSourceDefinition source = new ChannelSourceDefinition(channelUri, ChannelSide.PRODUCER);
        MockPhysicalDefinition target = new MockPhysicalDefinition(targetUri);

        attacher.attach(source, target, connection);
        attacher.detach(source, target);

        EasyMock.verify(channelManager, connection, channel);
    }

    private class MockPhysicalDefinition extends PhysicalConnectionTargetDefinition {
        private static final long serialVersionUID = 9073512301309928102L;

        public MockPhysicalDefinition(URI uri) {
            setUri(uri);
        }
    }
}