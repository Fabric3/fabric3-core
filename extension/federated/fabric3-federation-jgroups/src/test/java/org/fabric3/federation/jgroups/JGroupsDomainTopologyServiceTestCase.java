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

/**
 * @version $Rev$ $Date$
 */
public class JGroupsDomainTopologyServiceTestCase extends AbstractJGroupsTestCase {

    public void _testBroadcast() throws Exception {
        executorRegistry.execute(EasyMock.isA(MockCommand.class));
        EasyMock.expectLastCall().times(2);
        EasyMock.replay(executorRegistry);

        JGroupsDomainTopologyService domainTopologyService =
                new JGroupsDomainTopologyService(info, executorRegistry, eventService, executor, helper, monitor);
        domainTopologyService.init();
        joinDomain(domainTopologyService);

        JGroupsZoneTopologyService zoneTopologyService = createAndJoin("service1");
        JGroupsZoneTopologyService zoneTopologyService2 = createAndJoin("service2", "domain2");

        Thread.sleep(8000);
        // message should be received by both services in the different zones
        domainTopologyService.broadcastMessage(serializedCommand);

        Thread.sleep(1000);

        stop(domainTopologyService, zoneTopologyService, zoneTopologyService2);

        EasyMock.verify(executorRegistry);
    }

    public void _testBroadcastToZone() throws Exception {
        executorRegistry.execute(EasyMock.isA(MockCommand.class));
        // should only receive one call
        EasyMock.expectLastCall().times(1);
        EasyMock.replay(executorRegistry);

        JGroupsDomainTopologyService domainTopologyService =
                new JGroupsDomainTopologyService(info, executorRegistry, eventService, executor, helper, monitor);
        domainTopologyService.init();
        joinDomain(domainTopologyService);

        JGroupsZoneTopologyService zoneTopologyService = createAndJoin("service1");
        JGroupsZoneTopologyService zoneTopologyService2 = createAndJoin("service2", "domain2");

        // service2 should not receive any messages since it is in a different zone
        domainTopologyService.broadcastMessage("default.zone", serializedCommand);

        Thread.sleep(1000);

        stop(domainTopologyService, zoneTopologyService, zoneTopologyService2);

        EasyMock.verify(executorRegistry);
    }

    public void _testSendToRuntime() throws Exception {
        executorRegistry.execute(EasyMock.isA(MockCommand.class));
        EasyMock.expectLastCall().times(2);
        EasyMock.replay(executorRegistry);

        JGroupsDomainTopologyService domainTopologyService =
                new JGroupsDomainTopologyService(info, executorRegistry, eventService, executor, helper, monitor);
        domainTopologyService.init();
        joinDomain(domainTopologyService);

        JGroupsZoneTopologyService zoneTopologyService = createAndJoin("service1");
        JGroupsZoneTopologyService zoneTopologyService2 = createAndJoin("service2");

        // send messages to both services
        domainTopologyService.sendSynchronousMessage(zoneTopologyService.getRuntimeName(), serializedCommand, 2000);
        domainTopologyService.sendSynchronousMessage(zoneTopologyService2.getRuntimeName(), serializedCommand, 2000);

        Thread.sleep(1000);

        stop(domainTopologyService, zoneTopologyService, zoneTopologyService2);

        EasyMock.verify(executorRegistry);
    }

    public void _testSendToZone() throws Exception {
        executorRegistry.execute(EasyMock.isA(MockCommand.class));
        EasyMock.expectLastCall().times(2);
        EasyMock.replay(executorRegistry);

        JGroupsDomainTopologyService domainTopologyService =
                new JGroupsDomainTopologyService(info, executorRegistry, eventService, executor, helper, monitor);
        domainTopologyService.init();
        joinDomain(domainTopologyService);

        JGroupsZoneTopologyService zoneTopologyService = createAndJoin("service1");
        JGroupsZoneTopologyService zoneTopologyService2 = createAndJoin("service2");

        // should send messages to both services as they are in the same zone
        domainTopologyService.sendSynchronousMessageToZone("default.zone", serializedCommand, 2000);

        Thread.sleep(1000);

        stop(domainTopologyService, zoneTopologyService, zoneTopologyService2);

        EasyMock.verify(executorRegistry);
    }


}
