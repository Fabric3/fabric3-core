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
package org.fabric3.implementation.bytecode.proxy.channel;

import java.lang.reflect.Method;
import java.net.URI;
import java.util.function.Supplier;

import org.fabric3.api.host.Fabric3Exception;
import org.fabric3.implementation.bytecode.proxy.common.ProxyFactory;
import org.fabric3.spi.container.channel.ChannelConnection;

/**
 * Creates a proxy for a channel.
 */
public class ChannelProxySupplier<T> implements Supplier<T> {
    private ProxyFactory proxyFactory;
    private URI uri;
    private Class<T> interfaze;
    private Method[] methods;
    private ChannelConnection connection;

    private T proxy;

    public ChannelProxySupplier(URI uri, Class<T> interfaze, Method method, ChannelConnection connection, ProxyFactory proxyFactory) {
        this.uri = uri;
        this.interfaze = interfaze;
        this.methods = new Method[]{method};
        this.connection = connection;
        this.proxyFactory = proxyFactory;
    }

    public T get() throws Fabric3Exception {
        if (proxy == null) {
            proxy = proxyFactory.createProxy(uri, interfaze, methods, ChannelProxyDispatcher.class, false);
            ((ChannelProxyDispatcher) proxy).init(connection);
        }
        return proxy;
    }
}
