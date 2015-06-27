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
 * Portions originally based on Apache Tuscany 2007
 * licensed under the Apache 2.0 license.
 */
package org.fabric3.implementation.timer.runtime;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import junit.framework.TestCase;
import org.easymock.EasyMock;
import org.fabric3.api.host.runtime.HostInfo;
import org.fabric3.api.implementation.timer.model.TimerData;
import org.fabric3.api.implementation.timer.model.TimerType;
import org.fabric3.api.model.type.RuntimeMode;
import org.fabric3.api.model.type.component.Scope;
import org.fabric3.implementation.pojo.manager.ImplementationManagerFactory;
import org.fabric3.spi.container.component.ScopeContainer;
import org.fabric3.spi.discovery.DiscoveryAgent;
import org.fabric3.timer.spi.TimerService;

/**
 *
 */
public class TimerComponentDomainScopeTestCase extends TestCase {
    private TimerComponent component;
    private DiscoveryAgent discoveryAgent;
    private TimerService timerService;

    @SuppressWarnings("unchecked")
    public void testNotLeaderNoSchedule() throws Exception {
        discoveryAgent.registerLeadershipListener(EasyMock.isA(Consumer.class));
        EasyMock.expect(discoveryAgent.isLeader()).andReturn(false);
        discoveryAgent.unRegisterLeadershipListener(EasyMock.isA(Consumer.class));

        EasyMock.replay(timerService, discoveryAgent);

        component.start();
        component.stop();

        EasyMock.verify(timerService, discoveryAgent);
    }

    @SuppressWarnings("unchecked")
    public void testScheduleWhenElectedLeader() throws Exception {

        ScheduledFuture<?> future = EasyMock.createNiceMock(ScheduledFuture.class);
        EasyMock.expect(future.isCancelled()).andReturn(true);

        timerService.scheduleWithFixedDelay(EasyMock.eq("testPool"),
                                            EasyMock.isA(NonTransactionalTimerInvoker.class),
                                            EasyMock.eq(2000l),
                                            EasyMock.eq(1000l),
                                            EasyMock.eq(TimeUnit.MILLISECONDS));
        EasyMock.expectLastCall().andReturn(future);

        discoveryAgent.registerLeadershipListener(EasyMock.isA(Consumer.class));
        EasyMock.expect(discoveryAgent.isLeader()).andReturn(false);
        discoveryAgent.unRegisterLeadershipListener(EasyMock.isA(Consumer.class));

        EasyMock.replay(timerService, discoveryAgent, future);

        component.start();
        component.onLeaderElected(true);

        component.stop();

        EasyMock.verify(timerService, discoveryAgent, future);
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
        EasyMock.expect(info.getRuntimeMode()).andReturn(RuntimeMode.NODE);

        ImplementationManagerFactory factory = EasyMock.createMock(ImplementationManagerFactory.class);
        EasyMock.expect(factory.getImplementationClass()).andReturn((Class) TimerInstance.class);

        EasyMock.replay(container, monitor, factory, info);

        discoveryAgent = EasyMock.createMock(DiscoveryAgent.class);

        component = new TimerComponent(null, data,
                                       TimerInstance.class,
                                       false,
                                       factory,
                                       container,
                                       timerService,
                                       null, discoveryAgent,
                                       info,
                                       monitor,
                                       true,
                                       null);
    }

    private interface TimerInstance extends Runnable {
        long nextInterval();
    }

}

