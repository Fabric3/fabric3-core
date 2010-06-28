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
*/
package org.fabric3.jmx;

import java.lang.reflect.Method;
import java.net.URI;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import javax.management.InstanceNotFoundException;
import javax.management.IntrospectionException;
import javax.management.JMException;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanInfo;
import javax.management.MBeanOperationInfo;
import javax.management.MBeanRegistrationException;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

import org.osoa.sca.annotations.EagerInit;
import org.osoa.sca.annotations.Reference;

import org.fabric3.api.annotation.management.ManagementOperation;
import org.fabric3.host.Names;
import org.fabric3.host.runtime.HostInfo;
import org.fabric3.spi.ObjectFactory;
import org.fabric3.spi.SingletonObjectFactory;
import org.fabric3.spi.management.ManagementException;
import org.fabric3.spi.management.ManagementService;
import org.fabric3.spi.model.type.java.ManagementInfo;
import org.fabric3.spi.model.type.java.ManagementOperationInfo;
import org.fabric3.spi.model.type.java.Signature;
import org.fabric3.spi.util.UriHelper;

/**
 * Exports components as MBeans using the runtime JMX MBean server.
 *
 * @version $Rev$ $Date$
 */
@EagerInit
public class JMXManagementService implements ManagementService {
    private static final String DOMAIN = "fabric3";
    private final MBeanServer mBeanServer;
    private final URI applicationDomain;

    public JMXManagementService(@Reference MBeanServer mBeanServer, @Reference HostInfo info) {
        this.mBeanServer = mBeanServer;
        this.applicationDomain = info.getDomain();
    }

    public void export(URI componentUri, ManagementInfo info, ObjectFactory<?> objectFactory, ClassLoader classLoader) throws ManagementException {
        if (mBeanServer == null) {
            return;
        }
        try {
            ObjectName name = getObjectName(componentUri, info);
            OptimizedMBean<?> mBean = createOptimizedMBean(info, objectFactory, classLoader);
            if (!mBeanServer.isRegistered(name)) {
                mBeanServer.registerMBean(mBean, name);
            }
        } catch (JMException e) {
            throw new ManagementException(e);
        } catch (ClassNotFoundException e) {
            throw new ManagementException(e);
        } catch (NoSuchMethodException e) {
            throw new ManagementException(e);
        }
    }

    public void export(String name, String group, String description, Object instance) throws ManagementException {
        try {
            ObjectName objectName = new ObjectName(DOMAIN + ":SubDomain=runtime, type=resource, group=" + group + ", name=" + name);
            Object managementBean;
            boolean isStandardMBean = isStandardMBean(instance);
            if (isStandardMBean) {
                // use the instance if it is a Standard MBean
                managementBean = instance;
            } else {
                SingletonObjectFactory<Object> factory = new SingletonObjectFactory<Object>(instance);
                Class<?> clazz = instance.getClass();
                ClassLoader loader = clazz.getClassLoader();
                ManagementInfo info = new ManagementInfo(name, group, description, clazz.getName());
                introspect(instance, info);
                managementBean = createOptimizedMBean(info, factory, loader);
            }
            if (!mBeanServer.isRegistered(objectName)) {
                mBeanServer.registerMBean(managementBean, objectName);
            }
        } catch (MalformedObjectNameException e) {
            throw new ManagementException(e);
        } catch (JMException e) {
            throw new ManagementException(e);
        } catch (ClassNotFoundException e) {
            throw new ManagementException(e);
        } catch (NoSuchMethodException e) {
            throw new ManagementException(e);
        }
    }

    private boolean isStandardMBean(Object instance) {
        boolean isStandardMBean = false;
        for (Class<?> interfaze : instance.getClass().getInterfaces()) {
            if (interfaze.getSimpleName().endsWith("MBean")) {
                isStandardMBean = true;
                break;
            }
        }
        return isStandardMBean;
    }

    public void remove(URI componentUri, ManagementInfo info) throws ManagementException {
        try {
            ObjectName name = getObjectName(componentUri, info);
            mBeanServer.unregisterMBean(name);
        } catch (JMException e) {
            throw new ManagementException(e);
        }
    }

    public void remove(String name, String group) throws ManagementException {
        try {
            ObjectName objectName = new ObjectName(DOMAIN + ":SubDomain=runtime, type=resource, group=" + group + ", name=" + name);
            mBeanServer.unregisterMBean(objectName);
        } catch (MalformedObjectNameException e) {
            throw new ManagementException(e);
        } catch (InstanceNotFoundException e) {
            throw new ManagementException(e);
        } catch (MBeanRegistrationException e) {
            throw new ManagementException(e);
        }
    }

    private void introspect(Object instance, ManagementInfo info) {
        for (Method method : instance.getClass().getMethods()) {
            ManagementOperation annotation = method.getAnnotation(ManagementOperation.class);
            if (annotation == null) {
                continue;
            }
            String description = annotation.description();
            if (description.trim().length() == 0) {
                description = null;
            }
            Signature signature = new Signature(method);
            ManagementOperationInfo operationInfo = new ManagementOperationInfo(signature, description);
            info.addOperation(operationInfo);
        }

    }

