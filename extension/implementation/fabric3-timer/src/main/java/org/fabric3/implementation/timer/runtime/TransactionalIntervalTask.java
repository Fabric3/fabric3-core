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

import java.lang.reflect.Method;
import javax.transaction.HeuristicMixedException;
import javax.transaction.HeuristicRollbackException;
import javax.transaction.NotSupportedException;
import javax.transaction.RollbackException;
import javax.transaction.SystemException;
import javax.transaction.TransactionManager;

import org.oasisopen.sca.ServiceRuntimeException;

import org.fabric3.spi.wire.InvocationRuntimeException;
import org.fabric3.timer.spi.Task;

/**
 * A {@link Task} implementation that returns the next firing interval by calling a <code>nextInterval</code> method on the timer component
 * implementation in the context of a transaction.
 *
 * @version $Rev: 7881 $ $Date: 2009-11-22 10:32:23 +0100 (Sun, 22 Nov 2009) $
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
        } catch (NotSupportedException e) {
            monitor.executeError(e);
            // propagate to the scheduler
            throw new ServiceRuntimeException(e);
        } catch (SystemException e) {
            monitor.executeError(e);
            // propagate to the scheduler
            throw new ServiceRuntimeException(e);
        } catch (HeuristicRollbackException e) {
            monitor.executeError(e);
            // propagate to the scheduler
            throw new ServiceRuntimeException(e);
        } catch (RollbackException e) {
            monitor.executeError(e);
            // propagate to the scheduler
            throw new ServiceRuntimeException(e);
        } catch (HeuristicMixedException e) {
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

