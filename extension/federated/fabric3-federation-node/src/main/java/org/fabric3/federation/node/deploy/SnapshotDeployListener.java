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
package org.fabric3.federation.node.deploy;

import javax.xml.namespace.QName;
import java.net.URI;

import org.fabric3.api.annotation.monitor.Monitor;
import org.fabric3.federation.node.command.DeploymentSnapshotCommand;
import org.fabric3.federation.node.snapshot.SnapshotHelper;
import org.fabric3.spi.domain.DeployListener;
import org.fabric3.spi.federation.topology.MessageException;
import org.fabric3.spi.federation.topology.NodeTopologyService;
import org.fabric3.spi.lcm.LogicalComponentManager;
import org.fabric3.spi.model.instance.LogicalCompositeComponent;
import org.fabric3.spi.model.instance.LogicalState;
import org.oasisopen.sca.annotation.EagerInit;
import org.oasisopen.sca.annotation.Reference;

/**
 * Broadcasts new deployment snapshots to domain members if the current runtime is a zone leader. Snapshots are created when a deployment or un-deployment is
 * performed
 * <p/>
 * Note only zone leaders need broadcast as other members are replicas and hence have the same state.
 */
@EagerInit
public class SnapshotDeployListener implements DeployListener {
    private LogicalComponentManager lcm;
    private NodeTopologyService topologyService;
    private ListenerMonitor monitor;

    public SnapshotDeployListener(@Reference(name = "lcm") LogicalComponentManager lcm,
                                  @Reference NodeTopologyService topologyService,
                                  @Monitor ListenerMonitor monitor) {
        this.lcm = lcm;
        this.topologyService = topologyService;
        this.monitor = monitor;
    }

    public void onDeployCompleted(URI uri) {
        broadcastSnapshot(uri, LogicalState.NEW);
    }

    public void onUnDeployCompleted(URI uri) {
        broadcastSnapshot(uri, LogicalState.MARKED);
    }

    public void onDeploy(URI uri) {
    }

    public void onUnDeploy(URI uri) {
    }

    public void onDeploy(QName deployable, String plan) {
    }

    public void onDeployCompleted(QName deployable, String plan) {
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
            DeploymentSnapshotCommand command = new DeploymentSnapshotCommand(snapshot);
            topologyService.broadcast(command);
        } catch (MessageException e) {
            monitor.error(e);
        }
    }

}
