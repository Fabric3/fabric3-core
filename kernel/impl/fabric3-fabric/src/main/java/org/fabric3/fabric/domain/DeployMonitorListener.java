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
package org.fabric3.fabric.domain;

import java.net.URI;
import javax.xml.namespace.QName;

import org.oasisopen.sca.annotation.Reference;

import org.fabric3.api.annotation.monitor.Monitor;
import org.fabric3.host.RuntimeMode;
import org.fabric3.host.runtime.HostInfo;
import org.fabric3.spi.domain.DeployListener;

/**
 * Listener that sends deployment and undeployment events to a monitor on the controller.
 */
public class DeployMonitorListener implements DeployListener {
    private boolean enabled;
    private DomainMonitor monitor;

    public DeployMonitorListener(@Reference HostInfo info, @Monitor DomainMonitor monitor) {
        enabled = RuntimeMode.CONTROLLER == info.getRuntimeMode();
        this.monitor = monitor;
    }

    public void onDeploy(QName deployable, String plan) {
        if (enabled) {
            monitor.deploy(deployable);
        }
    }

    public void onDeployCompleted(QName deployable, String plan) {
        if (enabled) {
            monitor.deploymentCompleted(deployable);
        }
    }

    public void onUndeploy(QName undeployed) {
        if (enabled) {
            monitor.undeploy(undeployed);
        }
    }

    public void onUndeployCompleted(QName undeployed) {
        if (enabled) {
            monitor.undeployCompleted(undeployed);
        }
    }

    public void onDeploy(URI contribution) {
        // no-op
    }

    public void onDeployCompleted(URI contribution) {
        // no-op
    }

    public void onUnDeploy(URI contribution) {
        // no-op
    }

    public void onUnDeployCompleted(URI contribution) {
        // no-op
    }
}
