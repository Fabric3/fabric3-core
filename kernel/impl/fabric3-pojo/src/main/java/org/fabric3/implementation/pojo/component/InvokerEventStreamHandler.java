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
 *
 * Portions originally based on Apache Tuscany 2007
 * licensed under the Apache 2.0 license.
 */
package org.fabric3.implementation.pojo.component;

import java.lang.reflect.InvocationTargetException;

import org.fabric3.api.host.ContainerException;
import org.fabric3.implementation.pojo.spi.reflection.ConsumerInvoker;
import org.fabric3.spi.container.channel.EventStreamHandler;
import org.fabric3.spi.container.component.AtomicComponent;
import org.fabric3.spi.container.invocation.WorkContextCache;
import org.fabric3.spi.container.wire.InvocationRuntimeException;

/**
 * Responsible for dispatching an event to a Java-based component implementation instance.
 */
public class InvokerEventStreamHandler implements EventStreamHandler {
    private AtomicComponent component;
    private ClassLoader targetTCCLClassLoader;
    private ConsumerInvoker invoker;

    /**
     * Constructor.
     *
     * @param invoker               the consumer invoker
     * @param component             the target component
     * @param targetTCCLClassLoader the classloader to set the TCCL to before dispatching.
     */
    public InvokerEventStreamHandler(ConsumerInvoker invoker, AtomicComponent component, ClassLoader targetTCCLClassLoader) {
        this.invoker = invoker;
        this.component = component;
        this.targetTCCLClassLoader = targetTCCLClassLoader;
    }

    public void setNext(EventStreamHandler next) {
        throw new IllegalStateException("This handler must be the last one in the handler sequence");
    }

    public EventStreamHandler getNext() {
        return null;
    }

    public void handle(Object event, boolean endOfBatch) {
        WorkContextCache.getAndResetThreadWorkContext();
        Object instance;
        try {
            instance = component.getInstance();
        } catch (ContainerException e) {
            throw new InvocationRuntimeException(e);
        }

        try {
            invoke(event, instance);
        } finally {
            try {
                component.releaseInstance(instance);
            } catch (ContainerException e) {
                throw new InvocationRuntimeException(e);
            }
        }
    }

    /**
     * Performs the invocation on the target component instance. If a target classloader is configured for the interceptor, it will be set as the TCCL.
     *
     * @param event    the event
     * @param instance the target component instance
     */
    private void invoke(Object event, Object instance) {
        try {
            if (targetTCCLClassLoader == null) {
                invoker.invoke(instance, event);
            } else {
                ClassLoader old = Thread.currentThread().getContextClassLoader();
                try {
                    Thread.currentThread().setContextClassLoader(targetTCCLClassLoader);
                    invoker.invoke(instance, event);
                } finally {
                    Thread.currentThread().setContextClassLoader(old);
                }
            }
        } catch (InvocationTargetException e) {
            throw new InvocationRuntimeException(e.getTargetException());
        } catch (IllegalAccessException e) {
            throw new InvocationRuntimeException(e);
        }
    }

}