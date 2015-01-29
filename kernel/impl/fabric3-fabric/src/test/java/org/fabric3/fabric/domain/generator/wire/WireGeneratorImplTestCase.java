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

import javax.xml.namespace.QName;
import java.net.URI;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import junit.framework.TestCase;
import org.easymock.EasyMock;
import org.fabric3.api.model.type.component.BindingDefinition;
import org.fabric3.api.model.type.component.ComponentDefinition;
import org.fabric3.api.model.type.component.ComponentType;
import org.fabric3.api.model.type.component.Implementation;
import org.fabric3.api.model.type.contract.ServiceContract;
import org.fabric3.fabric.domain.generator.GeneratorRegistry;
import org.fabric3.spi.contract.ContractMatcher;
import org.fabric3.spi.domain.generator.GenerationException;
import org.fabric3.spi.domain.generator.component.ComponentGenerator;
import org.fabric3.spi.domain.generator.wire.WireBindingGenerator;
import org.fabric3.spi.model.instance.LogicalBinding;
import org.fabric3.spi.model.instance.LogicalComponent;
import org.fabric3.spi.model.instance.LogicalReference;
import org.fabric3.spi.model.instance.LogicalService;
import org.fabric3.spi.model.instance.LogicalWire;
import org.fabric3.spi.model.physical.PhysicalOperationDefinition;
import org.fabric3.spi.model.physical.PhysicalWireDefinition;
import org.fabric3.spi.model.physical.PhysicalWireSourceDefinition;
import org.fabric3.spi.model.physical.PhysicalWireTargetDefinition;

/**
 *
 */
public class WireGeneratorImplTestCase extends TestCase {
    private static final URI CONTRIBUTION_URI = URI.create("contribution");
    private static final QName DEPLOYABLE = new QName("test", "deployable");

    @SuppressWarnings({"unchecked"})
    public void testGenerateBoundService() throws Exception {
        GeneratorRegistry registry = EasyMock.createMock(GeneratorRegistry.class);
        ComponentGenerator componentGenerator = setupTargetComponentGenerator(registry);
        WireBindingGenerator bindingGenerator = setupBindingGenerator(registry);
        ContractMatcher matcher = EasyMock.createMock(ContractMatcher.class);
        PhysicalOperationGenerator operationGenerator = setupOperationGenerator();

        EasyMock.replay(registry, matcher, operationGenerator, componentGenerator, bindingGenerator);

        WireGeneratorImpl generator = new WireGeneratorImpl(registry, matcher, operationGenerator);
        LogicalBinding<?> binding = createServiceBinding();
        PhysicalWireDefinition definition = generator.generateBoundService(binding, null);
        assertNotNull(definition.getSource());
        assertNotNull(definition.getTarget());

        EasyMock.verify(registry, matcher, operationGenerator, componentGenerator, bindingGenerator);
    }

    public void testGenerateBoundServiceCallback() throws Exception {
        GeneratorRegistry registry = EasyMock.createMock(GeneratorRegistry.class);
        ComponentGenerator componentGenerator = setupCallbackComponentGenerator(registry);
        WireBindingGenerator bindingGenerator = setupTargetBindingGenerator(registry);
        ContractMatcher matcher = EasyMock.createMock(ContractMatcher.class);
        PhysicalOperationGenerator operationGenerator = setupOperationGenerator();

        EasyMock.replay(registry, matcher, operationGenerator, componentGenerator, bindingGenerator);

        WireGeneratorImpl generator = new WireGeneratorImpl(registry, matcher, operationGenerator);
        LogicalBinding<?> binding = createServiceCallbackBinding();
        PhysicalWireDefinition definition = generator.generateBoundServiceCallback(binding);
        assertNotNull(definition.getSource());
        assertNotNull(definition.getTarget());

        EasyMock.verify(registry, matcher, operationGenerator, componentGenerator, bindingGenerator);
    }

