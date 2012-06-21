/*
* Fabric3
* Copyright (c) 2009-2012 Metaform Systems
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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.fabric3.spi.component.InstanceDestructionException;
import org.fabric3.spi.component.InstanceLifecycleException;
import org.fabric3.spi.invocation.CallFrame;
import org.fabric3.spi.invocation.WorkContext;
import org.fabric3.spi.invocation.WorkContextTunnel;
import org.fabric3.spi.wire.InvocationRuntimeException;
import org.fabric3.timer.spi.Task;

/**
 * A {@link Task} implementation that returns the next firing interval by calling a <code>nextInterval</code> method on the timer component
 * implementation.
 *
 * @version $Rev: 7881 $ $Date: 2009-11-22 10:32:23 +0100 (Sun, 22 Nov 2009) $
 */
public class NonTransactionalIntervalTask implements Task {
    private static final CallFrame FRAME = new CallFrame();
    private TimerComponent component;
    private Method method;
    private Runnable delegate;
    private InvokerMonitor monitor;

    public NonTransactionalIntervalTask(TimerComponent component, Runnable delegate, Method method, InvokerMonitor monitor)
            throws NoSuchMethodException {
        this.component = component;
        this.delegate = delegate;
        this.method = method;
        this.monitor = monitor;
    }

    public long nextInterval() {
        WorkContext workContext = new WorkContext();
        workContext.addCallFrame(FRAME);
        WorkContext oldWorkContext = WorkContextTunnel.setThreadWorkContext(workContext);

        Object instance = null;
        try {
            instance = component.getInstance(workContext);
            return (Long) method.invoke(instance);
        } catch (InstanceLifecycleException e) {
            monitor.executeError(e);
            throw new InvocationRuntimeException(e);
        } catch (InvocationTargetException e) {
            monitor.executeError(e);
            throw new InvocationRuntimeException(e);
        } catch (IllegalAccessException e) {
            monitor.executeError(e);
            throw new InvocationRuntimeException(e);
        } finally {
            WorkContextTunnel.setThreadWorkContext(oldWorkContext);
            if (instance != null) {
                try {
                    component.releaseInstance(instance, workContext);
                } catch (InstanceDestructionException e) {
                    monitor.executeError(e);
                }
            }
        }
    }

    public void run() {
        delegate.run();
    }
}

