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
package org.fabric3.fabric.generator.channel;

import javax.xml.namespace.QName;
import java.net.URI;
import java.util.Collections;
import java.util.Map;

import junit.framework.TestCase;
import org.easymock.EasyMock;
import org.fabric3.fabric.generator.GeneratorRegistry;
import org.fabric3.model.type.component.ChannelDefinition;
import org.fabric3.spi.channel.ChannelConstants;
import org.fabric3.spi.generator.ChannelDirection;
import org.fabric3.spi.generator.ChannelGeneratorExtension;
import org.fabric3.spi.generator.ConnectionBindingGenerator;
import org.fabric3.spi.generator.GenerationException;
import org.fabric3.spi.model.instance.LogicalBinding;
import org.fabric3.spi.model.instance.LogicalChannel;
import org.fabric3.spi.model.instance.LogicalState;
import org.fabric3.spi.model.physical.ChannelDeliveryType;
import org.fabric3.spi.model.physical.PhysicalChannelBindingDefinition;
import org.fabric3.spi.model.physical.PhysicalChannelDefinition;

/**
 *
 */
public class ChannelGeneratorImplTestCase extends TestCase {
    private static final QName DEPLOYABLE = new QName("foo", "bar");

    public void testGenerateLocalChannelBuild() throws Exception {

        ChannelGeneratorExtension channelGenerator = getChannelGenerator();

        GeneratorRegistry registry = EasyMock.createMock(GeneratorRegistry.class);

        EasyMock.replay(registry, channelGenerator);

        ChannelGeneratorImpl generator = new ChannelGeneratorImpl(registry);
        Map<String, ChannelGeneratorExtension> map = Collections.singletonMap("default", channelGenerator);
        generator.setExtensions(map);
        LogicalChannel channel = createChannel();
        PhysicalChannelDefinition definition = generator.generateChannelDefinition(channel, DEPLOYABLE, ChannelDirection.CONSUMER);

        assertNotNull(definition);

        EasyMock.verify(registry, channelGenerator);
    }

    public void testGenerateLocalChannelFullBuild() throws Exception {
        ChannelGeneratorExtension channelGenerator = getChannelGenerator();

        GeneratorRegistry registry = EasyMock.createMock(GeneratorRegistry.class);

        EasyMock.replay(registry, channelGenerator);

        ChannelGeneratorImpl generator = new ChannelGeneratorImpl(registry);
        Map<String, ChannelGeneratorExtension> map = Collections.singletonMap("default", channelGenerator);
        generator.setExtensions(map);
        LogicalChannel channel = createChannel();
        channel.setState(LogicalState.PROVISIONED);
        PhysicalChannelDefinition definition = generator.generateChannelDefinition(channel, DEPLOYABLE, ChannelDirection.CONSUMER);

        assertNotNull(definition);

        EasyMock.verify(registry, channelGenerator);
    }

    @SuppressWarnings({"unchecked"})
    public void testGenerateBoundChannelBuild() throws Exception {
        ChannelGeneratorExtension channelGenerator = getChannelGenerator();

        ConnectionBindingGenerator<?> bindingGenerator = EasyMock.createMock(ConnectionBindingGenerator.class);
        EasyMock.expect(bindingGenerator.generateChannelBinding(EasyMock.isA(LogicalBinding.class),
                                                                EasyMock.isA(ChannelDeliveryType.class))).andReturn(new MockPhysicalDefinition());

        GeneratorRegistry registry = EasyMock.createMock(GeneratorRegistry.class);
        registry.getConnectionBindingGenerator(MockBinding.class);
        EasyMock.expectLastCall().andReturn(bindingGenerator);

        EasyMock.replay(registry, bindingGenerator, channelGenerator);

        ChannelGeneratorImpl generator = new ChannelGeneratorImpl(registry);
        Map<String, ChannelGeneratorExtension> map = Collections.singletonMap("default", channelGenerator);
        generator.setExtensions(map);

        LogicalChannel channel = createChannel();
        LogicalBinding<MockBinding> binding = new LogicalBinding<MockBinding>(new MockBinding(), channel);
        channel.addBinding(binding);

        PhysicalChannelDefinition definition = generator.generateChannelDefinition(channel, DEPLOYABLE, ChannelDirection.CONSUMER);

        assertNotNull(definition);

        EasyMock.verify(registry, bindingGenerator, channelGenerator);
    }

