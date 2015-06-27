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

import junit.framework.TestCase;
import org.easymock.EasyMock;
import org.fabric3.api.implementation.timer.model.TimerData;
import org.fabric3.api.implementation.timer.model.TimerType;
import org.fabric3.api.model.type.component.Scope;
import org.fabric3.implementation.pojo.manager.ImplementationManagerFactory;
import org.fabric3.spi.container.component.ScopeContainer;
import org.fabric3.timer.spi.TimerService;

/**
 *
 */
public class TimerComponentTestCase extends TestCase {

    public void testScheduleRecurringNonTransactional() throws Exception {

        TimerData data = new TimerData();
        data.setPoolName("testPool");
        data.setType(TimerType.RECURRING);
        data.setIntervalMethod(true);

        ScheduledFuture<?> future = EasyMock.createNiceMock(ScheduledFuture.class);
        TimerService timerService = EasyMock.createMock(TimerService.class);
        timerService.scheduleRecurring(EasyMock.eq("testPool"), EasyMock.isA(NonTransactionalIntervalTask.class));
        EasyMock.expectLastCall().andReturn(future);

        EasyMock.replay(timerService);

        TimerComponent component = createComponent(data, timerService, false);

        component.start();

        EasyMock.verify(timerService);
    }

    public void testScheduleRecurringTransactional() throws Exception {

        TimerData data = new TimerData();
        data.setPoolName("testPool");
        data.setType(TimerType.RECURRING);
        data.setIntervalMethod(true);

        ScheduledFuture<?> future = EasyMock.createNiceMock(ScheduledFuture.class);
        TimerService timerService = EasyMock.createMock(TimerService.class);
        timerService.scheduleRecurring(EasyMock.eq("testPool"), EasyMock.isA(TransactionalIntervalTask.class));
        EasyMock.expectLastCall().andReturn(future);

        EasyMock.replay(timerService);

        TimerComponent component = createComponent(data, timerService, true);

        component.start();

        EasyMock.verify(timerService);
    }

    public void testScheduleRecurringIntervalClass() throws Exception {

        TimerData data = new TimerData();
        data.setPoolName("testPool");
        data.setType(TimerType.RECURRING);
        data.setIntervalClass(IntervalInstance.class.getName());

        ScheduledFuture<?> future = EasyMock.createNiceMock(ScheduledFuture.class);
        TimerService timerService = EasyMock.createMock(TimerService.class);
        timerService.scheduleRecurring(EasyMock.eq("testPool"), EasyMock.isA(IntervalClassTask.class));
        EasyMock.expectLastCall().andReturn(future);

        EasyMock.replay(timerService);

        TimerComponent component = createComponent(data, timerService, true);

        component.start();

        EasyMock.verify(timerService);
    }

    public void testScheduleAtFixedRate() throws Exception {

        TimerData data = new TimerData();
        data.setPoolName("testPool");
        data.setType(TimerType.FIXED_RATE);
        data.setFixedRate(1000);
        data.setInitialDelay(2000);

        ScheduledFuture<?> future = EasyMock.createNiceMock(ScheduledFuture.class);
        TimerService timerService = EasyMock.createMock(TimerService.class);
        timerService.scheduleAtFixedRate(EasyMock.eq("testPool"),
                                         EasyMock.isA(TransactionalTimerInvoker.class),
                                         EasyMock.eq(2000l),
                                         EasyMock.eq(1000l),
                                         EasyMock.eq(TimeUnit.MILLISECONDS));
        EasyMock.expectLastCall().andReturn(future);

        EasyMock.replay(timerService);

        TimerComponent component = createComponent(data, timerService, true);

        component.start();

        EasyMock.verify(timerService);
    }

    public void testScheduleInterval() throws Exception {

        TimerData data = new TimerData();
        data.setPoolName("testPool");
        data.setType(TimerType.INTERVAL);
        data.setRepeatInterval(1000);
        data.setInitialDelay(2000);

        ScheduledFuture<?> future = EasyMock.createNiceMock(ScheduledFuture.class);
        TimerService timerService = EasyMock.createMock(TimerService.class);
        timerService.scheduleWithFixedDelay(EasyMock.eq("testPool"),
                                            EasyMock.isA(TransactionalTimerInvoker.class),
                                            EasyMock.eq(2000l),
                                            EasyMock.eq(1000l),
                                            EasyMock.eq(TimeUnit.MILLISECONDS));
        EasyMock.expectLastCall().andReturn(future);

        EasyMock.replay(timerService);

        TimerComponent component = createComponent(data, timerService, true);

        component.start();

        EasyMock.verify(timerService);
    }

    public void testScheduleOnce() throws Exception {

        TimerData data = new TimerData();
        data.setPoolName("testPool");
        data.setType(TimerType.ONCE);
        data.setFireOnce(1000);

        ScheduledFuture<?> future = EasyMock.createNiceMock(ScheduledFuture.class);
        TimerService timerService = EasyMock.createMock(TimerService.class);
        timerService.schedule(EasyMock.eq("testPool"),
                                            EasyMock.isA(TransactionalTimerInvoker.class),
                                            EasyMock.eq(1000l),
                                            EasyMock.eq(TimeUnit.MILLISECONDS));
        EasyMock.expectLastCall().andReturn(future);

        EasyMock.replay(timerService);

        TimerComponent component = createComponent(data, timerService, true);

        component.start();

        EasyMock.verify(timerService);
    }

    @SuppressWarnings({"unchecked"})
    private TimerComponent createComponent(TimerData data, TimerService timerService, boolean transactional) {
        InvokerMonitor monitor = EasyMock.createNiceMock(InvokerMonitor.class);

        ScopeContainer container = EasyMock.createNiceMock(ScopeContainer.class);
        EasyMock.expect(container.getScope()).andReturn(Scope.COMPOSITE);

        ImplementationManagerFactory factory = EasyMock.createMock(ImplementationManagerFactory.class);
        EasyMock.expect(factory.getImplementationClass()).andReturn((Class) TimerInstance.class);

        EasyMock.replay(container, monitor, factory);

        return new TimerComponent(null, data, TimerInstance.class, transactional, factory, container, timerService, null, null, null, monitor, true, null);
    }

    private interface TimerInstance extends Runnable {
        long nextInterval();
    }

    private static class IntervalInstance implements Runnable {
        public IntervalInstance() {
        }

        public long nextInterval() {
            return 10;
        }

        public void run() {

        }
    }

}

