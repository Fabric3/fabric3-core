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

import org.fabric3.timer.spi.Task;
import org.oasisopen.sca.ServiceRuntimeException;

/**
 * A {@link Task} implementation that returns the next firing interval by calling a <code>nextInterval</code> method on a specialized interval class.
 */
public class IntervalClassTask implements Task {
    private Method method;
    private Object interval;
    private Runnable delegate;

    public IntervalClassTask(Object interval, Runnable delegate) throws NoSuchMethodException {
        this.method = interval.getClass().getMethod("nextInterval");
        this.interval = interval;
        this.delegate = delegate;
    }

    public long nextInterval() {
        try {
            if (method == null) {
                // the interval class was invalid
                return Task.DONE;
            }
            return (Long) method.invoke(interval);
        } catch (ClassCastException e) {
            throw new ServiceRuntimeException("Invalid interval type returned", e);
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new ServiceRuntimeException(e);
        }
    }

    public void run() {
        delegate.run();
    }
}

