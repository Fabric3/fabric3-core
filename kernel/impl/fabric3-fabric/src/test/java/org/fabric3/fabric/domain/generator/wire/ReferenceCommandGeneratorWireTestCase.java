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

import junit.framework.TestCase;
import org.easymock.EasyMock;
import org.fabric3.api.model.type.component.ComponentDefinition;
import org.fabric3.spi.model.type.component.CompositeImplementation;
import org.fabric3.api.model.type.component.Multiplicity;
import org.fabric3.api.model.type.component.ReferenceDefinition;
import org.fabric3.api.model.type.component.ServiceDefinition;
import org.fabric3.fabric.container.command.ConnectionCommand;
import org.fabric3.spi.domain.LogicalComponentManager;
import org.fabric3.spi.domain.generator.wire.WireGenerator;
import org.fabric3.spi.model.instance.LogicalComponent;
import org.fabric3.spi.model.instance.LogicalCompositeComponent;
import org.fabric3.spi.model.instance.LogicalReference;
import org.fabric3.spi.model.instance.LogicalService;
import org.fabric3.spi.model.instance.LogicalState;
import org.fabric3.spi.model.instance.LogicalWire;
import org.fabric3.spi.model.physical.PhysicalWireDefinition;
import org.fabric3.spi.model.type.java.JavaServiceContract;

/**
 *
 */
public class ReferenceCommandGeneratorWireTestCase extends TestCase {
    private static final QName DEPLOYABLE = new QName("foo", "bar");
    private ReferenceCommandGenerator generator;
    private WireGenerator wireGenerator;
    private LogicalComponentManager lcm;

    @SuppressWarnings({"unchecked"})
    public void testAttach() throws Exception {
        URI root = URI.create("root");
        ComponentDefinition<CompositeImplementation> definition = new ComponentDefinition<>(null);
        LogicalCompositeComponent composite = new LogicalCompositeComponent(root, definition, null);

        URI targetUri = URI.create("target");
        ComponentDefinition<?> targetDefinition = new ComponentDefinition(null);
        LogicalComponent<?> target = new LogicalComponent(targetUri, targetDefinition, composite);
        JavaServiceContract contract = new JavaServiceContract();
        ServiceDefinition serviceDefinition = new ServiceDefinition("service", contract);
        LogicalService service = new LogicalService(URI.create("target#service"), serviceDefinition, target);
        target.addService(service);
        composite.addComponent(target);

        URI sourceUri = URI.create("source");
        ComponentDefinition<?> sourceDefinition = new ComponentDefinition(null);
        LogicalComponent<?> source = new LogicalComponent(sourceUri, sourceDefinition, composite);
        ReferenceDefinition referenceDefinition = new ReferenceDefinition("reference", Multiplicity.ONE_ONE);
        referenceDefinition.setServiceContract(contract);
        LogicalReference reference = new LogicalReference(URI.create("source#reference"), referenceDefinition, source);
        source.addReference(reference);
        LogicalWire wire = new LogicalWire(composite, reference, service, DEPLOYABLE);
        composite.addWire(reference, wire);
        composite.addComponent(source);


        wireGenerator.generateWire(wire);
        EasyMock.expectLastCall().andReturn(new PhysicalWireDefinition(null, null, null));

        EasyMock.replay(lcm, wireGenerator);

        ConnectionCommand command = generator.generate(source);

        EasyMock.verify(lcm, wireGenerator);
        assertEquals(1, command.getAttachCommands().size());
        assertEquals(0, command.getDetachCommands().size());
    }

