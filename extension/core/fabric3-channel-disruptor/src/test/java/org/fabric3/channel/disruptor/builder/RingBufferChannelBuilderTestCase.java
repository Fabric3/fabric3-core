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

import javax.xml.namespace.QName;
import java.net.URI;
import java.util.concurrent.ExecutorService;

import junit.framework.TestCase;
import org.easymock.EasyMock;
import org.fabric3.api.model.type.component.RingBufferData;
import org.fabric3.spi.container.channel.Channel;
import org.fabric3.spi.model.physical.ChannelDeliveryType;
import org.fabric3.spi.model.physical.PhysicalChannelDefinition;

/**
 *
 */
public class RingBufferChannelBuilderTestCase extends TestCase {
    public static final QName DEPLOYABLE = new QName("test", "test");
    public static final URI URI = java.net.URI.create("test");

    private RingBufferChannelBuilder builder;
    private PhysicalChannelDefinition definition;

    public void testBuild() throws Exception {
        Channel channel = builder.build(definition);

        assertEquals(URI, channel.getUri());
        assertEquals(DEPLOYABLE, channel.getDeployable());
    }

    public void testDispose() throws Exception {
        Channel channel = EasyMock.createMock(Channel.class);

        builder.dispose(definition, channel);
    }

    public void setUp() throws Exception {
        super.setUp();
        ExecutorService executorService = EasyMock.createMock(ExecutorService.class);
        EasyMock.replay(executorService);

        builder = new RingBufferChannelBuilder(executorService);

        definition = new PhysicalChannelDefinition(URI, DEPLOYABLE, "ring.buffer", ChannelDeliveryType.ASYNCHRONOUS_WORKER);
        definition.setMetadata(new RingBufferData());
    }
}
