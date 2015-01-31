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
import java.util.List;
import java.util.Map;

import junit.framework.TestCase;
import org.easymock.EasyMock;
import org.fabric3.api.host.ContainerException;
import org.fabric3.api.model.type.component.Component;
import org.fabric3.api.model.type.component.Consumer;
import org.fabric3.fabric.container.command.BuildChannelCommand;
import org.fabric3.fabric.container.command.ChannelConnectionCommand;
import org.fabric3.fabric.container.command.DisposeChannelCommand;
import org.fabric3.spi.domain.generator.channel.ChannelDirection;
import org.fabric3.spi.domain.generator.channel.ConnectionGenerator;
import org.fabric3.spi.model.instance.LogicalChannel;
import org.fabric3.spi.model.instance.LogicalComponent;
import org.fabric3.spi.model.instance.LogicalCompositeComponent;
import org.fabric3.spi.model.instance.LogicalConsumer;
import org.fabric3.spi.model.instance.LogicalState;
import org.fabric3.spi.model.physical.PhysicalChannelConnectionDefinition;
import org.fabric3.spi.model.physical.PhysicalChannelDefinition;

/**
 *
 */
public class ConsumerCommandGeneratorTestCase extends TestCase {
    private static final QName DEPLOYABLE = new QName("test", "test");

    private BuildChannelCommand buildChannelCommand;
    private DisposeChannelCommand disposeChannelCommand;

    public void testGenerateChannelNotFound() throws Exception {
        ConnectionGenerator connectionGenerator = EasyMock.createMock(ConnectionGenerator.class);

        ChannelCommandGenerator channelGenerator = EasyMock.createMock(ChannelCommandGenerator.class);
        EasyMock.replay(connectionGenerator, channelGenerator);

        ConsumerCommandGenerator generator = new ConsumerCommandGenerator(connectionGenerator, channelGenerator);

        LogicalCompositeComponent parent = new LogicalCompositeComponent(URI.create("domain"), null, null);
        URI channelUri = URI.create("NotFoundChannel");
        Component definition = new Component("component");
        LogicalComponent<?> component = new LogicalComponent(URI.create("component"), definition, parent);
        component.setDeployable(DEPLOYABLE);
        LogicalConsumer consumer = new LogicalConsumer(URI.create("component#consumer"), new Consumer("consumer"), component);
        component.addConsumer(consumer);

        consumer.addSource(channelUri);

        try {
            generator.generate(component);
            fail();
        } catch (ContainerException e) {
            // expected;
        }
        EasyMock.verify(connectionGenerator, channelGenerator);
    }

    public void testGenerateBuildChannel() throws Exception {
        ConnectionGenerator connectionGenerator = EasyMock.createMock(ConnectionGenerator.class);
        List<PhysicalChannelConnectionDefinition> list = Collections.singletonList(new PhysicalChannelConnectionDefinition(null, null, null));
        EasyMock.expect(connectionGenerator.generateConsumer(EasyMock.isA(LogicalConsumer.class), EasyMock.isA(Map.class))).andReturn(list);

        ChannelCommandGenerator channelGenerator = EasyMock.createMock(ChannelCommandGenerator.class);
        EasyMock.expect(channelGenerator.generateBuild(EasyMock.isA(LogicalChannel.class),
                                                       EasyMock.isA(QName.class),
                                                       EasyMock.isA(ChannelDirection.class))).andReturn(buildChannelCommand);
        EasyMock.replay(connectionGenerator, channelGenerator);

        ConsumerCommandGenerator generator = new ConsumerCommandGenerator(connectionGenerator, channelGenerator);
        LogicalComponent<?> component = createComponent();

        ChannelConnectionCommand command = generator.generate(component);

        assertNotNull(command);
        List<BuildChannelCommand> buildChannelCommands = command.getBuildChannelCommands();
        assertEquals(1, buildChannelCommands.size());
        assertFalse(command.getAttachCommands().isEmpty());
        assertTrue(command.getDetachCommands().isEmpty());
        EasyMock.verify(connectionGenerator, channelGenerator);
    }

    public void testGenerateAttach() throws Exception {
        ConnectionGenerator connectionGenerator = EasyMock.createMock(ConnectionGenerator.class);
        List<PhysicalChannelConnectionDefinition> list = Collections.singletonList(new PhysicalChannelConnectionDefinition(null, null, null));
        EasyMock.expect(connectionGenerator.generateConsumer(EasyMock.isA(LogicalConsumer.class), EasyMock.isA(Map.class))).andReturn(list);

        ChannelCommandGenerator channelGenerator = EasyMock.createMock(ChannelCommandGenerator.class);
        EasyMock.expect(channelGenerator.generateBuild(EasyMock.isA(LogicalChannel.class),
                                                       EasyMock.isA(QName.class),
                                                       EasyMock.isA(ChannelDirection.class))).andReturn(buildChannelCommand);
        EasyMock.replay(connectionGenerator, channelGenerator);

        ConsumerCommandGenerator generator = new ConsumerCommandGenerator(connectionGenerator, channelGenerator);
        LogicalComponent<?> component = createComponent();
        ChannelConnectionCommand command = generator.generate(component);

        assertNotNull(command);
        assertFalse(command.getAttachCommands().isEmpty());
        assertTrue(command.getDetachCommands().isEmpty());
        EasyMock.verify(connectionGenerator, channelGenerator);
    }

