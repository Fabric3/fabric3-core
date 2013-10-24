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
package org.fabric3.fabric.deployment.generator.wire;

import java.net.URI;

import junit.framework.TestCase;
import org.easymock.EasyMock;

import org.fabric3.fabric.deployment.command.ConnectionCommand;
import org.fabric3.api.model.type.component.BindingDefinition;
import org.fabric3.api.model.type.contract.ServiceContract;
import org.fabric3.spi.deployment.generator.wire.WireGenerator;
import org.fabric3.spi.model.instance.LogicalBinding;
import org.fabric3.spi.model.instance.LogicalComponent;
import org.fabric3.spi.model.instance.LogicalService;
import org.fabric3.spi.model.instance.LogicalState;
import org.fabric3.spi.model.physical.PhysicalWireDefinition;

/**
 *
 */
public class BoundServiceCommandGeneratorTestCase extends TestCase {
    private WireGenerator wireGenerator;


    public void testGenerateIncremental() throws Exception {
        MockWireDefinition wireDefinition = new MockWireDefinition();
        EasyMock.expect(wireGenerator.generateBoundService(EasyMock.isA(LogicalBinding.class), (URI) EasyMock.isNull())).andReturn(wireDefinition);
        EasyMock.replay(wireGenerator);
        LogicalComponent<?> component = createComponent();

        BoundServiceCommandGenerator generator = new BoundServiceCommandGenerator(wireGenerator);
        ConnectionCommand command = generator.generate(component, true);
        assertEquals(1, command.getAttachCommands().size());

        EasyMock.verify(wireGenerator);
    }

    public void testNoGenerateIncremental() throws Exception {
        EasyMock.replay(wireGenerator);
        LogicalComponent<?> component = createComponent();
        setBindingState(component, LogicalState.PROVISIONED);
        BoundServiceCommandGenerator generator = new BoundServiceCommandGenerator(wireGenerator);
        assertNull(generator.generate(component, true));

        EasyMock.verify(wireGenerator);
    }

    public void testGenerateFull() throws Exception {
        MockWireDefinition wireDefinition = new MockWireDefinition();
        EasyMock.expect(wireGenerator.generateBoundService(EasyMock.isA(LogicalBinding.class), (URI) EasyMock.isNull())).andReturn(wireDefinition);
        EasyMock.replay(wireGenerator);
        LogicalComponent<?> component = createComponent();
        setBindingState(component, LogicalState.PROVISIONED);

        BoundServiceCommandGenerator generator = new BoundServiceCommandGenerator(wireGenerator);
        ConnectionCommand command = generator.generate(component, false);
        assertEquals(1, command.getAttachCommands().size());

        EasyMock.verify(wireGenerator);
    }

    public void testGenerateNoBindingIncremental() throws Exception {
        BoundServiceCommandGenerator generator = new BoundServiceCommandGenerator(wireGenerator);

        EasyMock.replay(wireGenerator);

        LogicalComponent component = new LogicalComponent(URI.create("component"), null, null);
        LogicalService service = new LogicalService(URI.create("component#service"), null, null);
        component.addService(service);

        generator.generate(component, true);

        EasyMock.verify(wireGenerator);
    }

    public void testGenerateDetachIncremental() throws Exception {
        MockWireDefinition wireDefinition = new MockWireDefinition();
        EasyMock.expect(wireGenerator.generateBoundService(EasyMock.isA(LogicalBinding.class), (URI) EasyMock.isNull())).andReturn(wireDefinition);
        EasyMock.replay(wireGenerator);
        LogicalComponent<?> component = createComponent();
        setBindingState(component, LogicalState.MARKED);

        BoundServiceCommandGenerator generator = new BoundServiceCommandGenerator(wireGenerator);
        ConnectionCommand command = generator.generate(component, true);
        assertEquals(1, command.getDetachCommands().size());

        EasyMock.verify(wireGenerator);
    }

    public void testGenerateCallbackIncremental() throws Exception {
        MockWireDefinition wireDefinition = new MockWireDefinition();
        EasyMock.expect(wireGenerator.generateBoundService(EasyMock.isA(LogicalBinding.class), EasyMock.isA(URI.class))).andReturn(wireDefinition);
        EasyMock.expect(wireGenerator.generateBoundServiceCallback(EasyMock.isA(LogicalBinding.class))).andReturn(wireDefinition);
        EasyMock.replay(wireGenerator);

        LogicalComponent<?> component = createComponent();

        MockContract callbackContract = new MockContract();
        LogicalService service = component.getServices().iterator().next();
        service.getServiceContract().setCallbackContract(callbackContract);

        MockBindingDefinition definition = new MockBindingDefinition();
        LogicalBinding<?> binding = new LogicalBinding(definition, service);
        service.addCallbackBinding(binding);

        BoundServiceCommandGenerator generator = new BoundServiceCommandGenerator(wireGenerator);
        ConnectionCommand command = generator.generate(component, true);
        assertEquals(2, command.getAttachCommands().size());

        EasyMock.verify(wireGenerator);
    }

    public void testGenerateDetachCallbackIncremental() throws Exception {
        MockWireDefinition wireDefinition = new MockWireDefinition();
        EasyMock.expect(wireGenerator.generateBoundService(EasyMock.isA(LogicalBinding.class), EasyMock.isA(URI.class))).andReturn(wireDefinition);
        EasyMock.expect(wireGenerator.generateBoundServiceCallback(EasyMock.isA(LogicalBinding.class))).andReturn(wireDefinition);
        EasyMock.replay(wireGenerator);

        LogicalComponent<?> component = createComponent();

        MockContract callbackContract = new MockContract();
        LogicalService service = component.getServices().iterator().next();
        service.getServiceContract().setCallbackContract(callbackContract);

        MockBindingDefinition definition = new MockBindingDefinition();
        LogicalBinding<?> binding = new LogicalBinding(definition, service);
        binding.setState(LogicalState.MARKED);
        service.addCallbackBinding(binding);
        setBindingState(component, LogicalState.MARKED);

        BoundServiceCommandGenerator generator = new BoundServiceCommandGenerator(wireGenerator);
        ConnectionCommand command = generator.generate(component, true);
        assertEquals(2, command.getDetachCommands().size());

        EasyMock.verify(wireGenerator);
    }


    private LogicalComponent<?> createComponent() {
        LogicalComponent component = new LogicalComponent(URI.create("component"), null, null);
        LogicalService service = new LogicalService(URI.create("component#service"), null, null);
        ServiceContract contract = new MockContract();
        service.setServiceContract(contract);
        LogicalBinding<?> binding = new LogicalBinding(null, service);
        service.addBinding(binding);
        component.addService(service);
        return component;
    }


    @Override
    protected void setUp() throws Exception {
        super.setUp();
        wireGenerator = EasyMock.createMock(WireGenerator.class);
    }

    private void setBindingState(LogicalComponent<?> component, LogicalState state) {
        component.getServices().iterator().next().getBindings().get(0).setState(state);
    }

    private class MockBindingDefinition extends BindingDefinition {
        private static final long serialVersionUID = 6341221394239456452L;

        public MockBindingDefinition() {
            super(URI.create("target"), null);
        }
    }

    private class MockContract extends ServiceContract {
        private static final long serialVersionUID = -2909070237720164262L;

        @Override
        public String getQualifiedInterfaceName() {
            return "test";
        }
    }

    private class MockWireDefinition extends PhysicalWireDefinition {
        private static final long serialVersionUID = 358078346745301821L;

        private MockWireDefinition() {
            super(null, null, null);
        }
    }
}