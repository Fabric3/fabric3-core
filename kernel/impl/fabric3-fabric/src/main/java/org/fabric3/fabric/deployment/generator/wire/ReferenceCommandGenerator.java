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
package org.fabric3.fabric.deployment.generator.wire;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.fabric3.fabric.deployment.command.AttachWireCommand;
import org.fabric3.fabric.deployment.command.ConnectionCommand;
import org.fabric3.fabric.deployment.command.DetachWireCommand;
import org.fabric3.fabric.deployment.generator.CommandGenerator;
import org.fabric3.model.type.component.BindingDefinition;
import org.fabric3.model.type.component.Multiplicity;
import org.fabric3.spi.deployment.generator.binding.CallbackBindingGenerator;
import org.fabric3.spi.deployment.generator.GenerationException;
import org.fabric3.spi.deployment.generator.wire.WireGenerator;
import org.fabric3.spi.model.instance.LogicalBinding;
import org.fabric3.spi.model.instance.LogicalComponent;
import org.fabric3.spi.model.instance.LogicalCompositeComponent;
import org.fabric3.spi.model.instance.LogicalReference;
import org.fabric3.spi.model.instance.LogicalService;
import org.fabric3.spi.model.instance.LogicalState;
import org.fabric3.spi.model.instance.LogicalWire;
import org.fabric3.spi.model.physical.PhysicalWireDefinition;
import org.fabric3.spi.model.type.binding.SCABinding;
import org.oasisopen.sca.annotation.Property;
import org.oasisopen.sca.annotation.Reference;

/**
 * Generates a command to bind or attach a wire to a reference.
 */
public class ReferenceCommandGenerator implements CommandGenerator {
    private WireGenerator wireGenerator;
    private Map<Class<?>, CallbackBindingGenerator> generators = Collections.emptyMap();

    private int order;

    /**
     * Constructor.
     *
     * @param wireGenerator the physical wire generator
     * @param order         the order for this command generator
     */
    public ReferenceCommandGenerator(@Reference WireGenerator wireGenerator, @Property(name = "order") int order) {
        this.wireGenerator = wireGenerator;
        this.order = order;
    }

    @Reference(required = false)
    public void setCallbackBindingGenerators(Map<Class<?>, CallbackBindingGenerator> generators) {
        this.generators = generators;
    }

    public int getOrder() {
        return order;
    }

    public ConnectionCommand generate(LogicalComponent<?> component, boolean incremental) throws GenerationException {
        if (component instanceof LogicalCompositeComponent) {
            return null;
        }
        ConnectionCommand command = new ConnectionCommand(component.getUri());

        for (LogicalReference reference : component.getReferences()) {
            if (!reference.getWires().isEmpty()) {
                generateWires(reference, command, incremental);
            } else {
                generateBindings(reference, component, incremental, command);
            }

        }
        if (command.getAttachCommands().isEmpty() && command.getDetachCommands().isEmpty()) {
            return null;
        }
        return command;
    }

