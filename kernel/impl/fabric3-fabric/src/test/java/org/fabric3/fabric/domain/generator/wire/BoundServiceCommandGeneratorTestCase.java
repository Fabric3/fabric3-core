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
package org.fabric3.fabric.domain.generator.wire;

import java.net.URI;

import junit.framework.TestCase;
import org.easymock.EasyMock;
import org.fabric3.api.model.type.component.Binding;
import org.fabric3.api.model.type.component.Component;
import org.fabric3.api.model.type.component.Service;
import org.fabric3.api.model.type.contract.ServiceContract;
import org.fabric3.api.model.type.java.InjectingComponentType;
import org.fabric3.api.model.type.java.JavaImplementation;
import org.fabric3.fabric.container.command.ConnectionCommand;
import org.fabric3.spi.model.instance.LogicalBinding;
import org.fabric3.spi.model.instance.LogicalComponent;
import org.fabric3.spi.model.instance.LogicalService;
import org.fabric3.spi.model.instance.LogicalState;
import org.fabric3.spi.model.physical.PhysicalWire;

/**
 *
 */
public class BoundServiceCommandGeneratorTestCase extends TestCase {
    private WireGenerator wireGenerator;

    @SuppressWarnings("unchecked")
    public void testGenerate() throws Exception {
        MockWire wireDefinition = new MockWire();
        EasyMock.expect(wireGenerator.generateService(EasyMock.isA(LogicalBinding.class), EasyMock.isNull())).andReturn(wireDefinition);
        EasyMock.replay(wireGenerator);
        LogicalComponent<?> component = createComponent();

        BoundServiceCommandGenerator generator = new BoundServiceCommandGenerator(wireGenerator);
        ConnectionCommand command = generator.generate(component).get();
        assertEquals(1, command.getAttachCommands().size());

        EasyMock.verify(wireGenerator);
    }

    public void testNoGenerate() throws Exception {
        EasyMock.replay(wireGenerator);
        LogicalComponent<?> component = createComponent();
        setBindingState(component, LogicalState.PROVISIONED);
        BoundServiceCommandGenerator generator = new BoundServiceCommandGenerator(wireGenerator);
        assertFalse(generator.generate(component).isPresent());

        EasyMock.verify(wireGenerator);
    }

    @SuppressWarnings("unchecked")
    public void testGenerateNoBinding() throws Exception {
        BoundServiceCommandGenerator generator = new BoundServiceCommandGenerator(wireGenerator);

        EasyMock.replay(wireGenerator);

        Service serviceDefinition = new Service("service");
        InjectingComponentType componentType = new InjectingComponentType();
        componentType.add(serviceDefinition);
        JavaImplementation implementation = new JavaImplementation();
        implementation.setComponentType(componentType);
        Component<JavaImplementation> definition = new Component<>("component", implementation);
        LogicalComponent<?> component = new LogicalComponent<>(URI.create("component"), definition, null);

        LogicalService service = new LogicalService(URI.create("component#service"), null, component);
        component.addService(service);

        generator.generate(component);

        EasyMock.verify(wireGenerator);
    }

    @SuppressWarnings("unchecked")
    public void testGenerateDetach() throws Exception {
        MockWire wireDefinition = new MockWire();
        EasyMock.expect(wireGenerator.generateService(EasyMock.isA(LogicalBinding.class), EasyMock.isNull())).andReturn(wireDefinition);
        EasyMock.replay(wireGenerator);
        LogicalComponent<?> component = createComponent();
        setBindingState(component, LogicalState.MARKED);

        BoundServiceCommandGenerator generator = new BoundServiceCommandGenerator(wireGenerator);
        ConnectionCommand command = generator.generate(component).get();
        assertEquals(1, command.getDetachCommands().size());

        EasyMock.verify(wireGenerator);
    }

    @SuppressWarnings("unchecked")
    public void testGenerateCallback() throws Exception {
        MockWire wireDefinition = new MockWire();
        EasyMock.expect(wireGenerator.generateService(EasyMock.isA(LogicalBinding.class), EasyMock.isA(URI.class))).andReturn(wireDefinition);
        EasyMock.expect(wireGenerator.generateServiceCallback(EasyMock.isA(LogicalBinding.class))).andReturn(wireDefinition);
        EasyMock.replay(wireGenerator);

        LogicalComponent<?> component = createComponent();

        MockContract callbackContract = new MockContract();
        LogicalService service = component.getServices().iterator().next();
        service.getServiceContract().setCallbackContract(callbackContract);

        MockBindingDefinition definition = new MockBindingDefinition();
        LogicalBinding<?> binding = new LogicalBinding(definition, service);
        service.addCallbackBinding(binding);

        BoundServiceCommandGenerator generator = new BoundServiceCommandGenerator(wireGenerator);
        ConnectionCommand command = generator.generate(component).get();
        assertEquals(2, command.getAttachCommands().size());

        EasyMock.verify(wireGenerator);
    }

    @SuppressWarnings("unchecked")
    public void testGenerateDetachCallback() throws Exception {
        MockWire wireDefinition = new MockWire();
        EasyMock.expect(wireGenerator.generateService(EasyMock.isA(LogicalBinding.class), EasyMock.isA(URI.class))).andReturn(wireDefinition);
        EasyMock.expect(wireGenerator.generateServiceCallback(EasyMock.isA(LogicalBinding.class))).andReturn(wireDefinition);
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
        ConnectionCommand command = generator.generate(component).get();
        assertEquals(2, command.getDetachCommands().size());

        EasyMock.verify(wireGenerator);
    }

    @SuppressWarnings("unchecked")
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

    private class MockBindingDefinition extends Binding {
        public MockBindingDefinition() {
            super(URI.create("target"), null);
        }
    }

    private class MockContract extends ServiceContract {
        public String getQualifiedInterfaceName() {
            return "test";
        }
    }

    private class MockWire extends PhysicalWire {
        private MockWire() {
            super(null, null, null);
        }
    }
}