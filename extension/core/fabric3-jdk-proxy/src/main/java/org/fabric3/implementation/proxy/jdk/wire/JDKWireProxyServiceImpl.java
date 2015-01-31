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
 *
 * Portions originally based on Apache Tuscany 2007
 * licensed under the Apache 2.0 license.
 */
package org.fabric3.implementation.proxy.jdk.wire;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.fabric3.spi.classloader.ClassLoaderRegistry;
import org.fabric3.api.host.ContainerException;
import org.fabric3.spi.container.objectfactory.ObjectFactory;
import org.fabric3.spi.container.wire.InvocationChain;
import org.fabric3.spi.container.wire.Wire;
import org.fabric3.spi.model.physical.PhysicalOperationDefinition;
import org.oasisopen.sca.ServiceReference;
import org.oasisopen.sca.annotation.Reference;

/**
 * The default WireProxyService implementation that uses JDK dynamic proxies.
 */
public class JDKWireProxyServiceImpl implements JDKWireProxyService {
    private ClassLoaderRegistry classLoaderRegistry;

    public JDKWireProxyServiceImpl(@Reference ClassLoaderRegistry classLoaderRegistry) {
        this.classLoaderRegistry = classLoaderRegistry;
    }

    public boolean isDefault() {
        return true;
    }

    public <T> ObjectFactory<T> createObjectFactory(Class<T> interfaze, Wire wire, String callbackUri) throws ContainerException {
        Map<Method, InvocationChain> mappings = createInterfaceToWireMapping(interfaze, wire);
        return new WireObjectFactory<>(interfaze, callbackUri, this, mappings);
    }

    public <T> ObjectFactory<T> createCallbackObjectFactory(Class<T> interfaze, boolean multiThreaded, URI callbackUri, Wire wire)
           throws ContainerException  {
        Map<Method, InvocationChain> operationMappings = createInterfaceToWireMapping(interfaze, wire);
        Map<String, Map<Method, InvocationChain>> mappings = new HashMap<>();
        mappings.put(callbackUri.toString(), operationMappings);
        return new CallbackWireObjectFactory<>(interfaze, multiThreaded, this, mappings);
    }

    public <T> ObjectFactory<?> updateCallbackObjectFactory(ObjectFactory<?> factory, Class<T> interfaze, boolean multiThreaded, URI callbackUri, Wire wire)
            throws ContainerException {
        if (!(factory instanceof CallbackWireObjectFactory)) {
            // a placeholder object factory (i.e. created when the callback is not wired) needs to be replaced 
            return createCallbackObjectFactory(interfaze, multiThreaded, callbackUri, wire);
        }
        CallbackWireObjectFactory<?> callbackFactory = (CallbackWireObjectFactory) factory;
        Map<Method, InvocationChain> operationMappings = createInterfaceToWireMapping(interfaze, wire);
        callbackFactory.updateMappings(callbackUri.toString(), operationMappings);
        return callbackFactory;
    }

    public <T> T createProxy(Class<T> interfaze, String callbackUri, Map<Method, InvocationChain> mappings) throws ContainerException {
        JDKInvocationHandler<T> handler;
        handler = new JDKInvocationHandler<>(interfaze, callbackUri, mappings);
        return handler.getService();
    }

    public <T> T createMultiThreadedCallbackProxy(Class<T> interfaze, Map<String, Map<Method, InvocationChain>> mappings) throws ContainerException {
        ClassLoader cl = interfaze.getClassLoader();
        MultiThreadedCallbackInvocationHandler<T> handler = new MultiThreadedCallbackInvocationHandler<>(mappings);
        return interfaze.cast(Proxy.newProxyInstance(cl, new Class[]{interfaze}, handler));
    }

    public <T> T createCallbackProxy(Class<T> interfaze, Map<Method, InvocationChain> mapping) {
        ClassLoader cl = interfaze.getClassLoader();
        StatefulCallbackInvocationHandler<T> handler = new StatefulCallbackInvocationHandler<>(mapping);
        return interfaze.cast(Proxy.newProxyInstance(cl, new Class[]{interfaze}, handler));
    }

    @SuppressWarnings("unchecked")
    public <B, R extends ServiceReference<B>> R cast(B target) throws IllegalArgumentException {
        InvocationHandler handler = Proxy.getInvocationHandler(target);
        if (handler instanceof JDKInvocationHandler) {
            JDKInvocationHandler<B> jdkHandler = (JDKInvocationHandler<B>) handler;
            return (R) jdkHandler.getServiceReference();
        } else if (handler instanceof MultiThreadedCallbackInvocationHandler) {
            // TODO return a CallbackReference
            throw new UnsupportedOperationException();
        } else {
            throw new IllegalArgumentException("Not a Fabric3 SCA proxy");
        }
    }

    private Map<Method, InvocationChain> createInterfaceToWireMapping(Class<?> interfaze, Wire wire) throws ContainerException {

        List<InvocationChain> invocationChains = wire.getInvocationChains();

        Map<Method, InvocationChain> chains = new HashMap<>(invocationChains.size());
        for (InvocationChain chain : invocationChains) {
            PhysicalOperationDefinition operation = chain.getPhysicalOperation();
            try {
                Method method = findMethod(interfaze, operation);
                chains.put(method, chain);
            } catch (NoSuchMethodException e) {
                throw new ContainerException(operation.getName());
            } catch (ClassNotFoundException e) {
                throw new ContainerException(e);
            }
        }
        return chains;
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
