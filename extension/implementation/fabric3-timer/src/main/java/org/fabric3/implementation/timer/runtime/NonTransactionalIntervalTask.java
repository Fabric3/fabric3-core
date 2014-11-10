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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.fabric3.spi.container.component.InstanceDestructionException;
import org.fabric3.spi.container.component.InstanceLifecycleException;
import org.fabric3.spi.container.invocation.WorkContext;
import org.fabric3.spi.container.invocation.WorkContextCache;
import org.fabric3.spi.container.wire.InvocationRuntimeException;
import org.fabric3.timer.spi.Task;

/**
 * A {@link Task} implementation that returns the next firing interval by calling a <code>nextInterval</code> method on the timer component
 * implementation.
 */
public class NonTransactionalIntervalTask implements Task {
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
        WorkContext workContext = WorkContextCache.getAndResetThreadWorkContext();

        Object instance = null;
        try {
            instance = component.getInstance();
            return (Long) method.invoke(instance);
        } catch (InstanceLifecycleException | IllegalAccessException | InvocationTargetException e) {
            monitor.executeError(e);
            throw new InvocationRuntimeException(e);
        } finally {
            if (instance != null) {
                try {
                    component.releaseInstance(instance);
                } catch (InstanceDestructionException e) {
                    monitor.executeError(e);
                }
            }
            workContext.reset();
        }
    }

    public void run() {
        delegate.run();
    }
}

