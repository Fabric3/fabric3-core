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

import java.util.Optional;

import org.fabric3.fabric.container.command.AttachWireCommand;
import org.fabric3.fabric.container.command.ConnectionCommand;
import org.fabric3.fabric.domain.generator.CommandGenerator;
import org.fabric3.spi.domain.generator.wire.WireGenerator;
import org.fabric3.spi.model.instance.LogicalComponent;
import org.fabric3.spi.model.instance.LogicalCompositeComponent;
import org.fabric3.spi.model.instance.LogicalResourceReference;
import org.fabric3.spi.model.instance.LogicalState;
import org.fabric3.spi.model.physical.PhysicalWire;
import org.oasisopen.sca.annotation.Reference;

/**
 * Generates commands to attach a component to its resources.
 */
public class ResourceReferenceCommandGenerator implements CommandGenerator<ConnectionCommand> {
    private WireGenerator wireGenerator;

    public ResourceReferenceCommandGenerator(@Reference WireGenerator wireGenerator) {
        this.wireGenerator = wireGenerator;
    }

    public int getOrder() {
        return ATTACH;
    }

    public Optional<ConnectionCommand> generate(LogicalComponent<?> component) {
        if (component instanceof LogicalCompositeComponent || component.getResourceReferences().isEmpty() || (component.getState() != LogicalState.NEW)) {
            return Optional.empty();
        }
        ConnectionCommand command = new ConnectionCommand(component.getUri());
        for (LogicalResourceReference<?> resourceReference : component.getResourceReferences()) {
            AttachWireCommand attachWireCommand = new AttachWireCommand();
            PhysicalWire physicalWire = wireGenerator.generateResource(resourceReference);
            attachWireCommand.setPhysicalWireDefinition(physicalWire);
            command.add(attachWireCommand);
        }
        return Optional.of(command);
    }

}