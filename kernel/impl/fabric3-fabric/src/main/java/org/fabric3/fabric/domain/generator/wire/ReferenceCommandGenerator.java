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

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.fabric3.api.host.Fabric3Exception;
import org.fabric3.api.model.type.component.Binding;
import org.fabric3.api.model.type.component.Multiplicity;
import org.fabric3.fabric.container.command.AttachWireCommand;
import org.fabric3.fabric.container.command.ConnectionCommand;
import org.fabric3.fabric.container.command.DetachWireCommand;
import org.fabric3.fabric.domain.generator.CommandGenerator;
import org.fabric3.spi.domain.generator.CallbackBindingGenerator;
import org.fabric3.spi.model.instance.LogicalBinding;
import org.fabric3.spi.model.instance.LogicalComponent;
import org.fabric3.spi.model.instance.LogicalCompositeComponent;
import org.fabric3.spi.model.instance.LogicalReference;
import org.fabric3.spi.model.instance.LogicalService;
import org.fabric3.spi.model.instance.LogicalState;
import org.fabric3.spi.model.instance.LogicalWire;
import org.fabric3.spi.model.physical.PhysicalWire;
import org.oasisopen.sca.annotation.Reference;

/**
 * Generates a command to bind or attach a wire to a reference.
 */
public class ReferenceCommandGenerator implements CommandGenerator<ConnectionCommand> {
    private WireGenerator wireGenerator;
    private Map<Class<?>, CallbackBindingGenerator> generators = Collections.emptyMap();

    /**
     * Constructor.
     *
     * @param wireGenerator the physical wire generator
     */
    public ReferenceCommandGenerator(@Reference WireGenerator wireGenerator) {
        this.wireGenerator = wireGenerator;
    }

    @Reference(required = false)
    public void setCallbackBindingGenerators(Map<Class<?>, CallbackBindingGenerator> generators) {
        this.generators = generators;
    }

    public int getOrder() {
        return ATTACH;
    }

