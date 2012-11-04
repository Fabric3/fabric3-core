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
package org.fabric3.spi.model.instance;

import java.net.URI;
import java.util.List;
import javax.xml.namespace.QName;

import junit.framework.TestCase;

import org.fabric3.model.type.component.BindingDefinition;
import org.fabric3.model.type.component.ChannelDefinition;
import org.fabric3.model.type.component.ComponentDefinition;
import org.fabric3.model.type.component.ConsumerDefinition;
import org.fabric3.model.type.component.ProducerDefinition;
import org.fabric3.model.type.component.ReferenceDefinition;
import org.fabric3.model.type.component.ResourceDefinition;
import org.fabric3.model.type.component.ServiceDefinition;
import org.fabric3.model.type.contract.ServiceContract;

/**
 *
 */
public class CopyUtilTestCase extends TestCase {

    public void testCopy() throws Exception {
        LogicalCompositeComponent originalParent = createComposite("parent", null);
        LogicalCompositeComponent originalChildComposite = createComposite("childComposite", originalParent);
        LogicalComponent originalComponent = createComponent("component", originalChildComposite);
        LogicalService originalService = createService("#service", originalComponent);
        LogicalReference originalReference = createReference("#reference", originalComponent);
        LogicalWire originalWire = createWire(originalService, originalReference);
        LogicalChannel originalChannel = createChannel("channel", originalChildComposite);
        LogicalResource<?> originalResource = createResource(originalChildComposite);
        LogicalProducer originalProducer = createProducer("#producer", originalComponent);
        LogicalConsumer originalConsumer = createConsumer("#consumer", originalComponent);

        LogicalCompositeComponent copy = CopyUtil.copy(originalParent);

        assertNotSame(copy, originalParent);

        assertEquals(1, copy.getComponents().size());
        LogicalCompositeComponent childComposite = (LogicalCompositeComponent) copy.getComponent(originalChildComposite.getUri());
        assertNotSame(originalChildComposite, childComposite);

        assertEquals(1, childComposite.getChannels().size());
        LogicalChannel channel = childComposite.getChannel(originalChannel.getUri());
        assertNotSame(originalChannel, channel);
        assertEquals(1, channel.getBindings().size());
        assertNotSame(originalChannel.getBindings().get(0), channel.getBindings().get(0));
        assertEquals("zone1", channel.getZone());
        assertEquals(LogicalState.PROVISIONED, channel.getState());
        assertNotSame(originalResource, childComposite.getResources().iterator().next());


        assertEquals(1, childComposite.getComponents().size());
        LogicalComponent component = childComposite.getComponent(originalComponent.getUri());
        assertNotSame(originalComponent, component);
        assertEquals(LogicalState.PROVISIONED, component.getState());
        assertEquals("zone1", component.getZone());

        assertEquals(1, component.getServices().size());
        LogicalService service = component.getService("service");
        assertNotSame(originalService, service);
        assertNotSame(originalService.getLeafComponent(), service.getLeafComponent());
        assertNotSame(originalService.getLeafService(), service.getLeafService());

        assertEquals(1, component.getReferences().size());
        LogicalReference reference = component.getReference("reference");
        assertNotSame(originalReference, reference);
        assertNotSame(originalReference.getLeafReference(), reference.getLeafReference());

        assertEquals(1, component.getProducers().size());
        LogicalProducer producer = component.getProducer("producer");
        assertNotSame(originalProducer, producer);
        assertEquals(1, producer.getTargets().size());
        assertEquals(URI.create("target"), producer.getTargets().get(0));

        assertEquals(1, component.getConsumers().size());
        LogicalConsumer consumer = component.getConsumer("consumer");
        assertNotSame(originalConsumer, consumer);
        assertEquals(1, consumer.getSources().size());
        assertEquals(URI.create("source"), consumer.getSources().get(0));


        List<LogicalWire> wires = childComposite.getWires(reference);
        assertEquals(1, wires.size());
        LogicalWire wire = wires.get(0);
        LogicalBinding referenceBinding = reference.getBindings().get(0);
        LogicalBinding serviceBinding = service.getBindings().get(0);
        assertNotSame(originalWire, wire);
        assertSame(reference, wire.getSource());
        assertSame(service, wire.getTarget());
        assertSame(referenceBinding, wire.getSourceBinding());
        assertSame(serviceBinding, wire.getTargetBinding());

    }

