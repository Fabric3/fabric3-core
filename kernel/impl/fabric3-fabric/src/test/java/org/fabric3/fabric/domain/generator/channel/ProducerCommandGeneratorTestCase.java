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
package org.fabric3.fabric.domain.generator.channel;

import javax.xml.namespace.QName;
import java.net.URI;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import junit.framework.TestCase;
import org.easymock.EasyMock;
import org.fabric3.fabric.domain.command.BuildChannelCommand;
import org.fabric3.fabric.domain.command.ChannelConnectionCommand;
import org.fabric3.fabric.domain.command.DisposeChannelCommand;
import org.fabric3.api.model.type.component.ComponentDefinition;
import org.fabric3.api.model.type.component.ProducerDefinition;
import org.fabric3.spi.domain.generator.channel.ChannelDirection;
import org.fabric3.spi.domain.generator.channel.ConnectionGenerator;
import org.fabric3.spi.model.instance.LogicalChannel;
import org.fabric3.spi.model.instance.LogicalComponent;
import org.fabric3.spi.model.instance.LogicalCompositeComponent;
import org.fabric3.spi.model.instance.LogicalProducer;
import org.fabric3.spi.model.instance.LogicalState;
import org.fabric3.spi.model.physical.PhysicalChannelConnectionDefinition;
import org.fabric3.spi.model.physical.PhysicalChannelDefinition;

/**
 *
 */
public class ProducerCommandGeneratorTestCase extends TestCase {
    private static final QName DEPLOYABLE = new QName("test", "test");

    private BuildChannelCommand buildChannelCommand;
    private DisposeChannelCommand disposeChannelCommand;

    public void testChannelNotFound() throws Exception {
        ConnectionGenerator connectionGenerator = EasyMock.createMock(ConnectionGenerator.class);

        ChannelCommandGenerator channelGenerator = EasyMock.createMock(ChannelCommandGenerator.class);
        EasyMock.replay(connectionGenerator, channelGenerator);

        ProducerCommandGenerator generator = new ProducerCommandGenerator(connectionGenerator, channelGenerator);

        LogicalCompositeComponent parent = new LogicalCompositeComponent(URI.create("domain"), null, null);
        URI channelUri = URI.create("ChannelNotFound");
        ComponentDefinition definition = new ComponentDefinition("component");
        LogicalComponent<?> component = new LogicalComponent(URI.create("component"), definition, parent);
        component.setDeployable(DEPLOYABLE);
        LogicalProducer producer = new LogicalProducer(URI.create("component#producer"), new ProducerDefinition("consumer"), component);
        producer.addTarget(channelUri);
        component.addProducer(producer);

        try {
            generator.generate(component, true);
            fail();
        } catch (ChannelNotFoundException e) {
            // expected
        }
        EasyMock.verify(connectionGenerator, channelGenerator);
    }

    public void testGenerateIncrementalAttach() throws Exception {
        ConnectionGenerator connectionGenerator = EasyMock.createMock(ConnectionGenerator.class);
        List<PhysicalChannelConnectionDefinition> list = Collections.singletonList(new PhysicalChannelConnectionDefinition(null, null, null));
        EasyMock.expect(connectionGenerator.generateProducer(EasyMock.isA(LogicalProducer.class), EasyMock.isA(Map.class))).andReturn(list);

        ChannelCommandGenerator channelGenerator = EasyMock.createMock(ChannelCommandGenerator.class);
        EasyMock.expect(channelGenerator.generateBuild(EasyMock.isA(LogicalChannel.class),
                                                       EasyMock.isA(QName.class),
                                                       EasyMock.isA(ChannelDirection.class))).andReturn(buildChannelCommand);
        EasyMock.replay(connectionGenerator, channelGenerator);

        ProducerCommandGenerator generator = new ProducerCommandGenerator(connectionGenerator, channelGenerator);
        LogicalComponent<?> component = createComponent();
        ChannelConnectionCommand command = generator.generate(component, true);

        assertNotNull(command);
        assertFalse(command.getAttachCommands().isEmpty());
        assertTrue(command.getDetachCommands().isEmpty());
        EasyMock.verify(connectionGenerator, channelGenerator);
    }

    public void testGenerateIncrementalDetach() throws Exception {
        ConnectionGenerator connectionGenerator = EasyMock.createMock(ConnectionGenerator.class);
        List<PhysicalChannelConnectionDefinition> list = Collections.singletonList(new PhysicalChannelConnectionDefinition(null, null, null));
        EasyMock.expect(connectionGenerator.generateProducer(EasyMock.isA(LogicalProducer.class), EasyMock.isA(Map.class))).andReturn(list);

        ChannelCommandGenerator channelGenerator = EasyMock.createMock(ChannelCommandGenerator.class);
        EasyMock.expect(channelGenerator.generateDispose(EasyMock.isA(LogicalChannel.class),
                                                         EasyMock.isA(QName.class),
                                                         EasyMock.isA(ChannelDirection.class))).andReturn(disposeChannelCommand);
        EasyMock.replay(connectionGenerator, channelGenerator);

        ProducerCommandGenerator generator = new ProducerCommandGenerator(connectionGenerator, channelGenerator);
        LogicalComponent<?> component = createComponent();
        component.setState(LogicalState.MARKED);
        ChannelConnectionCommand command = generator.generate(component, true);

        assertNotNull(command);
        assertFalse(command.getDetachCommands().isEmpty());
        assertTrue(command.getAttachCommands().isEmpty());
        EasyMock.verify(connectionGenerator, channelGenerator);
    }

