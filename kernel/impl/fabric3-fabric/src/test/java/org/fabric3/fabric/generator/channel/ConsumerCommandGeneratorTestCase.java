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

import java.net.URI;
import java.util.Collections;
import java.util.List;

import junit.framework.TestCase;
import org.easymock.classextension.EasyMock;

import org.fabric3.fabric.command.ChannelConnectionCommand;
import org.fabric3.model.type.component.ComponentDefinition;
import org.fabric3.model.type.component.ConsumerDefinition;
import org.fabric3.spi.model.instance.LogicalComponent;
import org.fabric3.spi.model.instance.LogicalConsumer;
import org.fabric3.spi.model.instance.LogicalState;
import org.fabric3.spi.model.physical.PhysicalChannelConnectionDefinition;

/**
 *
 */
public class ConsumerCommandGeneratorTestCase extends TestCase {

    public void testGenerateIncrementalAttach() throws Exception {
        ConnectionGenerator connectionGenerator = EasyMock.createMock(ConnectionGenerator.class);
        List<PhysicalChannelConnectionDefinition> list = Collections.singletonList(new PhysicalChannelConnectionDefinition(null, null, null));
        EasyMock.expect(connectionGenerator.generateConsumer(EasyMock.isA(LogicalConsumer.class))).andReturn(list);
        EasyMock.replay(connectionGenerator);

        ConsumerCommandGenerator generator = new ConsumerCommandGenerator(connectionGenerator, 0);
        LogicalComponent<?> component = createComponent();
        ChannelConnectionCommand command = generator.generate(component, true);

        assertNotNull(command);
        assertFalse(command.getAttachCommands().isEmpty());
        assertTrue(command.getDetachCommands().isEmpty());
        EasyMock.verify(connectionGenerator);
    }

    public void testGenerateIncrementalDetach() throws Exception {
        ConnectionGenerator connectionGenerator = EasyMock.createMock(ConnectionGenerator.class);
        List<PhysicalChannelConnectionDefinition> list = Collections.singletonList(new PhysicalChannelConnectionDefinition(null, null, null));
        EasyMock.expect(connectionGenerator.generateConsumer(EasyMock.isA(LogicalConsumer.class))).andReturn(list);
        EasyMock.replay(connectionGenerator);

        ConsumerCommandGenerator generator = new ConsumerCommandGenerator(connectionGenerator, 0);
        LogicalComponent<?> component = createComponent();
        component.setState(LogicalState.MARKED);
        ChannelConnectionCommand command = generator.generate(component, true);

        assertNotNull(command);
        assertFalse(command.getDetachCommands().isEmpty());
        assertTrue(command.getAttachCommands().isEmpty());
        EasyMock.verify(connectionGenerator);
    }

    public void testGenerateFullAttach() throws Exception {
        ConnectionGenerator connectionGenerator = EasyMock.createMock(ConnectionGenerator.class);
        List<PhysicalChannelConnectionDefinition> list = Collections.singletonList(new PhysicalChannelConnectionDefinition(null, null, null));
        EasyMock.expect(connectionGenerator.generateConsumer(EasyMock.isA(LogicalConsumer.class))).andReturn(list);
        EasyMock.replay(connectionGenerator);

        ConsumerCommandGenerator generator = new ConsumerCommandGenerator(connectionGenerator, 0);
        LogicalComponent<?> component = createComponent();
        component.setState(LogicalState.PROVISIONED);
        ChannelConnectionCommand command = generator.generate(component, false);

        assertNotNull(command);
        assertFalse(command.getAttachCommands().isEmpty());
        assertTrue(command.getDetachCommands().isEmpty());
        EasyMock.verify(connectionGenerator);
    }

    public void testGenerateFullDetach() throws Exception {
        ConnectionGenerator connectionGenerator = EasyMock.createMock(ConnectionGenerator.class);
        List<PhysicalChannelConnectionDefinition> list = Collections.singletonList(new PhysicalChannelConnectionDefinition(null, null, null));
        EasyMock.expect(connectionGenerator.generateConsumer(EasyMock.isA(LogicalConsumer.class))).andReturn(list);
        EasyMock.replay(connectionGenerator);

        ConsumerCommandGenerator generator = new ConsumerCommandGenerator(connectionGenerator, 0);
        LogicalComponent<?> component = createComponent();
        component.setState(LogicalState.MARKED);
        ChannelConnectionCommand command = generator.generate(component, false);

        assertNotNull(command);
        assertTrue(command.getAttachCommands().isEmpty());
        assertFalse(command.getDetachCommands().isEmpty());
        EasyMock.verify(connectionGenerator);
    }

    public void testGenerateNothingIncremental() throws Exception {
        ConnectionGenerator connectionGenerator = EasyMock.createMock(ConnectionGenerator.class);
        EasyMock.replay(connectionGenerator);

        ConsumerCommandGenerator generator = new ConsumerCommandGenerator(connectionGenerator, 0);
        LogicalComponent<?> component = createComponent();
        component.setState(LogicalState.PROVISIONED);
        assertNull(generator.generate(component, true));
        EasyMock.verify(connectionGenerator);
    }


    @SuppressWarnings({"unchecked"})
    private LogicalComponent<?> createComponent() {
        ComponentDefinition definition = new ComponentDefinition("component");
        LogicalComponent<?> component = new LogicalComponent(URI.create("component"), definition, null);
        LogicalConsumer consumer = new LogicalConsumer(URI.create("component#consumer"), new ConsumerDefinition("consumer"), component);
        component.addConsumer(consumer);
        return component;
    }

}