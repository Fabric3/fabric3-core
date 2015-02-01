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
 *
 * Portions originally based on Apache Tuscany 2007
 * licensed under the Apache 2.0 license.
 */
package org.fabric3.fabric.domain.generator.wire;

import java.net.URI;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.fabric3.api.host.Fabric3Exception;
import org.fabric3.api.model.type.component.Binding;
import org.fabric3.api.model.type.contract.ServiceContract;
import org.fabric3.fabric.container.command.AttachWireCommand;
import org.fabric3.fabric.container.command.ConnectionCommand;
import org.fabric3.fabric.container.command.DetachWireCommand;
import org.fabric3.fabric.domain.generator.CommandGenerator;
import org.fabric3.spi.domain.generator.wire.CallbackBindingGenerator;
import org.fabric3.spi.domain.generator.wire.WireGenerator;
import org.fabric3.spi.model.instance.LogicalBinding;
import org.fabric3.spi.model.instance.LogicalComponent;
import org.fabric3.spi.model.instance.LogicalCompositeComponent;
import org.fabric3.spi.model.instance.LogicalService;
import org.fabric3.spi.model.instance.LogicalState;
import org.fabric3.spi.model.physical.PhysicalWireDefinition;
import org.oasisopen.sca.annotation.Reference;

/**
 * Generates commands to attach/detach the source end of physical wires to their transports for components being deployed or undeployed.
 */
public class BoundServiceCommandGenerator implements CommandGenerator {
    private WireGenerator wireGenerator;

    private Map<Class<?>, CallbackBindingGenerator> generators = Collections.emptyMap();

    public BoundServiceCommandGenerator(@Reference WireGenerator wireGenerator) {
        this.wireGenerator = wireGenerator;
    }

    public int getOrder() {
        return ATTACH;
    }

    @Reference(required = false)
    public void setCallbackBindingGenerators(Map<Class<?>, CallbackBindingGenerator> generators) {
        this.generators = generators;
    }

    public ConnectionCommand generate(LogicalComponent<?> component) throws Fabric3Exception {
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
        if (LogicalState.PROVISIONED == component.getState() && !bindingChange) {
            return null;
        }

        ConnectionCommand command = new ConnectionCommand(component.getUri());
        generatePhysicalWires(component, command);
        if (command.getAttachCommands().isEmpty() && command.getDetachCommands().isEmpty()) {
            return null;
        }
        return command;
    }

    private void generatePhysicalWires(LogicalComponent<?> component, ConnectionCommand command) throws Fabric3Exception {
        for (LogicalService service : component.getServices()) {
            if (service.getBindings().isEmpty()) {
                continue;
            }
            ServiceContract callbackContract = service.getServiceContract().getCallbackContract();
            LogicalBinding<?> callbackBinding = null;
            URI callbackUri = null;
            if (callbackContract != null) {
                List<LogicalBinding<?>> callbackBindings = service.getCallbackBindings();

                if (callbackBindings.isEmpty()) {
                    // generate callback bindings as some transports do not require an explicit callback binding configuration on the reference
                    generateCallbackBindings(service);
                }

                if (callbackBindings.size() != 1) {
                    String uri = service.getUri().toString();
                    throw new UnsupportedOperationException("The runtime requires exactly one callback binding to be specified on service: " + uri);
                }
                callbackBinding = callbackBindings.get(0);
                // xcv FIXME should be on the logical binding
                callbackUri = callbackBinding.getDefinition().getTargetUri();
            }

            for (LogicalBinding<?> binding : service.getBindings()) {
                if (binding.getState() == LogicalState.NEW || binding.getState() == LogicalState.MARKED) {
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
            if (callbackBinding != null && ((callbackBinding.getState() == LogicalState.NEW || callbackBinding.getState() == LogicalState.MARKED))) {
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

    @SuppressWarnings("unchecked")
    private void generateCallbackBindings(LogicalService service) throws Fabric3Exception {
        for (LogicalBinding<?> logicalBinding : service.getBindings()) {
            CallbackBindingGenerator generator = generators.get(logicalBinding.getDefinition().getClass());
            if (generator == null) {
                throw new Fabric3Exception("Callback generator not found for:" + logicalBinding.getDefinition().getType());
            }
            Binding definition = generator.generateServiceCallback(logicalBinding);
            definition.setParent(service.getDefinition());
            LogicalBinding<?> logicalCallback = new LogicalBinding(definition, service);
            service.addCallbackBinding(logicalCallback);
        }
    }

}