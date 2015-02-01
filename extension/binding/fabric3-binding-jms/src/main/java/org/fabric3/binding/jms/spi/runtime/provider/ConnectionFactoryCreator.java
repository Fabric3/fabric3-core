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
 */
package org.fabric3.binding.jms.spi.runtime.provider;

import javax.jms.ConnectionFactory;

import org.fabric3.api.binding.jms.resource.ConnectionFactoryConfiguration;
import org.fabric3.api.host.Fabric3Exception;

/**
 * Implemented by a JMS provider to create connection factories on demand.
 */
public interface ConnectionFactoryCreator {

    /**
     * Creates the connection factory.
     *
     * @param configuration the configuration
     * @return the connection factory
     * @throws Fabric3Exception if there is an error creating the connection factory
     */
    ConnectionFactory create(ConnectionFactoryConfiguration configuration) throws Fabric3Exception;

    /**
     * Releases the connection factory. Implementations may close open connections and remove any resources allocated by the connection factory.
     *
     * @param factory the factory to release
     */
    void release(ConnectionFactory factory);
}