    @SuppressWarnings({"unchecked"})
    public void testTargetCollectDetach() throws Exception {
        URI root = URI.create("root");
        ComponentDefinition<CompositeImplementation> definition = new ComponentDefinition<>(null);
        LogicalCompositeComponent composite = new LogicalCompositeComponent(root, definition, null);

        URI targetUri = URI.create("target");
        ComponentDefinition<?> targetDefinition = new ComponentDefinition(null);
        LogicalComponent<?> target = new LogicalComponent(targetUri, targetDefinition, composite);
        JavaServiceContract contract = new JavaServiceContract();
        ServiceDefinition serviceDefinition = new ServiceDefinition("service", contract);
        LogicalService service = new LogicalService(URI.create("target#service"), serviceDefinition, target);
        target.addService(service);
        composite.addComponent(target);

        // mark target to be collected
        target.setState(LogicalState.MARKED);

        URI sourceUri = URI.create("source");
        ComponentDefinition<?> sourceDefinition = new ComponentDefinition(null);
        LogicalComponent<?> source = new LogicalComponent(sourceUri, sourceDefinition, composite);
        ReferenceDefinition referenceDefinition = new ReferenceDefinition("reference", Multiplicity.ONE_ONE);
        referenceDefinition.setServiceContract(contract);
        LogicalReference reference = new LogicalReference(URI.create("source#reference"), referenceDefinition, source);
        source.addReference(reference);

        LogicalWire wire = new LogicalWire(composite, reference, service, DEPLOYABLE);
        wire.setState(LogicalState.PROVISIONED);
        composite.addWire(reference, wire);
        composite.addComponent(source);

        wireGenerator.generateWire(wire);
        EasyMock.expectLastCall().andReturn(new PhysicalWireDefinition(null, null, null));

        EasyMock.replay(lcm, wireGenerator);

        ConnectionCommand command = generator.generate(source);

        EasyMock.verify(lcm, wireGenerator);
        assertEquals(0, command.getAttachCommands().size());
        assertEquals(1, command.getDetachCommands().size());
    }

    @SuppressWarnings({"unchecked"})
    public void testTargetCollectDetachMultiplicity1ToNReference() throws Exception {
        URI root = URI.create("root");
        ComponentDefinition<CompositeImplementation> definition = new ComponentDefinition<>(null);
        LogicalCompositeComponent composite = new LogicalCompositeComponent(root, definition, null);

        JavaServiceContract contract = new JavaServiceContract();

        URI targetUri2 = URI.create("target2");
        ComponentDefinition<?> targetDefinition2 = new ComponentDefinition(null);
        LogicalComponent<?> target2 = new LogicalComponent(targetUri2, targetDefinition2, composite);
        ServiceDefinition serviceDefinition2 = new ServiceDefinition("service", contract);
        LogicalService service2 = new LogicalService(URI.create("source#service"), serviceDefinition2, target2);
        target2.addService(service2);
        composite.addComponent(target2);

        URI targetUri = URI.create("target");
        ComponentDefinition<?> targetDefinition = new ComponentDefinition(null);
        LogicalComponent<?> target = new LogicalComponent(targetUri, targetDefinition, composite);
        ServiceDefinition serviceDefinition = new ServiceDefinition("service", contract);
        LogicalService service = new LogicalService(URI.create("source#service"), serviceDefinition, target);
        target.addService(service);
        composite.addComponent(target);

        // mark target to be collected
        target.setState(LogicalState.MARKED);

        URI sourceUri = URI.create("source");
        ComponentDefinition<?> sourceDefinition = new ComponentDefinition(null);
        LogicalComponent<?> source = new LogicalComponent(sourceUri, sourceDefinition, composite);
        ReferenceDefinition referenceDefinition = new ReferenceDefinition("reference", Multiplicity.ONE_ONE);
        referenceDefinition.setServiceContract(contract);
        referenceDefinition.setMultiplicity(Multiplicity.ONE_N);
        LogicalReference reference = new LogicalReference(URI.create("source#reference"), referenceDefinition, source);
        source.addReference(reference);

        LogicalWire wire = new LogicalWire(composite, reference, service, DEPLOYABLE);
        wire.setState(LogicalState.PROVISIONED);
        composite.addWire(reference, wire);
        LogicalWire wire2 = new LogicalWire(composite, reference, service2, DEPLOYABLE);
        wire2.setState(LogicalState.PROVISIONED);
        composite.addWire(reference, wire2);
        composite.addComponent(source);

        PhysicalWireDefinition wireDefinition = new PhysicalWireDefinition(null, null, null);
        EasyMock.expect(wireGenerator.generateWire(wire)).andReturn(wireDefinition);
        EasyMock.expect(wireGenerator.generateWire(wire2)).andReturn(wireDefinition);

        EasyMock.replay(lcm, wireGenerator);

        ConnectionCommand command = generator.generate(source);

        EasyMock.verify(lcm, wireGenerator);
        // The generator should create:
        // a. One detach command for target as it was marked
        // b. One attach command for target2. Target2 is still active and the reference needs to be reinjected as it is a multiplicity
        assertEquals(1, command.getAttachCommands().size());
        assertEquals(1, command.getDetachCommands().size());
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        wireGenerator = EasyMock.createMock(WireGenerator.class);
        lcm = EasyMock.createMock(LogicalComponentManager.class);
        generator = new ReferenceCommandGenerator(wireGenerator);
    }

}
