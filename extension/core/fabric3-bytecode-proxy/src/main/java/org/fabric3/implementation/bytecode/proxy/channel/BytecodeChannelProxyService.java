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

import org.fabric3.api.host.Fabric3Exception;
import org.fabric3.api.host.Names;
import org.fabric3.implementation.bytecode.proxy.common.ProxyFactory;
import org.fabric3.implementation.pojo.spi.proxy.ChannelProxyServiceExtension;
import org.fabric3.spi.classloader.MultiParentClassLoader;
import org.fabric3.spi.container.channel.ChannelConnection;
import org.fabric3.spi.container.channel.EventStream;
import org.fabric3.spi.container.channel.EventStreamHandler;
import org.fabric3.spi.container.objectfactory.ObjectFactory;
import org.oasisopen.sca.annotation.Reference;

/**
 * Implementation that delegates to a {@link ProxyFactory} to create channel proxies.
 */
public class BytecodeChannelProxyService implements ChannelProxyServiceExtension {
    private ProxyFactory proxyFactory;

    public BytecodeChannelProxyService(@Reference ProxyFactory proxyFactory) {
        this.proxyFactory = proxyFactory;
    }

    public boolean isDefault() {
        return false;
    }

    public <T> ObjectFactory<T> createObjectFactory(Class<T> interfaze, ChannelConnection connection) throws Fabric3Exception{
        URI uri = getClassLoaderUri(interfaze);

        Method[] methods = interfaze.getMethods();
        if (methods.length > 1) {
            throw new Fabric3Exception("Channel interface must have only one method: " + interfaze.getName());
        } else if (methods.length == 0) {
            throw new Fabric3Exception("Channel interface must have one method: " + interfaze.getName());
        }

        EventStream stream = connection.getEventStream();
        Method method = methods[0];
        EventStreamHandler handler = stream.getHeadHandler();
        return new ChannelProxyObjectFactory<>(uri, interfaze, method, handler, proxyFactory);
    }

    private <T> URI getClassLoaderUri(Class<T> interfaze) {
        if (!(interfaze.getClassLoader() instanceof MultiParentClassLoader)) {
            return Names.BOOT_CONTRIBUTION;
        }
        return ((MultiParentClassLoader) interfaze.getClassLoader()).getName();
    }

}
