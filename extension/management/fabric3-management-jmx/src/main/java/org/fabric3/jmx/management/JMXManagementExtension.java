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
import java.lang.reflect.Method;
import java.net.URI;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.fabric3.api.Role;
import org.fabric3.api.annotation.management.Management;
import org.fabric3.api.annotation.management.ManagementOperation;
import org.fabric3.api.host.Fabric3Exception;
import org.fabric3.api.host.Names;
import org.fabric3.api.host.runtime.HostInfo;
import org.fabric3.api.model.type.java.ManagementInfo;
import org.fabric3.api.model.type.java.ManagementOperationInfo;
import org.fabric3.api.model.type.java.OperationType;
import org.fabric3.api.model.type.java.Signature;
import org.fabric3.spi.container.objectfactory.ObjectFactory;
import org.fabric3.spi.container.objectfactory.SingletonObjectFactory;
import org.fabric3.spi.management.ManagementExtension;
import org.fabric3.spi.util.UriHelper;
import org.oasisopen.sca.annotation.EagerInit;
import org.oasisopen.sca.annotation.Property;
import org.oasisopen.sca.annotation.Reference;

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

    public void export(URI componentUri, ManagementInfo info, ObjectFactory<?> objectFactory, ClassLoader classLoader) throws Fabric3Exception {
        if (mBeanServer == null) {
            return;
        }
        try {
            ObjectName name = getObjectName(componentUri, info);
            OptimizedMBean<?> mBean = createOptimizedMBean(info, objectFactory, classLoader);
            if (!mBeanServer.isRegistered(name)) {
                mBeanServer.registerMBean(mBean, name);
            }
        } catch (JMException | NoSuchMethodException | ClassNotFoundException e) {
            throw new Fabric3Exception(e);
        }
    }

    public void export(String name, String group, String description, Object instance) throws Fabric3Exception {
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
                SingletonObjectFactory<Object> factory = new SingletonObjectFactory<>(instance);
                Class<?> clazz = instance.getClass();
                ClassLoader loader = clazz.getClassLoader();

                Set<Role> readRoles = new HashSet<>();
                Set<Role> writeRoles = new HashSet<>();
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
        } catch (NoSuchMethodException | ClassNotFoundException | JMException e) {
            throw new Fabric3Exception(e);
        }
    }

    public void remove(URI componentUri, ManagementInfo info) throws Fabric3Exception {
        try {
            ObjectName name = getObjectName(componentUri, info);
            mBeanServer.unregisterMBean(name);
        } catch (JMException e) {
            throw new Fabric3Exception(e);
        }
    }

    public void remove(String name, String group) throws Fabric3Exception {
        try {
            group = parseGroup(group);
            ObjectName objectName = new ObjectName(DOMAIN + ":SubDomain=runtime, type=resource, group=" + group + ", name=" + name);
            mBeanServer.unregisterMBean(objectName);
        } catch (MalformedObjectNameException | MBeanRegistrationException | InstanceNotFoundException e) {
            throw new Fabric3Exception(e);
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
            Set<Role> roles = new HashSet<>();
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
        Set<AttributeDescription> attributes = new HashSet<>();
        Map<String, MethodHolder> getters = new HashMap<>();
        Map<String, MethodHolder> setters = new HashMap<>();
        Map<OperationKey, MethodHolder> operations = new HashMap<>();
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
        return new OptimizedMBean<>(objectFactory, mbeanInfo, getters, setters, operations, authorization);
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
