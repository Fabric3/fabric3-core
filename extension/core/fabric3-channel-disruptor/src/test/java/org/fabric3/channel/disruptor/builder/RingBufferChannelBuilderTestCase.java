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
package org.fabric3.channel.disruptor.builder;

import java.net.URI;
import java.util.concurrent.ExecutorService;

import junit.framework.TestCase;
import org.easymock.EasyMock;
import org.fabric3.api.model.type.component.RingBufferData;
import org.fabric3.spi.container.channel.Channel;
import org.fabric3.spi.model.physical.DeliveryType;
import org.fabric3.spi.model.physical.PhysicalChannel;

/**
 *
 */
public class RingBufferChannelBuilderTestCase extends TestCase {
    public static final URI CONTRIBUTION_URI = URI.create("test");
    public static final URI TEST = java.net.URI.create("test");

    private RingBufferChannelBuilder builder;
    private PhysicalChannel physicalChannel;

    public void testBuild() throws Exception {
        Channel channel = builder.build(physicalChannel);

        assertEquals(TEST, channel.getUri());
        assertEquals(CONTRIBUTION_URI, channel.getContributionUri());
    }

    public void testDispose() throws Exception {
        Channel channel = EasyMock.createMock(Channel.class);

        builder.dispose(physicalChannel, channel);
    }

    public void setUp() throws Exception {
        super.setUp();
        ExecutorService executorService = EasyMock.createMock(ExecutorService.class);
        EasyMock.replay(executorService);

        builder = new RingBufferChannelBuilder(executorService);

        physicalChannel = new PhysicalChannel(TEST, "ring.buffer", DeliveryType.ASYNCHRONOUS_WORKER, CONTRIBUTION_URI);
        physicalChannel.setMetadata(new RingBufferData());
    }
}
