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

import javax.xml.namespace.QName;
import java.net.URI;

import org.fabric3.api.host.ContainerException;
import org.fabric3.federation.node.command.DomainSnapshotCommand;
import org.fabric3.federation.node.command.DomainSnapshotResponse;
import org.fabric3.federation.node.snapshot.SnapshotHelper;
import org.fabric3.spi.container.executor.CommandExecutor;
import org.fabric3.spi.container.executor.CommandExecutorRegistry;
import org.fabric3.spi.domain.DeployListener;
import org.fabric3.spi.domain.LogicalComponentManager;
import org.fabric3.spi.model.instance.LogicalCompositeComponent;
import org.fabric3.spi.model.instance.LogicalState;
import org.oasisopen.sca.annotation.EagerInit;
import org.oasisopen.sca.annotation.Init;
import org.oasisopen.sca.annotation.Reference;

/**
 * Executes a request for a snapshot from another runtime by returning this runtime's logical domain view.
 * <p/>
 * The current snapshot is cached until it is invalidated by a deployment event.
 */
@EagerInit
public class DomainSnapshotCommandExecutor implements CommandExecutor<DomainSnapshotCommand>, DeployListener {
    private CommandExecutorRegistry executorRegistry;
    private LogicalComponentManager lcm;
    private LogicalCompositeComponent snapshot;

    public DomainSnapshotCommandExecutor(@Reference CommandExecutorRegistry executorRegistry, @Reference(name = "lcm") LogicalComponentManager lcm) {
        this.executorRegistry = executorRegistry;
        this.lcm = lcm;
    }

    @Init
    public void init() {
        executorRegistry.register(DomainSnapshotCommand.class, this);
    }

    public synchronized void execute(DomainSnapshotCommand command) throws ContainerException {
        if (snapshot == null) {
            LogicalCompositeComponent domain = lcm.getRootComponent();
            snapshot = SnapshotHelper.snapshot(domain, LogicalState.NEW);
        }
        DomainSnapshotResponse response = new DomainSnapshotResponse(snapshot);
        command.setResponse(response);
    }

    public synchronized void onDeploy(URI uri) {
        // invalidate the snapshot
        snapshot = null;
    }

    public synchronized void onUnDeploy(URI uri) {
        // invalidate the snapshot
        snapshot = null;
    }

    public void onDeployCompleted(URI uri) {
    }

    public void onUnDeployCompleted(URI uri) {
    }

    public void onDeploy(QName deployable) {
    }

    public void onDeployCompleted(QName deployable) {
    }

    public void onUndeploy(QName undeployed) {
    }

    public void onUndeployCompleted(QName undeployed) {
    }
}
