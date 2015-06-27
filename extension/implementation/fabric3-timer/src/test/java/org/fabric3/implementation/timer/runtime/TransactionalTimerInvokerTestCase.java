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

import junit.framework.TestCase;
import org.easymock.EasyMock;
import org.fabric3.api.model.type.component.Scope;
import org.fabric3.implementation.pojo.manager.ImplementationManagerFactory;
import org.fabric3.spi.container.component.ScopeContainer;
import org.oasisopen.sca.ServiceRuntimeException;

/**
 *
 */
public class TransactionalTimerInvokerTestCase extends TestCase {
    private InvokerMonitor monitor;
    private TimerInstance instance;

    @SuppressWarnings({"unchecked"})
    public void testInvoke() throws Exception {
        instance.run();

        TransactionManager tm = EasyMock.createMock(TransactionManager.class);
        tm.begin();
        tm.commit();

        TimerComponent component = createComponent(instance);

        EasyMock.replay(instance, tm);

        TransactionalTimerInvoker invoker = new TransactionalTimerInvoker(component, tm, monitor);
        invoker.run();
        EasyMock.verify(instance, tm);
    }

    @SuppressWarnings({"ThrowableInstanceNeverThrown"})
    public void testRollbackRuntimeException() throws Exception {
        instance.run();
        EasyMock.expectLastCall().andThrow(new RuntimeException());

        TransactionManager tm = EasyMock.createMock(TransactionManager.class);
        tm.begin();
        tm.rollback();

        TimerComponent component = createComponent(instance);

        EasyMock.replay(instance, tm);

        TransactionalTimerInvoker invoker = new TransactionalTimerInvoker(component, tm, monitor);
        try {
            invoker.run();
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

        return new TimerComponent(null, null, TimerInstance.class, false, factory, container, null, null, null, null, null, true, null);
    }

    private interface TimerInstance extends Runnable {
        long nextInterval();
    }

}

