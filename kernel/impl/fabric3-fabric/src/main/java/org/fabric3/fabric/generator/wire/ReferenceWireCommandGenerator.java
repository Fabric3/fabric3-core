/*
* Fabric3
* Copyright (c) 2009 Metaform Systems
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

import java.util.List;

import org.osoa.sca.annotations.Property;
import org.osoa.sca.annotations.Reference;

import org.fabric3.fabric.command.AttachWireCommand;
import org.fabric3.fabric.command.ConnectionCommand;
import org.fabric3.fabric.command.DetachWireCommand;
import org.fabric3.model.type.component.Multiplicity;
import org.fabric3.spi.generator.CommandGenerator;
import org.fabric3.spi.generator.GenerationException;
import org.fabric3.spi.model.instance.LogicalBinding;
import org.fabric3.spi.model.instance.LogicalComponent;
import org.fabric3.spi.model.instance.LogicalCompositeComponent;
import org.fabric3.spi.model.instance.LogicalReference;
import org.fabric3.spi.model.instance.LogicalState;
import org.fabric3.spi.model.physical.PhysicalWireDefinition;

/**
 * Generates commands to attach reference wires to their transports for components being deployed or commands to detach reference wires for components
 * being undeployed.
 *
 * @version $Rev$ $Date$
 */
public class ReferenceWireCommandGenerator implements CommandGenerator {

    private final WireGenerator wireGenerator;
    private final int order;

    public ReferenceWireCommandGenerator(@Reference WireGenerator wireGenerator, @Property(name = "order") int order) {
        this.wireGenerator = wireGenerator;
        this.order = order;
    }

    public int getOrder() {
        return order;
    }

    @SuppressWarnings("unchecked")
    public ConnectionCommand generate(LogicalComponent<?> component, boolean incremental) throws GenerationException {
        if (component instanceof LogicalCompositeComponent) {
            return null;
        }
        ConnectionCommand command = new ConnectionCommand();

        for (LogicalReference logicalReference : component.getReferences()) {
            boolean reinjection = isReinjection(logicalReference, incremental);

            for (LogicalBinding<?> logicalBinding : logicalReference.getBindings()) {
                generateCommand(component, logicalReference, logicalBinding, command, incremental, reinjection, false);
            }
            if (logicalReference.getDefinition().getServiceContract().getCallbackContract() != null) {
                List<LogicalBinding<?>> callbackBindings = logicalReference.getCallbackBindings();
                boolean bindings = !logicalReference.getBindings().isEmpty();
                if (bindings) {
                    if (callbackBindings.size() != 1) {
                        // if the reference is explicitly bound, it must have one callback binding
                        String uri = logicalReference.getUri().toString();
                        throw new UnsupportedOperationException("The runtime requires exactly one callback binding to be " +
                                "specified on reference: " + uri);
                    }
                    LogicalBinding<?> callbackBinding = callbackBindings.get(0);
                    generateCommand(component, logicalReference, callbackBinding, command, incremental, reinjection, true);
                }
            }

        }
        if (command.getAttachCommands().isEmpty() && command.getDetachCommands().isEmpty()) {
            return null;
        }
        return command;
    }

    private void generateCommand(LogicalComponent<?> component,
                                 LogicalReference logicalReference,
                                 LogicalBinding<?> logicalBinding,
                                 ConnectionCommand command,
                                 boolean incremental,
                                 boolean reinjection,
                                 boolean callback) throws GenerationException {
        if (LogicalState.MARKED == component.getState() || LogicalState.MARKED == logicalBinding.getState()) {
            PhysicalWireDefinition wireDefinition;
            if (callback) {
                wireDefinition = wireGenerator.generateBoundCallbackReferenceWire(logicalReference, logicalBinding);
            } else {
                wireDefinition = wireGenerator.generateBoundReferenceWire(logicalReference, logicalBinding);
            }
            DetachWireCommand wireCommand = new DetachWireCommand();
            wireCommand.setPhysicalWireDefinition(wireDefinition);
            command.add(wireCommand);

        } else if (LogicalState.NEW == logicalBinding.getState() || !incremental || reinjection) {
            PhysicalWireDefinition wireDefinition;
            if (callback) {
                wireDefinition = wireGenerator.generateBoundCallbackReferenceWire(logicalReference, logicalBinding);
            } else {
                wireDefinition = wireGenerator.generateBoundReferenceWire(logicalReference, logicalBinding);
            }
            AttachWireCommand wireCommand = new AttachWireCommand();
            wireCommand.setPhysicalWireDefinition(wireDefinition);
            command.add(wireCommand);
        }

    }

    private boolean isReinjection(LogicalReference logicalReference, boolean incremental) {
        Multiplicity multiplicity = logicalReference.getDefinition().getMultiplicity();
        if (incremental && multiplicity == Multiplicity.ZERO_N || multiplicity == Multiplicity.ONE_N) {
            for (LogicalBinding<?> binding : logicalReference.getBindings()) {
                if (binding.getState() == LogicalState.NEW || binding.getState() == LogicalState.MARKED) {
                    return true;
                }
            }
        }
        return false;
    }

}