    @SuppressWarnings({"unchecked"})
    public void testGenerateBoundReference() throws Exception {
        GeneratorRegistry registry = EasyMock.createMock(GeneratorRegistry.class);
        ComponentGenerator componentGenerator = setupSourceComponentGenerator(registry);
        WireBindingGenerator bindingGenerator = setupTargetBindingGenerator(registry);
        ContractMatcher matcher = EasyMock.createMock(ContractMatcher.class);
        PhysicalOperationGenerator operationGenerator = setupOperationGenerator();

        EasyMock.replay(registry, matcher, operationGenerator, componentGenerator, bindingGenerator);

        WireGeneratorImpl generator = new WireGeneratorImpl(registry, matcher, operationGenerator);
        LogicalBinding<?> binding = createReferenceBinding();
        PhysicalWireDefinition definition = generator.generateBoundReference(binding);
        assertNotNull(definition.getSource());
        assertNotNull(definition.getTarget());

        EasyMock.verify(registry, matcher, operationGenerator, componentGenerator, bindingGenerator);
    }

    @SuppressWarnings({"unchecked"})
    public void testGenerateBoundReferenceCallback() throws Exception {
        GeneratorRegistry registry = EasyMock.createMock(GeneratorRegistry.class);
        ComponentGenerator componentGenerator = setupTargetComponentGenerator(registry);
        WireBindingGenerator bindingGenerator = setupBindingGenerator(registry);
        ContractMatcher matcher = EasyMock.createMock(ContractMatcher.class);
        PhysicalOperationGenerator operationGenerator = setupOperationGenerator();

        EasyMock.replay(registry, matcher, operationGenerator, componentGenerator, bindingGenerator);

        WireGeneratorImpl generator = new WireGeneratorImpl(registry, matcher, operationGenerator);
        LogicalBinding<?> binding = createReferenceCallbackBinding();

        PhysicalWireDefinition definition = generator.generateBoundReferenceCallback(binding);
        assertNotNull(definition.getSource());
        assertNotNull(definition.getTarget());

        EasyMock.verify(registry, matcher, operationGenerator, componentGenerator, bindingGenerator);
    }

    public void testGenerateLocalWire() throws Exception {
        LogicalReference reference = createReference();
        LogicalService service = createService();

        GeneratorRegistry registry = EasyMock.createMock(GeneratorRegistry.class);
        ComponentGenerator sourceComponentGenerator = setupSourceComponentGenerator(registry);
        ComponentGenerator targetComponentGenerator = setupTargetComponentGenerator(registry);
        ContractMatcher matcher = EasyMock.createMock(ContractMatcher.class);
        PhysicalOperationGenerator operationGenerator = setupLocalOperationGenerator();

        EasyMock.replay(registry, matcher, operationGenerator, sourceComponentGenerator, targetComponentGenerator);

        WireGeneratorImpl generator = new WireGeneratorImpl(registry, matcher, operationGenerator);

        LogicalWire wire = new LogicalWire(reference.getParent(), reference, service, DEPLOYABLE);
        PhysicalWireDefinition definition = generator.generateWire(wire);
        assertNotNull(definition.getSource());
        assertNotNull(definition.getTarget());

        EasyMock.verify(registry, matcher, operationGenerator, sourceComponentGenerator, targetComponentGenerator);
    }

    public void testGenerateLocalWireCallback() throws Exception {
        LogicalReference reference = createCallbackReference();
        LogicalService service = createCallbackService();

        GeneratorRegistry registry = EasyMock.createMock(GeneratorRegistry.class);
        ComponentGenerator sourceComponentGenerator = setupCallbackComponentGenerator(registry);
        ComponentGenerator targetComponentGenerator = setupTargetComponentGenerator(registry);
        ContractMatcher matcher = EasyMock.createMock(ContractMatcher.class);
        PhysicalOperationGenerator operationGenerator = setupLocalOperationGenerator();

        EasyMock.replay(registry, matcher, operationGenerator, sourceComponentGenerator, targetComponentGenerator);

        WireGeneratorImpl generator = new WireGeneratorImpl(registry, matcher, operationGenerator);

        LogicalWire wire = new LogicalWire(reference.getParent(), reference, service, DEPLOYABLE);
        PhysicalWireDefinition definition = generator.generateWireCallback(wire);
        assertNotNull(definition.getSource());
        assertNotNull(definition.getTarget());

        EasyMock.verify(registry, matcher, operationGenerator, sourceComponentGenerator, targetComponentGenerator);
    }

