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
package org.fabric3.binding.jms.runtime.resolver;

import javax.jms.ConnectionFactory;

import org.fabric3.api.binding.jms.model.ConnectionFactoryDefinition;
import org.fabric3.spi.container.ContainerException;

/**
 * Strategy for looking up connection factories.
 */
public interface ConnectionFactoryStrategy {

    /**
     * Gets the connection factory.
     *
     * @param definition the connection factory definition.
     * @return the connection factory
     * @throws ContainerException if there is an error returning the connection factory
     */
    ConnectionFactory getConnectionFactory(ConnectionFactoryDefinition definition) throws ContainerException;

    /**
     * Signals that a connection factory is being released and resources can be disposed.
     *
     * @param definition the definition that created the connection factory
     * @throws ContainerException if there is an error releasing resources
     */
    void release(ConnectionFactoryDefinition definition) throws ContainerException;

}
