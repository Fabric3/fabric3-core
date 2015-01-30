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

import org.fabric3.spi.container.ContainerException;
import org.fabric3.spi.container.invocation.WorkContext;
import org.fabric3.spi.container.invocation.WorkContextCache;
import org.fabric3.spi.container.wire.InvocationRuntimeException;

/**
 * Invokes a timer component instance when a trigger has fired.
 */
public class NonTransactionalTimerInvoker implements Runnable {
    private TimerComponent component;
    private InvokerMonitor monitor;

    public NonTransactionalTimerInvoker(TimerComponent component, InvokerMonitor monitor) {
        this.component = component;
        this.monitor = monitor;
    }

    public void run() {
        // create a new work context
        WorkContext workContext = WorkContextCache.getAndResetThreadWorkContext();
        Object instance;
        try {
            instance = component.getInstance();
        } catch (ContainerException e) {
            monitor.initError(e);
            throw new InvocationRuntimeException(e);
        }

        ClassLoader old = Thread.currentThread().getContextClassLoader();
        try {
            Thread.currentThread().setContextClassLoader(instance.getClass().getClassLoader());
            try {
                ((Runnable) instance).run();
            } catch (RuntimeException e) {
                monitor.executeError(e);
                throw e;
            }
        } finally {
            try {
                component.releaseInstance(instance);
            } catch (ContainerException e) {
                monitor.disposeError(e);
                //noinspection ThrowFromFinallyBlock
                throw new InvocationRuntimeException(e);
            }
            Thread.currentThread().setContextClassLoader(old);
            workContext.reset();
        }

    }
}
