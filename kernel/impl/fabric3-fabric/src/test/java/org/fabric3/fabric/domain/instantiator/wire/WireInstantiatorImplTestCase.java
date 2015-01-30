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

import javax.xml.namespace.QName;
import java.net.URI;
import java.util.List;
import java.util.Map;

import junit.framework.TestCase;
import org.easymock.EasyMock;
import org.fabric3.api.model.type.component.Binding;
import org.fabric3.api.model.type.component.Component;
import org.fabric3.api.model.type.component.ComponentType;
import org.fabric3.api.model.type.component.Composite;
import org.fabric3.spi.model.type.component.CompositeImplementation;
import org.fabric3.api.model.type.component.Multiplicity;
import org.fabric3.api.model.type.component.Reference;
import org.fabric3.api.model.type.component.Service;
import org.fabric3.api.model.type.component.Target;
import org.fabric3.api.model.type.component.Wire;
import org.fabric3.api.model.type.contract.ServiceContract;
import org.fabric3.fabric.domain.instantiator.AmbiguousService;
import org.fabric3.fabric.domain.instantiator.InstantiationContext;
import org.fabric3.fabric.domain.instantiator.ServiceNotFound;
import org.fabric3.spi.contract.ContractMatcher;
import org.fabric3.spi.contract.MatchResult;
import org.fabric3.spi.model.instance.LogicalBinding;
import org.fabric3.spi.model.instance.LogicalComponent;
import org.fabric3.spi.model.instance.LogicalCompositeComponent;
import org.fabric3.spi.model.instance.LogicalReference;
import org.fabric3.spi.model.instance.LogicalService;
import org.fabric3.spi.model.instance.LogicalWire;

/**
 *
 */
public class WireInstantiatorImplTestCase extends TestCase {
    private ContractMatcher matcher;
    private InstantiationContext context;

    public void testInstantiateCompositeWires() throws Exception {
        EasyMock.replay(matcher);

        Composite composite = createCompositeWithWire();
        LogicalCompositeComponent logicalComposite = createLogicalComposite(composite);
        WireInstantiatorImpl instantiator = new WireInstantiatorImpl(matcher);

        instantiator.instantiateCompositeWires(composite, logicalComposite, context);

        EasyMock.verify(matcher);
        assertFalse(context.hasErrors());
        for (LogicalReference logicalReference : logicalComposite.getReferences()) {
            List<LogicalWire> referenceWires = logicalReference.getWires();
            for (LogicalWire wire : referenceWires) {
                assertNotNull(wire.getSource());
                assertNotNull(wire.getTarget());
            }
        }
    }

    public void testInstantiateReferenceWires() throws Exception {
        EasyMock.replay(matcher);

        createCompositeWithWire();
        LogicalComponent<?> logicalComponent = createLogicalComponent();
        WireInstantiatorImpl instantiator = new WireInstantiatorImpl(matcher);

        instantiator.instantiateReferenceWires(logicalComponent, context);

        EasyMock.verify(matcher);
        assertFalse(context.hasErrors());
        for (LogicalReference logicalReference : logicalComponent.getReferences()) {
            List<LogicalWire> referenceWires = logicalReference.getWires();
            for (LogicalWire wire : referenceWires) {
                assertNotNull(wire.getSource());
                assertNotNull(wire.getTarget());
            }
        }
    }

    public void testInstantiateBoundReferenceWires() throws Exception {
        EasyMock.replay(matcher);

        Target serviceTarget = new Target("component", "service", "binding");
        LogicalComponent<?> logicalComponent = createLogicalComponentWithBoundReference(serviceTarget);

        WireInstantiatorImpl instantiator = new WireInstantiatorImpl(matcher);

        instantiator.instantiateReferenceWires(logicalComponent, context);

        EasyMock.verify(matcher);
        assertFalse(context.hasErrors());
        for (LogicalReference logicalReference : logicalComponent.getReferences()) {
            List<LogicalWire> referenceWires = logicalReference.getWires();
            for (LogicalWire wire : referenceWires) {
                assertNotNull(wire.getSource());
                assertNotNull(wire.getTarget());
            }
        }
    }

