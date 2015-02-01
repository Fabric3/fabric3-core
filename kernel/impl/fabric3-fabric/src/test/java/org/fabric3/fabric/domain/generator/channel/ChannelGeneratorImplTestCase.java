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
 * Portions originally based on Apache Tuscany 2007
 * licensed under the Apache 2.0 license.
 */
package org.fabric3.fabric.domain.generator.channel;

import javax.xml.namespace.QName;
import java.net.URI;
import java.util.Collections;
import java.util.Map;

import junit.framework.TestCase;
import org.easymock.EasyMock;
import org.fabric3.api.host.Fabric3Exception;
import org.fabric3.api.model.type.component.Channel;
import org.fabric3.fabric.domain.generator.GeneratorRegistry;
import org.fabric3.spi.domain.generator.channel.ChannelDirection;
import org.fabric3.spi.domain.generator.channel.ChannelGeneratorExtension;
import org.fabric3.spi.domain.generator.channel.ConnectionBindingGenerator;
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
        LogicalBinding<MockBinding> binding = new LogicalBinding<>(new MockBinding(), channel);
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
        LogicalBinding<MockBinding> binding = new LogicalBinding<>(new MockBinding(), channel);
        channel.addBinding(binding);

        PhysicalChannelDefinition definition = generator.generateChannelDefinition(channel, DEPLOYABLE, ChannelDirection.CONSUMER);

        assertNotNull(definition);

        EasyMock.verify(registry, bindingGenerator, channelGenerator);
    }

    private ChannelGeneratorExtension getChannelGenerator() throws Fabric3Exception {
        ChannelGeneratorExtension channelGenerator = EasyMock.createMock(ChannelGeneratorExtension.class);
        PhysicalChannelDefinition definition = new PhysicalChannelDefinition(URI.create("channel"), new QName("test", "test"));
        EasyMock.expect(channelGenerator.generate(EasyMock.isA(LogicalChannel.class), EasyMock.eq(DEPLOYABLE))).andReturn(definition);
        return channelGenerator;
    }

    private LogicalChannel createChannel() {
        Channel definition = new Channel("channel");
        return new LogicalChannel(URI.create("channel"), definition, null);
    }

    private class MockPhysicalDefinition extends PhysicalChannelBindingDefinition {
        private static final long serialVersionUID = -5237182726243360124L;

        private MockPhysicalDefinition() {
            super(ChannelDeliveryType.DEFAULT);
        }
    }

}