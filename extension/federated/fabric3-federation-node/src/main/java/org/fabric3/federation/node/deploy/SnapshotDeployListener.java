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
package org.fabric3.federation.node.deploy;

import javax.xml.namespace.QName;
import java.net.URI;

import org.fabric3.api.annotation.monitor.Monitor;
import org.fabric3.api.host.Fabric3Exception;
import org.fabric3.api.host.HostNamespaces;
import org.fabric3.api.host.Names;
import org.fabric3.api.host.runtime.HostInfo;
import org.fabric3.federation.node.command.DeploymentSnapshotCommand;
import org.fabric3.federation.node.snapshot.SnapshotHelper;
import org.fabric3.spi.domain.DeployListener;
import org.fabric3.spi.domain.LogicalComponentManager;
import org.fabric3.spi.federation.topology.NodeTopologyService;
import org.fabric3.spi.model.instance.LogicalCompositeComponent;
import org.fabric3.spi.model.instance.LogicalState;
import org.oasisopen.sca.annotation.EagerInit;
import org.oasisopen.sca.annotation.Reference;

/**
 * Broadcasts new deployment snapshots to domain members if the current runtime is a zone leader. Snapshots are created when a deployment or un-deployment is
 * performed
 *
 * Note only zone leaders need broadcast as other members are replicas and hence have the same state.
 */
@EagerInit
public class SnapshotDeployListener implements DeployListener {
    private LogicalComponentManager lcm;
    private NodeTopologyService topologyService;
    private HostInfo info;
    private ListenerMonitor monitor;

    public SnapshotDeployListener(@Reference(name = "lcm") LogicalComponentManager lcm,
                                  @Reference NodeTopologyService topologyService,
                                  @Reference HostInfo info,
                                  @Monitor ListenerMonitor monitor) {
        this.lcm = lcm;
        this.topologyService = topologyService;
        this.info = info;
        this.monitor = monitor;
    }

    public void onDeployCompleted(URI uri) {
        broadcastSnapshot(uri, LogicalState.NEW);
    }

    public void onUnDeploy(URI uri) {
        broadcastSnapshot(uri, LogicalState.MARKED);
    }

    public void onUnDeployCompleted(URI uri) {
    }

    public void onDeploy(URI uri) {
    }

    public void onDeploy(QName deployable) {
    }

    public void onDeployCompleted(QName deployable) {
        // a component is deployed programmatically via the fabric API
        if (HostNamespaces.SYNTHESIZED.equals(deployable.getNamespaceURI())) {
            broadcastSnapshot(Names.HOST_CONTRIBUTION, LogicalState.NEW);
        }
    }

    public void onUndeploy(QName undeployed) {
    }

    public void onUndeployCompleted(QName undeployed) {
    }

    private void broadcastSnapshot(URI uri, LogicalState state) {
        if (!topologyService.isZoneLeader()) {
            // only the zone leader should broadcast
            return;
        }
        try {
            LogicalCompositeComponent domain = lcm.getRootComponent();
            LogicalCompositeComponent snapshot = SnapshotHelper.snapshot(domain, uri, state);
            if (snapshot.getComponents().isEmpty() && snapshot.getChannels().isEmpty()) {
                return; // no artifacts deployed
            }
            String runtimeName = info.getRuntimeName();
            DeploymentSnapshotCommand command = new DeploymentSnapshotCommand(runtimeName, snapshot);
            topologyService.broadcast(command);
        } catch (Fabric3Exception e) {
            monitor.error(e);
        }
    }

}