    public void testServiceNotFound() throws Exception {
        Target serviceTarget = new Target("component", "NoService", null);
        LogicalComponent<?> logicalComponent = createLogicalComponentWithBoundReference(serviceTarget);

        WireInstantiatorImpl instantiator = new WireInstantiatorImpl(matcher);
        instantiator.instantiateReferenceWires(logicalComponent, context);

        assertTrue(context.hasErrors());
        assertTrue(context.getErrors().get(0) instanceof ServiceNotFound);
    }

    public void testServiceBindingNotFound() throws Exception {
        EasyMock.replay(matcher);

        Target serviceTarget = new Target("component", "service", "noBinding");
        LogicalComponent<?> logicalComponent = createLogicalComponentWithBoundReference(serviceTarget);

        WireInstantiatorImpl instantiator = new WireInstantiatorImpl(matcher);
        instantiator.instantiateReferenceWires(logicalComponent, context);

        EasyMock.verify(matcher);
        assertTrue(context.hasErrors());
        assertTrue(context.getErrors().get(0) instanceof BindingNotFound);
    }

    public void testAmbiguousService() throws Exception {

        Target serviceTarget = new Target("component");
        LogicalComponent<?> logicalComponent = createLogicalComponentWithBoundReference(serviceTarget);
        createLogicalService("service2", logicalComponent);

        WireInstantiatorImpl instantiator = new WireInstantiatorImpl(matcher);
        instantiator.instantiateReferenceWires(logicalComponent, context);

        assertTrue(context.hasErrors());
        assertTrue(context.getErrors().get(0) instanceof AmbiguousService);
    }

    public void testIncompatibleContracts() throws Exception {
        // replace matcher
        matcher = EasyMock.createMock(ContractMatcher.class);

        EasyMock.expect(matcher.isAssignableFrom(EasyMock.isA(ServiceContract.class), EasyMock.isA(ServiceContract.class), EasyMock.anyBoolean())).andReturn(
                MatchResult.NO_MATCH);
        EasyMock.replay(matcher);
        createCompositeWithWire();
        LogicalComponent<?> logicalComponent = createLogicalComponent();
        WireInstantiatorImpl instantiator = new WireInstantiatorImpl(matcher);

        instantiator.instantiateReferenceWires(logicalComponent, context);

        EasyMock.verify(matcher);
        assertTrue(context.hasErrors());
        assertTrue(context.getErrors().get(0) instanceof IncompatibleContracts);
    }

    @SuppressWarnings({"unchecked"})
    private LogicalComponent<?> createLogicalComponentWithBoundReference(Target serviceTarget) {
        Component<CompositeImplementation> definition = new Component<>("component", null);
        LogicalCompositeComponent logicalComposite = new LogicalCompositeComponent(URI.create("composite"), definition, null);
        LogicalComponent<?> logicalComponent = new LogicalComponent(URI.create("composite/component"), definition, logicalComposite);
        logicalComposite.addComponent(logicalComponent);

        createLogicalReference(logicalComponent, serviceTarget);

        LogicalService logicalService = createLogicalService("service", logicalComponent);
        LogicalBinding<MockBinding> logicalBinding = new LogicalBinding<>(new MockBinding(), logicalService);
        logicalService.addBinding(logicalBinding);
        logicalComponent.addService(logicalService);

        return logicalComponent;
    }

    @SuppressWarnings({"unchecked"})
    private LogicalComponent<?> createLogicalComponent() {
        Component<CompositeImplementation> definition = new Component<>("component", null);
        LogicalCompositeComponent logicalComposite = new LogicalCompositeComponent(URI.create("composite"), definition, null);
        LogicalComponent<?> logicalComponent = new LogicalComponent(URI.create("composite/component"), definition, logicalComposite);
        logicalComposite.addComponent(logicalComponent);

        Target serviceTarget = new Target("component");
        createLogicalReference(logicalComponent, serviceTarget);

        createLogicalService("service", logicalComponent);

        return logicalComponent;
    }

