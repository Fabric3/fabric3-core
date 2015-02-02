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
package org.fabric3.implementation.pojo.spi.proxy;

import java.util.function.Supplier;

import org.fabric3.api.host.Fabric3Exception;
import org.fabric3.spi.container.channel.ChannelConnection;

/**
 * Delegates to a {@link ChannelProxyServiceExtension} to create proxy factories for a channel.
 */

public interface ChannelProxyService {

    /**
     * Creates a Supplier.
     *
     * @param interfaze  the interface the proxy implements
     * @param connection the channel connection to proxy
     * @param <T>        the interface type
     * @return the Supplier
     * @throws Fabric3Exception if there is an error creating the factory
     */
    <T> Supplier<T> createSupplier(Class<T> interfaze, ChannelConnection connection) throws Fabric3Exception;

}