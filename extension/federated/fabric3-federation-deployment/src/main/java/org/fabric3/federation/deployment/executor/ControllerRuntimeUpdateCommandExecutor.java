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
package org.fabric3.federation.deployment.executor;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.util.List;

import org.fabric3.api.annotation.monitor.Monitor;
import org.fabric3.federation.deployment.command.DeploymentCommand;
import org.fabric3.federation.deployment.command.RuntimeUpdateCommand;
import org.fabric3.federation.deployment.command.RuntimeUpdateResponse;
import org.fabric3.federation.deployment.command.SerializedDeploymentUnit;
import org.fabric3.spi.classloader.MultiClassLoaderObjectOutputStream;
import org.fabric3.spi.container.ContainerException;
import org.fabric3.spi.container.executor.CommandExecutor;
import org.fabric3.spi.container.executor.CommandExecutorRegistry;
import org.fabric3.spi.domain.LogicalComponentManager;
import org.fabric3.spi.container.command.CompensatableCommand;
import org.fabric3.spi.domain.generator.Deployment;
import org.fabric3.spi.domain.generator.DeploymentUnit;
import org.fabric3.spi.domain.generator.GenerationException;
import org.fabric3.spi.domain.generator.Generator;
import org.fabric3.spi.model.instance.LogicalCompositeComponent;
import org.oasisopen.sca.annotation.EagerInit;
import org.oasisopen.sca.annotation.Init;
import org.oasisopen.sca.annotation.Reference;

/**
 * Processes a {@link RuntimeUpdateCommand} on the controller by regenerating deployment commands for the current state of the zone.
 */
@EagerInit
public class ControllerRuntimeUpdateCommandExecutor implements CommandExecutor<RuntimeUpdateCommand> {
    private CommandExecutorRegistry executorRegistry;
    private RuntimeUpdateMonitor monitor;
    private LogicalComponentManager lcm;
    private Generator generator;

    public ControllerRuntimeUpdateCommandExecutor(@Reference(name = "lcm") LogicalComponentManager lcm,
                                                  @Reference(name = "generator") Generator generator,
                                                  @Reference CommandExecutorRegistry executorRegistry,
                                                  @Monitor RuntimeUpdateMonitor monitor) {
        this.lcm = lcm;
        this.generator = generator;
        this.executorRegistry = executorRegistry;
        this.monitor = monitor;
    }

    @Init
    public void init() {
        executorRegistry.register(RuntimeUpdateCommand.class, this);
    }

    public void execute(RuntimeUpdateCommand command) throws ContainerException {
        String runtimeName = command.getRuntimeName();
        monitor.updateRequest(runtimeName);
        String zone = command.getZoneName();

        // A full generation must be performed since the runtime requesting the update has no previous deployment state (i.e. it is booting).
        // The full and current deployment will therefore be the same.
        DeploymentUnit unit = regenerate(zone);

        List<CompensatableCommand> provisionCommands = unit.getProvisionCommands();
        byte[] serializedProvisionCommands = serialize((Serializable) provisionCommands);
        List<CompensatableCommand> extensionCommands = unit.getExtensionCommands();
        byte[] serializedExtensionCommands = serialize((Serializable) extensionCommands);
        List<CompensatableCommand> commands = unit.getCommands();
        byte[] serializedCommands = serialize((Serializable) commands);
        SerializedDeploymentUnit serializedUnit = new SerializedDeploymentUnit(serializedProvisionCommands, serializedExtensionCommands, serializedCommands);
        DeploymentCommand deploymentCommand = new DeploymentCommand(zone, serializedUnit, serializedUnit);
        RuntimeUpdateResponse response = new RuntimeUpdateResponse(deploymentCommand);
        command.setResponse(response);
        monitor.sendingUpdate(runtimeName);
    }

    private DeploymentUnit regenerate(String zoneId) throws ContainerException {
        LogicalCompositeComponent domain = lcm.getRootComponent();
        Deployment deployment;
        try {
            deployment = generator.generate(domain, false);
        } catch (GenerationException e) {
            throw new ContainerException(e);
        }
        return deployment.getDeploymentUnit(zoneId);
    }

    private byte[] serialize(Serializable serializable) throws ContainerException {
        try {
            ByteArrayOutputStream bas = new ByteArrayOutputStream();
            MultiClassLoaderObjectOutputStream stream = new MultiClassLoaderObjectOutputStream(bas);
            stream.writeObject(serializable);
            return bas.toByteArray();
        } catch (IOException e) {
            throw new ContainerException(e);
        }
    }
}