    public void testGenerateFullAttach() throws Exception {
        ConnectionGenerator connectionGenerator = EasyMock.createMock(ConnectionGenerator.class);
        List<PhysicalChannelConnectionDefinition> list = Collections.singletonList(new PhysicalChannelConnectionDefinition(null, null, null));
        EasyMock.expect(connectionGenerator.generateProducer(EasyMock.isA(LogicalProducer.class), EasyMock.isA(Map.class))).andReturn(list);

        ChannelCommandGenerator channelGenerator = EasyMock.createMock(ChannelCommandGenerator.class);
        EasyMock.expect(channelGenerator.generateBuild(EasyMock.isA(LogicalChannel.class),
                                                       EasyMock.isA(QName.class),
                                                       EasyMock.isA(ChannelDirection.class))).andReturn(buildChannelCommand);
        EasyMock.replay(connectionGenerator, channelGenerator);

        ProducerCommandGenerator generator = new ProducerCommandGenerator(connectionGenerator, channelGenerator);
        LogicalComponent<?> component = createComponent();
        component.setState(LogicalState.PROVISIONED);
        ChannelConnectionCommand command = generator.generate(component, false);

        assertNotNull(command);
        assertFalse(command.getAttachCommands().isEmpty());
        assertTrue(command.getDetachCommands().isEmpty());
        EasyMock.verify(connectionGenerator, channelGenerator);
    }

    public void testGenerateFullDetach() throws Exception {
        ConnectionGenerator connectionGenerator = EasyMock.createMock(ConnectionGenerator.class);
        List<PhysicalChannelConnectionDefinition> list = Collections.singletonList(new PhysicalChannelConnectionDefinition(null, null, null));
        EasyMock.expect(connectionGenerator.generateProducer(EasyMock.isA(LogicalProducer.class), EasyMock.isA(Map.class))).andReturn(list);

        ChannelCommandGenerator channelGenerator = EasyMock.createMock(ChannelCommandGenerator.class);
        EasyMock.expect(channelGenerator.generateDispose(EasyMock.isA(LogicalChannel.class),
                                                         EasyMock.isA(QName.class),
                                                         EasyMock.isA(ChannelDirection.class))).andReturn(disposeChannelCommand);
        EasyMock.replay(connectionGenerator, channelGenerator);

        ProducerCommandGenerator generator = new ProducerCommandGenerator(connectionGenerator, channelGenerator);
        LogicalComponent<?> component = createComponent();
        component.setState(LogicalState.MARKED);
        ChannelConnectionCommand command = generator.generate(component, false);

        assertNotNull(command);
        assertTrue(command.getAttachCommands().isEmpty());
        assertFalse(command.getDetachCommands().isEmpty());
        EasyMock.verify(connectionGenerator, channelGenerator);
    }

    public void testGenerateNothingIncremental() throws Exception {
        ConnectionGenerator connectionGenerator = EasyMock.createMock(ConnectionGenerator.class);
        ChannelCommandGenerator channelGenerator = EasyMock.createMock(ChannelCommandGenerator.class);
        EasyMock.replay(connectionGenerator, channelGenerator);

        ProducerCommandGenerator generator = new ProducerCommandGenerator(connectionGenerator, channelGenerator);
        LogicalComponent<?> component = createComponent();
        component.setState(LogicalState.PROVISIONED);
        assertNull(generator.generate(component, true));
        EasyMock.verify(connectionGenerator, channelGenerator);
    }

    @SuppressWarnings({"unchecked"})
    private LogicalComponent<?> createComponent() {
        LogicalCompositeComponent parent = new LogicalCompositeComponent(URI.create("domain"), null, null);
        URI channelUri = URI.create("channel");
        LogicalChannel channel = new LogicalChannel(channelUri, null, parent);
        parent.addChannel(channel);

        ComponentDefinition definition = new ComponentDefinition("component");
        LogicalComponent<?> component = new LogicalComponent(URI.create("component"), definition, parent);
        component.setDeployable(DEPLOYABLE);
        LogicalProducer producer = new LogicalProducer(URI.create("component#producer"), new ProducerDefinition("consumer"), component);
        producer.addTarget(channelUri);
        component.addProducer(producer);
        return component;
    }

    protected void setUp() throws Exception {
        PhysicalChannelDefinition definition = new PhysicalChannelDefinition(URI.create("test"), new QName("foo", "bar"));
        buildChannelCommand = new BuildChannelCommand(definition);
        disposeChannelCommand = new DisposeChannelCommand(definition);
    }

}