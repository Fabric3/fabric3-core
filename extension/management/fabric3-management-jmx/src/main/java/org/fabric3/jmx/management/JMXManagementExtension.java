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
*/
package org.fabric3.jmx.management;

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

import org.oasisopen.sca.annotation.EagerInit;
import org.oasisopen.sca.annotation.Property;
import org.oasisopen.sca.annotation.Reference;

import org.fabric3.api.Role;
import org.fabric3.api.annotation.management.Management;
import org.fabric3.api.annotation.management.ManagementOperation;
import org.fabric3.host.Names;
import org.fabric3.host.runtime.HostInfo;
import org.fabric3.spi.management.ManagementException;
import org.fabric3.spi.management.ManagementExtension;
import org.fabric3.spi.model.type.java.ManagementInfo;
import org.fabric3.spi.model.type.java.ManagementOperationInfo;
import org.fabric3.spi.model.type.java.OperationType;
import org.fabric3.spi.model.type.java.Signature;
import org.fabric3.spi.objectfactory.ObjectFactory;
import org.fabric3.spi.objectfactory.SingletonObjectFactory;
import org.fabric3.spi.util.UriHelper;

/**
 * Exports components as MBeans using the runtime JMX MBean server.
 */
@EagerInit
public class JMXManagementExtension implements ManagementExtension {
    private static final String DOMAIN = "fabric3";
    boolean authorization;

    private MBeanServer mBeanServer;
    private URI applicationDomain;

    @Property(required = false)
    public void setSecurity(String security) {
        authorization = "AUTHORIZATION".equals(security.toUpperCase());
    }

    public JMXManagementExtension(@Reference MBeanServer mBeanServer, @Reference HostInfo info) {
        this.mBeanServer = mBeanServer;
        this.applicationDomain = info.getDomain();
    }

