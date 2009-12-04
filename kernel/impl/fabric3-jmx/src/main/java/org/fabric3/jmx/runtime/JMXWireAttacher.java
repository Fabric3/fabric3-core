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
package org.fabric3.jmx.runtime;

import java.lang.reflect.Method;
import java.net.URI;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import javax.management.IntrospectionException;
import javax.management.JMException;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanInfo;
import javax.management.MBeanOperationInfo;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

import org.osoa.sca.annotations.EagerInit;
import org.osoa.sca.annotations.Reference;

import org.fabric3.host.Names;
import org.fabric3.host.runtime.HostInfo;
import org.fabric3.jmx.provision.JMXSourceDefinition;
import org.fabric3.spi.ObjectFactory;
import org.fabric3.spi.builder.WiringException;
import org.fabric3.spi.builder.component.SourceWireAttacher;
import org.fabric3.spi.classloader.ClassLoaderRegistry;
import org.fabric3.spi.model.physical.PhysicalTargetDefinition;
import org.fabric3.spi.util.UriHelper;
import org.fabric3.spi.wire.Wire;

/**
 * @version $Rev$ $Date$
 */
@EagerInit
public class JMXWireAttacher implements SourceWireAttacher<JMXSourceDefinition> {

    private static final String DOMAIN = "fabric3";
    private final MBeanServer mBeanServer;
    private final ClassLoaderRegistry classLoaderRegistry;
    private final URI applicationDomain;

    public JMXWireAttacher(@Reference MBeanServer mBeanServer,
                           @Reference ClassLoaderRegistry classLoaderRegistry,
                           @Reference HostInfo info) {
        this.mBeanServer = mBeanServer;
        this.classLoaderRegistry = classLoaderRegistry;
        this.applicationDomain = info.getDomain();
    }

    public void attach(JMXSourceDefinition source, PhysicalTargetDefinition target, Wire wire) throws WiringException {
        throw new UnsupportedOperationException();
    }

    public void detach(JMXSourceDefinition source, PhysicalTargetDefinition target) throws WiringException {
        throw new AssertionError();
    }

    public void attachObjectFactory(JMXSourceDefinition source, ObjectFactory<?> objectFactory, PhysicalTargetDefinition target)
            throws WiringException {
        if (mBeanServer == null) {
            return;
        }

        try {
            Class<?> managementInterface = classLoaderRegistry.loadClass(source.getClassLoaderId(), source.getInterfaceName());
            URI uri = source.getUri();
            ObjectName name = getObjectName(uri);
            OptimizedMBean<?> mbean = createOptimizedMBean(objectFactory, managementInterface);
            if (!mBeanServer.isRegistered(name)) {
                mBeanServer.registerMBean(mbean, name);
            }
        } catch (JMException e) {
            throw new WiringException(e);
        } catch (ClassNotFoundException e) {
            throw new WiringException(e);
        }
    }

    public void detachObjectFactory(JMXSourceDefinition source, PhysicalTargetDefinition target) throws WiringException {
        try {
            URI uri = source.getUri();
            ObjectName name = getObjectName(uri);
            mBeanServer.unregisterMBean(name);
        } catch (JMException e) {
            throw new WiringException(e);
        }
    }

    private ObjectName getObjectName(URI uri) throws MalformedObjectNameException {
        String component;
        String subDomain;
        if (uri.toString().startsWith(Names.RUNTIME_NAME)) {
            subDomain = "runtime";
            component = UriHelper.getDefragmentedNameAsString(uri).substring(Names.RUNTIME_NAME.length() + 1);
        } else {
            subDomain = applicationDomain.getAuthority();
            component = UriHelper.getDefragmentedNameAsString(uri).substring(applicationDomain.toString().length() + 1);
        }
        return new ObjectName(DOMAIN + ":SubDomain=" + subDomain + ", type=component, name=" + component);
    }

    private <T> OptimizedMBean<T> createOptimizedMBean(ObjectFactory<T> objectFactory, Class<?> service) throws IntrospectionException {
        String className = service.getName();
        Set<String> attributeNames = new HashSet<String>();
        Map<String, Method> getters = new HashMap<String, Method>();
        Map<String, Method> setters = new HashMap<String, Method>();
        Map<OperationKey, Method> operations = new HashMap<OperationKey, Method>();
        for (Method method : service.getMethods()) {
            switch (getType(method)) {
            case GETTER:
                String getterName = getAttributeName(method);
                attributeNames.add(getterName);
                getters.put(getterName, method);
                break;
            case SETTER:
                String setterName = getAttributeName(method);
                attributeNames.add(setterName);
                setters.put(setterName, method);
                break;
            case OPERATION:
                operations.put(new OperationKey(method), method);
                break;
            }
        }

        MBeanAttributeInfo[] mbeanAttributes = createAttributeInfo(attributeNames, getters, setters);
        MBeanOperationInfo[] mbeanOperations = createOperationInfo(operations.values());
        MBeanInfo mbeanInfo = new MBeanInfo(className, null, mbeanAttributes, null, mbeanOperations, null);
        return new OptimizedMBean<T>(objectFactory, mbeanInfo, getters, setters, operations);
    }

    private MBeanOperationInfo[] createOperationInfo(Collection<Method> operations) {
        MBeanOperationInfo[] mbeanOperations = new MBeanOperationInfo[operations.size()];
        int i = 0;
        for (Method method : operations) {
            mbeanOperations[i++] = new MBeanOperationInfo(null, method);
        }
        return mbeanOperations;
    }

    private MBeanAttributeInfo[] createAttributeInfo(Set<String> attributeNames, Map<String, Method> getters, Map<String, Method> setters)
            throws IntrospectionException {
        MBeanAttributeInfo[] mbeanAttributes = new MBeanAttributeInfo[attributeNames.size()];
        int i = 0;
        for (String name : attributeNames) {
            mbeanAttributes[i++] = new MBeanAttributeInfo(name, null, getters.get(name), setters.get(name));
        }
        return mbeanAttributes;
    }

    private static enum MethodType {
        GETTER, SETTER, OPERATION
    }

    private static MethodType getType(Method method) {
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

    private static String getAttributeName(Method method) {
        String name = method.getName();
        if (name.startsWith("is")) {
            return name.substring(2);
        } else {
            return name.substring(3);
        }
    }
}
