/*
* Fabric3
* Copyright (c) 2009-2013 Metaform Systems
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
import javax.xml.namespace.QName;

import junit.framework.TestCase;
import org.easymock.EasyMock;

import org.fabric3.fabric.deployment.command.ConnectionCommand;
import org.fabric3.model.type.component.ComponentDefinition;
import org.fabric3.model.type.component.ComponentReference;
import org.fabric3.model.type.component.CompositeImplementation;
import org.fabric3.model.type.component.Multiplicity;
import org.fabric3.model.type.component.ServiceDefinition;
import org.fabric3.spi.generator.wire.WireGenerator;
import org.fabric3.spi.domain.LogicalComponentManager;
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
    public void testIncrementalAttach() throws Exception {
        URI root = URI.create("root");
        ComponentDefinition<CompositeImplementation> definition = new ComponentDefinition<CompositeImplementation>(null);
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
        ComponentReference referenceDefinition = new ComponentReference("reference", Multiplicity.ONE_ONE);
        referenceDefinition.setServiceContract(contract);
        LogicalReference reference = new LogicalReference(URI.create("source#reference"), referenceDefinition, source);
        source.addReference(reference);
        LogicalWire wire = new LogicalWire(composite, reference, service, DEPLOYABLE);
        composite.addWire(reference, wire);
        composite.addComponent(source);


        wireGenerator.generateWire(wire);
        EasyMock.expectLastCall().andReturn(new PhysicalWireDefinition(null, null, null));

        EasyMock.replay(lcm, wireGenerator);

        ConnectionCommand command = generator.generate(source, true);

        EasyMock.verify(lcm, wireGenerator);
        assertEquals(1, command.getAttachCommands().size());
        assertEquals(0, command.getDetachCommands().size());
    }

    @SuppressWarnings({"unchecked"})
    public void testRegeneration() throws Exception {
        URI root = URI.create("root");
        ComponentDefinition<CompositeImplementation> definition = new ComponentDefinition<CompositeImplementation>(null);
        LogicalCompositeComponent composite = new LogicalCompositeComponent(root, definition, null);

        URI targetUri = URI.create("target");
        ComponentDefinition<?> targetDefinition = new ComponentDefinition(null);
        LogicalComponent<?> target = new LogicalComponent(targetUri, targetDefinition, composite);
        target.setState(LogicalState.PROVISIONED);
        JavaServiceContract contract = new JavaServiceContract();
        ServiceDefinition serviceDefinition = new ServiceDefinition("service", contract);
        LogicalService service = new LogicalService(URI.create("target#service"), serviceDefinition, target);
        target.addService(service);
        composite.addComponent(target);

        URI sourceUri = URI.create("source");
        ComponentDefinition<?> sourceDefinition = new ComponentDefinition(null);
        LogicalComponent<?> source = new LogicalComponent(sourceUri, sourceDefinition, composite);
        source.setState(LogicalState.PROVISIONED);
        ComponentReference referenceDefinition = new ComponentReference("reference", Multiplicity.ONE_ONE);
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

        ConnectionCommand command = generator.generate(source, false);

        EasyMock.verify(lcm, wireGenerator);
        assertEquals(1, command.getAttachCommands().size());
        assertEquals(0, command.getDetachCommands().size());
    }

    @SuppressWarnings({"unchecked"})
    public void testTargetCollectDetach() throws Exception {
        URI root = URI.create("root");
        ComponentDefinition<CompositeImplementation> definition = new ComponentDefinition<CompositeImplementation>(null);
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
        ComponentReference referenceDefinition = new ComponentReference("reference", Multiplicity.ONE_ONE);
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

        ConnectionCommand command = generator.generate(source, true);

        EasyMock.verify(lcm, wireGenerator);
        assertEquals(0, command.getAttachCommands().size());
        assertEquals(1, command.getDetachCommands().size());
    }

    @SuppressWarnings({"unchecked"})
    public void testTargetCollectDetachMultiplicity1ToNReference() throws Exception {
        URI root = URI.create("root");
        ComponentDefinition<CompositeImplementation> definition = new ComponentDefinition<CompositeImplementation>(null);
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
        ComponentReference referenceDefinition = new ComponentReference("reference", Multiplicity.ONE_ONE);
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

        ConnectionCommand command = generator.generate(source, true);

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
        generator = new ReferenceCommandGenerator(wireGenerator, 0);
    }

}
