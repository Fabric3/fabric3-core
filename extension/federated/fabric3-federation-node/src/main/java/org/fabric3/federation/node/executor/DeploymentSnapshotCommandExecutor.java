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
package org.fabric3.federation.node.executor;

import org.fabric3.api.annotation.monitor.Monitor;
import org.fabric3.federation.node.command.DeploymentSnapshotCommand;
import org.fabric3.federation.node.merge.DomainMergeService;
import org.fabric3.spi.container.executor.CommandExecutor;
import org.fabric3.spi.container.executor.CommandExecutorRegistry;
import org.oasisopen.sca.annotation.EagerInit;
import org.oasisopen.sca.annotation.Init;
import org.oasisopen.sca.annotation.Reference;

/**
 * Executes a deployment snapshot command received from another node. The snapshot is merged into the current logical domain view.
 */
@EagerInit
public class DeploymentSnapshotCommandExecutor implements CommandExecutor<DeploymentSnapshotCommand> {
    private CommandExecutorRegistry executorRegistry;
    private DomainMergeService mergeService;
    private DeploymentMonitor monitor;

    public DeploymentSnapshotCommandExecutor(@Reference CommandExecutorRegistry executorRegistry,
                                             @Reference DomainMergeService mergeService,
                                             @Monitor DeploymentMonitor monitor) {
        this.executorRegistry = executorRegistry;
        this.mergeService = mergeService;
        this.monitor = monitor;
    }

    @Init
    public void init() {
        executorRegistry.register(DeploymentSnapshotCommand.class, this);
    }

    public void execute(DeploymentSnapshotCommand command) {
        monitor.received(command.getRuntimeName());
        mergeService.merge(command.getSnapshot());
    }
}
