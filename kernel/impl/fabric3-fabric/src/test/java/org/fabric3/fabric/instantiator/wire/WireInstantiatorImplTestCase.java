/*
* Fabric3
* Copyright (c) 2009-2011 Metaform Systems
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
package org.fabric3.fabric.instantiator.wire;

import java.net.URI;
import java.util.List;
import java.util.Map;
import javax.xml.namespace.QName;

import junit.framework.TestCase;
import org.easymock.EasyMock;

import org.fabric3.fabric.instantiator.AmbiguousService;
import org.fabric3.fabric.instantiator.InstantiationContext;
import org.fabric3.fabric.instantiator.ServiceNotFound;
import org.fabric3.model.type.component.BindingDefinition;
import org.fabric3.model.type.component.ComponentDefinition;
import org.fabric3.model.type.component.ComponentReference;
import org.fabric3.model.type.component.ComponentService;
import org.fabric3.model.type.component.Composite;
import org.fabric3.model.type.component.CompositeImplementation;
import org.fabric3.model.type.component.Multiplicity;
import org.fabric3.model.type.component.Target;
import org.fabric3.model.type.component.WireDefinition;
import org.fabric3.model.type.contract.ServiceContract;
import org.fabric3.spi.contract.ContractMatcher;
import org.fabric3.spi.contract.MatchResult;
import org.fabric3.spi.model.instance.LogicalBinding;
import org.fabric3.spi.model.instance.LogicalComponent;
import org.fabric3.spi.model.instance.LogicalCompositeComponent;
import org.fabric3.spi.model.instance.LogicalReference;
import org.fabric3.spi.model.instance.LogicalService;
import org.fabric3.spi.model.instance.LogicalWire;

/**
 * @version $Rev$ $Date$
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
        MatchResult result = new MatchResult(false);
        EasyMock.expect(matcher.isAssignableFrom(EasyMock.isA(ServiceContract.class),
                                                 EasyMock.isA(ServiceContract.class),
                                                 EasyMock.anyBoolean())).andReturn(result);
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
        ComponentDefinition<CompositeImplementation> definition = new ComponentDefinition<CompositeImplementation>("component", null);
        LogicalCompositeComponent logicalComposite = new LogicalCompositeComponent(URI.create("composite"), definition, null);
        LogicalComponent<?> logicalComponent = new LogicalComponent(URI.create("composite/component"), definition, logicalComposite);
        logicalComposite.addComponent(logicalComponent);

        createLogicalReference(logicalComponent, serviceTarget);

        LogicalService logicalService = createLogicalService("service", logicalComponent);
        LogicalBinding<MockBinding> logicalBinding = new LogicalBinding<MockBinding>(new MockBinding(), logicalService);
        logicalService.addBinding(logicalBinding);
        logicalComponent.addService(logicalService);

        return logicalComponent;
    }

    @SuppressWarnings({"unchecked"})
    private LogicalComponent<?> createLogicalComponent() {
        ComponentDefinition<CompositeImplementation> definition = new ComponentDefinition<CompositeImplementation>("component", null);
        LogicalCompositeComponent logicalComposite = new LogicalCompositeComponent(URI.create("composite"), definition, null);
        LogicalComponent<?> logicalComponent = new LogicalComponent(URI.create("composite/component"), definition, logicalComposite);
        logicalComposite.addComponent(logicalComponent);

        Target serviceTarget = new Target("component");
        createLogicalReference(logicalComponent, serviceTarget);

        createLogicalService("service", logicalComponent);

        return logicalComponent;
    }

    private LogicalService createLogicalService(String name, LogicalComponent<?> logicalComponent) {
        ComponentService service = new ComponentService(name);
        logicalComponent.getDefinition().add(service);

        URI serviceUri = URI.create("composite/component#" + name);
        LogicalService logicalService = new LogicalService(serviceUri, service, logicalComponent);
        logicalService.setServiceContract(new MockContract());
        logicalComponent.addService(logicalService);
        return logicalService;
    }

    private LogicalReference createLogicalReference(LogicalComponent<?> logicalComponent, Target target) {
        ComponentReference reference = new ComponentReference("reference", Multiplicity.ONE_ONE);
        reference.addTarget(target);
        logicalComponent.getDefinition().add(reference);

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
        ComponentDefinition<CompositeImplementation> definition = new ComponentDefinition<CompositeImplementation>("composite", implementation);
        LogicalCompositeComponent logicalComposite = new LogicalCompositeComponent(URI.create("composite"), definition, null);
        for (ComponentDefinition component : composite.getComponents().values()) {
            String nameStr = composite.getName().getLocalPart() + "/" + component.getName();
            URI uri = URI.create(nameStr);
            LogicalComponent logicalComponent = new LogicalComponent(uri, component, logicalComposite);
            logicalComposite.addComponent(logicalComponent);
            Map<String, ComponentReference> references = component.getReferences();
            for (ComponentReference reference : references.values()) {
                URI referenceUri = URI.create(nameStr + "#" + reference.getName());
                LogicalReference logicalReference = new LogicalReference(referenceUri, reference, logicalComponent);
                logicalReference.setServiceContract(new MockContract());
                logicalComponent.addReference(logicalReference);
            }
            Map<String, ComponentService> services = component.getServices();
            for (ComponentService service : services.values()) {
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

        ComponentDefinition fooComponent = new ComponentDefinition("foo");
        ComponentReference reference = new ComponentReference("reference", Multiplicity.ONE_ONE);
        fooComponent.add(reference);
        composite.add(fooComponent);

        ComponentDefinition barComponent = new ComponentDefinition("bar");
        ComponentService service = new ComponentService("reference");
        barComponent.add(service);
        composite.add(barComponent);

        WireDefinition wireDefinition = createWire();
        composite.add(wireDefinition);
        return composite;
    }

    private WireDefinition createWire() {
        Target referenceTarget = new Target("foo");
        Target serviceTarget = new Target("bar");
        return new WireDefinition(referenceTarget, serviceTarget, false);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        matcher = EasyMock.createMock(ContractMatcher.class);
        MatchResult result = new MatchResult(true);
        EasyMock.expect(matcher.isAssignableFrom(EasyMock.isA(ServiceContract.class),
                                                 EasyMock.isA(ServiceContract.class),
                                                 EasyMock.anyBoolean())).andReturn(result);

        context = new InstantiationContext();
    }

    private class MockContract extends ServiceContract {
        private static final long serialVersionUID = 5990290963800888327L;

        @Override
        public String getQualifiedInterfaceName() {
            return null;
        }
    }

    private class MockBinding extends BindingDefinition {
        private static final long serialVersionUID = -7088192438672216044L;

        public MockBinding() {
            super("binding", null, null);
        }
    }
}
