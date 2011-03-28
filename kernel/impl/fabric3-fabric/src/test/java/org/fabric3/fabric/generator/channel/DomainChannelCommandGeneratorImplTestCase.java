/*
* Fabric3
* Copyright (c) 2009-2011 Metaform Systems
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

import java.net.URI;

import junit.framework.TestCase;
import org.easymock.classextension.EasyMock;

import org.fabric3.fabric.command.BuildChannelsCommand;
import org.fabric3.fabric.command.DisposeChannelsCommand;
import org.fabric3.fabric.generator.GeneratorRegistry;
import org.fabric3.model.type.component.ChannelDefinition;
import org.fabric3.spi.channel.ChannelIntents;
import org.fabric3.spi.generator.ConnectionBindingGenerator;
import org.fabric3.spi.model.instance.LogicalBinding;
import org.fabric3.spi.model.instance.LogicalChannel;
import org.fabric3.spi.model.instance.LogicalState;
import org.fabric3.spi.model.physical.PhysicalChannelBindingDefinition;
import org.fabric3.spi.model.physical.PhysicalChannelDefinition;

/**
 * @version $Revision: 9763 $ $Date: 2011-01-03 01:48:06 +0100 (Mon, 03 Jan 2011) $
 */
public class DomainChannelCommandGeneratorImplTestCase extends TestCase {

    public void testGenerateLocalChannelBuild() throws Exception {
        GeneratorRegistry registry = EasyMock.createMock(GeneratorRegistry.class);
        EasyMock.replay(registry);

        DomainChannelCommandGeneratorImpl generator = new DomainChannelCommandGeneratorImpl(registry);
        LogicalChannel channel = createChannel();
        BuildChannelsCommand command = generator.generateBuild(channel, true);

        assertNotNull(command);
        assertFalse(command.getDefinitions().isEmpty());
        PhysicalChannelDefinition definition = command.getDefinitions().get(0);
        assertEquals("channel", definition.getUri().toString());
        assertTrue(definition.isReplicate());
        assertTrue(definition.isSynchronous());
        EasyMock.verify(registry);
    }

    public void testGenerateLocalChannelFullBuild() throws Exception {
        GeneratorRegistry registry = EasyMock.createMock(GeneratorRegistry.class);
        EasyMock.replay(registry);

        DomainChannelCommandGeneratorImpl generator = new DomainChannelCommandGeneratorImpl(registry);
        LogicalChannel channel = createChannel();
        channel.setState(LogicalState.PROVISIONED);
        BuildChannelsCommand command = generator.generateBuild(channel, false);

        assertNotNull(command);
        assertFalse(command.getDefinitions().isEmpty());
        PhysicalChannelDefinition definition = command.getDefinitions().get(0);
        assertEquals("channel", definition.getUri().toString());
        assertTrue(definition.isReplicate());
        assertTrue(definition.isSynchronous());
        EasyMock.verify(registry);
    }

    @SuppressWarnings({"unchecked"})
    public void testGenerateBoundChannelBuild() throws Exception {
        ConnectionBindingGenerator<?> bindingGenerator = EasyMock.createMock(ConnectionBindingGenerator.class);
        EasyMock.expect(bindingGenerator.generateChannelBinding(EasyMock.isA(LogicalBinding.class))).andReturn(new MockPhysicalDefinition());

        GeneratorRegistry registry = EasyMock.createMock(GeneratorRegistry.class);
        registry.getConnectionBindingGenerator(MockBinding.class);
        EasyMock.expectLastCall().andReturn(bindingGenerator);
        EasyMock.replay(registry, bindingGenerator);

        DomainChannelCommandGeneratorImpl generator = new DomainChannelCommandGeneratorImpl(registry);

        LogicalChannel channel = createChannel();
        LogicalBinding<MockBinding> binding = new LogicalBinding<MockBinding>(new MockBinding(), channel);
        channel.addBinding(binding);

        BuildChannelsCommand command = generator.generateBuild(channel, true);

        assertNotNull(command);
        EasyMock.verify(registry, bindingGenerator);
    }

    public void testGenerateLocalChannelDispose() throws Exception {
        GeneratorRegistry registry = EasyMock.createMock(GeneratorRegistry.class);
        EasyMock.replay(registry);

        DomainChannelCommandGeneratorImpl generator = new DomainChannelCommandGeneratorImpl(registry);
        LogicalChannel channel = createChannel();
        channel.setState(LogicalState.MARKED);
        DisposeChannelsCommand command = generator.generateDispose(channel, true);

        assertNotNull(command);
        assertFalse(command.getDefinitions().isEmpty());
        PhysicalChannelDefinition definition = command.getDefinitions().get(0);
        assertEquals("channel", definition.getUri().toString());
        assertTrue(definition.isReplicate());
        assertTrue(definition.isSynchronous());
        EasyMock.verify(registry);
    }

    @SuppressWarnings({"unchecked"})
    public void testGenerateBoundChannelDispose() throws Exception {
        ConnectionBindingGenerator<?> bindingGenerator = EasyMock.createMock(ConnectionBindingGenerator.class);
        EasyMock.expect(bindingGenerator.generateChannelBinding(EasyMock.isA(LogicalBinding.class))).andReturn(new MockPhysicalDefinition());

        GeneratorRegistry registry = EasyMock.createMock(GeneratorRegistry.class);
        registry.getConnectionBindingGenerator(MockBinding.class);
        EasyMock.expectLastCall().andReturn(bindingGenerator);
        EasyMock.replay(registry, bindingGenerator);

        DomainChannelCommandGeneratorImpl generator = new DomainChannelCommandGeneratorImpl(registry);

        LogicalChannel channel = createChannel();
        channel.setState(LogicalState.MARKED);
        LogicalBinding<MockBinding> binding = new LogicalBinding<MockBinding>(new MockBinding(), channel);
        channel.addBinding(binding);

        DisposeChannelsCommand command = generator.generateDispose(channel, true);

        assertNotNull(command);
        EasyMock.verify(registry, bindingGenerator);
    }

    public void testNoGenerateLocalChannelBuild() throws Exception {
        GeneratorRegistry registry = EasyMock.createMock(GeneratorRegistry.class);
        EasyMock.replay(registry);

        DomainChannelCommandGeneratorImpl generator = new DomainChannelCommandGeneratorImpl(registry);
        LogicalChannel channel = createChannel();
        channel.setState(LogicalState.PROVISIONED);
        assertNull(generator.generateBuild(channel, true));
        EasyMock.verify(registry);
    }

    private LogicalChannel createChannel() {
        ChannelDefinition definition = new ChannelDefinition("channel", URI.create("contribution"));
        definition.addIntent(ChannelIntents.SYNC_INTENT);
        definition.addIntent(ChannelIntents.REPLICATE_INTENT);
        return new LogicalChannel(URI.create("channel"), definition, null);
    }

    private class MockPhysicalDefinition extends PhysicalChannelBindingDefinition {
        private static final long serialVersionUID = -5237182726243360124L;
    }

}