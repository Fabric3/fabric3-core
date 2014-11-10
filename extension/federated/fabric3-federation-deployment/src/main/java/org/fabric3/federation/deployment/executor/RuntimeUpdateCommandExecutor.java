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

import org.fabric3.api.annotation.monitor.Monitor;
import org.fabric3.federation.deployment.command.DeploymentCommand;
import org.fabric3.federation.deployment.command.RuntimeUpdateCommand;
import org.fabric3.federation.deployment.command.RuntimeUpdateResponse;
import org.fabric3.federation.deployment.coordinator.DeploymentCache;
import org.fabric3.spi.container.ContainerException;
import org.fabric3.spi.container.executor.CommandExecutor;
import org.fabric3.spi.container.executor.CommandExecutorRegistry;
import org.oasisopen.sca.annotation.EagerInit;
import org.oasisopen.sca.annotation.Init;
import org.oasisopen.sca.annotation.Reference;

/**
 * Processes a {@link RuntimeUpdateCommand} on a zone leader by returning the cached deployment command for the current state of the zone.
 */
@EagerInit
public class RuntimeUpdateCommandExecutor implements CommandExecutor<RuntimeUpdateCommand> {
    private DeploymentCache cache;
    private CommandExecutorRegistry executorRegistry;
    private RuntimeUpdateMonitor monitor;

    public RuntimeUpdateCommandExecutor(@Reference DeploymentCache cache,
                                        @Reference CommandExecutorRegistry executorRegistry,
                                        @Monitor RuntimeUpdateMonitor monitor) {
        this.cache = cache;
        this.executorRegistry = executorRegistry;
        this.monitor = monitor;
    }

    @Init
    public void init() {
        executorRegistry.register(RuntimeUpdateCommand.class, this);
    }

    public void execute(RuntimeUpdateCommand command) throws ContainerException {
        String runtime = command.getRuntimeName();
        monitor.updateRequest(runtime);
        // pull from cache
        DeploymentCommand deploymentCommand = cache.get();
        RuntimeUpdateResponse response;
        // if the deployment command is null, this runtime has not been updated
        if (deploymentCommand == null) {
            monitor.notUpdated(runtime);
            response = new RuntimeUpdateResponse();
        } else {
            monitor.sendingUpdate(runtime);
            response = new RuntimeUpdateResponse(deploymentCommand);
        }
        command.setResponse(response);
    }
}