    private LogicalWire createWire(LogicalService service, LogicalReference reference) {
        LogicalCompositeComponent parent = service.getParent().getParent();
        QName deployable = new QName("deployable");
        LogicalWire wire = new LogicalWire(parent, reference, service, deployable);
        wire.setSourceBinding(reference.getBindings().get(0));
        wire.setTargetBinding(service.getBindings().get(0));
        parent.addWire(reference, wire);
        return wire;
    }

    private LogicalService createService(String name, LogicalComponent parent) {
        URI uri = URI.create(name);
        ServiceDefinition definition = new ServiceDefinition(name);
        LogicalService service = new LogicalService(uri, definition, parent);
        createBinding(service);
        parent.addService(service);
        return service;
    }

    private LogicalReference createReference(String name, LogicalComponent parent) {
        URI uri = URI.create(name);
        ReferenceDefinition definition = new ReferenceDefinition(name, (ServiceContract) null);
        LogicalReference reference = new LogicalReference(uri, definition, parent);
        createBinding(reference);
        parent.addReference(reference);
        return reference;
    }

    private LogicalProducer createProducer(String name, LogicalComponent parent) {
        URI uri = URI.create(name);
        ProducerDefinition definition = new ProducerDefinition(name);
        LogicalProducer producer = new LogicalProducer(uri, definition, parent);
        producer.addTarget(URI.create("target"));
        parent.addProducer(producer);
        return producer;
    }

    private LogicalConsumer createConsumer(String name, LogicalComponent parent) {
        URI uri = URI.create(name);
        ConsumerDefinition definition = new ConsumerDefinition(name);
        LogicalConsumer consumer = new LogicalConsumer(uri, definition, parent);
        consumer.addSource(URI.create("source"));
        parent.addConsumer(consumer);
        return consumer;
    }

    private LogicalBinding createBinding(Bindable parent) {
        MockBinding definition = new MockBinding();
        LogicalBinding<MockBinding> binding = new LogicalBinding<MockBinding>(definition, parent);
        parent.addBinding(binding);
        return binding;
    }

    @SuppressWarnings({"unchecked"})
    private LogicalChannel createChannel(String name, LogicalCompositeComponent parent) {
        URI uri = URI.create(name);
        ChannelDefinition definition = new ChannelDefinition(name, URI.create("contribution"));
        LogicalChannel channel = new LogicalChannel(uri, definition, parent);
        channel.setState(LogicalState.PROVISIONED);
        channel.setZone("zone1");
        createBinding(channel);
        parent.addChannel(channel);
        return channel;
    }

    @SuppressWarnings({"unchecked"})
    private LogicalComponent createComponent(String name, LogicalCompositeComponent parent) {
        URI uri = URI.create(name);
        ComponentDefinition definition = new ComponentDefinition(name, null);
        LogicalComponent component = new LogicalComponent(uri, definition, parent);
        component.setState(LogicalState.PROVISIONED);
        component.setZone("zone1");
        parent.addComponent(component);
        return component;
    }

    @SuppressWarnings({"unchecked"})
    private LogicalCompositeComponent createComposite(String name, LogicalCompositeComponent parent) {
        URI uri = URI.create(name);
        ComponentDefinition definition = new ComponentDefinition(name, null);
        LogicalCompositeComponent composite = new LogicalCompositeComponent(uri, definition, parent);
        if (parent != null) {
            parent.addComponent(composite);
        }
        return composite;
    }

    @SuppressWarnings({"unchecked"})
    private LogicalResource createResource(LogicalCompositeComponent parent) {
        MockResourceDefinition definition = new MockResourceDefinition();
        LogicalResource resource = new LogicalResource(definition, parent);
        parent.addResource(resource);
        return resource;
    }

    private class MockBinding extends BindingDefinition {
        private static final long serialVersionUID = 2964005114618102630L;

        public MockBinding() {
            super("name", null, null);
        }
    }

    private class MockResourceDefinition extends ResourceDefinition {
        private static final long serialVersionUID = -8809333483461908752L;

    }
}
