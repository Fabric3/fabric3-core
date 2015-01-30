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
package org.fabric3.fabric.domain.instantiator.wire;

import java.net.URI;

import junit.framework.TestCase;
import org.fabric3.api.model.type.component.Component;
import org.fabric3.api.model.type.component.ComponentType;
import org.fabric3.api.model.type.component.Composite;
import org.fabric3.api.model.type.component.Implementation;
import org.fabric3.api.model.type.component.Multiplicity;
import org.fabric3.api.model.type.component.Reference;
import org.fabric3.api.model.type.component.Service;
import org.fabric3.api.model.type.contract.ServiceContract;
import org.fabric3.fabric.contract.DefaultContractMatcher;
import org.fabric3.fabric.contract.JavaContractMatcherExtension;
import org.fabric3.fabric.domain.instantiator.InstantiationContext;
import org.fabric3.fabric.domain.instantiator.ReferenceNotFound;
import org.fabric3.spi.domain.instantiator.AutowireResolver;
import org.fabric3.spi.model.instance.LogicalComponent;
import org.fabric3.spi.model.instance.LogicalCompositeComponent;
import org.fabric3.spi.model.instance.LogicalReference;
import org.fabric3.spi.model.instance.LogicalService;
import org.fabric3.spi.model.type.component.CompositeImplementation;
import org.fabric3.spi.model.type.java.JavaServiceContract;

/**
 *
 */
public class AutowireInstantiatorImplTestCase extends TestCase {
    private static final URI REFERENCE_URI = URI.create("source#ref");
    private static final URI SOURCE_URI = URI.create("source");
    private static final URI TARGET_URI = URI.create("target#service");
    private LogicalCompositeComponent domain;
    private AutowireInstantiatorImpl resolutionService;

    public void testAutowireAtomicToAtomic() throws Exception {
        LogicalCompositeComponent composite = createWiredComposite(domain, Foo.class, Foo.class);
        InstantiationContext context = new InstantiationContext();
        resolutionService.instantiate(composite, context);
        LogicalComponent<?> source = composite.getComponent(SOURCE_URI);
        assertEquals(TARGET_URI, source.getReference("ref").getWires().get(0).getTarget().getUri());
    }

    public void testAutowireAtomicToAtomicRequiresSuperInterface() throws Exception {
        LogicalCompositeComponent composite = createWiredComposite(domain, SuperFoo.class, Foo.class);
        InstantiationContext context = new InstantiationContext();
        resolutionService.instantiate(composite, context);
        LogicalComponent<?> source = composite.getComponent(SOURCE_URI);
        resolutionService.instantiate(composite, context);
        assertEquals(TARGET_URI, source.getReference("ref").getWires().get(0).getTarget().getUri());
    }

    public void testAutowireAtomicToAtomicRequiresSubInterface() throws Exception {
        LogicalComponent<CompositeImplementation> composite = createWiredComposite(domain, Foo.class, SuperFoo.class);
        InstantiationContext context = new InstantiationContext();
        resolutionService.instantiate(composite, context);
        assertTrue(context.getErrors().get(0) instanceof ReferenceNotFound);
    }

    public void testAutowireAtomicToAtomicIncompatibleInterfaces() throws Exception {
        LogicalComponent<CompositeImplementation> composite = createWiredComposite(domain, Foo.class, String.class);
        InstantiationContext context = new InstantiationContext();
        resolutionService.instantiate(composite, context);
        assertTrue(context.getErrors().get(0) instanceof ReferenceNotFound);
    }

    public void testNestedAutowireAtomicToAtomic() throws Exception {
        LogicalCompositeComponent composite = createWiredComposite(domain, Foo.class, Foo.class);
        LogicalCompositeComponent parent = createComposite("parent", composite);
        parent.addComponent(composite);
        parent.getDefinition().getImplementation().getComponentType().add(composite.getDefinition());
        InstantiationContext context = new InstantiationContext();
        resolutionService.instantiate(parent, context);
        LogicalComponent<?> source = composite.getComponent(SOURCE_URI);
        assertEquals(TARGET_URI, source.getReference("ref").getWires().get(0).getTarget().getUri());
    }

    public void testAutowireIncludeInComposite() throws Exception {
        LogicalCompositeComponent composite = createComposite("composite", domain);
        LogicalComponent<?> source = createSourceAtomic(Foo.class, composite);
        composite.addComponent(source);
        LogicalComponent<?> target = createTargetAtomic(Foo.class, composite);
        composite.addComponent(target);
        InstantiationContext context = new InstantiationContext();
        resolutionService.instantiate(source, context);
    }

