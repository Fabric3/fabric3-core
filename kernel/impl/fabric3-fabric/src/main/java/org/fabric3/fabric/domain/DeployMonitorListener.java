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
package org.fabric3.fabric.domain;

import javax.xml.namespace.QName;
import java.net.URI;

import org.fabric3.api.annotation.monitor.Monitor;
import org.fabric3.api.host.runtime.HostInfo;
import org.fabric3.api.model.type.RuntimeMode;
import org.fabric3.spi.domain.DeployListener;
import org.oasisopen.sca.annotation.Reference;

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

    public void onDeploy(QName deployable) {
        if (enabled) {
            monitor.deploy(deployable);
        }
    }

    public void onDeployCompleted(QName deployable) {
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
