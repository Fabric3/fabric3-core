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
package org.fabric3.fabric.domain;

import java.util.List;
import java.util.ListIterator;

import org.osoa.sca.annotations.Reference;

import org.fabric3.api.annotation.monitor.Monitor;
import org.fabric3.host.domain.DeploymentException;
import org.fabric3.model.type.component.Scope;
import org.fabric3.spi.command.Command;
import org.fabric3.spi.command.CompensatableCommand;
import org.fabric3.spi.component.InstanceLifecycleException;
import org.fabric3.spi.component.ScopeRegistry;
import org.fabric3.spi.domain.Deployer;
import org.fabric3.spi.domain.DeployerMonitor;
import org.fabric3.spi.domain.DeploymentPackage;
import org.fabric3.spi.executor.CommandExecutorRegistry;
import org.fabric3.spi.executor.ExecutionException;
import org.fabric3.spi.generator.DeploymentUnit;

import static org.fabric3.spi.model.instance.LogicalComponent.LOCAL_ZONE;

/**
 * A Deployer that sends DeploymentUnits to the local runtime instance.
 *
 * @version $Rev$ $Date$
 */
public class LocalDeployer implements Deployer {
    private CommandExecutorRegistry executorRegistry;
    private ScopeRegistry scopeRegistry;
    private DeployerMonitor monitor;

    public LocalDeployer(@Reference CommandExecutorRegistry executorRegistry,
                         @Reference ScopeRegistry scopeRegistry,
                         @Monitor DeployerMonitor monitor) {
        this.executorRegistry = executorRegistry;
        this.scopeRegistry = scopeRegistry;
        this.monitor = monitor;
    }

    public void deploy(DeploymentPackage deploymentPackage) throws DeploymentException {
        DeploymentUnit unit = deploymentPackage.getCurrentDeployment().getDeploymentUnit(LOCAL_ZONE);
        List<CompensatableCommand> provisionCommands = unit.getProvisionCommands();
        execute(provisionCommands);

        // ignore extension commands since extensions will already be loaded locally
        List<CompensatableCommand> commands = unit.getCommands();
        execute(commands);
        try {
            if (scopeRegistry != null) {
                scopeRegistry.getScopeContainer(Scope.COMPOSITE).reinject();
            }
        } catch (InstanceLifecycleException e) {
            throw new DeploymentException(e);
        }
    }

    /**
     * Executes the commands, performing a rollback on error.
     *
     * @param commands the commands
     * @throws DeploymentException if a deployment error occurs
     */
    private void execute(List<CompensatableCommand> commands) throws DeploymentException {
        int marker = 0;
        for (Command command : commands) {
            try {
                executorRegistry.execute(command);
                ++marker;
            } catch (ExecutionException e) {
                rollback(commands, marker);
                throw new DeploymentException(e);
            }
        }
    }

    /**
     * Rolls back the runtime state after a failed deployment by executing a collection of compensating commands.
     *
     * @param commands the deployment commands that failed
     * @param marker   the deployment command index where the failure occurred
     */
    private void rollback(List<CompensatableCommand> commands, int marker) {
        try {
            monitor.rollback("local");
            ListIterator<CompensatableCommand> iter = commands.listIterator(marker);
            while (iter.hasPrevious()) {
                CompensatableCommand command = iter.previous();
                Command compensating = command.getCompensatingCommand();
                executorRegistry.execute(compensating);
            }
            if (scopeRegistry != null) {
                scopeRegistry.getScopeContainer(Scope.COMPOSITE).reinject();
            }
        } catch (ExecutionException ex) {
            monitor.rollbackError("local", ex);
        } catch (InstanceLifecycleException ex) {
            monitor.rollbackError("local", ex);
        }
    }

}