    private LogicalService createLogicalService(String name, LogicalComponent<?> logicalComponent) {
        Service<Component> componentService = new Service<>(name);
        logicalComponent.getDefinition().add(componentService);

        Service<ComponentType> service = new Service<>(name);

        URI serviceUri = URI.create("composite/component#" + name);
        LogicalService logicalService = new LogicalService(serviceUri, service, logicalComponent);
        logicalService.setServiceContract(new MockContract());
        logicalComponent.addService(logicalService);
        return logicalService;
    }

    private LogicalReference createLogicalReference(LogicalComponent<?> logicalComponent, Target target) {
        Reference<Component> componentReference = new Reference<>("reference", Multiplicity.ONE_ONE);
        componentReference.addTarget(target);
        logicalComponent.getDefinition().add(componentReference);

        Reference<ComponentType> reference = new Reference<>("reference", Multiplicity.ONE_ONE);

        URI referenceUri = URI.create("composite/component#reference");
        LogicalReference logicalReference = new LogicalReference(referenceUri, reference, logicalComponent);
        logicalReference.setServiceContract(new MockContract());
        logicalComponent.addReference(logicalReference);
        return logicalReference;
    }

    @SuppressWarnings({"unchecked"})
    private LogicalCompositeComponent createLogicalComposite(Composite composite) {
        CompositeImplementation implementation = new CompositeImplementation();
        implementation.setComponentType(composite);
        Component<CompositeImplementation> definition = new Component<>("composite", implementation);
        LogicalCompositeComponent logicalComposite = new LogicalCompositeComponent(URI.create("composite"), definition, null);
        for (Component component : composite.getComponents().values()) {
            String nameStr = composite.getName().getLocalPart() + "/" + component.getName();
            URI uri = URI.create(nameStr);
            LogicalComponent logicalComponent = new LogicalComponent(uri, component, logicalComposite);
            logicalComposite.addComponent(logicalComponent);
            Map<String, Reference> references = component.getReferences();
            for (Reference reference : references.values()) {
                URI referenceUri = URI.create(nameStr + "#" + reference.getName());
                LogicalReference logicalReference = new LogicalReference(referenceUri, reference, logicalComponent);
                logicalReference.setServiceContract(new MockContract());
                logicalComponent.addReference(logicalReference);
            }
            Map<String, Service> services = component.getServices();
            for (Service service : services.values()) {
                URI serviceUri = URI.create(nameStr + "#" + service.getName());
                LogicalService logicalService = new LogicalService(serviceUri, service, logicalComponent);
                logicalService.setServiceContract(new MockContract());
                logicalComponent.addService(logicalService);
            }
        }
        return logicalComposite;
    }

    @SuppressWarnings({"unchecked"})
    private Composite createCompositeWithWire() {
        QName name = new QName("test", "composite");
        Composite composite = new Composite(name);

        Component fooComponent = new Component("foo");
        Reference reference = new Reference("reference", Multiplicity.ONE_ONE);
        fooComponent.add(reference);
        composite.add(fooComponent);

        Component barComponent = new Component("bar");
        Service service = new Service("reference");
        barComponent.add(service);
        composite.add(barComponent);

        Wire wire = createWire();
        composite.add(wire);
        return composite;
    }

    private Wire createWire() {
        Target referenceTarget = new Target("foo");
        Target serviceTarget = new Target("bar");
        return new Wire(referenceTarget, serviceTarget);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        matcher = EasyMock.createMock(ContractMatcher.class);
        EasyMock.expect(matcher.isAssignableFrom(EasyMock.isA(ServiceContract.class), EasyMock.isA(ServiceContract.class), EasyMock.anyBoolean())).andReturn(
                MatchResult.MATCH);

        context = new InstantiationContext();
    }

    private class MockContract extends ServiceContract {
        private static final long serialVersionUID = 5990290963800888327L;

        @Override
        public String getQualifiedInterfaceName() {
            return null;
        }
    }

    private class MockBinding extends Binding {
        private static final long serialVersionUID = -7088192438672216044L;

        public MockBinding() {
            super("binding", null, null);
        }
    }
}
