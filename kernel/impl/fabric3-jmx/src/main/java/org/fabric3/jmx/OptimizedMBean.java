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
package org.fabric3.jmx;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.security.AccessControlContext;
import java.security.AccessController;
import java.util.Map;
import java.util.Set;
import javax.management.Attribute;
import javax.management.AttributeNotFoundException;
import javax.management.InvalidAttributeValueException;
import javax.management.MBeanException;
import javax.management.MBeanInfo;
import javax.management.ReflectionException;
import javax.security.auth.Subject;

import org.fabric3.api.Role;
import org.fabric3.spi.invocation.CallFrame;
import org.fabric3.spi.invocation.WorkContext;
import org.fabric3.spi.invocation.WorkContextTunnel;
import org.fabric3.spi.objectfactory.ObjectCreationException;
import org.fabric3.spi.objectfactory.ObjectFactory;

/**
 * Wraps a Java-based component as an MBean and allows it to be invoked by an MBean server.
 *
 * @version $Rev$ $Date$
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

    public void setAttribute(Attribute attribute)
            throws AttributeNotFoundException, InvalidAttributeValueException, MBeanException, ReflectionException {
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
        WorkContext workContext = new WorkContext();
        workContext.addCallFrame(new CallFrame());
        WorkContext oldContext = WorkContextTunnel.setThreadWorkContext(workContext);
        try {
            T instance = objectFactory.getInstance();
            return method.invoke(instance, args);
        } catch (ObjectCreationException e) {
            throw new ReflectionException(e);
        } catch (IllegalAccessException e) {
            throw new AssertionError(e);
        } catch (InvocationTargetException e) {
            Throwable cause = e.getCause();
            // FIXME print to a monitor
            cause.printStackTrace();
            if (cause instanceof Exception) {
                throw new MBeanException((Exception) e.getCause());
            } else {
                throw new ReflectionException(e);
            }
        } finally {
            WorkContextTunnel.setThreadWorkContext(oldContext);
        }
    }
}