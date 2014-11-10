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

import junit.framework.TestCase;
import org.easymock.EasyMock;

import org.fabric3.api.model.type.RuntimeMode;
import org.fabric3.api.host.runtime.HostInfo;

/**
 *
 */
public class DeployMonitorListenerTestCase extends TestCase {
    private static final QName DEPLOYABLE = new QName("test", "composite");

    public void testEnabled() throws Exception {
        HostInfo info = EasyMock.createMock(HostInfo.class);
        EasyMock.expect(info.getRuntimeMode()).andReturn(RuntimeMode.CONTROLLER);

        DomainMonitor monitor = EasyMock.createMock(DomainMonitor.class);
        monitor.undeployCompleted(DEPLOYABLE);
        monitor.deploy(DEPLOYABLE);
        monitor.deploymentCompleted(DEPLOYABLE);
        monitor.undeploy(DEPLOYABLE);
        EasyMock.replay(info, monitor);

        DeployMonitorListener listener = new DeployMonitorListener(info, monitor);
        listener.onDeploy(DEPLOYABLE);
        listener.onDeployCompleted(DEPLOYABLE);
        listener.onUndeploy(DEPLOYABLE);
        listener.onUndeployCompleted(DEPLOYABLE);

        EasyMock.verify(info, monitor);
    }

    public void testNotEnabled() throws Exception {
        HostInfo info = EasyMock.createMock(HostInfo.class);
        EasyMock.expect(info.getRuntimeMode()).andReturn(RuntimeMode.PARTICIPANT);

        DomainMonitor monitor = EasyMock.createMock(DomainMonitor.class);
        EasyMock.replay(info, monitor);

        DeployMonitorListener listener = new DeployMonitorListener(info, monitor);
        listener.onDeploy(DEPLOYABLE);
        listener.onDeployCompleted(DEPLOYABLE);
        listener.onUndeploy(DEPLOYABLE);
        listener.onUndeployCompleted(DEPLOYABLE);

        EasyMock.verify(info, monitor);
    }

}
