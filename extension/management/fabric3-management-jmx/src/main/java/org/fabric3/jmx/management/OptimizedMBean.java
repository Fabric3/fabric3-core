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
package org.fabric3.jmx.management;

import javax.management.Attribute;
import javax.management.AttributeNotFoundException;
import javax.management.InvalidAttributeValueException;
import javax.management.MBeanException;
import javax.management.MBeanInfo;
import javax.management.ReflectionException;
import javax.security.auth.Subject;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.security.AccessControlContext;
import java.security.AccessController;
import java.util.Map;
import java.util.Set;

import org.fabric3.api.Role;
import org.fabric3.api.host.Fabric3Exception;
import org.fabric3.spi.container.invocation.WorkContext;
import org.fabric3.spi.container.invocation.WorkContextCache;
import org.fabric3.spi.container.objectfactory.ObjectFactory;

/**
 * Wraps a Java-based component as an MBean and allows it to be invoked by an MBean server.
 */
public class OptimizedMBean<T> extends AbstractMBean {
    private final ObjectFactory<T> objectFactory;
    private final Map<String, MethodHolder> getters;
    private final Map<String, MethodHolder> setters;
    private final Map<OperationKey, MethodHolder> operations;
    private boolean authorization;

    public OptimizedMBean(ObjectFactory<T> objectFactory,
                          MBeanInfo mbeanInfo,
                          Map<String, MethodHolder> getters,
                          Map<String, MethodHolder> setters,
                          Map<OperationKey, MethodHolder> operations,
                          boolean authorization) {
        super(mbeanInfo);
        this.objectFactory = objectFactory;
        this.getters = getters;
        this.setters = setters;
        this.operations = operations;
        this.authorization = authorization;
    }

    public Object getAttribute(String attribute) throws AttributeNotFoundException, MBeanException, ReflectionException {
        MethodHolder holder = getters.get(attribute);
        if (holder == null) {
            throw new AttributeNotFoundException(attribute);
        }
        if (authorization) {
            authorize(holder.getRoles());
        }
        Method method = holder.getMethod();
        return invoke(method, null);
    }

    public void setAttribute(Attribute attribute) throws AttributeNotFoundException, InvalidAttributeValueException, MBeanException, ReflectionException {
        MethodHolder holder = setters.get(attribute.getName());
        if (holder == null) {
            throw new AttributeNotFoundException(attribute.getName());
        }
        if (authorization) {
            authorize(holder.getRoles());
        }
        Method method = holder.getMethod();
        invoke(method, new Object[]{attribute.getValue()});
    }

    @SuppressWarnings({"ThrowableInstanceNeverThrown"})
    public Object invoke(String s, Object[] objects, String[] strings) throws MBeanException, ReflectionException {
        OperationKey operation = new OperationKey(s, strings);
        MethodHolder holder = operations.get(operation);
        if (holder == null) {
            throw new ReflectionException(new NoSuchMethodException(operation.toString()));
        }
        if (authorization) {
            authorize(holder.getRoles());
        }
        Method method = holder.getMethod();
        return invoke(method, objects);
    }

    @SuppressWarnings({"ThrowableInstanceNeverThrown"})
    private void authorize(Set<Role> roles) throws MBeanException {
        // retrieve the current security context set by the JMXAuthenticator when the JMX client connected to the MBeanServer
        AccessControlContext acc = AccessController.getContext();
        Subject subject = Subject.getSubject(acc);
        for (Role role : roles) {
            if (subject.getPrincipals().contains(role)) {
                return;
            }
        }
        throw new MBeanException(new Exception("Not authorized"));
    }

    Object invoke(Method method, Object[] args) throws MBeanException, ReflectionException {
        WorkContext workContext = WorkContextCache.getAndResetThreadWorkContext();
        try {
            T instance = objectFactory.getInstance();
            return method.invoke(instance, args);
        } catch (Fabric3Exception e) {
            throw new ReflectionException(e);
        } catch (IllegalAccessException e) {
            throw new AssertionError(e);
        } catch (InvocationTargetException e) {
            Throwable cause = e.getCause();
            cause.printStackTrace();
            if (cause instanceof Exception) {
                throw new MBeanException((Exception) e.getCause());
            } else {
                throw new ReflectionException(e);
            }
        } finally {
            workContext.reset();
        }
    }
}