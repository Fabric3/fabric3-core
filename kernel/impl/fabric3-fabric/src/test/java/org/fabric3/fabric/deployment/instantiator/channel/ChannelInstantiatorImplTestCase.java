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
package org.fabric3.fabric.deployment.instantiator.channel;

import java.net.URI;
import javax.xml.namespace.QName;

import junit.framework.TestCase;

import org.fabric3.fabric.deployment.instantiator.InstantiationContext;
import org.fabric3.api.model.type.component.BindingDefinition;
import org.fabric3.api.model.type.component.ChannelDefinition;
import org.fabric3.api.model.type.component.ComponentDefinition;
import org.fabric3.api.model.type.component.Composite;
import org.fabric3.api.model.type.component.CompositeImplementation;
import org.fabric3.spi.model.instance.LogicalChannel;
import org.fabric3.spi.model.instance.LogicalCompositeComponent;

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
        ChannelDefinition channel = new ChannelDefinition("channel", contributionUri);
        BindingDefinition binding = new MockBinding();
        channel.addBinding(binding);

        composite = new Composite(COMPOSITE_NAME);
        composite.add(channel);

        URI parentUri = URI.create("parent");
        ComponentDefinition<CompositeImplementation> definition = new ComponentDefinition<>("parent");
        parent = new LogicalCompositeComponent(parentUri, definition, null);
        context = new InstantiationContext();

    }


    private class MockBinding extends BindingDefinition {
        private static final long serialVersionUID = -7088192438672216044L;

        public MockBinding() {
            super(null, null);
        }
    }
}