    public void testGenerateRemoteWire() throws Exception {
        LogicalBinding referenceBinding = createReferenceBinding();
        LogicalBinding serviceBinding = createServiceBinding();
        LogicalReference reference = (LogicalReference) referenceBinding.getParent();
        LogicalService service = (LogicalService) serviceBinding.getParent();

        GeneratorRegistry registry = EasyMock.createMock(GeneratorRegistry.class);
        WireBindingGenerator bindingGenerator = setupServiceBindingTargetGenerator(registry);
        ComponentGenerator componentGenerator = setupSourceComponentGenerator(registry);
        ContractMatcher matcher = EasyMock.createMock(ContractMatcher.class);
        PhysicalOperationGenerator operationGenerator = setupOperationGenerator();

        EasyMock.replay(registry, matcher, operationGenerator, componentGenerator, bindingGenerator);

        WireGeneratorImpl generator = new WireGeneratorImpl(registry, matcher, operationGenerator);

        LogicalWire wire = new LogicalWire(reference.getParent(), reference, service, DEPLOYABLE);
        wire.setSourceBinding(referenceBinding);
        wire.setTargetBinding(serviceBinding);

        PhysicalWireDefinition definition = generator.generateWire(wire);

        assertNotNull(definition.getSource());
        assertNotNull(definition.getTarget());

        EasyMock.verify(registry, matcher, operationGenerator, componentGenerator, bindingGenerator);
    }

    public void testGenerateRemoteCallbackWire() throws Exception {
        LogicalBinding referenceBinding = createReferenceCallbackBinding();
        LogicalBinding serviceBinding = createServiceCallbackBinding();
        LogicalReference reference = (LogicalReference) referenceBinding.getParent();
        LogicalService service = (LogicalService) serviceBinding.getParent();

        GeneratorRegistry registry = EasyMock.createMock(GeneratorRegistry.class);
        WireBindingGenerator sourceBindingGenerator = setupBindingGenerator(registry);
        ComponentGenerator componentGenerator = setupTargetComponentGenerator(registry);
        ContractMatcher matcher = EasyMock.createMock(ContractMatcher.class);
        PhysicalOperationGenerator operationGenerator = setupOperationGenerator();

        EasyMock.replay(registry, matcher, operationGenerator, componentGenerator, sourceBindingGenerator);

        WireGeneratorImpl generator = new WireGeneratorImpl(registry, matcher, operationGenerator);

        LogicalWire wire = new LogicalWire(reference.getParent(), reference, service, DEPLOYABLE);
        wire.setSourceBinding(referenceBinding);
        wire.setTargetBinding(serviceBinding);

        PhysicalWireDefinition definition = generator.generateWireCallback(wire);

        assertNotNull(definition.getSource());
        assertNotNull(definition.getTarget());

        EasyMock.verify(registry, matcher, operationGenerator, componentGenerator, sourceBindingGenerator);
    }

    @SuppressWarnings({"unchecked"})
    private PhysicalOperationGenerator setupOperationGenerator() throws GenerationException {
        PhysicalOperationGenerator operationGenerator = EasyMock.createMock(PhysicalOperationGenerator.class);
        Set<PhysicalOperationDefinition> set = Collections.<PhysicalOperationDefinition>singleton(new MockOperationDefinition());
        EasyMock.expect(operationGenerator.generateOperations(EasyMock.isA(List.class))).andReturn(set);
        return operationGenerator;
    }

    @SuppressWarnings({"unchecked"})
    private PhysicalOperationGenerator setupLocalOperationGenerator() throws GenerationException {
        PhysicalOperationGenerator operationGenerator = EasyMock.createMock(PhysicalOperationGenerator.class);
        Set<PhysicalOperationDefinition> set = Collections.<PhysicalOperationDefinition>singleton(new MockOperationDefinition());
        EasyMock.expect(operationGenerator.generateOperations(EasyMock.isA(List.class), EasyMock.isA(List.class), EasyMock.anyBoolean())).andReturn(set);
        return operationGenerator;
    }

