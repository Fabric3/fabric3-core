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
        listener.onDeploy(DEPLOYABLE, "plan");
        listener.onDeployCompleted(DEPLOYABLE, "plan");
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
        listener.onDeploy(DEPLOYABLE, "plan");
        listener.onDeployCompleted(DEPLOYABLE, "plan");
        listener.onUndeploy(DEPLOYABLE);
        listener.onUndeployCompleted(DEPLOYABLE);

        EasyMock.verify(info, monitor);
    }

}
