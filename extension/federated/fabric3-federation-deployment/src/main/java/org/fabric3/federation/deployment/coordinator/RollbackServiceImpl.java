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
 */
package org.fabric3.federation.deployment.coordinator;

import java.io.IOException;
import java.util.List;
import java.util.ListIterator;

import org.fabric3.api.annotation.management.Management;
import org.fabric3.api.annotation.management.ManagementOperation;
import org.fabric3.api.annotation.management.OperationType;
import org.fabric3.api.model.type.component.Scope;
import org.fabric3.federation.deployment.command.DeploymentCommand;
import org.fabric3.spi.classloader.SerializationService;
import org.fabric3.spi.container.ContainerException;
import org.fabric3.spi.container.executor.CommandExecutorRegistry;
import org.fabric3.spi.container.component.ScopeRegistry;
import org.fabric3.spi.container.command.Command;
import org.fabric3.spi.container.command.CompensatableCommand;
import org.oasisopen.sca.annotation.Reference;

/**
 *
 */
@Management(name = "RollbackService",
            path = "/runtime/deployment/rollback",
            group = "deployment",
            description = "Performs deployment rollback operations")
public class RollbackServiceImpl implements RollbackService {
    private DeploymentCache cache;
    private CommandExecutorRegistry executorRegistry;
    private ScopeRegistry scopeRegistry;
    private SerializationService serializationService;

    public RollbackServiceImpl(@Reference CommandExecutorRegistry executorRegistry,
                               @Reference DeploymentCache cache,
                               @Reference ScopeRegistry scopeRegistry,
                               @Reference SerializationService serializationService) {
        this.executorRegistry = executorRegistry;
        this.cache = cache;
        this.scopeRegistry = scopeRegistry;
        this.serializationService = serializationService;
    }

    @SuppressWarnings({"unchecked"})
    @ManagementOperation(path = "/", type = OperationType.POST, description = "Rollbacks the previous deployment")
    public void rollback() throws RollbackException {
        DeploymentCommand command = cache.undo();
        if (command == null) {
            return;
        }
        byte[] serializedCommands = command.getCurrentDeploymentUnit().getCommands();
        try {
            List<CompensatableCommand> commands = serializationService.deserialize(List.class, serializedCommands);
            rollback(commands, commands.size());
        } catch (ClassNotFoundException | IOException e) {
            throw new RollbackException(e);
        }
    }

    public void rollback(List<CompensatableCommand> commands, int marker) throws RollbackException {
        try {
            ListIterator<CompensatableCommand> iter = commands.listIterator(marker);
            while (iter.hasPrevious()) {
                CompensatableCommand command = iter.previous();
                Command compensating = command.getCompensatingCommand();
                executorRegistry.execute(compensating);
            }
            if (scopeRegistry != null) {
                scopeRegistry.getScopeContainer(Scope.COMPOSITE).reinject();
            }
        } catch (ContainerException e) {
            throw new RollbackException(e);
        }
    }

}