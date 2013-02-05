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
 * ----------------------------------------------------
 *
 * Portions originally based on Apache Tuscany 2007
 * licensed under the Apache 2.0 license.
 *
 */
package org.fabric3.implementation.proxy.jdk.wire;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.oasisopen.sca.ServiceReference;
import org.oasisopen.sca.annotation.Reference;

import org.fabric3.implementation.pojo.builder.ProxyCreationException;
import org.fabric3.implementation.pojo.builder.WireProxyService;
import org.fabric3.spi.classloader.ClassLoaderRegistry;
import org.fabric3.spi.model.physical.PhysicalOperationDefinition;
import org.fabric3.spi.objectfactory.ObjectFactory;
import org.fabric3.spi.wire.InvocationChain;
import org.fabric3.spi.wire.Wire;

/**
 * The default WireProxyService implementation that uses JDK dynamic proxies.
 */
public class JDKWireProxyService implements WireProxyService {
    private ClassLoaderRegistry classLoaderRegistry;

    public JDKWireProxyService(@Reference ClassLoaderRegistry classLoaderRegistry) {
        this.classLoaderRegistry = classLoaderRegistry;
    }

    public <T> ObjectFactory<T> createObjectFactory(Class<T> interfaze, Wire wire, String callbackUri) throws ProxyCreationException {
        Map<Method, InvocationChain> mappings = createInterfaceToWireMapping(interfaze, wire);
        return new WireObjectFactory<T>(interfaze, callbackUri, this, mappings);
    }

    public <T> ObjectFactory<T> createCallbackObjectFactory(Class<T> interfaze, boolean multiThreaded, URI callbackUri, Wire wire)
            throws ProxyCreationException {
        Map<Method, InvocationChain> operationMappings = createInterfaceToWireMapping(interfaze, wire);
        Map<String, Map<Method, InvocationChain>> mappings = new HashMap<String, Map<Method, InvocationChain>>();
        mappings.put(callbackUri.toString(), operationMappings);
        return new CallbackWireObjectFactory<T>(interfaze, multiThreaded, this, mappings);
    }

    public <T> ObjectFactory<?> updateCallbackObjectFactory(ObjectFactory<?> factory,
                                                            Class<T> interfaze,
                                                            boolean multiThreaded,
                                                            URI callbackUri,
                                                            Wire wire) throws ProxyCreationException {
        if (!(factory instanceof CallbackWireObjectFactory)) {
            // a placeholder object factory (i.e. created when the callback is not wired) needs to be replaced 
            return createCallbackObjectFactory(interfaze, multiThreaded, callbackUri, wire);
        }
        CallbackWireObjectFactory<?> callbackFactory = (CallbackWireObjectFactory) factory;
        Map<Method, InvocationChain> operationMappings = createInterfaceToWireMapping(interfaze, wire);
        callbackFactory.updateMappings(callbackUri.toString(), operationMappings);
        return callbackFactory;
    }

    public <T> T createProxy(Class<T> interfaze, String callbackUri, Map<Method, InvocationChain> mappings) throws ProxyCreationException {
        JDKInvocationHandler<T> handler;
        handler = new JDKInvocationHandler<T>(interfaze, callbackUri, mappings);
        return handler.getService();
    }

    public <T> T createMultiThreadedCallbackProxy(Class<T> interfaze, Map<String, Map<Method, InvocationChain>> mappings)
            throws ProxyCreationException {
        ClassLoader cl = interfaze.getClassLoader();
        MultiThreadedCallbackInvocationHandler<T> handler = new MultiThreadedCallbackInvocationHandler<T>(interfaze, mappings);
        return interfaze.cast(Proxy.newProxyInstance(cl, new Class[]{interfaze}, handler));
    }

    public <T> T createCallbackProxy(Class<T> interfaze, Map<Method, InvocationChain> mapping) {
        ClassLoader cl = interfaze.getClassLoader();
        StatefulCallbackInvocationHandler<T> handler = new StatefulCallbackInvocationHandler<T>(interfaze, mapping);
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

    private Map<Method, InvocationChain> createInterfaceToWireMapping(Class<?> interfaze, Wire wire) throws NoMethodForOperationException {

        List<InvocationChain> invocationChains = wire.getInvocationChains();

        Map<Method, InvocationChain> chains = new HashMap<Method, InvocationChain>(invocationChains.size());
        for (InvocationChain chain : invocationChains) {
            PhysicalOperationDefinition operation = chain.getPhysicalOperation();
            try {
                Method method = findMethod(interfaze, operation);
                chains.put(method, chain);
            } catch (NoSuchMethodException e) {
                throw new NoMethodForOperationException(operation.getName());
            } catch (ClassNotFoundException e) {
                throw new NoMethodForOperationException(e);
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
