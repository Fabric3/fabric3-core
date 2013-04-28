/*
 * Fabric3
 * Copyright (c) 2009-2012 Metaform Systems
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
package org.fabric3.fabric.generator.channel;

import javax.xml.namespace.QName;
import java.net.URI;

import junit.framework.TestCase;
import org.easymock.EasyMock;
import org.fabric3.fabric.command.BuildChannelsCommand;
import org.fabric3.fabric.generator.GeneratorRegistry;
import org.fabric3.model.type.component.ChannelDefinition;
import org.fabric3.spi.generator.ChannelGenerator;
import org.fabric3.spi.generator.ConnectionBindingGenerator;
import org.fabric3.spi.model.instance.LogicalBinding;
import org.fabric3.spi.model.instance.LogicalChannel;
import org.fabric3.spi.model.instance.LogicalCompositeComponent;
import org.fabric3.spi.model.instance.LogicalState;
import org.fabric3.spi.model.physical.PhysicalChannelBindingDefinition;
import org.fabric3.spi.model.physical.PhysicalChannelDefinition;

/**
 *
 */
public class BuildChannelCommandGeneratorTestCase extends TestCase {

    @SuppressWarnings({"unchecked"})
    public void testGenerateIncremental() throws Exception {
        ConnectionBindingGenerator<MockBinding> bindingGenerator = EasyMock.createMock(ConnectionBindingGenerator.class);
        MockPhysicalChannelBindingDefinition physical = new MockPhysicalChannelBindingDefinition();
        EasyMock.expect(bindingGenerator.generateChannelBinding(EasyMock.isA(LogicalBinding.class))).andReturn(physical);

        ChannelGenerator channelGenerator = EasyMock.createMock(ChannelGenerator.class);
        PhysicalChannelDefinition definition = new PhysicalChannelDefinition(URI.create("newChannel"), new QName("test", "test"), false);
        EasyMock.expect(channelGenerator.generate(EasyMock.isA(LogicalChannel.class))).andReturn(definition);

        GeneratorRegistry registry = EasyMock.createMock(GeneratorRegistry.class);
        registry.getConnectionBindingGenerator(MockBinding.class);
        EasyMock.expectLastCall().andReturn(bindingGenerator);

        EasyMock.replay(bindingGenerator, registry, channelGenerator);

        BuildChannelCommandGenerator generator = new BuildChannelCommandGenerator(0, channelGenerator, registry);

        LogicalCompositeComponent composite = createComposite();

        BuildChannelsCommand command = generator.generate(composite, true);

        assertEquals(1, command.getDefinitions().size());
        assertEquals("newChannel", command.getDefinitions().get(0).getUri().toString());
        EasyMock.verify(bindingGenerator, registry, channelGenerator);
    }

    @SuppressWarnings({"unchecked"})
    public void testGenerateFull() throws Exception {
        ConnectionBindingGenerator<MockBinding> bindingGenerator = EasyMock.createMock(ConnectionBindingGenerator.class);
        MockPhysicalChannelBindingDefinition physical = new MockPhysicalChannelBindingDefinition();
        EasyMock.expect(bindingGenerator.generateChannelBinding(EasyMock.isA(LogicalBinding.class))).andReturn(physical);

        ChannelGenerator channelGenerator = EasyMock.createMock(ChannelGenerator.class);
        PhysicalChannelDefinition definition = new PhysicalChannelDefinition(URI.create("newChannel"), new QName("test", "test"), false);
        EasyMock.expect(channelGenerator.generate(EasyMock.isA(LogicalChannel.class))).andReturn(definition).times(2);

        GeneratorRegistry registry = EasyMock.createMock(GeneratorRegistry.class);
        registry.getConnectionBindingGenerator(MockBinding.class);
        EasyMock.expectLastCall().andReturn(bindingGenerator);

        EasyMock.replay(bindingGenerator, registry, channelGenerator);

        BuildChannelCommandGenerator generator = new BuildChannelCommandGenerator(0, channelGenerator, registry);

        LogicalCompositeComponent composite = createComposite();

        BuildChannelsCommand command = generator.generate(composite, false);

        assertEquals(2, command.getDefinitions().size());
        EasyMock.verify(bindingGenerator, registry, channelGenerator);
    }

    private LogicalCompositeComponent createComposite() {
        LogicalCompositeComponent composite = new LogicalCompositeComponent(URI.create("composite"), null, null);

        ChannelDefinition newDefinition = new ChannelDefinition("newChannel", URI.create("contribution"));
        LogicalChannel newChannel = new LogicalChannel(URI.create("newChannel"), newDefinition, composite);
        LogicalBinding<MockBinding> binding = new LogicalBinding<MockBinding>(new MockBinding(), newChannel);
        newChannel.addBinding(binding);
        composite.addChannel(newChannel);

        ChannelDefinition oldDefinition = new ChannelDefinition("oldChannel", URI.create("contribution2"));
        LogicalChannel oldChannel = new LogicalChannel(URI.create("oldChannel"), oldDefinition, composite);
        oldChannel.setState(LogicalState.PROVISIONED);
        composite.addChannel(oldChannel);

        return composite;
    }

    private class MockPhysicalChannelBindingDefinition extends PhysicalChannelBindingDefinition {
        private static final long serialVersionUID = 4942207684356048495L;
    }
}
