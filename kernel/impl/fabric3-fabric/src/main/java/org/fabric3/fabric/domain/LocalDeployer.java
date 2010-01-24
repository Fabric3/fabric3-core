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

import org.osoa.sca.annotations.Reference;

import org.fabric3.host.domain.DeploymentException;
import org.fabric3.model.type.component.Scope;
import org.fabric3.spi.command.Command;
import org.fabric3.spi.component.InstanceLifecycleException;
import org.fabric3.spi.component.ScopeRegistry;
import org.fabric3.spi.domain.Deployer;
import org.fabric3.spi.domain.DeploymentPackage;
import org.fabric3.spi.executor.CommandExecutorRegistry;
import org.fabric3.spi.executor.ExecutionException;

/**
 * A Deployer that sends DeploymentUnits to the local runtime instance.
 *
 * @version $Rev$ $Date$
 */
public class LocalDeployer implements Deployer {

    private CommandExecutorRegistry registry;
    private ScopeRegistry scopeRegistry;

    public LocalDeployer(@Reference CommandExecutorRegistry registry, @Reference ScopeRegistry scopeRegistry) {
        this.registry = registry;
        this.scopeRegistry = scopeRegistry;
    }

    public void deploy(DeploymentPackage deploymentPackage) throws DeploymentException {
        // ignore extension commands since extensions will already be loaded locally
        List<Command> commands = deploymentPackage.getCurrentDeployment().getDeploymentUnit(null).getCommands();
        for (Command command : commands) {
            try {
                registry.execute(command);
            } catch (ExecutionException e) {
                throw new DeploymentException(e);
            }
        }

        try {
            if (scopeRegistry != null) {
                scopeRegistry.getScopeContainer(Scope.COMPOSITE).reinject();
            }
        } catch (InstanceLifecycleException e) {
            throw new DeploymentException(e);
        }

    }

}
