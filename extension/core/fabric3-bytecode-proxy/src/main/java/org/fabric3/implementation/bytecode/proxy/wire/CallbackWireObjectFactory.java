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
import java.util.Map;

import org.fabric3.implementation.bytecode.proxy.common.ProxyFactory;
import org.fabric3.spi.container.ContainerException;
import org.fabric3.spi.container.objectfactory.ObjectFactory;
import org.fabric3.spi.container.wire.InvocationChain;

/**
 * Creates proxies for a callback wire.
 */
public class CallbackWireObjectFactory<T> implements ObjectFactory<T> {
    private URI uri;
    private Class<T> interfaze;
    private Method[] methods;
    private Map<String, InvocationChain[]> chains;
    private ProxyFactory proxyFactory;

    private T proxy;

    public CallbackWireObjectFactory(URI uri,
                                     Class<T> interfaze,
                                     Method[] methods,
                                     String callbackUri,
                                     InvocationChain[] invocationChains,
                                     ProxyFactory proxyFactory) {
        this.uri = uri;
        this.interfaze = interfaze;

        this.methods = methods;
        this.chains = new HashMap<>();
        this.chains.put(callbackUri, invocationChains);
        this.proxyFactory = proxyFactory;
    }

    public T getInstance() throws ContainerException {
        if (proxy != null) {
            return proxy;
        }
        if (chains.size() == 1) {
            // if the component is only one callback, there will only be one invocation chain; use and optimized dispatcher
            OptimizedCallbackDispatcher dispatcher = (OptimizedCallbackDispatcher) proxyFactory.createProxy(uri,
                                                                                                            interfaze,
                                                                                                            methods,
                                                                                                            OptimizedCallbackDispatcher.class,
                                                                                                            true);
            dispatcher.init(chains.values().iterator().next());
            return interfaze.cast(dispatcher);
        } else {
            CallbackDispatcher dispatcher = (CallbackDispatcher) proxyFactory.createProxy(uri, interfaze, methods, CallbackDispatcher.class, true);
            dispatcher.init(chains);
            return interfaze.cast(dispatcher);
        }
    }

    public void updateMappings(String callbackUri, InvocationChain[] invocationChains) {
        chains.put(callbackUri, invocationChains);
        proxy = null;
    }

}
