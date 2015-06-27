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
 *
 * Portions originally based on Apache Tuscany 2007
 * licensed under the Apache 2.0 license.
 */
package org.fabric3.fabric.container.command;

import java.net.URI;

import junit.framework.TestCase;
import org.easymock.EasyMock;
import org.fabric3.fabric.container.builder.channel.ChannelBuilderRegistry;
import org.fabric3.spi.container.channel.Channel;
import org.fabric3.spi.model.physical.PhysicalChannel;

/**
 *
 */
public class BuildChannelCommandExecutorTestCase extends TestCase {

    public void testBuildChannel() throws Exception {
        PhysicalChannel physicalChannel = new PhysicalChannel(URI.create("test"), URI.create("bar"));

        Channel channel = EasyMock.createMock(Channel.class);

        ChannelBuilderRegistry channelBuilderRegistry = EasyMock.createMock(ChannelBuilderRegistry.class);
        EasyMock.expect(channelBuilderRegistry.build(EasyMock.isA(PhysicalChannel.class))).andReturn(channel);

        EasyMock.replay(channelBuilderRegistry, channel);

        BuildChannelCommandExecutor executor = new BuildChannelCommandExecutor(channelBuilderRegistry, null);

        BuildChannelCommand command = new BuildChannelCommand(physicalChannel);
        executor.execute(command);

        EasyMock.verify(channelBuilderRegistry, channel);
    }

}
