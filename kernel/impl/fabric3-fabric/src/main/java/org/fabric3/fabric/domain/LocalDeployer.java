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
package org.fabric3.fabric.domain;

import java.util.List;

import org.fabric3.api.host.ContainerException;
import org.fabric3.api.model.type.component.Scope;
import org.fabric3.spi.container.command.Command;
import org.fabric3.spi.container.component.ScopeRegistry;
import org.fabric3.spi.container.executor.CommandExecutorRegistry;
import org.fabric3.spi.domain.Deployer;
import org.fabric3.spi.domain.generator.Deployment;
import org.oasisopen.sca.annotation.Reference;

/**
 *
 */
public class LocalDeployer implements Deployer {
    private CommandExecutorRegistry executorRegistry;
    private ScopeRegistry scopeRegistry;

    public LocalDeployer(@Reference CommandExecutorRegistry executorRegistry, @Reference ScopeRegistry scopeRegistry) {
        this.executorRegistry = executorRegistry;
        this.scopeRegistry = scopeRegistry;
    }

    public void deploy(Deployment deployment) throws ContainerException {
        List<Command> commands = deployment.getCommands();
        execute(commands);
        if (scopeRegistry != null) {
            scopeRegistry.getScopeContainer(Scope.COMPOSITE).reinject();
        }
    }

    /**
     * Executes the commands, performing a rollback on error.
     *
     * @param commands the commands
     * @throws ContainerException if a deployment error occurs
     */
    private void execute(List<Command> commands) throws ContainerException {
        for (Command command : commands) {
            executorRegistry.execute(command);
        }
    }

}
