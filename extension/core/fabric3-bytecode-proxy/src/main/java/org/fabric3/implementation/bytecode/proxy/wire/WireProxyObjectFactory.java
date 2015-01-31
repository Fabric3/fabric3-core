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

import org.fabric3.api.host.ContainerException;
import org.fabric3.implementation.bytecode.proxy.common.ProxyFactory;
import org.fabric3.spi.container.objectfactory.ObjectFactory;
import org.fabric3.spi.container.wire.InvocationChain;

/**
 * Creates a proxy for a wire.
 */
public class WireProxyObjectFactory<T> implements ObjectFactory<T> {
    private ProxyFactory proxyFactory;
    private URI uri;
    private Class<T> interfaze;
    private String callbackUri;
    private Method[] methods;
    private InvocationChain[] chains;

    private T proxy;

    public WireProxyObjectFactory(URI uri, Class<T> interfaze, Method[] methods, InvocationChain[] chains, String callbackUri, ProxyFactory proxyFactory) {
        this.uri = uri;
        this.interfaze = interfaze;
        this.methods = methods;
        this.chains = chains;
        this.callbackUri = callbackUri;
        this.proxyFactory = proxyFactory;
    }

    @SuppressWarnings("unchecked")
    public T getInstance() throws ContainerException {
        if (proxy == null) {
            proxy = proxyFactory.createProxy(uri, interfaze, methods, WireProxyDispatcher.class, true);
            WireProxyDispatcher dispatcher = (WireProxyDispatcher) proxy;
            dispatcher.init(interfaze, callbackUri, chains);
        }
        return proxy;
    }
}
