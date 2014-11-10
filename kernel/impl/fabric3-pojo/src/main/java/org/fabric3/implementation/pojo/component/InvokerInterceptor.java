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

import org.fabric3.implementation.pojo.spi.reflection.ServiceInvoker;
import org.fabric3.spi.container.ContainerException;
import org.fabric3.spi.container.component.AtomicComponent;
import org.fabric3.spi.container.component.InstanceLifecycleException;
import org.fabric3.spi.container.invocation.Message;
import org.fabric3.spi.container.wire.Interceptor;
import org.fabric3.spi.container.wire.InvocationRuntimeException;

/**
 * Responsible for dispatching an invocation to a Java-based component implementation instance.
 */
public class InvokerInterceptor implements Interceptor {
    private ServiceInvoker invoker;
    private AtomicComponent component;
    private ClassLoader targetTCCLClassLoader;

    /**
     * Creates a new interceptor instance.
     *
     * @param invoker   the target invoker
     * @param component the target component
     */
    public InvokerInterceptor(ServiceInvoker invoker, AtomicComponent component) {
        this.invoker = invoker;
        this.component = component;
    }

    /**
     * Creates a new interceptor instance that sets the TCCL to the given classloader before dispatching an invocation.
     *
     * @param invoker               the target invoker
     * @param component             the target component
     * @param targetTCCLClassLoader the classloader to set the TCCL to before dispatching.
     */
    public InvokerInterceptor(ServiceInvoker invoker, AtomicComponent component, ClassLoader targetTCCLClassLoader) {
        this.invoker = invoker;
        this.component = component;
        this.targetTCCLClassLoader = targetTCCLClassLoader;
    }

    public void setNext(Interceptor next) {
        throw new IllegalStateException("This interceptor must be the last one in an target interceptor chain");
    }

    public Interceptor getNext() {
        return null;
    }

    public Message invoke(Message msg) {
        Object instance;
        try {
            instance = component.getInstance();
        } catch (InstanceLifecycleException e) {
            throw new InvocationRuntimeException(e);
        }

        try {
            return invoke(msg, instance);
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
     * @param msg      the messaging containing the invocation data
     * @param instance the target component instance
     * @return the response message
     */
    private Message invoke(Message msg, Object instance) {
        try {
            Object body = msg.getBody();
            if (targetTCCLClassLoader == null) {
                msg.setBody(invoker.invoke(instance, body));
            } else {
                ClassLoader old = Thread.currentThread().getContextClassLoader();
                try {
                    Thread.currentThread().setContextClassLoader(targetTCCLClassLoader);
                    msg.setBody(invoker.invoke(instance, body));
                } finally {
                    Thread.currentThread().setContextClassLoader(old);
                }
            }
        } catch (InvocationTargetException e) {
            msg.setBodyWithFault(e.getCause());
        } catch (IllegalAccessException e) {
            throw new InvocationRuntimeException(e);
        }
        return msg;
    }

}
