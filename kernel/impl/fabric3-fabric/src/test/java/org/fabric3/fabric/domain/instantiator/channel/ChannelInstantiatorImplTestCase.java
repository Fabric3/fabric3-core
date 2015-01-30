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
package org.fabric3.fabric.domain.instantiator.channel;

import javax.xml.namespace.QName;
import java.net.URI;

import junit.framework.TestCase;
import org.fabric3.api.model.type.component.Binding;
import org.fabric3.api.model.type.component.Channel;
import org.fabric3.api.model.type.component.Component;
import org.fabric3.api.model.type.component.Composite;
import org.fabric3.fabric.domain.instantiator.InstantiationContext;
import org.fabric3.spi.model.instance.LogicalChannel;
import org.fabric3.spi.model.instance.LogicalCompositeComponent;
import org.fabric3.spi.model.type.component.CompositeImplementation;

/**
 *
 */
public class ChannelInstantiatorImplTestCase extends TestCase {
    private static final QName COMPOSITE_NAME = new QName("test", "test");
    private static final URI CHANNEL = URI.create("parent/channel");

    private ChannelInstantiatorImpl instantiator;
    private InstantiationContext context;
    private Composite composite;
    private LogicalCompositeComponent parent;

    public void testInstantiateChannels() throws Exception {
        instantiator.instantiateChannels(composite, parent, context);
        assertFalse(context.hasErrors());
        LogicalChannel logicalChannel = parent.getChannel(CHANNEL);
        assertEquals(CHANNEL, logicalChannel.getUri());
        assertEquals(COMPOSITE_NAME, logicalChannel.getDeployable());
        assertNotNull(logicalChannel.getDefinition());
        assertFalse(logicalChannel.getBindings().isEmpty());
    }

    public void testDuplicateChannels() throws Exception {
        LogicalChannel channel = new LogicalChannel(CHANNEL, null, null);
        parent.addChannel(channel);

        instantiator.instantiateChannels(composite, parent, context);
        assertTrue(context.hasErrors());
        assertTrue(context.getErrors().get(0) instanceof DuplicateChannel);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        instantiator = new ChannelInstantiatorImpl();

        URI contributionUri = URI.create("contribution");
        Channel channel = new Channel("channel");
        Binding binding = new MockBinding();
        channel.addBinding(binding);

        composite = new Composite(COMPOSITE_NAME);
        composite.add(channel);

        URI parentUri = URI.create("parent");
        Component<CompositeImplementation> definition = new Component<>("parent");
        parent = new LogicalCompositeComponent(parentUri, definition, null);
        context = new InstantiationContext();

    }


    private class MockBinding extends Binding {
        private static final long serialVersionUID = -7088192438672216044L;

        public MockBinding() {
            super(null, null);
        }
    }
}