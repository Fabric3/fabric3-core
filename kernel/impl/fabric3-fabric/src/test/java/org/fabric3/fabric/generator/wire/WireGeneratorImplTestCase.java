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
package org.fabric3.fabric.generator.wire;

import java.net.URI;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import javax.xml.namespace.QName;

import junit.framework.TestCase;
import org.easymock.EasyMock;

import org.fabric3.fabric.generator.GeneratorRegistry;
import org.fabric3.model.type.component.BindingDefinition;
import org.fabric3.model.type.component.ComponentDefinition;
import org.fabric3.model.type.component.ComponentType;
import org.fabric3.model.type.component.Implementation;
import org.fabric3.model.type.contract.ServiceContract;
import org.fabric3.spi.contract.ContractMatcher;
import org.fabric3.spi.generator.BindingGenerator;
import org.fabric3.spi.generator.ComponentGenerator;
import org.fabric3.spi.generator.EffectivePolicy;
import org.fabric3.spi.generator.GenerationException;
import org.fabric3.spi.generator.policy.PolicyResolutionException;
import org.fabric3.spi.generator.policy.PolicyResolver;
import org.fabric3.spi.generator.policy.PolicyResult;
import org.fabric3.spi.model.instance.LogicalBinding;
import org.fabric3.spi.model.instance.LogicalComponent;
import org.fabric3.spi.model.instance.LogicalReference;
import org.fabric3.spi.model.instance.LogicalService;
import org.fabric3.spi.model.instance.LogicalWire;
import org.fabric3.spi.model.physical.PhysicalOperationDefinition;
import org.fabric3.spi.model.physical.PhysicalSourceDefinition;
import org.fabric3.spi.model.physical.PhysicalTargetDefinition;
import org.fabric3.spi.model.physical.PhysicalWireDefinition;

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
        BindingGenerator bindingGenerator = setupBindingGenerator(registry);
        ContractMatcher matcher = EasyMock.createMock(ContractMatcher.class);
        PolicyResolver policyResolver = setupPolicyResolver();
        PhysicalOperationGenerator operationGenerator = setupOperationGenerator();

        EasyMock.replay(registry, matcher, policyResolver, operationGenerator, componentGenerator, bindingGenerator);

        WireGeneratorImpl generator = new WireGeneratorImpl(registry, matcher, policyResolver, operationGenerator);
        LogicalBinding<?> binding = createServiceBinding();
        PhysicalWireDefinition definition = generator.generateBoundService(binding, null);
        assertNotNull(definition.getSource());
        assertNotNull(definition.getTarget());

        EasyMock.verify(registry, matcher, policyResolver, operationGenerator, componentGenerator, bindingGenerator);
    }

    public void testGenerateBoundServiceCallback() throws Exception {
        GeneratorRegistry registry = EasyMock.createMock(GeneratorRegistry.class);
        ComponentGenerator componentGenerator = setupCallbackComponentGenerator(registry);
        BindingGenerator bindingGenerator = setupTargetBindingGenerator(registry);
        ContractMatcher matcher = EasyMock.createMock(ContractMatcher.class);
        PolicyResolver policyResolver = setupCallbackPolicyResolver();
        PhysicalOperationGenerator operationGenerator = setupOperationGenerator();

        EasyMock.replay(registry, matcher, policyResolver, operationGenerator, componentGenerator, bindingGenerator);

        WireGeneratorImpl generator = new WireGeneratorImpl(registry, matcher, policyResolver, operationGenerator);
        LogicalBinding<?> binding = createServiceCallbackBinding();
        PhysicalWireDefinition definition = generator.generateBoundServiceCallback(binding);
        assertNotNull(definition.getSource());
        assertNotNull(definition.getTarget());

        EasyMock.verify(registry, matcher, policyResolver, operationGenerator, componentGenerator, bindingGenerator);
    }

    @SuppressWarnings({"unchecked"})
    public void testGenerateBoundReference() throws Exception {
        GeneratorRegistry registry = EasyMock.createMock(GeneratorRegistry.class);
        ComponentGenerator componentGenerator = setupSourceComponentGenerator(registry);
        BindingGenerator bindingGenerator = setupTargetBindingGenerator(registry);
        ContractMatcher matcher = EasyMock.createMock(ContractMatcher.class);
        PolicyResolver policyResolver = setupPolicyResolver();
        PhysicalOperationGenerator operationGenerator = setupOperationGenerator();

        EasyMock.replay(registry, matcher, policyResolver, operationGenerator, componentGenerator, bindingGenerator);

        WireGeneratorImpl generator = new WireGeneratorImpl(registry, matcher, policyResolver, operationGenerator);
        LogicalBinding<?> binding = createReferenceBinding();
        PhysicalWireDefinition definition = generator.generateBoundReference(binding);
        assertNotNull(definition.getSource());
        assertNotNull(definition.getTarget());

        EasyMock.verify(registry, matcher, policyResolver, operationGenerator, componentGenerator, bindingGenerator);
    }

    @SuppressWarnings({"unchecked"})
    public void testGenerateBoundReferenceCallback() throws Exception {
        GeneratorRegistry registry = EasyMock.createMock(GeneratorRegistry.class);
        ComponentGenerator componentGenerator = setupTargetComponentGenerator(registry);
        BindingGenerator bindingGenerator = setupBindingGenerator(registry);
        ContractMatcher matcher = EasyMock.createMock(ContractMatcher.class);
        PolicyResolver policyResolver = setupCallbackPolicyResolver();
        PhysicalOperationGenerator operationGenerator = setupOperationGenerator();

        EasyMock.replay(registry, matcher, policyResolver, operationGenerator, componentGenerator, bindingGenerator);

        WireGeneratorImpl generator = new WireGeneratorImpl(registry, matcher, policyResolver, operationGenerator);
        LogicalBinding<?> binding = createReferenceCallbackBinding();

        PhysicalWireDefinition definition = generator.generateBoundReferenceCallback(binding);
        assertNotNull(definition.getSource());
        assertNotNull(definition.getTarget());

        EasyMock.verify(registry, matcher, policyResolver, operationGenerator, componentGenerator, bindingGenerator);
    }

    public void testGenerateLocalWire() throws Exception {
        LogicalReference reference = createReference();
        LogicalService service = createService();

        GeneratorRegistry registry = EasyMock.createMock(GeneratorRegistry.class);
        ComponentGenerator sourceComponentGenerator = setupSourceComponentGenerator(registry);
        ComponentGenerator targetComponentGenerator = setupTargetComponentGenerator(registry);
        ContractMatcher matcher = EasyMock.createMock(ContractMatcher.class);
        PolicyResolver policyResolver = setupLocalPolicyResolver();
        PhysicalOperationGenerator operationGenerator = setupLocalOperationGenerator();

        EasyMock.replay(registry, matcher, policyResolver, operationGenerator, sourceComponentGenerator, targetComponentGenerator);

        WireGeneratorImpl generator = new WireGeneratorImpl(registry, matcher, policyResolver, operationGenerator);

        LogicalWire wire = new LogicalWire(reference.getParent(), reference, service, DEPLOYABLE);
        PhysicalWireDefinition definition = generator.generateWire(wire);
        assertNotNull(definition.getSource());
        assertNotNull(definition.getTarget());

        EasyMock.verify(registry, matcher, policyResolver, operationGenerator, sourceComponentGenerator, targetComponentGenerator);
    }

    public void testGenerateLocalWireCallback() throws Exception {
        LogicalReference reference = createCallbackReference();
        LogicalService service = createCallbackService();

        GeneratorRegistry registry = EasyMock.createMock(GeneratorRegistry.class);
        ComponentGenerator sourceComponentGenerator = setupCallbackComponentGenerator(registry);
        ComponentGenerator targetComponentGenerator = setupTargetComponentGenerator(registry);
        ContractMatcher matcher = EasyMock.createMock(ContractMatcher.class);
        PolicyResolver policyResolver = setupLocalCallbackPolicyResolver();
        PhysicalOperationGenerator operationGenerator = setupLocalOperationGenerator();

        EasyMock.replay(registry, matcher, policyResolver, operationGenerator, sourceComponentGenerator, targetComponentGenerator);

        WireGeneratorImpl generator = new WireGeneratorImpl(registry, matcher, policyResolver, operationGenerator);

        LogicalWire wire = new LogicalWire(reference.getParent(), reference, service, DEPLOYABLE);
        PhysicalWireDefinition definition = generator.generateWireCallback(wire);
        assertNotNull(definition.getSource());
        assertNotNull(definition.getTarget());

        EasyMock.verify(registry, matcher, policyResolver, operationGenerator, sourceComponentGenerator, targetComponentGenerator);
    }


    public void testGenerateRemoteWire() throws Exception {
        LogicalBinding referenceBinding = createReferenceBinding();
        LogicalBinding serviceBinding = createServiceBinding();
        LogicalReference reference = (LogicalReference) referenceBinding.getParent();
        LogicalService service = (LogicalService) serviceBinding.getParent();


        GeneratorRegistry registry = EasyMock.createMock(GeneratorRegistry.class);
        BindingGenerator bindingGenerator = setupServiceBindingTargetGenerator(registry);
        ComponentGenerator componentGenerator = setupSourceComponentGenerator(registry);
        ContractMatcher matcher = EasyMock.createMock(ContractMatcher.class);
        PolicyResolver policyResolver = setupRemotePolicyResolver();
        PhysicalOperationGenerator operationGenerator = setupOperationGenerator();

        EasyMock.replay(registry, matcher, policyResolver, operationGenerator, componentGenerator, bindingGenerator);

        WireGeneratorImpl generator = new WireGeneratorImpl(registry, matcher, policyResolver, operationGenerator);

        LogicalWire wire = new LogicalWire(reference.getParent(), reference, service, DEPLOYABLE);
        wire.setSourceBinding(referenceBinding);
        wire.setTargetBinding(serviceBinding);

        PhysicalWireDefinition definition = generator.generateWire(wire);

        assertNotNull(definition.getSource());
        assertNotNull(definition.getTarget());

        EasyMock.verify(registry, matcher, policyResolver, operationGenerator, componentGenerator, bindingGenerator);
    }

    public void testGenerateRemoteCallbackWire() throws Exception {
        LogicalBinding referenceBinding = createReferenceCallbackBinding();
        LogicalBinding serviceBinding = createServiceCallbackBinding();
        LogicalReference reference = (LogicalReference) referenceBinding.getParent();
        LogicalService service = (LogicalService) serviceBinding.getParent();


        GeneratorRegistry registry = EasyMock.createMock(GeneratorRegistry.class);
        BindingGenerator sourceBindingGenerator = setupBindingGenerator(registry);
        ComponentGenerator componentGenerator = setupTargetComponentGenerator(registry);
        ContractMatcher matcher = EasyMock.createMock(ContractMatcher.class);
        PolicyResolver policyResolver = setupRemoteCallbackPolicyResolver();
        PhysicalOperationGenerator operationGenerator = setupOperationGenerator();

        EasyMock.replay(registry, matcher, policyResolver, operationGenerator, componentGenerator, sourceBindingGenerator);

        WireGeneratorImpl generator = new WireGeneratorImpl(registry, matcher, policyResolver, operationGenerator);

        LogicalWire wire = new LogicalWire(reference.getParent(), reference, service, DEPLOYABLE);
        wire.setSourceBinding(referenceBinding);
        wire.setTargetBinding(serviceBinding);

        PhysicalWireDefinition definition = generator.generateWireCallback(wire);

        assertNotNull(definition.getSource());
        assertNotNull(definition.getTarget());

        EasyMock.verify(registry, matcher, policyResolver, operationGenerator, componentGenerator,sourceBindingGenerator);
    }


    private PolicyResolver setupPolicyResolver() throws PolicyResolutionException {
        PolicyResolver policyResolver = EasyMock.createMock(PolicyResolver.class);
        EasyMock.expect(policyResolver.resolvePolicies(EasyMock.isA(LogicalBinding.class))).andReturn(new MockPolicyResult());
        return policyResolver;
    }

    private PolicyResolver setupLocalPolicyResolver() throws PolicyResolutionException {
        PolicyResolver policyResolver = EasyMock.createMock(PolicyResolver.class);
        EasyMock.expect(policyResolver.resolveLocalPolicies(EasyMock.isA(LogicalWire.class))).andReturn(new MockPolicyResult());
        return policyResolver;
    }

    private PolicyResolver setupRemotePolicyResolver() throws PolicyResolutionException {
        PolicyResolver policyResolver = EasyMock.createMock(PolicyResolver.class);
        EasyMock.expect(policyResolver.resolveRemotePolicies(EasyMock.isA(LogicalWire.class))).andReturn(new MockPolicyResult());
        return policyResolver;
    }

    private PolicyResolver setupLocalCallbackPolicyResolver() throws PolicyResolutionException {
        PolicyResolver policyResolver = EasyMock.createMock(PolicyResolver.class);
        EasyMock.expect(policyResolver.resolveLocalCallbackPolicies(EasyMock.isA(LogicalWire.class))).andReturn(new MockPolicyResult());
        return policyResolver;
    }

    private PolicyResolver setupCallbackPolicyResolver() throws PolicyResolutionException {
        PolicyResolver policyResolver = EasyMock.createMock(PolicyResolver.class);
        EasyMock.expect(policyResolver.resolveCallbackPolicies(EasyMock.isA(LogicalBinding.class))).andReturn(new MockPolicyResult());
        return policyResolver;
    }

    private PolicyResolver setupRemoteCallbackPolicyResolver() throws PolicyResolutionException {
        PolicyResolver policyResolver = EasyMock.createMock(PolicyResolver.class);
        EasyMock.expect(policyResolver.resolveRemoteCallbackPolicies(EasyMock.isA(LogicalWire.class))).andReturn(new MockPolicyResult());
        return policyResolver;
    }

    @SuppressWarnings({"unchecked"})
    private PhysicalOperationGenerator setupOperationGenerator() throws GenerationException {
        PhysicalOperationGenerator operationGenerator = EasyMock.createMock(PhysicalOperationGenerator.class);
        Set<PhysicalOperationDefinition> set = Collections.<PhysicalOperationDefinition>singleton(new MockOperationDefinition());
        EasyMock.expect(operationGenerator.generateOperations(EasyMock.isA(List.class),
                                                              EasyMock.eq(true),
                                                              EasyMock.isA(PolicyResult.class))).andReturn(set);
        return operationGenerator;
    }

    @SuppressWarnings({"unchecked"})
    private PhysicalOperationGenerator setupLocalOperationGenerator() throws GenerationException {
        PhysicalOperationGenerator operationGenerator = EasyMock.createMock(PhysicalOperationGenerator.class);
        Set<PhysicalOperationDefinition> set = Collections.<PhysicalOperationDefinition>singleton(new MockOperationDefinition());
        EasyMock.expect(operationGenerator.generateOperations(EasyMock.isA(List.class),
                                                              EasyMock.eq(false),
                                                              EasyMock.isA(PolicyResult.class))).andReturn(set);
        return operationGenerator;
    }

    @SuppressWarnings({"unchecked"})
    private BindingGenerator setupBindingGenerator(GeneratorRegistry registry) throws GenerationException {
        BindingGenerator bindingGenerator = EasyMock.createMock(BindingGenerator.class);
        EasyMock.expect(bindingGenerator.generateSource(EasyMock.isA(LogicalBinding.class),
                                                        EasyMock.isA(ServiceContract.class),
                                                        EasyMock.isA(List.class),
                                                        (EffectivePolicy) EasyMock.isNull())).andReturn(new MockSourceDefinition());
        EasyMock.expect(registry.getBindingGenerator(EasyMock.isA(Class.class))).andReturn(bindingGenerator);
        return bindingGenerator;
    }

    @SuppressWarnings({"unchecked"})
    private BindingGenerator setupServiceBindingTargetGenerator(GeneratorRegistry registry) throws GenerationException {
        BindingGenerator bindingGenerator = EasyMock.createMock(BindingGenerator.class);
        EasyMock.expect(bindingGenerator.generateServiceBindingTarget(EasyMock.isA(LogicalBinding.class),
                                                                      EasyMock.isA(ServiceContract.class),
                                                                      EasyMock.isA(List.class),
                                                                      (EffectivePolicy) EasyMock.isNull())).andReturn(new MockTargetDefinition());
        EasyMock.expect(registry.getBindingGenerator(EasyMock.isA(Class.class))).andReturn(bindingGenerator);
        return bindingGenerator;
    }

    @SuppressWarnings({"unchecked"})
    private BindingGenerator setupReferenceBindingTargetGenerator(GeneratorRegistry registry) throws GenerationException {
        BindingGenerator bindingGenerator = EasyMock.createMock(BindingGenerator.class);
        EasyMock.expect(bindingGenerator.generateServiceBindingTarget(EasyMock.isA(LogicalBinding.class),
                                                                      EasyMock.isA(ServiceContract.class),
                                                                      EasyMock.isA(List.class),
                                                                      (EffectivePolicy) EasyMock.isNull())).andReturn(new MockTargetDefinition());
        EasyMock.expect(registry.getBindingGenerator(EasyMock.isA(Class.class))).andReturn(bindingGenerator);
        return bindingGenerator;
    }

    @SuppressWarnings({"unchecked"})
    private BindingGenerator setupTargetBindingGenerator(GeneratorRegistry registry) throws GenerationException {
        BindingGenerator bindingGenerator = EasyMock.createMock(BindingGenerator.class);
        EasyMock.expect(bindingGenerator.generateTarget(EasyMock.isA(LogicalBinding.class),
                                                        EasyMock.isA(ServiceContract.class),
                                                        EasyMock.isA(List.class),
                                                        (EffectivePolicy) EasyMock.isNull())).andReturn(new MockTargetDefinition());
        EasyMock.expect(registry.getBindingGenerator(EasyMock.isA(Class.class))).andReturn(bindingGenerator);
        return bindingGenerator;
    }

    @SuppressWarnings({"unchecked"})
    private ComponentGenerator setupTargetComponentGenerator(GeneratorRegistry registry) throws GenerationException {
        ComponentGenerator componentGenerator = EasyMock.createMock(ComponentGenerator.class);
        EasyMock.expect(componentGenerator.generateTarget(EasyMock.isA(LogicalService.class),
                                                          (EffectivePolicy) EasyMock.isNull())).andReturn(new MockTargetDefinition());

        EasyMock.expect(registry.getComponentGenerator(EasyMock.isA(Class.class))).andReturn(componentGenerator);
        return componentGenerator;
    }

    @SuppressWarnings({"unchecked"})
    private ComponentGenerator setupSourceComponentGenerator(GeneratorRegistry registry) throws GenerationException {
        ComponentGenerator componentGenerator = EasyMock.createMock(ComponentGenerator.class);
        EasyMock.expect(componentGenerator.generateSource(EasyMock.isA(LogicalReference.class),
                                                          (EffectivePolicy) EasyMock.isNull())).andReturn(new MockSourceDefinition());

        EasyMock.expect(registry.getComponentGenerator(EasyMock.isA(Class.class))).andReturn(componentGenerator);
        return componentGenerator;
    }

    @SuppressWarnings({"unchecked"})
    private ComponentGenerator setupCallbackComponentGenerator(GeneratorRegistry registry) throws GenerationException {
        ComponentGenerator componentGenerator = EasyMock.createMock(ComponentGenerator.class);
        EasyMock.expect(componentGenerator.generateCallbackSource(EasyMock.isA(LogicalService.class),
                                                                  (EffectivePolicy) EasyMock.isNull())).andReturn(new MockSourceDefinition());

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

    private class MockWireDefinition extends PhysicalWireDefinition {
        private static final long serialVersionUID = 358078346745301821L;

        private MockWireDefinition() {
            super(null, null, null);
        }
    }

    private class MockImplementation extends Implementation<ComponentType> {
        private static final long serialVersionUID = -8283300933931216298L;

        @Override
        public QName getType() {
            return null;
        }
    }

    private class MockSourceDefinition extends PhysicalSourceDefinition {
        private static final long serialVersionUID = -3656506060148889502L;
    }

    private class MockTargetDefinition extends PhysicalTargetDefinition {
        private static final long serialVersionUID = 9192215132618200005L;
    }

    private class MockOperationDefinition extends PhysicalOperationDefinition {
        private static final long serialVersionUID = 7905560274906591901L;
    }

}