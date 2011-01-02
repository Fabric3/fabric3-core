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
 *
 * ----------------------------------------------------
 *
 * Portions originally based on Apache Tuscany 2007
 * licensed under the Apache 2.0 license.
 *
 */
package org.fabric3.fabric.generator.wire;

import java.net.URI;
import java.util.List;

import org.osoa.sca.annotations.Property;
import org.osoa.sca.annotations.Reference;

import org.fabric3.fabric.command.AttachWireCommand;
import org.fabric3.fabric.command.ConnectionCommand;
import org.fabric3.fabric.command.DetachWireCommand;
import org.fabric3.fabric.generator.CommandGenerator;
import org.fabric3.model.type.contract.ServiceContract;
import org.fabric3.spi.generator.GenerationException;
import org.fabric3.spi.model.instance.LogicalBinding;
import org.fabric3.spi.model.instance.LogicalComponent;
import org.fabric3.spi.model.instance.LogicalCompositeComponent;
import org.fabric3.spi.model.instance.LogicalService;
import org.fabric3.spi.model.instance.LogicalState;
import org.fabric3.spi.model.physical.PhysicalWireDefinition;

/**
 * Generates commands to attach/detach the source end of physical wires to their transports for components being deployed or undeployed.
 *
 * @version $Revision$ $Date$
 */
public class BoundServiceCommandGenerator implements CommandGenerator {
    private final WireGenerator wireGenerator;
    private final int order;

    public BoundServiceCommandGenerator(@Reference WireGenerator wireGenerator, @Property(name = "order") int order) {
        this.wireGenerator = wireGenerator;
        this.order = order;
    }

    public int getOrder() {
        return order;
    }

    public ConnectionCommand generate(LogicalComponent<?> component, boolean incremental) throws GenerationException {
        if (component instanceof LogicalCompositeComponent) {
            return null;
        }

        // determine if a binding is being added or removed. If so, an AttachWireCommand or DetachWireCommand must be generated.
        boolean bindingChange = false;
        for (LogicalService service : component.getServices()) {
            for (LogicalBinding<?> binding : service.getBindings()) {
                if (binding.getState() == LogicalState.NEW || binding.getState() == LogicalState.MARKED) {
                    bindingChange = true;
                    break;
                }
            }
        }
        if (LogicalState.PROVISIONED == component.getState() && incremental && !bindingChange) {
            return null;
        }

        ConnectionCommand command = new ConnectionCommand();
        generatePhysicalWires(component, command, incremental);
        if (command.getAttachCommands().isEmpty() && command.getDetachCommands().isEmpty()) {
            return null;
        }
        return command;
    }

    private void generatePhysicalWires(LogicalComponent<?> component, ConnectionCommand command, boolean incremental) throws GenerationException {
        for (LogicalService service : component.getServices()) {
            if (service.getBindings().isEmpty()) {
                continue;
            }
            ServiceContract callbackContract = service.getServiceContract().getCallbackContract();
            LogicalBinding<?> callbackBinding = null;
            URI callbackUri = null;
            if (callbackContract != null) {
                List<LogicalBinding<?>> callbackBindings = service.getCallbackBindings();
                if (callbackBindings.size() != 1) {
                    String uri = service.getUri().toString();
                    throw new UnsupportedOperationException("The runtime requires exactly one callback binding to be specified on service: " + uri);
                }
                callbackBinding = callbackBindings.get(0);
                // xcv FIXME should be on the logical binding
                callbackUri = callbackBinding.getDefinition().getTargetUri();
            }

            for (LogicalBinding<?> binding : service.getBindings()) {
                if (binding.getState() == LogicalState.NEW || binding.getState() == LogicalState.MARKED || !incremental) {
                    PhysicalWireDefinition pwd = wireGenerator.generateBoundService(binding, callbackUri);
                    if (LogicalState.MARKED == binding.getState()) {
                        DetachWireCommand detachWireCommand = new DetachWireCommand();
                        detachWireCommand.setPhysicalWireDefinition(pwd);
                        command.add(detachWireCommand);
                    } else {
                        AttachWireCommand attachWireCommand = new AttachWireCommand();
                        attachWireCommand.setPhysicalWireDefinition(pwd);
                        command.add(attachWireCommand);
                    }

                }
            }
            // generate the callback command set
            if (callbackBinding != null
                    && (callbackBinding.getState() == LogicalState.NEW
                    || callbackBinding.getState() == LogicalState.MARKED
                    || !incremental)) {
                PhysicalWireDefinition callbackPwd = wireGenerator.generateBoundServiceCallback(callbackBinding);
                if (LogicalState.MARKED == callbackBinding.getState()) {
                    DetachWireCommand detachWireCommand = new DetachWireCommand();
                    detachWireCommand.setPhysicalWireDefinition(callbackPwd);
                    command.add(detachWireCommand);
                } else {
                    AttachWireCommand attachWireCommand = new AttachWireCommand();
                    attachWireCommand.setPhysicalWireDefinition(callbackPwd);
                    command.add(attachWireCommand);
                }
            }
        }
    }


}