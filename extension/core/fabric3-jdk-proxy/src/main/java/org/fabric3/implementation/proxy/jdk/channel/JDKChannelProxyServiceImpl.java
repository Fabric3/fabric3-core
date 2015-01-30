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

import java.lang.reflect.Proxy;

import org.fabric3.spi.container.channel.ChannelConnection;
import org.fabric3.spi.container.channel.EventStream;
import org.fabric3.spi.container.objectfactory.ObjectFactory;

/**
 * The default ChannelProxyService that uses JDK dynamic proxies.
 */
public class JDKChannelProxyServiceImpl implements JDKChannelProxyService {

    public boolean isDefault() {
        return true;
    }

    public <T> ObjectFactory<T> createObjectFactory(Class<T> interfaze, ChannelConnection connection) {
        return new ChannelConnectionObjectFactory<>(interfaze, this, connection.getEventStream());
    }

    public <T> T createProxy(Class<T> interfaze, EventStream stream) {
        ClassLoader loader = interfaze.getClassLoader();
        JDKEventHandler handler = new JDKEventHandler(stream);
        return interfaze.cast(Proxy.newProxyInstance(loader, new Class[]{interfaze}, handler));
    }

}