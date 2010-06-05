/*
 * Fabric3
 * Copyright (c) 2009 Metaform Systems
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
import java.lang.reflect.Method;

import org.osoa.sca.ConversationEndedException;

import org.fabric3.spi.channel.EventStreamHandler;
import org.fabric3.spi.component.AtomicComponent;
import org.fabric3.spi.component.InstanceDestructionException;
import org.fabric3.spi.component.InstanceLifecycleException;
import org.fabric3.spi.component.InstanceWrapper;
import org.fabric3.spi.component.ScopeContainer;
import org.fabric3.spi.invocation.WorkContext;
import org.fabric3.spi.invocation.WorkContextTunnel;
import org.fabric3.spi.wire.InvocationRuntimeException;

/**
 * Responsible for dispatching an event to a Java-based component implementation instance.
 *
 * @version $Rev$ $Date$
 * @param <T> the implementation class for the component being invoked
 */
public class InvokerEventStreamHandler<T> implements EventStreamHandler {
    private Method operation;
    private AtomicComponent<T> component;
    private ScopeContainer scopeContainer;
    private ClassLoader targetTCCLClassLoader;

    /**
     * Constructor.
     *
     * @param operation             the method to invoke on the target instance
     * @param component             the target component
     * @param scopeContainer        the ScopeContainer that manages implementation instances for the target component
     * @param targetTCCLClassLoader the classloader to set the TCCL to before dispatching.
     */
    public InvokerEventStreamHandler(Method operation,
                                     AtomicComponent<T> component,
                                     ScopeContainer scopeContainer,
                                     ClassLoader targetTCCLClassLoader) {
        this.operation = operation;
        this.component = component;
        this.scopeContainer = scopeContainer;
        this.targetTCCLClassLoader = targetTCCLClassLoader;
    }


    public void setNext(EventStreamHandler next) {
        throw new IllegalStateException("This handler must be the last one in the handler sequence");
    }

    public EventStreamHandler getNext() {
        return null;
    }

    public void handle(Object event) {
        WorkContext workContext = new WorkContext();
        InstanceWrapper<T> wrapper;
        try {
            wrapper = scopeContainer.getWrapper(component, workContext);
        } catch (ConversationEndedException e) {
            // this should not happen
            throw new AssertionError(e);
        } catch (InstanceLifecycleException e) {
            throw new InvocationRuntimeException(e);
        }

        try {
            Object instance = wrapper.getInstance();
            invoke(event, workContext, instance);
        } finally {
            try {
                scopeContainer.returnWrapper(component, workContext, wrapper);
            } catch (InstanceDestructionException e) {
                throw new InvocationRuntimeException(e);
            }
        }
    }

    /**
     * Performs the invocation on the target component instance. If a target classloader is configured for the interceptor, it will be set as the
     * TCCL.
     *
     * @param event       the event
     * @param workContext the current work context
     * @param instance    the target component instance
     */
    private void invoke(Object event, WorkContext workContext, Object instance) {
        WorkContext oldWorkContext = WorkContextTunnel.setThreadWorkContext(workContext);
        try {
            if (targetTCCLClassLoader == null) {
                operation.invoke(instance, (Object[]) event);
            } else {
                ClassLoader old = Thread.currentThread().getContextClassLoader();
                try {
                    Thread.currentThread().setContextClassLoader(targetTCCLClassLoader);
                    operation.invoke(instance, (Object[]) event);
                } finally {
                    Thread.currentThread().setContextClassLoader(old);
                }
            }
        } catch (InvocationTargetException e) {
            throw new InvocationRuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new InvocationRuntimeException(e);
        } finally {
            WorkContextTunnel.setThreadWorkContext(oldWorkContext);
        }
    }

}