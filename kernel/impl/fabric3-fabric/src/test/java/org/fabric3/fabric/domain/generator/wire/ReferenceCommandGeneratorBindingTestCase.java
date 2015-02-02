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

import java.net.URI;

import junit.framework.TestCase;
import org.easymock.EasyMock;
import org.fabric3.api.model.type.component.Component;
import org.fabric3.api.model.type.component.Reference;
import org.fabric3.fabric.container.command.ConnectionCommand;
import org.fabric3.spi.domain.generator.wire.WireGenerator;
import org.fabric3.spi.model.instance.LogicalBinding;
import org.fabric3.spi.model.instance.LogicalComponent;
import org.fabric3.spi.model.instance.LogicalCompositeComponent;
import org.fabric3.spi.model.instance.LogicalReference;
import org.fabric3.spi.model.instance.LogicalState;
import org.fabric3.spi.model.physical.PhysicalWireDefinition;
import org.fabric3.spi.model.type.component.CompositeImplementation;
import org.fabric3.spi.model.type.java.JavaServiceContract;

/**
 *
 */
public class ReferenceCommandGeneratorBindingTestCase extends TestCase {

    private ReferenceCommandGenerator generator;
    private WireGenerator wireGenerator;

    @SuppressWarnings({"unchecked"})
    public void testAttach() throws Exception {
        URI root = URI.create("root");
        Component<CompositeImplementation> definition = new Component<>(null);
        LogicalCompositeComponent composite = new LogicalCompositeComponent(root, definition, null);

        JavaServiceContract contract = new JavaServiceContract();

        URI sourceUri = URI.create("source");
        Component<?> sourceDefinition = new Component(null);
        LogicalComponent<?> source = new LogicalComponent(sourceUri, sourceDefinition, composite);
        Reference referenceDefinition = new Reference("reference", contract);
        LogicalReference reference = new LogicalReference(URI.create("source#reference"), referenceDefinition, source);
        source.addReference(reference);

        LogicalBinding<?> binding = new LogicalBinding(null, reference, null);
        reference.addBinding(binding);

        wireGenerator.generateBoundReference(binding);
        EasyMock.expectLastCall().andReturn(new PhysicalWireDefinition(null, null, null));

        EasyMock.replay(wireGenerator);

        ConnectionCommand command = generator.generate(source).get();

        EasyMock.verify(wireGenerator);
        assertEquals(1, command.getAttachCommands().size());
        assertEquals(0, command.getDetachCommands().size());
    }

    @SuppressWarnings({"unchecked"})
    public void testDetach() throws Exception {
        URI root = URI.create("root");
        Component<CompositeImplementation> definition = new Component<>(null);
        LogicalCompositeComponent composite = new LogicalCompositeComponent(root, definition, null);

        JavaServiceContract contract = new JavaServiceContract();

        URI sourceUri = URI.create("source");
        Component<?> sourceDefinition = new Component(null);
        LogicalComponent<?> source = new LogicalComponent(sourceUri, sourceDefinition, composite);
        Reference referenceDefinition = new Reference("reference", contract);
        LogicalReference reference = new LogicalReference(URI.create("source#reference"), referenceDefinition, source);
        source.addReference(reference);

        LogicalBinding<?> binding = new LogicalBinding(null, reference, null);
        binding.setState(LogicalState.MARKED);
        reference.addBinding(binding);

        wireGenerator.generateBoundReference(binding);
        EasyMock.expectLastCall().andReturn(new PhysicalWireDefinition(null, null, null));

        EasyMock.replay(wireGenerator);

        ConnectionCommand command = generator.generate(source).get();

        EasyMock.verify(wireGenerator);
        assertEquals(0, command.getAttachCommands().size());
        assertEquals(1, command.getDetachCommands().size());
    }

    @SuppressWarnings({"unchecked"})
    public void testReinject() throws Exception {
        URI root = URI.create("root");
        Component<CompositeImplementation> definition = new Component<>(null);
        LogicalCompositeComponent composite = new LogicalCompositeComponent(root, definition, null);

        JavaServiceContract contract = new JavaServiceContract();

        URI sourceUri = URI.create("source");
        Component<?> sourceDefinition = new Component(null);
        LogicalComponent<?> source = new LogicalComponent(sourceUri, sourceDefinition, composite);
        Reference referenceDefinition = new Reference("reference", contract);
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

        ConnectionCommand command = generator.generate(source).get();

        EasyMock.verify(wireGenerator);
        assertEquals(1, command.getAttachCommands().size());
        assertEquals(1, command.getDetachCommands().size());
    }

    @SuppressWarnings({"unchecked"})
    public void testNoGeneration() throws Exception {
        URI root = URI.create("root");
        Component<CompositeImplementation> definition = new Component<>(null);
        LogicalCompositeComponent composite = new LogicalCompositeComponent(root, definition, null);

        JavaServiceContract contract = new JavaServiceContract();

        URI sourceUri = URI.create("source");
        Component<?> sourceDefinition = new Component(null);
        LogicalComponent<?> source = new LogicalComponent(sourceUri, sourceDefinition, composite);
        Reference referenceDefinition = new Reference("reference", contract);
        LogicalReference reference = new LogicalReference(URI.create("source#reference"), referenceDefinition, source);
        source.addReference(reference);
        source.setState(LogicalState.PROVISIONED);
        LogicalBinding<?> binding = new LogicalBinding(null, reference, null);
        binding.setState(LogicalState.PROVISIONED);
        reference.addBinding(binding);

        EasyMock.replay(wireGenerator);

        assertFalse(generator.generate(source).isPresent());
        EasyMock.verify(wireGenerator);

    }

    @SuppressWarnings({"unchecked"})
    public void testCallbackGeneration() throws Exception {
        URI root = URI.create("root");
        Component<CompositeImplementation> definition = new Component<>(null);
        LogicalCompositeComponent composite = new LogicalCompositeComponent(root, definition, null);

        JavaServiceContract contract = new JavaServiceContract();
        JavaServiceContract callbackContract = new JavaServiceContract();
        contract.setCallbackContract(callbackContract);

        URI sourceUri = URI.create("source");
        Component<?> sourceDefinition = new Component(null);
        LogicalComponent<?> source = new LogicalComponent(sourceUri, sourceDefinition, composite);
        Reference referenceDefinition = new Reference("reference", contract);
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

        ConnectionCommand command = generator.generate(source).get();

        EasyMock.verify(wireGenerator);
        assertEquals(2, command.getAttachCommands().size());
        assertEquals(0, command.getDetachCommands().size());
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        wireGenerator = EasyMock.createMock(WireGenerator.class);
        generator = new ReferenceCommandGenerator(wireGenerator);
    }

}