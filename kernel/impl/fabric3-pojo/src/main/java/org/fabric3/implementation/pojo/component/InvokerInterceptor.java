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
 *
 * ----------------------------------------------------
 *
 * Portions originally based on Apache Tuscany 2007
 * licensed under the Apache 2.0 license.
 *
 */
package org.fabric3.implementation.pojo.component;

import java.lang.reflect.InvocationTargetException;

import org.fabric3.implementation.pojo.spi.reflection.TargetInvoker;
import org.fabric3.spi.component.AtomicComponent;
import org.fabric3.spi.component.ComponentException;
import org.fabric3.spi.component.InstanceLifecycleException;
import org.fabric3.spi.invocation.Message;
import org.fabric3.spi.invocation.WorkContext;
import org.fabric3.spi.invocation.WorkContextTunnel;
import org.fabric3.spi.wire.Interceptor;
import org.fabric3.spi.wire.InvocationRuntimeException;

/**
 * Responsible for dispatching an invocation to a Java-based component implementation instance.
 */
public class InvokerInterceptor implements Interceptor {
    private TargetInvoker invoker;
    private AtomicComponent component;
    private ClassLoader targetTCCLClassLoader;

    /**
     * Creates a new interceptor instance.
     *
     * @param invoker   the target invoker
     * @param component the target component
     */
    public InvokerInterceptor(TargetInvoker invoker, AtomicComponent component) {
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
    public InvokerInterceptor(TargetInvoker invoker, AtomicComponent component, ClassLoader targetTCCLClassLoader) {
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
        WorkContext workContext = msg.getWorkContext();
        Object instance;
        try {
            instance = component.getInstance(workContext);
        } catch (InstanceLifecycleException e) {
            throw new InvocationRuntimeException(e);
        }

        try {
            return invoke(msg, workContext, instance);
        } finally {
            try {
                component.releaseInstance(instance, workContext);
            } catch (ComponentException e) {
                throw new InvocationRuntimeException(e);
            }
        }
    }

    /**
     * Performs the invocation on the target component instance. If a target classloader is configured for the interceptor, it will be set as the TCCL.
     *
     * @param msg         the messaging containing the invocation data
     * @param workContext the current work context
     * @param instance    the target component instance
     * @return the response message
     */
    private Message invoke(Message msg, WorkContext workContext, Object instance) {
        WorkContext oldWorkContext = WorkContextTunnel.setThreadWorkContext(workContext);
        try {
            Object body = msg.getBody();
            if (targetTCCLClassLoader == null) {
                msg.setBody(invoker.invoke(instance, (Object[]) body));
            } else {
                ClassLoader old = Thread.currentThread().getContextClassLoader();
                try {
                    Thread.currentThread().setContextClassLoader(targetTCCLClassLoader);
                    msg.setBody(invoker.invoke(instance, (Object[]) body));
                } finally {
                    Thread.currentThread().setContextClassLoader(old);
                }
            }
        } catch (InvocationTargetException e) {
            msg.setBodyWithFault(e.getCause());
        } catch (IllegalAccessException e) {
            throw new InvocationRuntimeException(e);
        } finally {
            WorkContextTunnel.setThreadWorkContext(oldWorkContext);
        }
        return msg;
    }

}