    @SuppressWarnings({"unchecked"})
    private WireBindingGenerator setupBindingGenerator(GeneratorRegistry registry) throws GenerationException {
        WireBindingGenerator bindingGenerator = EasyMock.createMock(WireBindingGenerator.class);
        EasyMock.expect(bindingGenerator.generateSource(EasyMock.isA(LogicalBinding.class),
                                                        EasyMock.isA(ServiceContract.class),
                                                        EasyMock.isA(List.class))).andReturn(new MockWireSourceDefinition());
        EasyMock.expect(registry.getBindingGenerator(EasyMock.isA(Class.class))).andReturn(bindingGenerator);
        return bindingGenerator;
    }

    @SuppressWarnings({"unchecked"})
    private WireBindingGenerator setupServiceBindingTargetGenerator(GeneratorRegistry registry) throws GenerationException {
        WireBindingGenerator bindingGenerator = EasyMock.createMock(WireBindingGenerator.class);
        EasyMock.expect(bindingGenerator.generateServiceBindingTarget(EasyMock.isA(LogicalBinding.class),
                                                                      EasyMock.isA(ServiceContract.class),
                                                                      EasyMock.isA(List.class))).andReturn(new MockWireTargetDefinition());
        EasyMock.expect(registry.getBindingGenerator(EasyMock.isA(Class.class))).andReturn(bindingGenerator);
        return bindingGenerator;
    }

    @SuppressWarnings({"unchecked"})
    private WireBindingGenerator setupTargetBindingGenerator(GeneratorRegistry registry) throws GenerationException {
        WireBindingGenerator bindingGenerator = EasyMock.createMock(WireBindingGenerator.class);
        EasyMock.expect(bindingGenerator.generateTarget(EasyMock.isA(LogicalBinding.class),
                                                        EasyMock.isA(ServiceContract.class),
                                                        EasyMock.isA(List.class))).andReturn(new MockWireTargetDefinition());
        EasyMock.expect(registry.getBindingGenerator(EasyMock.isA(Class.class))).andReturn(bindingGenerator);
        return bindingGenerator;
    }

    @SuppressWarnings({"unchecked"})
    private ComponentGenerator setupTargetComponentGenerator(GeneratorRegistry registry) throws GenerationException {
        ComponentGenerator componentGenerator = EasyMock.createMock(ComponentGenerator.class);
        EasyMock.expect(componentGenerator.generateTarget(EasyMock.isA(LogicalService.class))).andReturn(new MockWireTargetDefinition());

        EasyMock.expect(registry.getComponentGenerator(EasyMock.isA(Class.class))).andReturn(componentGenerator);
        return componentGenerator;
    }

    @SuppressWarnings({"unchecked"})
    private ComponentGenerator setupSourceComponentGenerator(GeneratorRegistry registry) throws GenerationException {
        ComponentGenerator componentGenerator = EasyMock.createMock(ComponentGenerator.class);
        EasyMock.expect(componentGenerator.generateSource(EasyMock.isA(LogicalReference.class))).andReturn(new MockWireSourceDefinition());

        EasyMock.expect(registry.getComponentGenerator(EasyMock.isA(Class.class))).andReturn(componentGenerator);
        return componentGenerator;
    }

    @SuppressWarnings({"unchecked"})
    private ComponentGenerator setupCallbackComponentGenerator(GeneratorRegistry registry) throws GenerationException {
        ComponentGenerator componentGenerator = EasyMock.createMock(ComponentGenerator.class);
        EasyMock.expect(componentGenerator.generateCallbackSource(EasyMock.isA(LogicalService.class))).andReturn(new MockWireSourceDefinition());

        EasyMock.expect(registry.getComponentGenerator(EasyMock.isA(Class.class))).andReturn(componentGenerator);
        return componentGenerator;
    }

