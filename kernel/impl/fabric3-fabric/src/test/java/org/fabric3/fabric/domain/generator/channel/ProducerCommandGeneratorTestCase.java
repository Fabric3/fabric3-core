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

import java.net.URI;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import junit.framework.TestCase;
import org.easymock.EasyMock;
import org.fabric3.api.host.Fabric3Exception;
import org.fabric3.api.model.type.component.Component;
import org.fabric3.api.model.type.component.Producer;
import org.fabric3.fabric.container.command.BuildChannelCommand;
import org.fabric3.fabric.container.command.ChannelConnectionCommand;
import org.fabric3.fabric.container.command.DisposeChannelCommand;
import org.fabric3.spi.model.instance.LogicalChannel;
import org.fabric3.spi.model.instance.LogicalComponent;
import org.fabric3.spi.model.instance.LogicalCompositeComponent;
import org.fabric3.spi.model.instance.LogicalProducer;
import org.fabric3.spi.model.instance.LogicalState;
import org.fabric3.spi.model.physical.PhysicalChannel;
import org.fabric3.spi.model.physical.PhysicalChannelConnection;

/**
 *
 */
public class ProducerCommandGeneratorTestCase extends TestCase {

    private URI uri = URI.create("testChannel");

    private BuildChannelCommand buildChannelCommand;
    private DisposeChannelCommand disposeChannelCommand;

    public void testChannelNotFound() throws Exception {
        ConnectionGenerator connectionGenerator = EasyMock.createMock(ConnectionGenerator.class);

        ChannelCommandGenerator channelGenerator = EasyMock.createMock(ChannelCommandGenerator.class);
        EasyMock.replay(connectionGenerator, channelGenerator);

        ProducerCommandGenerator generator = new ProducerCommandGenerator(connectionGenerator, channelGenerator);

        LogicalCompositeComponent parent = new LogicalCompositeComponent(URI.create("domain"), null, null);
        URI channelUri = URI.create("ChannelNotFound");
        Component definition = new Component("component");
        LogicalComponent<?> component = new LogicalComponent(URI.create("component"), definition, parent);
        definition.setContributionUri(URI.create("test"));
        LogicalProducer producer = new LogicalProducer(URI.create("component#producer"), new Producer("consumer"), component);
        producer.addTarget(channelUri);
        component.addProducer(producer);

        try {
            generator.generate(component);
            fail();
        } catch (Fabric3Exception e) {
            // expected
        }
        EasyMock.verify(connectionGenerator, channelGenerator);
    }

    public void testGenerateAttach() throws Exception {
        ConnectionGenerator connectionGenerator = EasyMock.createMock(ConnectionGenerator.class);
        List<PhysicalChannelConnection> list = Collections.singletonList(new PhysicalChannelConnection(uri, URI.create("test"), null, null, null, false));
        EasyMock.expect(connectionGenerator.generateProducer(EasyMock.isA(LogicalProducer.class), EasyMock.isA(Map.class))).andReturn(list);

        ChannelCommandGenerator channelGenerator = EasyMock.createMock(ChannelCommandGenerator.class);
        EasyMock.expect(channelGenerator.generateBuild(EasyMock.isA(LogicalChannel.class),
                                                       EasyMock.isA(URI.class),
                                                       EasyMock.isA(ChannelDirection.class))).andReturn(buildChannelCommand);
        EasyMock.replay(connectionGenerator, channelGenerator);

        ProducerCommandGenerator generator = new ProducerCommandGenerator(connectionGenerator, channelGenerator);
        LogicalComponent<?> component = createComponent();
        ChannelConnectionCommand command = generator.generate(component).get();

        assertNotNull(command);
        assertFalse(command.getAttachCommands().isEmpty());
        assertTrue(command.getDetachCommands().isEmpty());
        EasyMock.verify(connectionGenerator, channelGenerator);
    }

