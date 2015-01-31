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
package org.fabric3.binding.jms.spi.runtime.manager;

import javax.jms.ConnectionFactory;
import java.util.Map;

import org.fabric3.api.host.ContainerException;

/**
 * Manages JMS connection factories. Implementations are responsible for registering connection factories provided by a JMS provider with the runtime JTA
 * transaction manager in a way specific to the latter. For example, a ConnectionFactoryManager may implement JMS connection and session pooling specific to the
 * transaction manager.
 */
public interface ConnectionFactoryManager {

    /**
     * Registers a connection factory.
     *
     * @param name       the connection factory name
     * @param factory    the connection factory
     * @param properties properties such as pooling configuration
     * @return the registered connection factory, which may be a wrapper
     * @throws ContainerException if there is an error registering
     */
    ConnectionFactory register(String name, ConnectionFactory factory, Map<String, String> properties) throws ContainerException;

    /**
     * Removes a registered connection factory.
     *
     * @param name the connection factory name
     * @return the unregistered connection factory
     * @throws ContainerException if there is an error un-registering
     */
    ConnectionFactory unregister(String name) throws ContainerException;

    /**
     * Returns the registered connection factory for the given name.
     *
     * @param name the name the connection factory was registered with
     * @return the connection factory or null if no factory for the name was registered
     */
    ConnectionFactory get(String name);

}