    public Optional<ConnectionCommand> generate(LogicalComponent<?> component) {
        if (component instanceof LogicalCompositeComponent) {
            return Optional.empty();
        }
        ConnectionCommand command = new ConnectionCommand(component.getUri());

        for (LogicalReference reference : component.getReferences()) {
            if (!reference.getWires().isEmpty()) {
                generateWires(reference, command);
            } else {
                generateBindings(reference, component, command);
            }

        }
        if (command.getAttachCommands().isEmpty() && command.getDetachCommands().isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(command);
    }

    private void generateBindings(LogicalReference reference, LogicalComponent<?> component, ConnectionCommand command) {
        boolean reinjection = isBoundReinjection(reference);

        for (LogicalBinding<?> logicalBinding : reference.getBindings()) {
            generateBinding(component, logicalBinding, command, reinjection, false);
        }
        if (reference.getServiceContract().getCallbackContract() != null) {
            boolean bindings = reference.isBound();
            if (bindings) {
                List<LogicalBinding<?>> callbackBindings = reference.getCallbackBindings();
                if (callbackBindings.isEmpty()) {
                    // generate callback bindings as some transports do not require an explicit callback binding configuration on the reference
                    generateCallbackBindings(reference);
                }
                if (callbackBindings.size() != 1) {
                    // if the reference is explicitly bound, it must have one callback binding
                    String uri = reference.getUri().toString();
                    throw new UnsupportedOperationException("The runtime requires exactly one callback binding to be specified on reference: " + uri);
                }
                LogicalBinding<?> callbackBinding = callbackBindings.get(0);
                generateBinding(component, callbackBinding, command, reinjection, true);
            }
        }
    }

    private void generateBinding(LogicalComponent<?> component,
                                 LogicalBinding<?> logicalBinding,
                                 ConnectionCommand command,
                                 boolean reinjection,
                                 boolean callback) {
        if (LogicalState.MARKED == component.getState() || LogicalState.MARKED == logicalBinding.getState()) {
            PhysicalWire physicalWire;
            if (callback) {
                physicalWire = wireGenerator.generateReferenceCallback(logicalBinding);
            } else {
                physicalWire = wireGenerator.generateReference(logicalBinding);
            }
            DetachWireCommand wireCommand = new DetachWireCommand();
            wireCommand.setPhysicalWireDefinition(physicalWire);
            command.add(wireCommand);

        } else if (LogicalState.NEW == logicalBinding.getState() || reinjection) {
            PhysicalWire physicalWire;
            if (callback) {
                physicalWire = wireGenerator.generateReferenceCallback(logicalBinding);
            } else {
                physicalWire = wireGenerator.generateReference(logicalBinding);
            }
            AttachWireCommand wireCommand = new AttachWireCommand();
            wireCommand.setPhysicalWireDefinition(physicalWire);
            command.add(wireCommand);
        }

    }

    private void generateWires(LogicalReference reference, ConnectionCommand command) {

        // if the reference is a multiplicity and one of the wires has changed, all of the wires need to be regenerated for reinjection
        boolean reinjection = isWireReinjection(reference);

        for (LogicalWire wire : reference.getWires()) {
            LogicalService service = wire.getTarget();
            LogicalComponent<?> targetComponent = service.getParent();
            if (!reinjection && (wire.getState() == LogicalState.PROVISIONED && targetComponent.getState() != LogicalState.MARKED)) {
                continue;
            }

            boolean attach = true;
            if (targetComponent.getState() == LogicalState.MARKED || wire.getState() == LogicalState.MARKED) {
                attach = false;
                PhysicalWire physicalWire;
                if (wire.getSourceBinding() != null && wire.getTargetBinding() == null) {
                    // wire is on a node runtime where the target component is on a different runtime and hence does not have a binding in the current runtime
                    physicalWire = wireGenerator.generateReference(wire.getSourceBinding());
                } else {
                    physicalWire = wireGenerator.generateWire(wire);
                }
                DetachWireCommand detachCommand = new DetachWireCommand();
                detachCommand.setPhysicalWireDefinition(physicalWire);
                command.add(detachCommand);
            } else if ((reinjection && targetComponent.getState() == LogicalState.NEW) || wire.getState() == LogicalState.NEW
                       || targetComponent.getState() == LogicalState.NEW) {
                PhysicalWire physicalWire;
                if (wire.getSourceBinding() != null && wire.getTargetBinding() == null) {
                    // wire is on a node runtime where the target component is on a different runtime and hence does not have a binding in the current runtime
                    physicalWire = wireGenerator.generateReference(wire.getSourceBinding());
                } else {
                    physicalWire = wireGenerator.generateWire(wire);
                }
                AttachWireCommand attachCommand = new AttachWireCommand();
                attachCommand.setPhysicalWireDefinition(physicalWire);
                command.add(attachCommand);
            }
            // generate physical callback wires if the forward service is bidirectional
            if (reference.getServiceContract().getCallbackContract() != null) {
                PhysicalWire physicalWire;
                if (wire.getSourceBinding() != null && wire.getTargetBinding() == null) {
                    // wire is on a node runtime where the target component is on a different runtime and hence does not have a binding in the current runtime
                    physicalWire = wireGenerator.generateReferenceCallback(reference.getCallbackBindings().get(0));
                } else {
                    physicalWire = wireGenerator.generateWireCallback(wire);
                }
                if (attach) {
                    AttachWireCommand attachCommand = new AttachWireCommand();
                    attachCommand.setPhysicalWireDefinition(physicalWire);
                    command.add(attachCommand);
                } else {
                    DetachWireCommand detachCommand = new DetachWireCommand();
                    detachCommand.setPhysicalWireDefinition(physicalWire);
                    command.add(detachCommand);
                }
            }

        }

    }

    @SuppressWarnings("unchecked")
    private void generateCallbackBindings(LogicalReference reference) {
        for (LogicalBinding<?> logicalBinding : reference.getBindings()) {
            CallbackBindingGenerator generator = generators.get(logicalBinding.getDefinition().getClass());
            if (generator == null) {
                throw new Fabric3Exception("Callback generator not found for:" + logicalBinding.getDefinition().getType());
            }
            Binding definition = generator.generateReferenceCallback(logicalBinding);
            definition.setParent(reference.getDefinition());
            LogicalBinding<?> logicalCallback = new LogicalBinding(definition, reference);
            reference.addCallbackBinding(logicalCallback);
        }
    }

    private boolean isWireReinjection(LogicalReference logicalReference) {
        Multiplicity multiplicity = logicalReference.getDefinition().getMultiplicity();
        if (multiplicity == Multiplicity.ZERO_N || multiplicity == Multiplicity.ONE_N) {
            for (LogicalWire wire : logicalReference.getWires()) {
                LogicalComponent<?> targetComponent = wire.getTarget().getParent();
                // check the source and target sides since a target may have been added or removed
                if (wire.getState() == LogicalState.NEW || wire.getState() == LogicalState.MARKED || targetComponent.getState() == LogicalState.NEW
                    || targetComponent.getState() == LogicalState.MARKED) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean isBoundReinjection(LogicalReference logicalReference) {
        Multiplicity multiplicity = logicalReference.getDefinition().getMultiplicity();
        if (multiplicity == Multiplicity.ZERO_N || multiplicity == Multiplicity.ONE_N) {
            for (LogicalBinding<?> binding : logicalReference.getBindings()) {
                if (binding.getState() == LogicalState.NEW || binding.getState() == LogicalState.MARKED) {
                    return true;
                }
            }
        }
        return false;
    }

}