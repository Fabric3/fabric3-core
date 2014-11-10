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

import javax.transaction.TransactionManager;
import java.lang.reflect.Method;

import junit.framework.TestCase;
import org.easymock.EasyMock;
import org.fabric3.implementation.pojo.manager.ImplementationManagerFactory;
import org.fabric3.api.model.type.component.Scope;
import org.fabric3.spi.container.component.ScopeContainer;
import org.fabric3.spi.container.wire.InvocationRuntimeException;
import org.oasisopen.sca.ServiceRuntimeException;

/**
 *
 */
public class TransactionalIntervalTaskTestCase extends TestCase {
    private InvokerMonitor monitor;
    private TimerInstance instance;

    @SuppressWarnings({"unchecked"})
    public void testNextIntervalMethod() throws Exception {
        EasyMock.expect(instance.nextInterval()).andReturn((long) 1000);

        TransactionManager tm = EasyMock.createMock(TransactionManager.class);
        tm.begin();
        tm.commit();

        TimerComponent component = createComponent(instance);

        EasyMock.replay(instance, tm);

        Method method = TimerInstance.class.getMethod("nextInterval");

        TransactionalIntervalTask task = new TransactionalIntervalTask(component, null, method, tm, monitor);
        assertEquals(1000, task.nextInterval());

        EasyMock.verify(instance, tm);
    }

    @SuppressWarnings({"ThrowableInstanceNeverThrown"})
    public void testRollbackInvocationRuntimeException() throws Exception {
        EasyMock.expect(instance.nextInterval()).andThrow(new InvocationRuntimeException());

        TransactionManager tm = EasyMock.createMock(TransactionManager.class);
        tm.begin();
        tm.rollback();

        TimerComponent component = createComponent(instance);

        EasyMock.replay(instance, tm);

        Method method = TimerInstance.class.getMethod("nextInterval");

        TransactionalIntervalTask task = new TransactionalIntervalTask(component, null, method, tm, monitor);
        try {
            task.nextInterval();
            fail();
        } catch (InvocationRuntimeException e) {
            // expected
        }
        EasyMock.verify(instance, tm);
    }

    @SuppressWarnings({"ThrowableInstanceNeverThrown"})
    public void testRollbackRuntimeException() throws Exception {
        EasyMock.expect(instance.nextInterval()).andReturn(1000l);

        TransactionManager tm = EasyMock.createMock(TransactionManager.class);
        tm.begin();
        tm.commit();
        EasyMock.expectLastCall().andThrow(new RuntimeException());
        tm.rollback();
        TimerComponent component = createComponent(instance);

        EasyMock.replay(instance, tm);

        Method method = TimerInstance.class.getMethod("nextInterval");

        TransactionalIntervalTask task = new TransactionalIntervalTask(component, null, method, tm, monitor);
        try {
            task.nextInterval();
            fail();
        } catch (ServiceRuntimeException e) {
            // expected
        }
        EasyMock.verify(instance, tm);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        monitor = EasyMock.createNiceMock(InvokerMonitor.class);

        instance = EasyMock.createMock(TimerInstance.class);
    }

    @SuppressWarnings({"unchecked"})
    private TimerComponent createComponent(TimerInstance instance) throws Exception {
        ScopeContainer container = EasyMock.createMock(ScopeContainer.class);
        EasyMock.expect(container.getScope()).andReturn(Scope.COMPOSITE);
        EasyMock.expect(container.getInstance(EasyMock.isA(TimerComponent.class))).andReturn(instance);
        container.releaseInstance(EasyMock.isA(TimerComponent.class), EasyMock.eq(instance));

        ImplementationManagerFactory factory = EasyMock.createMock(ImplementationManagerFactory.class);
        EasyMock.expect(factory.getImplementationClass()).andReturn((Class) TimerInstance.class);

        EasyMock.replay(container, factory);

        return new TimerComponent(null, null, null, TimerInstance.class, false, factory, container, null, null, null, null, null, true);
    }

    private interface TimerInstance extends Runnable {
        long nextInterval();
    }

}

