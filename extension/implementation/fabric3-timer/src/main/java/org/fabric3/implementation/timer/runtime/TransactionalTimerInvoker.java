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

import javax.transaction.HeuristicMixedException;
import javax.transaction.HeuristicRollbackException;
import javax.transaction.NotSupportedException;
import javax.transaction.RollbackException;
import javax.transaction.SystemException;
import javax.transaction.TransactionManager;

import org.oasisopen.sca.ServiceRuntimeException;

import org.fabric3.spi.component.InstanceDestructionException;
import org.fabric3.spi.component.InstanceLifecycleException;
import org.fabric3.spi.invocation.CallFrame;
import org.fabric3.spi.invocation.WorkContext;
import org.fabric3.spi.invocation.WorkContextTunnel;
import org.fabric3.spi.wire.InvocationRuntimeException;

/**
 * Invokes a timer component instance within the context of a transaction when a trigger has fired.
 *
 * @version $Rev: 7148 $ $Date: 2009-06-15 02:18:27 +0200 (Mon, 15 Jun 2009) $
 */
public class TransactionalTimerInvoker implements Runnable {
    private static final CallFrame FRAME = new CallFrame();
    private TimerComponent component;
    private TransactionManager tm;
    private InvokerMonitor monitor;

    public TransactionalTimerInvoker(TimerComponent component, TransactionManager tm, InvokerMonitor monitor) {
        this.component = component;
        this.tm = tm;
        this.monitor = monitor;
    }

    public void run() {
        // create a new work context
        WorkContext workContext = new WorkContext();
        workContext.addCallFrame(FRAME);
        Object instance;
        try {
            instance = component.getInstance(workContext);
        } catch (InstanceLifecycleException e) {
            monitor.initError(e);
            throw new InvocationRuntimeException(e);
        }

        WorkContext oldWorkContext = WorkContextTunnel.setThreadWorkContext(workContext);
        try {
            tm.begin();
            ((Runnable) instance).run();
            tm.commit();
        } catch (HeuristicRollbackException e) {
            monitor.executeError(e);
            // propagate to the scheduler
            throw new ServiceRuntimeException(e);
        } catch (RollbackException e) {
            monitor.executeError(e);
            // propagate to the scheduler
            throw new ServiceRuntimeException(e);
        } catch (SystemException e) {
            monitor.executeError(e);
            // propagate to the scheduler
            throw new ServiceRuntimeException(e);
        } catch (HeuristicMixedException e) {
            monitor.executeError(e);
            // propagate to the scheduler
            throw new ServiceRuntimeException(e);
        } catch (NotSupportedException e) {
            monitor.executeError(e);
            // propagate to the scheduler
            throw new ServiceRuntimeException(e);
        } catch (RuntimeException e) {
            monitor.executeError(e);
            try {
                tm.rollback();
            } catch (SystemException ex) {
                monitor.executeError(e);
            }
            throw new ServiceRuntimeException(e);
        } finally {
            WorkContextTunnel.setThreadWorkContext(oldWorkContext);
            try {
                component.releaseInstance(instance, workContext);
            } catch (InstanceDestructionException e) {
                monitor.disposeError(e);
            }
        }

    }
}