    public void testGenerateLocalChannelDispose() throws Exception {
        ChannelGeneratorExtension channelGenerator = getChannelGenerator();

        GeneratorRegistry registry = EasyMock.createMock(GeneratorRegistry.class);

        EasyMock.replay(registry, channelGenerator);

        ChannelGeneratorImpl generator = new ChannelGeneratorImpl(registry);
        Map<String, ChannelGeneratorExtension> map = Collections.singletonMap("default", channelGenerator);
        generator.setExtensions(map);
        LogicalChannel channel = createChannel();
        channel.setState(LogicalState.MARKED);
        PhysicalChannelDefinition definition = generator.generateChannelDefinition(channel, DEPLOYABLE, ChannelDirection.CONSUMER);

        assertNotNull(definition);

        EasyMock.verify(registry, channelGenerator);
    }

    @SuppressWarnings({"unchecked"})
    public void testGenerateBoundChannelDispose() throws Exception {
        ChannelGeneratorExtension channelGenerator = getChannelGenerator();

        ConnectionBindingGenerator<?> bindingGenerator = EasyMock.createMock(ConnectionBindingGenerator.class);
        EasyMock.expect(bindingGenerator.generateChannelBinding(EasyMock.isA(LogicalBinding.class),
                                                                EasyMock.isA(ChannelDeliveryType.class))).andReturn(new MockPhysicalDefinition());

        GeneratorRegistry registry = EasyMock.createMock(GeneratorRegistry.class);

        registry.getConnectionBindingGenerator(MockBinding.class);
        EasyMock.expectLastCall().andReturn(bindingGenerator);

        EasyMock.replay(registry, bindingGenerator, channelGenerator);

        ChannelGeneratorImpl generator = new ChannelGeneratorImpl(registry);
        Map<String, ChannelGeneratorExtension> map = Collections.singletonMap("default", channelGenerator);
        generator.setExtensions(map);

        LogicalChannel channel = createChannel();
        channel.setState(LogicalState.MARKED);
        LogicalBinding<MockBinding> binding = new LogicalBinding<MockBinding>(new MockBinding(), channel);
        channel.addBinding(binding);

        PhysicalChannelDefinition definition = generator.generateChannelDefinition(channel, DEPLOYABLE, ChannelDirection.CONSUMER);

        assertNotNull(definition);

        EasyMock.verify(registry, bindingGenerator, channelGenerator);
    }

    private ChannelGeneratorExtension getChannelGenerator() throws GenerationException {
        ChannelGeneratorExtension channelGenerator = EasyMock.createMock(ChannelGeneratorExtension.class);
        PhysicalChannelDefinition definition = new PhysicalChannelDefinition(URI.create("channel"), new QName("test", "test"), false);
        EasyMock.expect(channelGenerator.generate(EasyMock.isA(LogicalChannel.class), EasyMock.eq(DEPLOYABLE))).andReturn(definition);
        return channelGenerator;
    }

    private LogicalChannel createChannel() {
        ChannelDefinition definition = new ChannelDefinition("channel", URI.create("contribution"));
        definition.addIntent(ChannelConstants.REPLICATE_INTENT);
        return new LogicalChannel(URI.create("channel"), definition, null);
    }

    private class MockPhysicalDefinition extends PhysicalChannelBindingDefinition {
        private static final long serialVersionUID = -5237182726243360124L;

        private MockPhysicalDefinition() {
            super(ChannelDeliveryType.DEFAULT);
        }
    }

}