    public void testAutowireToSiblingIncludeInComposite() throws Exception {
        LogicalCompositeComponent parent = createComposite("parent", null);
        LogicalCompositeComponent composite = createComposite("composite", parent);
        LogicalComponent<?> source = createSourceAtomic(Foo.class, composite);
        composite.addComponent(source);
        LogicalComponent<?> target = createTargetAtomic(Foo.class, composite);
        parent.addComponent(composite);
        composite.addComponent(target);
        InstantiationContext context = new InstantiationContext();
        resolutionService.instantiate(composite, context);
    }

    protected void setUp() throws Exception {
        super.setUp();
        DefaultContractMatcher matcher = new DefaultContractMatcher();
        JavaContractMatcherExtension javaMatcher = new JavaContractMatcherExtension();
        matcher.addMatcherExtension(javaMatcher);
        AutowireResolver resolver = new TypeAutowireResolver(matcher);
        resolutionService = new AutowireInstantiatorImpl(resolver);
        URI domainUri = URI.create("fabric3://runtime");
        domain = new LogicalCompositeComponent(domainUri, null, null);
    }

    private LogicalCompositeComponent createWiredComposite(LogicalCompositeComponent parent, Class<?> sourceClass, Class<?> targetClass) {
        LogicalCompositeComponent composite = createComposite("composite", parent);
        LogicalComponent<?> source = createSourceAtomic(sourceClass, composite);
        composite.addComponent(source);
        Composite type = composite.getDefinition().getImplementation().getComponentType();
        type.add(source.getDefinition());
        LogicalComponent<?> target = createTargetAtomic(targetClass, composite);
        composite.addComponent(target);
        type.add(target.getDefinition());
        return composite;
    }

    private LogicalCompositeComponent createComposite(String uri, LogicalCompositeComponent parent) {
        URI parentUri = URI.create(uri);
        Composite type = new Composite(null);
        CompositeImplementation impl = new CompositeImplementation();
        impl.setComponentType(type);
        Component<CompositeImplementation> definition = new Component<>(parentUri.toString());
        definition.setImplementation(impl);
        return new LogicalCompositeComponent(parentUri, definition, parent);
    }

    private LogicalComponent<?> createSourceAtomic(Class<?> requiredInterface, LogicalCompositeComponent parent) {

        ServiceContract contract = new JavaServiceContract(requiredInterface);
        Reference<ComponentType> reference = new Reference<>("ref", contract, Multiplicity.ONE_ONE);
        ComponentType type = new ComponentType();
        type.add(reference);
        MockAtomicImpl impl = new MockAtomicImpl();
        impl.setComponentType(type);
        Component<MockAtomicImpl> definition = new Component<>(SOURCE_URI.toString());
        definition.setImplementation(impl);
        Reference<Component> target = new Reference<>(REFERENCE_URI.getFragment(), Multiplicity.ONE_ONE);
        definition.add(target);
        LogicalComponent<?> component = new LogicalComponent<>(SOURCE_URI, definition, parent);
        LogicalReference logicalReference = new LogicalReference(REFERENCE_URI, reference, component);
        component.addReference(logicalReference);
        return component;
    }

    private LogicalComponent<?> createTargetAtomic(Class<?> serviceInterface, LogicalCompositeComponent parent) {
        URI uri = URI.create("target");
        JavaServiceContract contract = new JavaServiceContract(serviceInterface);
        Service<ComponentType> service = new Service<>("service", contract);
        ComponentType type = new ComponentType();
        type.add(service);
        MockAtomicImpl impl = new MockAtomicImpl();
        impl.setComponentType(type);
        Component<MockAtomicImpl> definition = new Component<>(uri.toString());
        definition.setImplementation(impl);
        LogicalComponent component = new LogicalComponent<>(uri, definition, parent);
        LogicalService logicalService = new LogicalService(TARGET_URI, service, parent);
        component.addService(logicalService);
        return component;
    }

    private class MockAtomicImpl extends Implementation<ComponentType> {
        private static final long serialVersionUID = 9075647188452892957L;

        public String getType() {
            throw new UnsupportedOperationException();
        }
    }

    private interface SuperFoo {

    }

    private interface Foo extends SuperFoo {

    }

}
