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
 */
package org.fabric3.implementation.bytecode.proxy.wire;

import java.lang.reflect.Method;
import java.net.URI;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.fabric3.implementation.bytecode.proxy.common.ProxyFactory;
import org.fabric3.implementation.pojo.spi.proxy.ProxyCreationException;
import org.fabric3.implementation.pojo.spi.proxy.WireProxyServiceExtension;
import org.fabric3.spi.classloader.ClassLoaderRegistry;
import org.fabric3.spi.classloader.MultiParentClassLoader;
import org.fabric3.spi.model.physical.PhysicalOperationDefinition;
import org.fabric3.spi.model.type.java.Signature;
import org.fabric3.spi.objectfactory.ObjectFactory;
import org.fabric3.spi.wire.InvocationChain;
import org.fabric3.spi.wire.Wire;
import org.oasisopen.sca.ServiceReference;
import org.oasisopen.sca.annotation.EagerInit;
import org.oasisopen.sca.annotation.Reference;

/**
 *
 */
@EagerInit
public class BytecodeWireProxyService implements WireProxyServiceExtension {
    private ProxyFactory proxyFactory;
    private ClassLoaderRegistry classLoaderRegistry;

    public BytecodeWireProxyService(@Reference ProxyFactory proxyFactory, @Reference ClassLoaderRegistry classLoaderRegistry) {
        this.proxyFactory = proxyFactory;
        this.classLoaderRegistry = classLoaderRegistry;
    }

    public boolean isDefault() {
        return false;
    }

    public <T> ObjectFactory<T> createObjectFactory(Class<T> interfaze, Wire wire, String callbackUri) throws ProxyCreationException {
        URI uri = getClassLoaderUri(interfaze);

        List<InvocationChain> list = wire.getInvocationChains();
        Map<Method, InvocationChain> mappings = resolveMethods(interfaze, list);
        Method[] methods = mappings.keySet().toArray(new Method[mappings.size()]);
        InvocationChain[] chains = mappings.values().toArray(new InvocationChain[mappings.size()]);

        return new WireProxyObjectFactory<T>(uri, interfaze, methods, chains, callbackUri, proxyFactory);
    }

    public <T> ObjectFactory<T> createCallbackObjectFactory(Class<T> interfaze, boolean multiThreaded, URI callbackUri, Wire wire)
            throws ProxyCreationException {
        URI uri = getClassLoaderUri(interfaze);

        List<InvocationChain> list = wire.getInvocationChains();
        Map<Method, InvocationChain> mappings = resolveMethods(interfaze, list);
        Method[] methods = mappings.keySet().toArray(new Method[mappings.size()]);
        InvocationChain[] chains = mappings.values().toArray(new InvocationChain[mappings.size()]);

        String callbackString = callbackUri.toString();
        return new CallbackWireObjectFactory<T>(uri, interfaze, methods, callbackString, chains, proxyFactory);
    }

    public <T> ObjectFactory<?> updateCallbackObjectFactory(ObjectFactory<?> factory, Class<T> interfaze, boolean multiThreaded, URI callbackUri, Wire wire)
            throws ProxyCreationException {
        if (!(factory instanceof CallbackWireObjectFactory)) {
            throw new AssertionError("Expected object factory of type: " + CallbackWireObjectFactory.class.getName());
        }
        CallbackWireObjectFactory callbackFactory = (CallbackWireObjectFactory) factory;

        List<InvocationChain> list = wire.getInvocationChains();
        InvocationChain[] chains = list.toArray(new InvocationChain[list.size()]);
        callbackFactory.updateMappings(callbackUri.toString(), chains);
        return callbackFactory;
    }

    public <B, R extends ServiceReference<B>> R cast(B target) throws IllegalArgumentException {
        throw new UnsupportedOperationException();
    }

    private <T> URI getClassLoaderUri(Class<T> interfaze) {
        ClassLoader classLoader = interfaze.getClassLoader();
        if (!(classLoader instanceof MultiParentClassLoader)) {
            throw new AssertionError("Expected " + MultiParentClassLoader.class.getName());
        }
        return ((MultiParentClassLoader) classLoader).getName();
    }

    private Map<Method, InvocationChain> resolveMethods(Class<?> interfaze, List<InvocationChain> chains) throws ProxyCreationException {
        Map<Method, InvocationChain> chainMappings = new HashMap<Method, InvocationChain>(chains.size());
        for (InvocationChain chain : chains) {
            PhysicalOperationDefinition operation = chain.getPhysicalOperation();
            try {
                Method method = findMethod(interfaze, operation);
                chainMappings.put(method, chain);
            } catch (NoSuchMethodException e) {
                throw new ProxyCreationException(operation.getName());
            } catch (ClassNotFoundException e) {
                throw new ProxyCreationException(e);
            }
        }

        Map<String, Method> sorted = new TreeMap<String, Method>();
        for (Method method : chainMappings.keySet()) {
            String key = new Signature(method).toString();
            sorted.put(key, method);
        }

        Map<Method, InvocationChain> result = new LinkedHashMap<Method, InvocationChain>();
        for (Method method : sorted.values()) {
            result.put(method, chainMappings.get(method));
        }
        return result;
    }

    /**
     * Returns the matching method from the class for a given operation.
     *
     * @param clazz     the class to introspect
     * @param operation the operation to match
     * @return a matching method
     * @throws NoSuchMethodException  if a matching method is not found
     * @throws ClassNotFoundException if a parameter type specified in the operation is not found
     */
    private Method findMethod(Class<?> clazz, PhysicalOperationDefinition operation) throws NoSuchMethodException, ClassNotFoundException {
        String name = operation.getName();
        List<String> params = operation.getSourceParameterTypes();
        Class<?>[] types = new Class<?>[params.size()];
        for (int i = 0; i < params.size(); i++) {
            types[i] = classLoaderRegistry.loadClass(clazz.getClassLoader(), params.get(i));
        }
        return clazz.getMethod(name, types);
    }
}