    private LogicalBinding<?> createServiceCallbackBinding() {
        LogicalBinding<?> binding = createServiceBinding();
        LogicalService service = (LogicalService) binding.getParent();
        service.addCallbackBinding(binding);
        ServiceContract contract = new MockContract();
        service.getServiceContract().setCallbackContract(contract);
        return binding;
    }

    @SuppressWarnings({"unchecked"})
    private LogicalBinding<?> createServiceBinding() {
        LogicalService service = createService();
        MockBindingDefinition bindingDefinition = new MockBindingDefinition();
        LogicalBinding<?> binding = new LogicalBinding(bindingDefinition, service);
        service.addBinding(binding);
        return binding;
    }

    @SuppressWarnings({"unchecked"})
    private LogicalService createCallbackService() {
        LogicalService service = createService();
        ServiceContract contract = new MockContract();
        service.getServiceContract().setCallbackContract(contract);
        return service;
    }

    @SuppressWarnings({"unchecked"})
    private LogicalService createService() {
        ComponentDefinition definition = new ComponentDefinition("component", new MockImplementation());
        definition.setContributionUri(CONTRIBUTION_URI);
        LogicalComponent component = new LogicalComponent(URI.create("component"), definition, null);
        LogicalService service = new LogicalService(URI.create("component#service"), null, component);
        ServiceContract contract = new MockContract();
        service.setServiceContract(contract);
        component.addService(service);
        return service;
    }

    private LogicalBinding<?> createReferenceCallbackBinding() {
        LogicalBinding<?> binding = createReferenceBinding();
        LogicalReference reference = (LogicalReference) binding.getParent();
        ServiceContract contract = new MockContract();
        reference.getServiceContract().setCallbackContract(contract);
        reference.addCallbackBinding(binding);
        LogicalComponent<?> component = binding.getParent().getParent();
        LogicalService service = new LogicalService(URI.create("component#service"), null, component);
        service.setServiceContract(new MockContract());
        component.addService(service);
        return binding;
    }

    @SuppressWarnings({"unchecked"})
    private LogicalBinding<?> createReferenceBinding() {
        LogicalReference reference = createReference();
        MockBindingDefinition bindingDefinition = new MockBindingDefinition();
        LogicalBinding<?> binding = new LogicalBinding(bindingDefinition, reference);
        reference.addBinding(binding);
        return binding;
    }

    private LogicalReference createCallbackReference() {
        LogicalReference reference = createReference();
        ServiceContract contract = new MockContract();
        reference.getServiceContract().setCallbackContract(contract);
        LogicalComponent<?> component = reference.getParent();
        LogicalService service = new LogicalService(URI.create("component#service"), null, component);
        service.setServiceContract(new MockContract());
        component.addService(service);
        return reference;
    }

    @SuppressWarnings({"unchecked"})
    private LogicalReference createReference() {
        ComponentDefinition definition = new ComponentDefinition("component", new MockImplementation());
        definition.setContributionUri(CONTRIBUTION_URI);
        LogicalComponent component = new LogicalComponent(URI.create("component"), definition, null);
        LogicalReference reference = new LogicalReference(URI.create("component#reference"), null, component);
        ServiceContract contract = new MockContract();
        reference.setServiceContract(contract);
        component.addReference(reference);
        return reference;
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
            return "service";
        }

        @Override
        public String getInterfaceName() {
            return "service";
        }
    }

    private class MockImplementation extends Implementation<ComponentType> {
        private static final long serialVersionUID = -8283300933931216298L;

        @Override
        public QName getType() {
            return null;
        }
    }

    private class MockWireSourceDefinition extends PhysicalWireSourceDefinition {
        private static final long serialVersionUID = -3656506060148889502L;
    }

    private class MockWireTargetDefinition extends PhysicalWireTargetDefinition {
        private static final long serialVersionUID = 9192215132618200005L;
    }

    private class MockOperationDefinition extends PhysicalOperationDefinition {
        private static final long serialVersionUID = 7905560274906591901L;
    }

}