    private void generateBindings(LogicalReference reference, LogicalComponent<?> component, boolean incremental, ConnectionCommand command)
            throws GenerationException {
        boolean reinjection = isBoundReinjection(reference, incremental);

        for (LogicalBinding<?> logicalBinding : reference.getBindings()) {
            if (logicalBinding.getDefinition() instanceof SCABinding) {
                // skip SCA binding
                continue;
            }
            generateBinding(component, logicalBinding, command, incremental, reinjection, false);
        }
        if (reference.getServiceContract().getCallbackContract() != null) {
            boolean bindings = reference.isConcreteBound();
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
                generateBinding(component, callbackBinding, command, incremental, reinjection, true);
            }
        }
    }

    private void generateBinding(LogicalComponent<?> component,
                                 LogicalBinding<?> logicalBinding,
                                 ConnectionCommand command,
                                 boolean incremental,
                                 boolean reinjection,
                                 boolean callback) throws GenerationException {
        if (LogicalState.MARKED == component.getState() || LogicalState.MARKED == logicalBinding.getState()) {
            PhysicalWireDefinition wireDefinition;
            if (callback) {
                wireDefinition = wireGenerator.generateBoundReferenceCallback(logicalBinding);
            } else {
                wireDefinition = wireGenerator.generateBoundReference(logicalBinding);
            }
            DetachWireCommand wireCommand = new DetachWireCommand();
            wireCommand.setPhysicalWireDefinition(wireDefinition);
            command.add(wireCommand);

        } else if (LogicalState.NEW == logicalBinding.getState() || !incremental || reinjection) {
            PhysicalWireDefinition wireDefinition;
            if (callback) {
                wireDefinition = wireGenerator.generateBoundReferenceCallback(logicalBinding);
            } else {
                wireDefinition = wireGenerator.generateBoundReference(logicalBinding);
            }
            AttachWireCommand wireCommand = new AttachWireCommand();
            wireCommand.setPhysicalWireDefinition(wireDefinition);
            command.add(wireCommand);
        }

    }

    private void generateWires(LogicalReference reference, ConnectionCommand command, boolean incremental) throws GenerationException {

        // if the reference is a multiplicity and one of the wires has changed, all of the wires need to be regenerated for reinjection
        boolean reinjection = isWireReinjection(reference, incremental);

        for (LogicalWire wire : reference.getWires()) {
            LogicalService service = wire.getTarget();
            LogicalComponent<?> targetComponent = service.getLeafComponent();
            if (!reinjection && (wire.getState() == LogicalState.PROVISIONED && targetComponent.getState() != LogicalState.MARKED && incremental)) {
                continue;
            }

            boolean attach = true;
            if (targetComponent.getState() == LogicalState.MARKED || wire.getState() == LogicalState.MARKED) {
                attach = false;
                PhysicalWireDefinition pwd = wireGenerator.generateWire(wire);
                DetachWireCommand detachCommand = new DetachWireCommand();
                detachCommand.setPhysicalWireDefinition(pwd);
                command.add(detachCommand);
            } else if ((reinjection && targetComponent.getState() == LogicalState.NEW) || !incremental || wire.getState() == LogicalState.NEW
                       || targetComponent.getState() == LogicalState.NEW) {
                PhysicalWireDefinition pwd;
                if (wire.getSourceBinding()!= null && wire.getTargetBinding() == null) {
                    // wire is on a node runtime where the target component is on a different runtime and hence does not have a binding in the current runtime
                    pwd = wireGenerator.generateBoundReference(wire.getSourceBinding());
                } else {
                    pwd = wireGenerator.generateWire(wire);
                }
                AttachWireCommand attachCommand = new AttachWireCommand();
                attachCommand.setPhysicalWireDefinition(pwd);
                command.add(attachCommand);
            }
            // generate physical callback wires if the forward service is bidirectional
            if (reference.getServiceContract().getCallbackContract() != null) {
                PhysicalWireDefinition pwd;
                if (wire.getSourceBinding()!= null && wire.getTargetBinding() == null) {
                    // wire is on a node runtime where the target component is on a different runtime and hence does not have a binding in the current runtime
                    pwd = wireGenerator.generateBoundReferenceCallback(reference.getCallbackBindings().get(0));
                } else {
                    pwd = wireGenerator.generateWireCallback(wire);
                }
                if (attach) {
                    AttachWireCommand attachCommand = new AttachWireCommand();
                    attachCommand.setPhysicalWireDefinition(pwd);
                    command.add(attachCommand);
                } else {
                    DetachWireCommand detachCommand = new DetachWireCommand();
                    detachCommand.setPhysicalWireDefinition(pwd);
                    command.add(detachCommand);
                }
            }

        }

    }

    @SuppressWarnings("unchecked")
    private void generateCallbackBindings(LogicalReference reference) throws GenerationException {
        for (LogicalBinding<?> logicalBinding : reference.getBindings()) {
            if (logicalBinding.getDefinition() instanceof SCABinding) {
                // skip SCA binding
                continue;
            }
            CallbackBindingGenerator generator = generators.get(logicalBinding.getDefinition().getClass());
            if (generator == null) {
                throw new GenerationException("Callback generator not found for:" + logicalBinding.getDefinition().getType());
            }
            BindingDefinition definition = generator.generateReferenceCallback(logicalBinding);
            definition.setParent(reference.getDefinition());
            LogicalBinding<?> logicalCallback = new LogicalBinding(definition, reference);
            reference.addCallbackBinding(logicalCallback);
        }
    }

    private boolean isWireReinjection(LogicalReference logicalReference, boolean incremental) {
        Multiplicity multiplicity = logicalReference.getDefinition().getMultiplicity();
        if (incremental && multiplicity == Multiplicity.ZERO_N || multiplicity == Multiplicity.ONE_N) {
            for (LogicalWire wire : logicalReference.getWires()) {
                LogicalComponent<?> targetComponent = wire.getTarget().getLeafComponent();
                // check the source and target sides since a target may have been added or removed
                if (wire.getState() == LogicalState.NEW || wire.getState() == LogicalState.MARKED || targetComponent.getState() == LogicalState.NEW
                    || targetComponent.getState() == LogicalState.MARKED) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean isBoundReinjection(LogicalReference logicalReference, boolean incremental) {
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