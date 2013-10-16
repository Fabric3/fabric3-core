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
package org.fabric3.federation.deployment.coordinator;

import java.io.IOException;
import java.util.List;
import java.util.ListIterator;

import org.oasisopen.sca.annotation.Reference;

import org.fabric3.api.annotation.management.Management;
import org.fabric3.api.annotation.management.ManagementOperation;
import org.fabric3.api.annotation.management.OperationType;
import org.fabric3.federation.deployment.command.DeploymentCommand;
import org.fabric3.api.model.type.component.Scope;
import org.fabric3.spi.classloader.SerializationService;
import org.fabric3.spi.command.Command;
import org.fabric3.spi.command.CompensatableCommand;
import org.fabric3.spi.container.component.InstanceLifecycleException;
import org.fabric3.spi.container.component.ScopeRegistry;
import org.fabric3.spi.command.CommandExecutorRegistry;
import org.fabric3.spi.command.ExecutionException;

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
        } catch (ClassNotFoundException e) {
            throw new RollbackException(e);
        } catch (IOException e) {
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
        } catch (ExecutionException e) {
            throw new RollbackException(e);
        } catch (InstanceLifecycleException e) {
            throw new RollbackException(e);
        }
    }


}