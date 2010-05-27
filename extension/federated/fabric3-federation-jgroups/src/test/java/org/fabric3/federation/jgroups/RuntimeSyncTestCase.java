/*
 * Fabric3
 * Copyright (c) 2009 Metaform Systems
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
package org.fabric3.federation.jgroups;

import org.easymock.EasyMock;
import org.easymock.IAnswer;
import org.jgroups.ChannelException;

import org.fabric3.federation.deployment.command.DeploymentCommand;
import org.fabric3.federation.deployment.command.RuntimeUpdateCommand;
import org.fabric3.federation.deployment.command.RuntimeUpdateResponse;

/**
 * @version $Rev: 8584 $ $Date: 2010-01-16 20:18:48 +0100 (Sat, 16 Jan 2010) $
 */
public class RuntimeSyncTestCase extends AbstractJGroupsTestCase {

    public void _testControllerSync() throws Exception {
        executorRegistry.execute(EasyMock.isA(RuntimeUpdateCommand.class));
        EasyMock.expectLastCall().andAnswer(new IAnswer<Object>() {
            public Object answer() throws Throwable {
                RuntimeUpdateCommand command = (RuntimeUpdateCommand) EasyMock.getCurrentArguments()[0];
                DeploymentCommand deploymentCommand = new DeploymentCommand("zone", null, null);
                RuntimeUpdateResponse response = new RuntimeUpdateResponse(deploymentCommand);
                command.setResponse(response);
                return command;
            }
        });
        executorRegistry.execute(EasyMock.isA(DeploymentCommand.class));
        EasyMock.expectLastCall();
        EasyMock.replay(executorRegistry);

        JGroupsDomainTopologyService domainTopologyService =
                new JGroupsDomainTopologyService(info, executorRegistry, eventService, executor, helper, monitor);
        domainTopologyService.init();
        joinDomain(domainTopologyService);

        JGroupsZoneTopologyService zoneTopologyService = createAndSync("service1");
        Thread.sleep(1000);

        stop(domainTopologyService, zoneTopologyService);

        EasyMock.verify(executorRegistry);
    }

    public void _testZoneLeaderSync() throws Exception {
        executorRegistry.execute(EasyMock.isA(RuntimeUpdateCommand.class));
        EasyMock.expectLastCall().andAnswer(new IAnswer<Object>() {
            public Object answer() throws Throwable {
                RuntimeUpdateCommand command = (RuntimeUpdateCommand) EasyMock.getCurrentArguments()[0];
                DeploymentCommand deploymentCommand = new DeploymentCommand("zone", null, null);
                RuntimeUpdateResponse response = new RuntimeUpdateResponse(deploymentCommand);
                command.setResponse(response);
                return command;
            }
        });
        executorRegistry.execute(EasyMock.isA(DeploymentCommand.class));
        EasyMock.expectLastCall();
        EasyMock.replay(executorRegistry);

        JGroupsZoneTopologyService service2 = createAndJoin("service2");
        JGroupsZoneTopologyService service1 = createAndSync("service1");
        Thread.sleep(1000);

        stop(service1, service2);

        EasyMock.verify(executorRegistry);
    }


    protected JGroupsZoneTopologyService createAndSync(String name) throws ChannelException {
        TopologyServiceMonitor monitor = EasyMock.createNiceMock(TopologyServiceMonitor.class);
        EasyMock.replay(monitor);
        JGroupsZoneTopologyService service = new JGroupsZoneTopologyService(info, executorRegistry, eventService, executor, helper, monitor);
        service.init();
        joinDomain(service);
        return service;
    }


}