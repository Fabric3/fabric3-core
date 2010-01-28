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
public class JGroupsZoneTopologyServiceTestCase extends AbstractJGroupsTestCase {


    public void _testBroadcastMessage() throws Exception {
        executorRegistry.execute(EasyMock.isA(MockCommand.class));
        // two invocation expected since local delivery is off
        EasyMock.expectLastCall().times(2);
        EasyMock.replay(executorRegistry);

        JGroupsZoneTopologyService service1 = createAndJoin("service1");
        JGroupsZoneTopologyService service2 = createAndJoin("service2");
        JGroupsZoneTopologyService service3 = createAndJoin("service3");

        Thread.sleep(500);

        service1.broadcast(command);
        // verify that service2 becomes the zone manager
        Thread.sleep(1000);

        stop(service1, service2, service3);

        EasyMock.verify(executorRegistry);
    }

    public void _testSendSynchronousZoneMessage() throws Exception {
        executorRegistry.execute(EasyMock.isA(MockCommand.class));
        // invoke should be made twice from service2 and service3 since local delivery is off
        EasyMock.expectLastCall().times(2);
        EasyMock.replay(executorRegistry);

        JGroupsZoneTopologyService service1 = createAndJoin("service1");
        JGroupsZoneTopologyService service2 = createAndJoin("service2");
        JGroupsZoneTopologyService service3 = createAndJoin("service2");

        service1.sendSynchronous(command, 3000);

        stop(service1, service2, service3);

        EasyMock.verify(executorRegistry);
    }

    public void _testSendSynchronousRuntimeMessage() throws Exception {
        executorRegistry.execute(EasyMock.isA(MockCommand.class));
        // invoke should be made one from service2 and service3 since local delivery is off
        EasyMock.expectLastCall().times(1);
        EasyMock.replay(executorRegistry);

        JGroupsZoneTopologyService service1 = createAndJoin("service1");
        JGroupsZoneTopologyService service2 = createAndJoin("service2");

        service1.sendSynchronous(service2.getRuntimeName(), command, 3000);

        stop(service1, service2);

        EasyMock.verify(executorRegistry);
    }

    public void _testSendSynchronousControllerMessage() throws Exception {
        EasyMock.replay(executorRegistry);

        JGroupsDomainTopologyService domainTopologyService =
                new JGroupsDomainTopologyService(info, executorRegistry, eventService, executor, helper, monitor);
        domainTopologyService.init();
        joinDomain(domainTopologyService);

        JGroupsZoneTopologyService service1 = createAndJoin("service1");
        JGroupsZoneTopologyService service2 = createAndJoin("service2");

        Thread.sleep(500);

        service1.sendSynchronousToController(command, 3000);
        // verify that service2 becomes the zone manager
        Thread.sleep(5000);

        stop(domainTopologyService, service1, service2);

        EasyMock.verify(executorRegistry);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
    }


}