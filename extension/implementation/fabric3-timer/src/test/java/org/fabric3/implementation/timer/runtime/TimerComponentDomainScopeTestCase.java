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
package org.fabric3.implementation.timer.runtime;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import junit.framework.TestCase;
import org.easymock.EasyMock;

import org.fabric3.api.model.type.RuntimeMode;
import org.fabric3.api.host.runtime.HostInfo;
import org.fabric3.implementation.pojo.manager.ImplementationManagerFactory;
import org.fabric3.api.implementation.timer.model.TimerData;
import org.fabric3.api.implementation.timer.model.TimerType;
import org.fabric3.api.model.type.component.Scope;
import org.fabric3.spi.container.component.ScopeContainer;
import org.fabric3.spi.federation.topology.ParticipantTopologyService;
import org.fabric3.timer.spi.TimerService;

/**
 *
 */
public class TimerComponentDomainScopeTestCase extends TestCase {
    private TimerComponent component;
    private ParticipantTopologyService topologyService;
    private TimerService timerService;

    public void testNotLeaderNoSchedule() throws Exception {

        topologyService.register(component);
        EasyMock.expect(topologyService.isZoneLeader()).andReturn(false);
        topologyService.deregister(component);

        EasyMock.replay(timerService, topologyService);

        component.start();
        component.stop();

        EasyMock.verify(timerService, topologyService);
    }

    public void testScheduleWhenElectedLeader() throws Exception {

        ScheduledFuture<?> future = EasyMock.createNiceMock(ScheduledFuture.class);
        EasyMock.expect(future.isCancelled()).andReturn(true);

        timerService.scheduleWithFixedDelay(EasyMock.eq("testPool"),
                                            EasyMock.isA(NonTransactionalTimerInvoker.class),
                                            EasyMock.eq(2000l),
                                            EasyMock.eq(1000l),
                                            EasyMock.eq(TimeUnit.MILLISECONDS));
        EasyMock.expectLastCall().andReturn(future);

        topologyService.register(component);
        EasyMock.expect(topologyService.isZoneLeader()).andReturn(false);
        EasyMock.expect(topologyService.isZoneLeader()).andReturn(true);
        topologyService.deregister(component);

        EasyMock.replay(timerService, topologyService, future);

        component.start();
        component.onLeaderElected("vm");

        component.stop();

        EasyMock.verify(timerService, topologyService, future);
    }

    @SuppressWarnings({"unchecked"})
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        TimerData data = new TimerData();
        data.setPoolName("testPool");
        data.setType(TimerType.INTERVAL);
        data.setRepeatInterval(1000);
        data.setInitialDelay(2000);

        timerService = EasyMock.createMock(TimerService.class);

        InvokerMonitor monitor = EasyMock.createNiceMock(InvokerMonitor.class);

        ScopeContainer container = EasyMock.createNiceMock(ScopeContainer.class);
        EasyMock.expect(container.getScope()).andReturn(Scope.DOMAIN);

        HostInfo info = EasyMock.createMock(HostInfo.class);
        EasyMock.expect(info.getRuntimeMode()).andReturn(RuntimeMode.PARTICIPANT);

        ImplementationManagerFactory factory = EasyMock.createMock(ImplementationManagerFactory.class);
        EasyMock.expect(factory.getImplementationClass()).andReturn((Class) TimerInstance.class);

        EasyMock.replay(container, monitor, factory, info);

        topologyService = EasyMock.createMock(ParticipantTopologyService.class);

        component = new TimerComponent(null,
                                       null,
                                       data,
                                       TimerInstance.class,
                                       false,
                                       factory,
                                       container,
                                       timerService,
                                       null,
                                       topologyService,
                                       info,
                                       monitor,
                                       true);
    }

    private interface TimerInstance extends Runnable {
        long nextInterval();
    }

}