    private ObjectName getObjectName(URI uri, ManagementInfo info) throws MalformedObjectNameException {
        String component;
        String subDomain;
        if (uri.toString().startsWith(Names.RUNTIME_NAME)) {
            subDomain = "runtime";
            if (info.getName() == null) {
                component = UriHelper.getDefragmentedNameAsString(uri).substring(Names.RUNTIME_NAME.length() + 1);
            } else {
                component = info.getName();
            }
        } else {
            subDomain = applicationDomain.getAuthority();
            if (info.getName() == null) {
                component = UriHelper.getDefragmentedNameAsString(uri).substring(applicationDomain.toString().length() + 1);
            } else {
                component = info.getName();
            }
        }
        String group = info.getGroup();
        if (group != null) {
            return new ObjectName(DOMAIN + ":SubDomain=" + subDomain + ", type=component, group=" + group + ", name=" + component);
        } else {
            return new ObjectName(DOMAIN + ":SubDomain=" + subDomain + ", type=component, name=" + component);
        }
    }

    private <T> OptimizedMBean<T> createOptimizedMBean(ManagementInfo info, ObjectFactory<T> objectFactory, ClassLoader loader)
            throws IntrospectionException, ClassNotFoundException, NoSuchMethodException {
        String className = info.getManagementClass();
        Class<?> clazz = loader.loadClass(className);
        Set<AttributeDescription> attributes = new HashSet<AttributeDescription>();
        Map<String, Method> getters = new HashMap<String, Method>();
        Map<String, Method> setters = new HashMap<String, Method>();
        Map<OperationKey, Method> operations = new HashMap<OperationKey, Method>();
        for (ManagementOperationInfo operationInfo : info.getOperations()) {
            Method method = operationInfo.getSignature().getMethod(clazz);
            String description = operationInfo.getDescription();
            switch (getType(method)) {
            case GETTER:
                String getterName = getAttributeName(method);
                AttributeDescription attribute = new AttributeDescription(getterName, description);
                attributes.add(attribute);
                getters.put(getterName, method);
                break;
            case SETTER:
                String setterName = getAttributeName(method);
                attribute = new AttributeDescription(setterName, description);
                attributes.add(attribute);
                setters.put(setterName, method);
                break;
            case OPERATION:
                OperationKey key = new OperationKey(method, description);
                operations.put(key, method);
                break;
            }
        }

        MBeanAttributeInfo[] mBeanAttributes = createAttributeInfo(attributes, getters, setters);
        MBeanOperationInfo[] mBeanOperations = createOperationInfo(operations);
        String description = info.getDescription();
        MBeanInfo mbeanInfo = new MBeanInfo(className, description, mBeanAttributes, null, mBeanOperations, null);
        return new OptimizedMBean<T>(objectFactory, mbeanInfo, getters, setters, operations);
    }

    private MBeanOperationInfo[] createOperationInfo(Map<OperationKey, Method> operations) {
        MBeanOperationInfo[] mBeanOperations = new MBeanOperationInfo[operations.size()];
        int i = 0;
        for (Map.Entry<OperationKey, Method> entry : operations.entrySet()) {
            String description = entry.getKey().getDescription();
            Method value = entry.getValue();
            mBeanOperations[i++] = new MBeanOperationInfo(description, value);
        }
        return mBeanOperations;
    }

    private MBeanAttributeInfo[] createAttributeInfo(Set<AttributeDescription> descriptions, Map<String, Method> getters, Map<String, Method> setters)
            throws IntrospectionException {
        MBeanAttributeInfo[] mBeanAttributes = new MBeanAttributeInfo[descriptions.size()];
        int i = 0;
        for (AttributeDescription description : descriptions) {
            String name = description.getName();
            Method getter = getters.get(name);
            Method setter = setters.get(name);
            mBeanAttributes[i++] = new MBeanAttributeInfo(name, description.getDescription(), getter, setter);
        }
        return mBeanAttributes;
    }

    private MethodType getType(Method method) {
        String name = method.getName();
        Class<?> returnType = method.getReturnType();
        int paramCount = method.getParameterTypes().length;

        if (Void.TYPE.equals(returnType) && name.length() > 3 && name.startsWith("set") && paramCount == 1) {
            return MethodType.SETTER;
        } else if (Boolean.TYPE.equals(returnType) && name.length() > 2 && name.startsWith("is") && paramCount == 0) {
            return MethodType.GETTER;
        } else if (name.length() > 3 && name.startsWith("get") && paramCount == 0) {
            return MethodType.GETTER;
        } else {
            return MethodType.OPERATION;
        }
    }

    private String getAttributeName(Method method) {
        String name = method.getName();
        if (name.startsWith("is")) {
            return name.substring(2);
        } else {
            return name.substring(3);
        }
    }

    private static enum MethodType {
        GETTER, SETTER, OPERATION
    }

    private class AttributeDescription {
        private String name;
        private String description;

        private AttributeDescription(String name, String description) {
            this.name = name;
            this.description = description;
        }

        public String getName() {
            return name;
        }

        public String getDescription() {
            return description;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            AttributeDescription that = (AttributeDescription) o;

            return !(description != null ? !description.equals(that.description) : that.description != null)
                    && !(name != null ? !name.equals(that.name) : that.name != null);

        }

        @Override
        public int hashCode() {
            int result = name != null ? name.hashCode() : 0;
            result = 31 * result + (description != null ? description.hashCode() : 0);
            return result;
        }
    }
}
