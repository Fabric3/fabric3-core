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
package org.fabric3.fabric.generator.wire;

import java.net.URI;

import junit.framework.TestCase;
import org.easymock.EasyMock;

import org.fabric3.fabric.command.ConnectionCommand;
import org.fabric3.model.type.component.ComponentDefinition;
import org.fabric3.model.type.component.CompositeImplementation;
import org.fabric3.model.type.component.ReferenceDefinition;
import org.fabric3.spi.model.instance.LogicalBinding;
import org.fabric3.spi.model.instance.LogicalComponent;
import org.fabric3.spi.model.instance.LogicalCompositeComponent;
import org.fabric3.spi.model.instance.LogicalReference;
import org.fabric3.spi.model.instance.LogicalState;
import org.fabric3.spi.model.physical.PhysicalWireDefinition;
import org.fabric3.spi.model.type.java.JavaServiceContract;

/**
 * @version $Rev$ $Date$
 */
public class ReferenceCommandGeneratorBindingTestCase extends TestCase {

    private ReferenceCommandGenerator generator;
    private WireGenerator wireGenerator;

    @SuppressWarnings({"unchecked"})
    public void testIncrementalAttach() throws Exception {
        URI root = URI.create("root");
        ComponentDefinition<CompositeImplementation> definition = new ComponentDefinition<CompositeImplementation>(null);
        LogicalCompositeComponent composite = new LogicalCompositeComponent(root, definition, null);

        JavaServiceContract contract = new JavaServiceContract();

        URI sourceUri = URI.create("source");
        ComponentDefinition<?> sourceDefinition = new ComponentDefinition(null);
        LogicalComponent<?> source = new LogicalComponent(sourceUri, sourceDefinition, composite);
        ReferenceDefinition referenceDefinition = new ReferenceDefinition("reference", contract);
        LogicalReference reference = new LogicalReference(URI.create("source#reference"), referenceDefinition, source);
        source.addReference(reference);

        LogicalBinding<?> binding = new LogicalBinding(null, reference, null);
        reference.addBinding(binding);

        wireGenerator.generateBoundReference(binding);
        EasyMock.expectLastCall().andReturn(new PhysicalWireDefinition(null, null, null));

        EasyMock.replay(wireGenerator);

        ConnectionCommand command = generator.generate(source, true);

        EasyMock.verify(wireGenerator);
        assertEquals(1, command.getAttachCommands().size());
        assertEquals(0, command.getDetachCommands().size());
    }

    @SuppressWarnings({"unchecked"})
    public void testDetach() throws Exception {
        URI root = URI.create("root");
        ComponentDefinition<CompositeImplementation> definition = new ComponentDefinition<CompositeImplementation>(null);
        LogicalCompositeComponent composite = new LogicalCompositeComponent(root, definition, null);

        JavaServiceContract contract = new JavaServiceContract();

        URI sourceUri = URI.create("source");
        ComponentDefinition<?> sourceDefinition = new ComponentDefinition(null);
        LogicalComponent<?> source = new LogicalComponent(sourceUri, sourceDefinition, composite);
        ReferenceDefinition referenceDefinition = new ReferenceDefinition("reference", contract);
        LogicalReference reference = new LogicalReference(URI.create("source#reference"), referenceDefinition, source);
        source.addReference(reference);

        LogicalBinding<?> binding = new LogicalBinding(null, reference, null);
        binding.setState(LogicalState.MARKED);
        reference.addBinding(binding);

        wireGenerator.generateBoundReference(binding);
        EasyMock.expectLastCall().andReturn(new PhysicalWireDefinition(null, null, null));

        EasyMock.replay(wireGenerator);

        ConnectionCommand command = generator.generate(source, true);

        EasyMock.verify(wireGenerator);
        assertEquals(0, command.getAttachCommands().size());
        assertEquals(1, command.getDetachCommands().size());
    }

    @SuppressWarnings({"unchecked"})
    public void testReinject() throws Exception {
        URI root = URI.create("root");
        ComponentDefinition<CompositeImplementation> definition = new ComponentDefinition<CompositeImplementation>(null);
        LogicalCompositeComponent composite = new LogicalCompositeComponent(root, definition, null);

        JavaServiceContract contract = new JavaServiceContract();

        URI sourceUri = URI.create("source");
        ComponentDefinition<?> sourceDefinition = new ComponentDefinition(null);
        LogicalComponent<?> source = new LogicalComponent(sourceUri, sourceDefinition, composite);
        ReferenceDefinition referenceDefinition = new ReferenceDefinition("reference", contract);
        LogicalReference reference = new LogicalReference(URI.create("source#reference"), referenceDefinition, source);
        source.addReference(reference);

        LogicalBinding<?> binding = new LogicalBinding(null, reference, null);
        reference.addBinding(binding);


        LogicalBinding<?> markedBinding = new LogicalBinding(null, reference, null);
        markedBinding.setState(LogicalState.MARKED);
        reference.addBinding(markedBinding);

        wireGenerator.generateBoundReference(binding);
        EasyMock.expectLastCall().andReturn(new PhysicalWireDefinition(null, null, null));
        wireGenerator.generateBoundReference(markedBinding);
        EasyMock.expectLastCall().andReturn(new PhysicalWireDefinition(null, null, null));

        EasyMock.replay(wireGenerator);

        ConnectionCommand command = generator.generate(source, true);

        EasyMock.verify(wireGenerator);
        assertEquals(1, command.getAttachCommands().size());
        assertEquals(1, command.getDetachCommands().size());
    }

