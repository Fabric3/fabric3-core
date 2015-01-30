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
package org.fabric3.implementation.bytecode.proxy.wire;

import java.lang.reflect.Method;
import java.net.URI;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.fabric3.api.model.type.java.Signature;
import org.fabric3.implementation.bytecode.proxy.common.ProxyFactory;
import org.fabric3.implementation.pojo.spi.proxy.WireProxyServiceExtension;
import org.fabric3.spi.classloader.ClassLoaderRegistry;
import org.fabric3.spi.classloader.MultiParentClassLoader;
import org.fabric3.spi.container.ContainerException;
import org.fabric3.spi.container.objectfactory.ObjectFactory;
import org.fabric3.spi.container.wire.InvocationChain;
import org.fabric3.spi.container.wire.Wire;
import org.fabric3.spi.model.physical.PhysicalOperationDefinition;
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

    public <T> ObjectFactory<T> createObjectFactory(Class<T> interfaze, Wire wire, String callbackUri) throws ContainerException {
        URI uri = getClassLoaderUri(interfaze);

        List<InvocationChain> list = wire.getInvocationChains();
        Map<Method, InvocationChain> mappings = resolveMethods(interfaze, list);
        Method[] methods = mappings.keySet().toArray(new Method[mappings.size()]);
        InvocationChain[] chains = mappings.values().toArray(new InvocationChain[mappings.size()]);

        return new WireProxyObjectFactory<>(uri, interfaze, methods, chains, callbackUri, proxyFactory);
    }

    public <T> ObjectFactory<T> createCallbackObjectFactory(Class<T> interfaze, boolean multiThreaded, URI callbackUri, Wire wire) throws ContainerException {
        URI uri = getClassLoaderUri(interfaze);

        List<InvocationChain> list = wire.getInvocationChains();
        Map<Method, InvocationChain> mappings = resolveMethods(interfaze, list);
        Method[] methods = mappings.keySet().toArray(new Method[mappings.size()]);
        InvocationChain[] chains = mappings.values().toArray(new InvocationChain[mappings.size()]);

        String callbackString = callbackUri.toString();
        return new CallbackWireObjectFactory<>(uri, interfaze, methods, callbackString, chains, proxyFactory);
    }

    public <T> ObjectFactory<?> updateCallbackObjectFactory(ObjectFactory<?> factory, Class<T> interfaze, boolean multiThreaded, URI callbackUri, Wire wire)
            throws ContainerException {
        if (!(factory instanceof CallbackWireObjectFactory)) {
            throw new ContainerException("Expected object factory of type: " + CallbackWireObjectFactory.class.getName());
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

    private Map<Method, InvocationChain> resolveMethods(Class<?> interfaze, List<InvocationChain> chains) throws ContainerException {
        Map<Method, InvocationChain> chainMappings = new HashMap<>(chains.size());
        for (InvocationChain chain : chains) {
            PhysicalOperationDefinition operation = chain.getPhysicalOperation();
            try {
                Method method = findMethod(interfaze, operation);
                chainMappings.put(method, chain);
            } catch (NoSuchMethodException e) {
                throw new ContainerException(operation.getName());
            } catch (ClassNotFoundException e) {
                throw new ContainerException(e);
            }
        }

        Map<String, Method> sorted = new TreeMap<>();
        for (Method method : chainMappings.keySet()) {
            String key = new Signature(method).toString();
            sorted.put(key, method);
        }

        Map<Method, InvocationChain> result = new LinkedHashMap<>();
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