    public void testGenerateDetach() throws Exception {
        ConnectionGenerator connectionGenerator = EasyMock.createMock(ConnectionGenerator.class);
        List<PhysicalChannelConnectionDefinition> list = Collections.singletonList(new PhysicalChannelConnectionDefinition(null, null, null));
        EasyMock.expect(connectionGenerator.generateConsumer(EasyMock.isA(LogicalConsumer.class), EasyMock.isA(Map.class))).andReturn(list);

        ChannelCommandGenerator channelGenerator = EasyMock.createMock(ChannelCommandGenerator.class);
        EasyMock.expect(channelGenerator.generateDispose(EasyMock.isA(LogicalChannel.class),
                                                         EasyMock.isA(QName.class),
                                                         EasyMock.isA(ChannelDirection.class))).andReturn(disposeChannelCommand);

        EasyMock.replay(connectionGenerator, channelGenerator);

        ConsumerCommandGenerator generator = new ConsumerCommandGenerator(connectionGenerator, channelGenerator);
        LogicalComponent<?> component = createComponent();
        component.setState(LogicalState.MARKED);
        ChannelConnectionCommand command = generator.generate(component);

        List<DisposeChannelCommand> disposeChannelCommands = command.getDisposeChannelCommands();
        assertEquals(1, disposeChannelCommands.size());

        assertNotNull(command);
        assertFalse(command.getDetachCommands().isEmpty());
        assertTrue(command.getAttachCommands().isEmpty());
        EasyMock.verify(connectionGenerator, channelGenerator);
    }

    public void testGenerateFullDetach() throws Exception {
        ConnectionGenerator connectionGenerator = EasyMock.createMock(ConnectionGenerator.class);
        List<PhysicalChannelConnectionDefinition> list = Collections.singletonList(new PhysicalChannelConnectionDefinition(null, null, null));
        EasyMock.expect(connectionGenerator.generateConsumer(EasyMock.isA(LogicalConsumer.class), EasyMock.isA(Map.class))).andReturn(list);

        ChannelCommandGenerator channelGenerator = EasyMock.createMock(ChannelCommandGenerator.class);
        EasyMock.expect(channelGenerator.generateDispose(EasyMock.isA(LogicalChannel.class),
                                                         EasyMock.isA(QName.class),
                                                         EasyMock.isA(ChannelDirection.class))).andReturn(disposeChannelCommand);
        EasyMock.replay(connectionGenerator, channelGenerator);

        ConsumerCommandGenerator generator = new ConsumerCommandGenerator(connectionGenerator, channelGenerator);
        LogicalComponent<?> component = createComponent();
        component.setState(LogicalState.MARKED);
        ChannelConnectionCommand command = generator.generate(component);

        assertNotNull(command);
        assertTrue(command.getAttachCommands().isEmpty());
        assertFalse(command.getDetachCommands().isEmpty());
        EasyMock.verify(connectionGenerator, channelGenerator);
    }

    public void testGenerateNothing() throws Exception {
        ConnectionGenerator connectionGenerator = EasyMock.createMock(ConnectionGenerator.class);
        ChannelCommandGenerator channelGenerator = EasyMock.createMock(ChannelCommandGenerator.class);
        EasyMock.replay(connectionGenerator, channelGenerator);

        ConsumerCommandGenerator generator = new ConsumerCommandGenerator(connectionGenerator, channelGenerator);
        LogicalComponent<?> component = createComponent();
        component.setState(LogicalState.PROVISIONED);
        assertNull(generator.generate(component));
        EasyMock.verify(connectionGenerator, channelGenerator);
    }

    @SuppressWarnings({"unchecked"})
    private LogicalComponent<?> createComponent() {
        LogicalCompositeComponent parent = new LogicalCompositeComponent(URI.create("domain"), null, null);
        URI channelUri = URI.create("channel");
        LogicalChannel channel = new LogicalChannel(channelUri, null, parent);
        parent.addChannel(channel);
        Component definition = new Component("component");
        LogicalComponent<?> component = new LogicalComponent(URI.create("component"), definition, parent);
        component.setDeployable(DEPLOYABLE);
        LogicalConsumer consumer = new LogicalConsumer(URI.create("component#consumer"), new Consumer("consumer"), component);
        component.addConsumer(consumer);

        consumer.addSource(channelUri);

        return component;
    }

    protected void setUp() throws Exception {
        PhysicalChannelDefinition definition = new PhysicalChannelDefinition(URI.create("test"), new QName("foo", "bar"));
        buildChannelCommand = new BuildChannelCommand(definition);
        disposeChannelCommand = new DisposeChannelCommand(definition);
    }
}