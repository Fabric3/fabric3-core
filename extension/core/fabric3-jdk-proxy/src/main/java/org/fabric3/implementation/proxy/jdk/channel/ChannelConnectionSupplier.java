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
package org.fabric3.implementation.proxy.jdk.channel;

import java.util.function.Supplier;

import org.fabric3.api.host.Fabric3Exception;
import org.fabric3.spi.container.channel.EventStream;

/**
 * Creates a proxy for a channel connection that implements a specified interface with a single method.
 */
public class ChannelConnectionSupplier<T> implements Supplier<T> {
    private Class<T> interfaze;
    private JDKChannelProxyService proxyService;
    private EventStream stream;

    private T proxy;

    /**
     * Constructor.
     *
     * @param interfaze    the interface the proxy implements
     * @param proxyService the proxy creation service
     * @param stream       the stream
     */
    public ChannelConnectionSupplier(Class<T> interfaze, JDKChannelProxyService proxyService, EventStream stream) {
        this.interfaze = interfaze;
        this.proxyService = proxyService;
        this.stream = stream;
    }

    public T get() throws Fabric3Exception {
        // as an optimization, only create one proxy since they are stateless
        if (proxy == null) {
            proxy = interfaze.cast(proxyService.createProxy(interfaze, stream));
        }
        return proxy;
    }
}