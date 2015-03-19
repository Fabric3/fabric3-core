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

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import org.fabric3.api.host.Fabric3Exception;
import org.fabric3.spi.container.wire.InvocationChain;
import org.fabric3.spi.container.wire.Wire;
import org.fabric3.spi.model.physical.PhysicalOperationDefinition;

/**
 * The default WireProxyService implementation that uses JDK dynamic proxies.
 */
public class JDKWireProxyServiceImpl implements JDKWireProxyService {

    public boolean isDefault() {
        return true;
    }

    public <T> Supplier<T> createSupplier(Class<T> interfaze, Wire wire, String callbackUri) {
        Map<Method, InvocationChain> mappings = createInterfaceToWireMapping(interfaze, wire);
        return new WireSupplier<>(interfaze, callbackUri, this, mappings);
    }

    public <T> Supplier<T> createCallbackSupplier(Class<T> interfaze, boolean multiThreaded, URI callbackUri, Wire wire) {
        Map<Method, InvocationChain> operationMappings = createInterfaceToWireMapping(interfaze, wire);
        Map<String, Map<Method, InvocationChain>> mappings = new HashMap<>();
        mappings.put(callbackUri.toString(), operationMappings);
        return new CallbackWireSupplier<>(interfaze, multiThreaded, this, mappings);
    }

    public <T> Supplier<?> updateCallbackSupplier(Supplier<?> supplier, Class<T> interfaze, boolean multiThreaded, URI callbackUri, Wire wire) {
        if (!(supplier instanceof CallbackWireSupplier)) {
            // a placeholder Supplier (i.e. created when the callback is not wired) needs to be replaced
            return createCallbackSupplier(interfaze, multiThreaded, callbackUri, wire);
        }
        CallbackWireSupplier<?> callbackFactory = (CallbackWireSupplier) supplier;
        Map<Method, InvocationChain> operationMappings = createInterfaceToWireMapping(interfaze, wire);
        callbackFactory.updateMappings(callbackUri.toString(), operationMappings);
        return callbackFactory;
    }

    public <T> T createProxy(Class<T> interfaze, String callbackUri, Map<Method, InvocationChain> mappings) {
        JDKInvocationHandler<T> handler;
        handler = new JDKInvocationHandler<>(interfaze, callbackUri, mappings);
        return handler.getService();
    }

    public <T> T createMultiThreadedCallbackProxy(Class<T> interfaze, Map<String, Map<Method, InvocationChain>> mappings) {
        ClassLoader cl = interfaze.getClassLoader();
        MultiThreadedCallbackInvocationHandler<T> handler = new MultiThreadedCallbackInvocationHandler<>(mappings);
        return interfaze.cast(Proxy.newProxyInstance(cl, new Class[]{interfaze}, handler));
    }

    public <T> T createCallbackProxy(Class<T> interfaze, Map<Method, InvocationChain> mapping) {
        ClassLoader cl = interfaze.getClassLoader();
        StatefulCallbackInvocationHandler<T> handler = new StatefulCallbackInvocationHandler<>(mapping);
        return interfaze.cast(Proxy.newProxyInstance(cl, new Class[]{interfaze}, handler));
    }

    private Map<Method, InvocationChain> createInterfaceToWireMapping(Class<?> interfaze, Wire wire) {

        List<InvocationChain> invocationChains = wire.getInvocationChains();

        Map<Method, InvocationChain> chains = new HashMap<>(invocationChains.size());
        for (InvocationChain chain : invocationChains) {
            PhysicalOperationDefinition operation = chain.getPhysicalOperation();
            try {
                Method method = findMethod(interfaze, operation);
                chains.put(method, chain);
            } catch (NoSuchMethodException e) {
                throw new Fabric3Exception(operation.getName());
            } catch (ClassNotFoundException e) {
                throw new Fabric3Exception(e);
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
        List<Class<?>> params = operation.getSourceParameterTypes();
        return clazz.getMethod(name, params.toArray(new Class[params.size()]));
    }

}