    public String getType() {
        return "fabric3.jmx";
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
            group = parseGroup(group);
            ObjectName objectName = new ObjectName(DOMAIN + ":SubDomain=runtime, type=resource, group=" + group + ", name=" + name);
            Object managementBean;
            boolean isStandardMBean = isStandardMBean(instance);
            String path = "";
            if (isStandardMBean) {
                // use the instance if it is a Standard MBean
                managementBean = instance;
            } else {
                SingletonObjectFactory<Object> factory = new SingletonObjectFactory<Object>(instance);
                Class<?> clazz = instance.getClass();
                ClassLoader loader = clazz.getClassLoader();

                Set<Role> readRoles = new HashSet<Role>();
                Set<Role> writeRoles = new HashSet<Role>();
                Management annotation = clazz.getAnnotation(Management.class);
                if (annotation != null) {
                    String[] readRoleNames = annotation.readRoles();
                    for (String roleName : readRoleNames) {
                        readRoles.add(new Role(roleName));
                    }
                    String[] writeRoleNames = annotation.writeRoles();
                    for (String roleName : writeRoleNames) {
                        writeRoles.add(new Role(roleName));
                    }
                    path = annotation.path();
                }

                if (readRoles.isEmpty()) {
                    readRoles.add(new Role(Management.FABRIC3_ADMIN_ROLE));
                    readRoles.add(new Role(Management.FABRIC3_OBSERVER_ROLE));
                }
                if (writeRoles.isEmpty()) {
                    writeRoles.add(new Role(Management.FABRIC3_ADMIN_ROLE));
                }
                ManagementInfo info = new ManagementInfo(name, group, path, description, clazz.getName(), readRoles, writeRoles);
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
            group = parseGroup(group);
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

    /**
     * Parses a group hierarchy in the form path/subpath to convert it into a JMX hierarchy of the form group=path, group0=subpath.
     *
     * @param group the group hierarchy to parse
     * @return the parsed JMX representation
     */
    private String parseGroup(String group) {
        String[] path = group.split("/");
        if (path.length > 1) {
            StringBuilder builder = new StringBuilder(path[0]);
            for (int i = 1; i < path.length; i++) {
                String token = path[i];
                builder.append(", group").append(i).append("=").append(token);
            }
            group = builder.toString();
        }
        return group;
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
            String[] roleNames = annotation.rolesAllowed();
            Set<Role> roles = new HashSet<Role>();
            for (String name : roleNames) {
                roles.add(new Role(name));
            }
            String path = annotation.path();
            ManagementOperationInfo operationInfo = new ManagementOperationInfo(signature, path, OperationType.UNDEFINED, description, roles);
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
        Map<String, MethodHolder> getters = new HashMap<String, MethodHolder>();
        Map<String, MethodHolder> setters = new HashMap<String, MethodHolder>();
        Map<OperationKey, MethodHolder> operations = new HashMap<OperationKey, MethodHolder>();
        for (ManagementOperationInfo operationInfo : info.getOperations()) {
            Method method = operationInfo.getSignature().getMethod(clazz);
            String description = operationInfo.getDescription();
            Set<Role> roles = operationInfo.getRoles();

            switch (getType(method)) {
            case GETTER:
                String getterName = getAttributeName(method);
                AttributeDescription attribute = new AttributeDescription(getterName, description);
                attributes.add(attribute);
                if (roles.isEmpty()) {
                    // default to read roles specified on the implementation
                    roles = info.getReadRoles();
                }
                MethodHolder holder = new MethodHolder(method, roles);
                getters.put(getterName, holder);
                break;
            case SETTER:
                String setterName = getAttributeName(method);
                attribute = new AttributeDescription(setterName, description);
                attributes.add(attribute);
                if (roles.isEmpty()) {
                    // default to write roles specified on the implementation
                    roles = info.getWriteRoles();
                }
                holder = new MethodHolder(method, roles);
                setters.put(setterName, holder);
                break;
            case OPERATION:
                OperationKey key = new OperationKey(method, description);
                if (roles.isEmpty()) {
                    // default to write roles specified on the implementation
                    roles = info.getWriteRoles();
                }
                holder = new MethodHolder(method, roles);
                operations.put(key, holder);
                break;
            }
        }

        MBeanAttributeInfo[] mBeanAttributes = createAttributeInfo(attributes, getters, setters);
        MBeanOperationInfo[] mBeanOperations = createOperationInfo(operations);
        String description = info.getDescription();
        MBeanInfo mbeanInfo = new MBeanInfo(className, description, mBeanAttributes, null, mBeanOperations, null);
        return new OptimizedMBean<T>(objectFactory, mbeanInfo, getters, setters, operations, authorization);
    }

    private MBeanOperationInfo[] createOperationInfo(Map<OperationKey, MethodHolder> operations) {
        MBeanOperationInfo[] mBeanOperations = new MBeanOperationInfo[operations.size()];
        int i = 0;
        for (Map.Entry<OperationKey, MethodHolder> entry : operations.entrySet()) {
            String description = entry.getKey().getDescription();
            Method value = entry.getValue().getMethod();
            mBeanOperations[i++] = new MBeanOperationInfo(description, value);
        }
        return mBeanOperations;
    }

    private MBeanAttributeInfo[] createAttributeInfo(Set<AttributeDescription> descriptions,
                                                     Map<String, MethodHolder> getters,
                                                     Map<String, MethodHolder> setters) throws IntrospectionException {
        MBeanAttributeInfo[] mBeanAttributes = new MBeanAttributeInfo[descriptions.size()];
        int i = 0;
        for (AttributeDescription description : descriptions) {
            String name = description.getName();
            MethodHolder getterHolder = getters.get(name);
            Method getter = null;
            if (getterHolder != null) {
                getter = getterHolder.getMethod();
            }
            MethodHolder setterHolder = setters.get(name);
            Method setter = null;
            if (setterHolder != null) {
                setter = setterHolder.getMethod();
            }
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
