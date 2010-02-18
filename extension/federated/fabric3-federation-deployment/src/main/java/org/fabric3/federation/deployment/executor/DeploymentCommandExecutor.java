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
package org.fabric3.federation.deployment.executor;

import java.io.IOException;
import java.util.List;
import java.util.ListIterator;

import org.osoa.sca.annotations.EagerInit;
import org.osoa.sca.annotations.Init;
import org.osoa.sca.annotations.Reference;

import org.fabric3.api.annotation.Monitor;
import org.fabric3.federation.deployment.cache.DeploymentCache;
import org.fabric3.federation.deployment.command.DeploymentCommand;
import org.fabric3.federation.deployment.command.DeploymentErrorResponse;
import org.fabric3.federation.deployment.command.DeploymentResponse;
import org.fabric3.federation.deployment.command.SerializedDeploymentUnit;
import org.fabric3.model.type.component.Scope;
import org.fabric3.spi.classloader.SerializationService;
import org.fabric3.spi.command.Command;
import org.fabric3.spi.command.CompensatableCommand;
import org.fabric3.spi.component.InstanceLifecycleException;
import org.fabric3.spi.component.ScopeRegistry;
import org.fabric3.spi.executor.CommandExecutor;
import org.fabric3.spi.executor.CommandExecutorRegistry;
import org.fabric3.spi.executor.ExecutionException;

/**
 * Processes a {@link DeploymentCommand} on a participant. If there is an error processing the command, a rollback will be performed.
 *
 * @version $Rev$ $Date$
 */
@EagerInit
public class DeploymentCommandExecutor implements CommandExecutor<DeploymentCommand> {
    private CommandExecutorRegistry executorRegistry;
    private DeploymentCache cache;
    private SerializationService serializationService;
    private DeploymentCommandExecutorMonitor monitor;
    private ScopeRegistry scopeRegistry;

    public DeploymentCommandExecutor(@Reference CommandExecutorRegistry executorRegistry,
                                     @Reference ScopeRegistry scopeRegistry,
                                     @Reference DeploymentCache cache,
                                     @Reference SerializationService serializationService,
                                     @Monitor DeploymentCommandExecutorMonitor monitor) {
        this.executorRegistry = executorRegistry;
        this.cache = cache;
        this.serializationService = serializationService;
        this.monitor = monitor;
        this.scopeRegistry = scopeRegistry;
    }

    @Init
    public void init() {
        executorRegistry.register(DeploymentCommand.class, this);
    }

    public void execute(DeploymentCommand command) throws ExecutionException {
        monitor.received();
        // execute the extension commands first before deserializing the other commands as the other commands may contain extension-specific classes
        SerializedDeploymentUnit currentDeploymentUnit = command.getCurrentDeploymentUnit();

        byte[] serializedExtensionCommands = currentDeploymentUnit.getExtensionCommands();
        boolean result = execute(serializedExtensionCommands, command);
        if (!result) {
            // failed and rolled back
            return;
        }
        byte[] serializedCommands = currentDeploymentUnit.getCommands();
        result = execute(serializedCommands, command);
        if (!result) {
            // failed and rolled back
            return;
        }
        cacheDeployment(command);
        DeploymentResponse response = new DeploymentResponse();
        command.setResponse(response);
    }

    @SuppressWarnings({"unchecked"})
    private boolean execute(byte[] serializedCommands, DeploymentCommand command) {
        int marker = 0;
        List<CompensatableCommand> commands = null;
        try {
            commands = serializationService.deserialize(List.class, serializedCommands);
            for (Command cmd : commands) {
                executorRegistry.execute(cmd);
                marker++;
            }
            if (!commands.isEmpty()) {
                scopeRegistry.getScopeContainer(Scope.COMPOSITE).reinject();
            }
            return true;
        } catch (IOException e) {
            // no rollback because the commands were never deserialized
            monitor.error(e);
            DeploymentErrorResponse response = new DeploymentErrorResponse(e);
            command.setResponse(response);
            return false;
        } catch (ClassNotFoundException e) {
            // no rollback because the commands were never deserialized
            monitor.error(e);
            rollback(commands, marker);
            DeploymentErrorResponse response = new DeploymentErrorResponse(e);
            command.setResponse(response);
            return false;
        } catch (InstanceLifecycleException e) {
            monitor.error(e);
            rollback(commands, marker);
            DeploymentErrorResponse response = new DeploymentErrorResponse(e);
            command.setResponse(response);
            return false;
        } catch (ExecutionException e) {
            monitor.error(e);
            rollback(commands, marker);
            DeploymentErrorResponse response = new DeploymentErrorResponse(e);
            command.setResponse(response);
            return false;
        }

    }

    private void cacheDeployment(DeploymentCommand command) {
        String zone = command.getZone();
        SerializedDeploymentUnit fullDeploymentUnit = command.getFullDeploymentUnit();
        DeploymentCommand deploymentCommand = new DeploymentCommand(zone, fullDeploymentUnit, fullDeploymentUnit);
        cache.cache(deploymentCommand);
    }

    /**
     * Reverts the runtime state after a failed deployment by executing a collection of compensating commands.
     *
     * @param commands the deployment commands that failed
     * @param marker   the deployment command index where the failure occured
     */
    private void rollback(List<CompensatableCommand> commands, int marker) {
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
            monitor.error(e);
        } catch (InstanceLifecycleException e) {
            monitor.error(e);
        }
    }

}
