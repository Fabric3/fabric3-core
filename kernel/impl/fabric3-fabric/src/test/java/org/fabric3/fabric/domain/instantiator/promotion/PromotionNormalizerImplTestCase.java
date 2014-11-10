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
package org.fabric3.fabric.domain.instantiator.promotion;

import java.net.URI;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import javax.xml.namespace.QName;

import junit.framework.TestCase;
import org.easymock.EasyMock;

import org.fabric3.fabric.domain.instantiator.InstantiationContext;
import org.fabric3.api.model.type.component.BindingDefinition;
import org.fabric3.api.model.type.component.ComponentDefinition;
import org.fabric3.api.model.type.component.ComponentReference;
import org.fabric3.api.model.type.component.ComponentService;
import org.fabric3.api.model.type.component.Composite;
import org.fabric3.api.model.type.component.CompositeImplementation;
import org.fabric3.api.model.type.component.Implementation;
import org.fabric3.api.model.type.component.Multiplicity;
import org.fabric3.api.model.type.component.ReferenceDefinition;
import org.fabric3.api.model.type.contract.ServiceContract;
import org.fabric3.api.model.type.definitions.Intent;
import org.fabric3.spi.domain.generator.policy.PolicyRegistry;
import org.fabric3.spi.model.instance.LogicalBinding;
import org.fabric3.spi.model.instance.LogicalComponent;
import org.fabric3.spi.model.instance.LogicalCompositeComponent;
import org.fabric3.spi.model.instance.LogicalReference;
import org.fabric3.spi.model.instance.LogicalService;
import org.fabric3.spi.model.instance.LogicalWire;

public class PromotionNormalizerImplTestCase extends TestCase {
    private static final QName PROMOTED_INTENT = new QName("test", "intent");
    private static final QName PROMOTED_POLICY = new QName("test", "intent");

    private PolicyRegistry registry;
    private InstantiationContext context;

    public void testServiceBindingAndPolicyNormalization() throws Exception {
        registry.getDefinitions(Collections.singleton(PROMOTED_INTENT), Intent.class);
        EasyMock.expectLastCall().andReturn(Collections.<Intent>emptySet()).times(2);
        EasyMock.replay(registry);

        PromotionNormalizerImpl normalizer = new PromotionNormalizerImpl(registry);

        LogicalService service = createServiceHierarchy();
        LogicalComponent<?> component = service.getParent();

        normalizer.normalize(component, context);

        assertFalse(context.hasErrors());
        assertTrue(service.getBindings().get(0).getDefinition() instanceof MockPromotedBinding);
        assertTrue(service.getIntents().contains(PROMOTED_INTENT));
        assertTrue(service.getIntents().contains(PROMOTED_POLICY));
        EasyMock.verify(registry);
    }

    @SuppressWarnings({"unchecked"})
    public void testValidateMutuallyExclusiveServiceIntents() throws Exception {
        QName intent1Name = new QName("test", "intent1");
        QName intent2Name = new QName("test", "intent2");
        Intent intent1 = new Intent(intent1Name, null, null, null, true, Collections.singleton(intent2Name), null, false);
        Intent intent2 = new Intent(intent1Name, null, null, null, true, Collections.singleton(intent1Name), null, false);
        Set<Intent> intents = new HashSet<>();
        intents.add(intent1);
        intents.add(intent2);
        EasyMock.expect(registry.getDefinitions(EasyMock.isA(Set.class), EasyMock.eq(Intent.class))).andReturn(intents);
        EasyMock.expect(registry.getDefinitions(EasyMock.isA(Set.class), EasyMock.eq(Intent.class))).andReturn(Collections.<Intent>emptySet());
        EasyMock.replay(registry);

        PromotionNormalizerImpl normalizer = new PromotionNormalizerImpl(registry);

        LogicalService service = createServiceHierarchy();
        LogicalComponent<?> component = service.getParent();
        LogicalService parentService = service.getParent().getParent().getService("promotedService");
        parentService.addIntent(intent1Name);
        parentService.addIntent(intent2Name);

        normalizer.normalize(component, context);

        assertTrue(context.hasErrors());
        assertTrue(context.getErrors().get(0) instanceof MutuallyExclusiveIntents);
        EasyMock.verify(registry);
    }

    public void testReferenceBindingAndPolicyNormalization() throws Exception {
        registry.getDefinitions(Collections.singleton(PROMOTED_INTENT), Intent.class);
        EasyMock.expectLastCall().andReturn(Collections.<Intent>emptySet());
        EasyMock.replay(registry);

        PromotionNormalizerImpl normalizer = new PromotionNormalizerImpl(registry);

        LogicalReference reference = createReferenceHierarchy();
        LogicalComponent<?> component = reference.getParent();

        normalizer.normalize(component, context);

        assertFalse(context.hasErrors());
        assertTrue(reference.getBindings().get(0).getDefinition() instanceof MockPromotedBinding);
        assertTrue(reference.getIntents().contains(PROMOTED_INTENT));
        assertTrue(reference.getIntents().contains(PROMOTED_POLICY));
        EasyMock.verify(registry);
    }

