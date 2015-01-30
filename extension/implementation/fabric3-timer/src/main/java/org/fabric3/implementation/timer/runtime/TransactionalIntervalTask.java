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

import javax.transaction.HeuristicMixedException;
import javax.transaction.HeuristicRollbackException;
import javax.transaction.NotSupportedException;
import javax.transaction.RollbackException;
import javax.transaction.SystemException;
import javax.transaction.TransactionManager;
import java.lang.reflect.Method;

import org.fabric3.spi.container.wire.InvocationRuntimeException;
import org.fabric3.timer.spi.Task;
import org.oasisopen.sca.ServiceRuntimeException;

/**
 * A {@link Task} implementation that returns the next firing interval by calling a <code>nextInterval</code> method on the timer component
 * implementation in the context of a transaction.
 */
public class TransactionalIntervalTask extends NonTransactionalIntervalTask {
    private TransactionManager tm;
    private InvokerMonitor monitor;

    public TransactionalIntervalTask(TimerComponent component, Runnable delegate, Method method, TransactionManager tm, InvokerMonitor monitor)
            throws NoSuchMethodException {
        super(component, delegate, method, monitor);
        this.tm = tm;
        this.monitor = monitor;
    }

    public long nextInterval() {
        try {
            tm.begin();
            long value = super.nextInterval();
            tm.commit();
            return value;
        } catch (NotSupportedException | HeuristicMixedException | RollbackException | HeuristicRollbackException | SystemException e) {
            monitor.executeError(e);
            // propagate to the scheduler
            throw new ServiceRuntimeException(e);
        } catch (InvocationRuntimeException e) {
            // already been sent to the monitor from the super method
            try {
                tm.rollback();
            } catch (SystemException ex) {
                monitor.executeError(ex);
            }
            throw e;
        } catch (RuntimeException e) {
            monitor.executeError(e);
            try {
                tm.rollback();
            } catch (SystemException ex) {
                monitor.executeError(ex);
            }
            // propagate to the scheduler
            throw new ServiceRuntimeException(e);
        }
    }

}

