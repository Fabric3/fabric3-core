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
package org.fabric3.channel.builder;

import javax.xml.namespace.QName;
import java.net.URI;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ExecutorService;

import junit.framework.TestCase;
import org.easymock.EasyMock;
import org.fabric3.spi.container.builder.component.ChannelBindingBuilder;
import org.fabric3.spi.container.channel.Channel;
import org.fabric3.spi.model.physical.ChannelDeliveryType;
import org.fabric3.spi.model.physical.PhysicalChannelBindingDefinition;
import org.fabric3.spi.model.physical.PhysicalChannelDefinition;

/**
 *
 */
public class DefaultChannelBuilderTestCase extends TestCase {

    @SuppressWarnings({"unchecked"})
    public void testBuildChannel() throws Exception {
        PhysicalChannelDefinition definition = new PhysicalChannelDefinition(URI.create("test"), new QName("foo", "bar"));
        definition.setBindingDefinition(new MockBindingDefinition());

        Channel channel = EasyMock.createMock(Channel.class);

        ChannelBindingBuilder bindingBuilder = EasyMock.createMock(ChannelBindingBuilder.class);
        bindingBuilder.build(EasyMock.isA(PhysicalChannelBindingDefinition.class), EasyMock.isA(Channel.class));

        ExecutorService executorService = EasyMock.createMock(ExecutorService.class);

        EasyMock.replay(channel, bindingBuilder);

        DefaultChannelBuilder builder = new DefaultChannelBuilder(executorService);

        Map bindingBuilderMap = Collections.singletonMap(MockBindingDefinition.class, bindingBuilder);
        builder.setBindingBuilders(bindingBuilderMap);

        assertNotNull(builder.build(definition));

        EasyMock.verify(channel, bindingBuilder);
    }

    private class MockBindingDefinition extends PhysicalChannelBindingDefinition {
        private static final long serialVersionUID = -474926224717103363L;

        private MockBindingDefinition() {
            super(ChannelDeliveryType.DEFAULT);
        }
    }
}
