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

import org.fabric3.api.host.Fabric3Exception;
import org.fabric3.implementation.pojo.spi.proxy.ChannelProxyServiceExtension;
import org.fabric3.spi.container.channel.ChannelConnection;

/**
 * Creates channel proxies using JDK proxies.
 */
public interface JDKChannelProxyService extends ChannelProxyServiceExtension {

    /**
     * Creates an optimized proxy for an interface containing a single method which dispatches to an event stream.
     *
     * @param interfaze  the interface the proxy implements
     * @param connection the channel connection
     * @param <T>        the interface type
     * @return the proxy
     * @throws Fabric3Exception if there is an error creating the proxy
     */
    <T> T createProxy(Class<T> interfaze, ChannelConnection connection) throws Fabric3Exception;

}