    public void testGenerateDetach() throws Exception {
        ConnectionGenerator connectionGenerator = EasyMock.createMock(ConnectionGenerator.class);
        List<PhysicalChannelConnection> list = Collections.singletonList(new PhysicalChannelConnection(uri, URI.create("test"), null, null, null, false));
        EasyMock.expect(connectionGenerator.generateProducer(EasyMock.isA(LogicalProducer.class), EasyMock.isA(Map.class))).andReturn(list);

        ChannelCommandGenerator channelGenerator = EasyMock.createMock(ChannelCommandGenerator.class);
        EasyMock.expect(channelGenerator.generateDispose(EasyMock.isA(LogicalChannel.class),
                                                         EasyMock.isA(URI.class),
                                                         EasyMock.isA(ChannelDirection.class))).andReturn(disposeChannelCommand);
        EasyMock.replay(connectionGenerator, channelGenerator);

        ProducerCommandGenerator generator = new ProducerCommandGenerator(connectionGenerator, channelGenerator);
        LogicalComponent<?> component = createComponent();
        component.setState(LogicalState.MARKED);
        ChannelConnectionCommand command = generator.generate(component).get();

        assertNotNull(command);
        assertFalse(command.getDetachCommands().isEmpty());
        assertTrue(command.getAttachCommands().isEmpty());
        EasyMock.verify(connectionGenerator, channelGenerator);
    }

    public void testGenerateFullDetach() throws Exception {
        ConnectionGenerator connectionGenerator = EasyMock.createMock(ConnectionGenerator.class);
        List<PhysicalChannelConnection> list = Collections.singletonList(new PhysicalChannelConnection(uri, URI.create("test"), null, null, null, false));
        EasyMock.expect(connectionGenerator.generateProducer(EasyMock.isA(LogicalProducer.class), EasyMock.isA(Map.class))).andReturn(list);

        ChannelCommandGenerator channelGenerator = EasyMock.createMock(ChannelCommandGenerator.class);
        EasyMock.expect(channelGenerator.generateDispose(EasyMock.isA(LogicalChannel.class),
                                                         EasyMock.isA(URI.class),
                                                         EasyMock.isA(ChannelDirection.class))).andReturn(disposeChannelCommand);
        EasyMock.replay(connectionGenerator, channelGenerator);

        ProducerCommandGenerator generator = new ProducerCommandGenerator(connectionGenerator, channelGenerator);
        LogicalComponent<?> component = createComponent();
        component.setState(LogicalState.MARKED);
        ChannelConnectionCommand command = generator.generate(component).get();

        assertNotNull(command);
        assertTrue(command.getAttachCommands().isEmpty());
        assertFalse(command.getDetachCommands().isEmpty());
        EasyMock.verify(connectionGenerator, channelGenerator);
    }

    public void testGenerateNothing() throws Exception {
        ConnectionGenerator connectionGenerator = EasyMock.createMock(ConnectionGenerator.class);
        ChannelCommandGenerator channelGenerator = EasyMock.createMock(ChannelCommandGenerator.class);
        EasyMock.replay(connectionGenerator, channelGenerator);

        ProducerCommandGenerator generator = new ProducerCommandGenerator(connectionGenerator, channelGenerator);
        LogicalComponent<?> component = createComponent();
        component.setState(LogicalState.PROVISIONED);
        assertFalse(generator.generate(component).isPresent());
        EasyMock.verify(connectionGenerator, channelGenerator);
    }

    @SuppressWarnings({"unchecked"})
    private LogicalComponent<?> createComponent() {
        LogicalCompositeComponent parent = new LogicalCompositeComponent(URI.create("domain"), null, null);
        URI channelUri = URI.create("channel");
        LogicalChannel channel = new LogicalChannel(channelUri, null, parent);
        parent.addChannel(channel);

        Component definition = new Component("component");
        definition.setContributionUri(URI.create("test"));
        LogicalComponent<?> component = new LogicalComponent(URI.create("component"), definition, parent);
        LogicalProducer producer = new LogicalProducer(URI.create("component#producer"), new Producer("consumer"), component);
        producer.addTarget(channelUri);
        component.addProducer(producer);
        return component;
    }

    protected void setUp() throws Exception {
        PhysicalChannel physicalChannel = new PhysicalChannel(URI.create("test"), URI.create("bar"));
        buildChannelCommand = new BuildChannelCommand(physicalChannel);
        disposeChannelCommand = new DisposeChannelCommand(physicalChannel);
    }

}