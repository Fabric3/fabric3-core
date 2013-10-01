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
package org.fabric3.federation.node.executor;

import javax.xml.namespace.QName;
import java.net.URI;

import org.fabric3.federation.node.command.DomainSnapshotCommand;
import org.fabric3.federation.node.command.DomainSnapshotResponse;
import org.fabric3.federation.node.snapshot.SnapshotHelper;
import org.fabric3.spi.domain.DeployListener;
import org.fabric3.spi.executor.CommandExecutor;
import org.fabric3.spi.executor.CommandExecutorRegistry;
import org.fabric3.spi.executor.ExecutionException;
import org.fabric3.spi.lcm.LogicalComponentManager;
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

    public synchronized void execute(DomainSnapshotCommand command) throws ExecutionException {
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

    public void onDeploy(QName deployable, String plan) {
    }

    public void onDeployCompleted(QName deployable, String plan) {
    }

    public void onUndeploy(QName undeployed) {
    }

    public void onUndeployCompleted(QName undeployed) {
    }
}
