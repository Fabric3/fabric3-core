/*
* Fabric3
* Copyright (c) 2009-2011 Metaform Systems
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

import org.fabric3.implementation.pojo.instancefactory.InstanceFactoryProvider;
import org.fabric3.implementation.timer.provision.TimerData;
import org.fabric3.implementation.timer.provision.TimerType;
import org.fabric3.model.type.component.Scope;
import org.fabric3.spi.component.ScopeContainer;
import org.fabric3.timer.spi.TimerService;

/**
 * @version $Rev: 7881 $ $Date: 2009-11-22 10:32:23 +0100 (Sun, 22 Nov 2009) $
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

        InstanceFactoryProvider provider = EasyMock.createMock(InstanceFactoryProvider.class);
        EasyMock.expect(provider.getImplementationClass()).andReturn((Class) TimerInstance.class);

        EasyMock.replay(container, monitor, provider);

        return new TimerComponent(null, null, data, TimerInstance.class, transactional, provider, container, timerService, null, null, null, monitor);
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