    public void testWireNormalization() throws Exception {
        EasyMock.replay(registry);

        PromotionNormalizerImpl normalizer = new PromotionNormalizerImpl(registry);

        LogicalReference reference = createWiredReferenceHierarchy();
        LogicalComponent<?> component = reference.getParent();

        normalizer.normalize(component, context);

        assertFalse(context.hasErrors());
        EasyMock.verify(registry);
    }

    public void testMultiplicityOneToOneWireNormalization() throws Exception {
        EasyMock.replay(registry);

        PromotionNormalizerImpl normalizer = new PromotionNormalizerImpl(registry);

        LogicalReference reference = createMultiplicityReferenceHierarchy(Multiplicity.ONE_ONE);
        LogicalComponent<?> component = reference.getParent();

        normalizer.normalize(component, context);

        assertTrue(context.hasErrors());
        assertTrue(context.getErrors().get(0) instanceof InvalidNumberOfTargets);
        EasyMock.verify(registry);
    }

    public void testMultiplicityZeroToNWireNormalization() throws Exception {
        EasyMock.replay(registry);

        PromotionNormalizerImpl normalizer = new PromotionNormalizerImpl(registry);

        LogicalReference reference = createMultiplicityReferenceHierarchy(Multiplicity.ZERO_N);
        LogicalComponent<?> component = reference.getParent();

        normalizer.normalize(component, context);

        assertFalse(context.hasErrors());
        EasyMock.verify(registry);
    }

    public void testMultiplicityZeroToOneWireNormalization() throws Exception {
        EasyMock.replay(registry);

        PromotionNormalizerImpl normalizer = new PromotionNormalizerImpl(registry);

        LogicalReference reference = createMultiplicityReferenceHierarchy(Multiplicity.ZERO_ONE);
        LogicalComponent<?> component = reference.getParent();

        normalizer.normalize(component, context);

        assertTrue(context.hasErrors());
        assertTrue(context.getErrors().get(0) instanceof InvalidNumberOfTargets);
        EasyMock.verify(registry);
    }

    public void testMultiplicityOneToOneNoWireNormalization() throws Exception {
        EasyMock.replay(registry);

        PromotionNormalizerImpl normalizer = new PromotionNormalizerImpl(registry);

        LogicalReference reference = createNonWireMultiplicityReferenceHierarchy(Multiplicity.ONE_ONE);
        LogicalComponent<?> component = reference.getParent();

        normalizer.normalize(component, context);

        assertTrue(context.hasErrors());
        assertTrue(context.getErrors().get(0) instanceof InvalidNumberOfTargets);
        EasyMock.verify(registry);
    }

    public void testMultiplicityOneToNNoWireNormalization() throws Exception {
        EasyMock.replay(registry);

        PromotionNormalizerImpl normalizer = new PromotionNormalizerImpl(registry);

        LogicalReference reference = createNonWireMultiplicityReferenceHierarchy(Multiplicity.ONE_N);
        LogicalComponent<?> component = reference.getParent();

        normalizer.normalize(component, context);

        assertTrue(context.hasErrors());
        assertTrue(context.getErrors().get(0) instanceof InvalidNumberOfTargets);
        EasyMock.verify(registry);
    }

    @SuppressWarnings({"unchecked"})
    private LogicalService createServiceHierarchy() {
        LogicalCompositeComponent parent = createComposite();
        LogicalService parentService = new LogicalService(URI.create("parent#promotedService"), null, parent);
        parentService.setPromotedUri(URI.create("parent/component#service"));
        MockPromotedBinding binding = new MockPromotedBinding();
        LogicalBinding parentBinding = new LogicalBinding(binding, parentService);
        parentService.addBinding(parentBinding);
        parentService.addIntent(PROMOTED_INTENT);
        parentService.addPolicySet(PROMOTED_POLICY);
        parent.addService(parentService);

        LogicalComponent<?> component = new LogicalComponent(URI.create("component"), null, parent);
        parent.addComponent(component);

        ComponentService componentService = new ComponentService("service");
        componentService.setServiceContract(new MockContract());
        LogicalService service = new LogicalService(URI.create("parent/component#service"), componentService, component);

        service.setServiceContract(new MockContract());
        component.addService(service);
        return service;
    }

    @SuppressWarnings({"unchecked"})
    private LogicalReference createReferenceHierarchy() {
        LogicalCompositeComponent parent = createComposite();

        ReferenceDefinition parentReferenceDefinition = new ReferenceDefinition("promotedReference", null, Multiplicity.ONE_ONE);
        LogicalReference parentReference = new LogicalReference(URI.create("parent#promotedReference"), parentReferenceDefinition, parent);
        parentReference.addPromotedUri(URI.create("parent/component#reference"));
        MockPromotedBinding binding = new MockPromotedBinding();
        LogicalBinding parentBinding = new LogicalBinding(binding, parentReference);
        parentReference.addBinding(parentBinding);
        parentReference.addIntent(PROMOTED_INTENT);
        parentReference.addPolicySet(PROMOTED_POLICY);
        parent.addReference(parentReference);

        LogicalComponent<?> component = new LogicalComponent(URI.create("component"), null, parent);
        parent.addComponent(component);

        ComponentReference componentReference = new ComponentReference("reference", Multiplicity.ONE_ONE);
        componentReference.setServiceContract(new MockContract());
        LogicalReference reference = new LogicalReference(URI.create("parent/component#reference"), componentReference, component);

        reference.setServiceContract(new MockContract());
        component.addReference(reference);
        return reference;
    }

