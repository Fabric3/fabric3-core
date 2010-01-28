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

import java.net.URI;
import java.util.concurrent.Executor;

import junit.framework.TestCase;
import org.easymock.EasyMock;
import org.jgroups.ChannelException;
import org.jgroups.util.DirectExecutor;

import org.fabric3.host.runtime.HostInfo;
import org.fabric3.spi.command.ResponseCommand;
import org.fabric3.spi.event.EventService;
import org.fabric3.spi.event.Fabric3EventListener;
import org.fabric3.spi.event.JoinDomain;
import org.fabric3.spi.event.RuntimeStop;
import org.fabric3.spi.executor.CommandExecutorRegistry;

/**
 * @version $Rev$ $Date$
 */
public class AbstractJGroupsTestCase extends TestCase {
    protected HostInfo info;
    protected EventService eventService;
    protected CommandExecutorRegistry executorRegistry;
    protected JGroupsHelper helper;
    protected Executor executor;
    protected ResponseCommand command;
    protected TopologyServiceMonitor monitor;


    public void testBlank() {
        // no-op test to avoid JUnit error for commented-out tests
    }

    protected JGroupsZoneTopologyService createAndJoin(String name, String zoneName) throws ChannelException {
        JGroupsZoneTopologyService service = new JGroupsZoneTopologyService(info, executorRegistry, eventService, executor, helper, monitor);
        service.setRuntimeId(name);
        service.setZoneName(zoneName);
        service.setSynchronize(false);
        service.init();
        joinDomain(service);
        return service;
    }

    protected JGroupsZoneTopologyService createAndJoin(String name) throws ChannelException {
        return createAndJoin(name, "default.zone");
    }

    protected void joinDomain(AbstractTopologyService service) throws ChannelException {
        JoinDomain join = new JoinDomain();
        service.init();
        Fabric3EventListener<JoinDomain> joinListener = service.getJoinListener();
        joinListener.onEvent(join);
    }

    protected void stop(AbstractTopologyService... services) {
        RuntimeStop stop = new RuntimeStop();
        for (AbstractTopologyService service : services) {
            Fabric3EventListener<RuntimeStop> stopListener = service.getStopListener();
            System.out.println("Stoppping " + service.getRuntimeName());
            stopListener.onEvent(stop);
        }
    }


    @Override
    protected void setUp() throws Exception {
        super.setUp();
        info = EasyMock.createMock(HostInfo.class);
        EasyMock.expect(info.getDomain()).andReturn(URI.create("fabric3://domain")).anyTimes();
        eventService = EasyMock.createNiceMock(EventService.class);
        executorRegistry = EasyMock.createMock(CommandExecutorRegistry.class);
        monitor = EasyMock.createNiceMock(TopologyServiceMonitor.class);
        EasyMock.replay(info, eventService, monitor);

        MockClassLoaderRegistry classLoaderRegistry = new MockClassLoaderRegistry();
        helper = new JGroupsHelperImpl(classLoaderRegistry);
        executor = new DirectExecutor(); //use the JBoss executor to avoid shutdown errors

        command = new MockCommand();
    }


}