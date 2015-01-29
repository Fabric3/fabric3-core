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

import javax.xml.namespace.QName;
import java.net.URI;

import junit.framework.TestCase;
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
import org.fabric3.fabric.domain.instantiator.InstantiationContext;
import org.fabric3.spi.model.instance.LogicalBinding;
import org.fabric3.spi.model.instance.LogicalComponent;
import org.fabric3.spi.model.instance.LogicalCompositeComponent;
import org.fabric3.spi.model.instance.LogicalReference;
import org.fabric3.spi.model.instance.LogicalService;
import org.fabric3.spi.model.instance.LogicalWire;

public class PromotionNormalizerImplTestCase extends TestCase {

    private InstantiationContext context;

    public void testServiceBindingNormalization() throws Exception {

        PromotionNormalizerImpl normalizer = new PromotionNormalizerImpl();

        LogicalService service = createServiceHierarchy();
        LogicalComponent<?> component = service.getParent();

        normalizer.normalize(component, context);

        assertFalse(context.hasErrors());
        assertTrue(service.getBindings().get(0).getDefinition() instanceof MockPromotedBinding);
    }

    @SuppressWarnings({"unchecked"})
    public void testReferenceBinding() throws Exception {
        PromotionNormalizerImpl normalizer = new PromotionNormalizerImpl();

        LogicalReference reference = createReferenceHierarchy();
        LogicalComponent<?> component = reference.getParent();

        normalizer.normalize(component, context);

        assertFalse(context.hasErrors());
        assertTrue(reference.getBindings().get(0).getDefinition() instanceof MockPromotedBinding);
    }

    public void testWireNormalization() throws Exception {

        PromotionNormalizerImpl normalizer = new PromotionNormalizerImpl();

        LogicalReference reference = createWiredReferenceHierarchy();
        LogicalComponent<?> component = reference.getParent();

        normalizer.normalize(component, context);

        assertFalse(context.hasErrors());
    }

    public void testMultiplicityOneToOneWireNormalization() throws Exception {
        PromotionNormalizerImpl normalizer = new PromotionNormalizerImpl();

        LogicalReference reference = createMultiplicityReferenceHierarchy(Multiplicity.ONE_ONE);
        LogicalComponent<?> component = reference.getParent();

        normalizer.normalize(component, context);

        assertTrue(context.hasErrors());
        assertTrue(context.getErrors().get(0) instanceof InvalidNumberOfTargets);
    }

    public void testMultiplicityZeroToNWireNormalization() throws Exception {

        PromotionNormalizerImpl normalizer = new PromotionNormalizerImpl();

        LogicalReference reference = createMultiplicityReferenceHierarchy(Multiplicity.ZERO_N);
        LogicalComponent<?> component = reference.getParent();

        normalizer.normalize(component, context);

        assertFalse(context.hasErrors());
    }

    public void testMultiplicityZeroToOneWireNormalization() throws Exception {

        PromotionNormalizerImpl normalizer = new PromotionNormalizerImpl();

        LogicalReference reference = createMultiplicityReferenceHierarchy(Multiplicity.ZERO_ONE);
        LogicalComponent<?> component = reference.getParent();

        normalizer.normalize(component, context);

        assertTrue(context.hasErrors());
        assertTrue(context.getErrors().get(0) instanceof InvalidNumberOfTargets);
    }

    public void testMultiplicityOneToOneNoWireNormalization() throws Exception {

        PromotionNormalizerImpl normalizer = new PromotionNormalizerImpl();

        LogicalReference reference = createNonWireMultiplicityReferenceHierarchy(Multiplicity.ONE_ONE);
        LogicalComponent<?> component = reference.getParent();

        normalizer.normalize(component, context);

        assertTrue(context.hasErrors());
        assertTrue(context.getErrors().get(0) instanceof InvalidNumberOfTargets);
    }

    public void testMultiplicityOneToNNoWireNormalization() throws Exception {

        PromotionNormalizerImpl normalizer = new PromotionNormalizerImpl();

        LogicalReference reference = createNonWireMultiplicityReferenceHierarchy(Multiplicity.ONE_N);
        LogicalComponent<?> component = reference.getParent();

        normalizer.normalize(component, context);

        assertTrue(context.hasErrors());
        assertTrue(context.getErrors().get(0) instanceof InvalidNumberOfTargets);
    }

    @SuppressWarnings({"unchecked"})
    private LogicalService createServiceHierarchy() {
        LogicalCompositeComponent parent = createComposite();
        LogicalService parentService = new LogicalService(URI.create("parent#promotedService"), null, parent);
        parentService.setPromotedUri(URI.create("parent/component#service"));
        MockPromotedBinding binding = new MockPromotedBinding();
        LogicalBinding parentBinding = new LogicalBinding(binding, parentService);
        parentService.addBinding(parentBinding);
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
        LogicalReference reference = new LogicalReference(URI.create("parent/component#reference"), definition, component);
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
        LogicalReference reference = new LogicalReference(URI.create("parent/component#reference"), definition, component);
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