    @SuppressWarnings({"unchecked"})
    private LogicalReference createWiredReferenceHierarchy() {
        LogicalCompositeComponent parent = createComposite();

        LogicalReference parentReference = createParentReference("promotedReference", parent, Multiplicity.ONE_ONE);

        ComponentDefinition componentDefinition = new ComponentDefinition("component");
        LogicalComponent<?> component = new LogicalComponent(URI.create("component"), componentDefinition, parent);
        parent.addComponent(component);

        LogicalCompositeComponent domain = parent.getParent();
        LogicalWire wire = new LogicalWire(domain, parentReference, null, null);
        domain.addWire(parentReference, wire);

        return createComponentReference("reference", component, Multiplicity.ONE_ONE);
    }

    @SuppressWarnings({"unchecked"})
    private LogicalReference createMultiplicityReferenceHierarchy(Multiplicity multiplicity) {
        LogicalCompositeComponent parent = createComposite();

        LogicalReference parentReference = createParentReference("promotedReference", parent, multiplicity);

        ComponentDefinition componentDefinition = new ComponentDefinition("component");
        LogicalComponent<?> component = new LogicalComponent(URI.create("component"), componentDefinition, parent);
        parent.addComponent(component);

        LogicalCompositeComponent domain = parent.getParent();
        LogicalWire wire1 = new LogicalWire(domain, parentReference, null, null);
        domain.addWire(parentReference, wire1);
        LogicalWire wire2 = new LogicalWire(domain, parentReference, null, null);
        domain.addWire(parentReference, wire2);

        ReferenceDefinition definition = new ReferenceDefinition("reference", multiplicity);
        LogicalReference reference =  new LogicalReference(URI.create("parent/component#reference"), definition, component);
        reference.setServiceContract(new MockContract());
        component.addReference(reference);
        return reference;
    }

    @SuppressWarnings({"unchecked"})
    private LogicalReference createNonWireMultiplicityReferenceHierarchy(Multiplicity multiplicity) {
        LogicalCompositeComponent parent = createComposite();

        createParentReference("promotedReference", parent, multiplicity);

        ComponentDefinition componentDefinition = new ComponentDefinition("component");
        LogicalComponent<?> component = new LogicalComponent(URI.create("component"), componentDefinition, parent);
        parent.addComponent(component);


        ReferenceDefinition definition = new ReferenceDefinition("reference", multiplicity);
        LogicalReference reference =  new LogicalReference(URI.create("parent/component#reference"), definition, component);
        reference.setServiceContract(new MockContract());
        component.addReference(reference);
        return reference;
    }

    private LogicalReference createComponentReference(String name, LogicalComponent<?> component, Multiplicity multiplicity) {
        ComponentReference componentReference = new ComponentReference(name, multiplicity);
        ComponentDefinition<? extends Implementation<?>> definition = component.getDefinition();
        definition.add(componentReference);
        componentReference.setServiceContract(new MockContract());
        LogicalReference reference = new LogicalReference(URI.create("parent/component#" + name), componentReference, component);
        reference.setServiceContract(new MockContract());
        component.addReference(reference);
        return reference;
    }

    private LogicalReference createParentReference(String name, LogicalCompositeComponent parent, Multiplicity multiplicity) {
        ComponentReference parentReferenceDefinition = new ComponentReference(name, multiplicity);
        parent.getDefinition().add(parentReferenceDefinition);
        LogicalReference parentReference = new LogicalReference(URI.create("parent#" + name), parentReferenceDefinition, parent);
        parentReference.addPromotedUri(URI.create("parent/component#reference"));
        parent.addReference(parentReference);
        return parentReference;
    }

    private LogicalCompositeComponent createComposite() {
        Composite composite = new Composite(new QName("test", "composite"));
        CompositeImplementation implementation = new CompositeImplementation();
        implementation.setComponentType(composite);

        LogicalCompositeComponent domain = new LogicalCompositeComponent(URI.create("domain"), null, null);

        ComponentDefinition<CompositeImplementation> compositeDefinition = new ComponentDefinition<>("composite");
        compositeDefinition.setImplementation(implementation);
        return new LogicalCompositeComponent(URI.create("parent"), compositeDefinition, domain);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        registry = EasyMock.createMock(PolicyRegistry.class);
        context = new InstantiationContext();
    }

    private class MockContract extends ServiceContract {
        private static final long serialVersionUID = 4694922526004661018L;

        @Override
        public String getQualifiedInterfaceName() {
            return null;
        }
    }

    private class MockPromotedBinding extends BindingDefinition {
        private static final long serialVersionUID = -7088192438672216044L;

        public MockPromotedBinding() {
            super(null, null);
        }
    }

}

