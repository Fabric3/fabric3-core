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
package org.fabric3.binding.jms.runtime.resolver;

import javax.jms.ConnectionFactory;

import org.fabric3.api.binding.jms.model.ConnectionFactoryDefinition;
import org.fabric3.api.binding.jms.model.Destination;
import org.fabric3.binding.jms.spi.runtime.provider.JmsResolutionException;

/**
 * Resolves administered objects, specifically connection factories and destinations. Different strategies may be used for resolution as defined by {@link
 * ConnectionFactoryDefinition} or {@link Destination}.
 */
public interface AdministeredObjectResolver {

    /**
     * Resolves a ConnectionFactory.
     *
     * @param definition the connection factory definition
     * @return the connection factory.
     * @throws JmsResolutionException if there is an error during resolution
     */
    ConnectionFactory resolve(ConnectionFactoryDefinition definition) throws JmsResolutionException;

    /**
     * Resolves a destination.
     *
     * @param destination the destination definition
     * @param factory    the connection factory
     * @return the destination
     * @throws JmsResolutionException if there is an error during resolution
     */
    javax.jms.Destination resolve(Destination destination, ConnectionFactory factory) throws JmsResolutionException;

    /**
     * Signals that a connection factory is being released and resources can be disposed.
     *
     * @param definition the definition that created the connection factory
     * @throws JmsResolutionException if there is an error releasing resources
     */
    void release(ConnectionFactoryDefinition definition) throws JmsResolutionException;

}