    @SuppressWarnings({"unchecked"})
    public void testNoGeneration() throws Exception {
        URI root = URI.create("root");
        ComponentDefinition<CompositeImplementation> definition = new ComponentDefinition<CompositeImplementation>(null);
        LogicalCompositeComponent composite = new LogicalCompositeComponent(root, definition, null);

        JavaServiceContract contract = new JavaServiceContract();

        URI sourceUri = URI.create("source");
        ComponentDefinition<?> sourceDefinition = new ComponentDefinition(null);
        LogicalComponent<?> source = new LogicalComponent(sourceUri, sourceDefinition, composite);
        ReferenceDefinition referenceDefinition = new ReferenceDefinition("reference", contract);
        LogicalReference reference = new LogicalReference(URI.create("source#reference"), referenceDefinition, source);
        source.addReference(reference);
        source.setState(LogicalState.PROVISIONED);
        LogicalBinding<?> binding = new LogicalBinding(null, reference, null);
        binding.setState(LogicalState.PROVISIONED);
        reference.addBinding(binding);

        EasyMock.replay(wireGenerator);

        ConnectionCommand command = generator.generate(source, true);
        assertNull(command);
        EasyMock.verify(wireGenerator);

    }

    @SuppressWarnings({"unchecked"})
    public void testRegeneration() throws Exception {
        URI root = URI.create("root");
        ComponentDefinition<CompositeImplementation> definition = new ComponentDefinition<CompositeImplementation>(null);
        LogicalCompositeComponent composite = new LogicalCompositeComponent(root, definition, null);

        JavaServiceContract contract = new JavaServiceContract();

        URI sourceUri = URI.create("source");
        ComponentDefinition<?> sourceDefinition = new ComponentDefinition(null);
        LogicalComponent<?> source = new LogicalComponent(sourceUri, sourceDefinition, composite);
        ReferenceDefinition referenceDefinition = new ReferenceDefinition("reference", contract);
        LogicalReference reference = new LogicalReference(URI.create("source#reference"), referenceDefinition, source);
        source.addReference(reference);
        source.setState(LogicalState.PROVISIONED);
        LogicalBinding<?> binding = new LogicalBinding(null, reference, null);
        binding.setState(LogicalState.PROVISIONED);
        reference.addBinding(binding);

        wireGenerator.generateBoundReference(binding);
        EasyMock.expectLastCall().andReturn(new PhysicalWireDefinition(null, null, null));
        EasyMock.replay(wireGenerator);

        ConnectionCommand command = generator.generate(source, false);
        assertEquals(1, command.getAttachCommands().size());
        EasyMock.verify(wireGenerator);

    }

    @SuppressWarnings({"unchecked"})
    public void testCallbackGeneration() throws Exception {
        URI root = URI.create("root");
        ComponentDefinition<CompositeImplementation> definition = new ComponentDefinition<CompositeImplementation>(null);
        LogicalCompositeComponent composite = new LogicalCompositeComponent(root, definition, null);

        JavaServiceContract contract = new JavaServiceContract();
        JavaServiceContract callbackContract = new JavaServiceContract();
        contract.setCallbackContract(callbackContract);

        URI sourceUri = URI.create("source");
        ComponentDefinition<?> sourceDefinition = new ComponentDefinition(null);
        LogicalComponent<?> source = new LogicalComponent(sourceUri, sourceDefinition, composite);
        ReferenceDefinition referenceDefinition = new ReferenceDefinition("reference", contract);
        LogicalReference reference = new LogicalReference(URI.create("source#reference"), referenceDefinition, source);
        source.addReference(reference);

        LogicalBinding<?> binding = new LogicalBinding(null, reference, null);
        reference.addBinding(binding);
        reference.addCallbackBinding(binding);
        
        wireGenerator.generateBoundReference(binding);
        EasyMock.expectLastCall().andReturn(new PhysicalWireDefinition(null, null, null));
        wireGenerator.generateBoundReferenceCallback(binding);
        EasyMock.expectLastCall().andReturn(new PhysicalWireDefinition(null, null, null));

        EasyMock.replay(wireGenerator);

        ConnectionCommand command = generator.generate(source, true);

        EasyMock.verify(wireGenerator);
        assertEquals(2, command.getAttachCommands().size());
        assertEquals(0, command.getDetachCommands().size());
    }


    @Override
    protected void setUp() throws Exception {
        super.setUp();
        wireGenerator = EasyMock.createMock(WireGenerator.class);
        generator = new ReferenceCommandGenerator(wireGenerator, 0);
    }

}