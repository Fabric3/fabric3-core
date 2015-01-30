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
package org.fabric3.binding.jms.runtime.resolver.destination;

import javax.jms.ConnectionFactory;
import java.util.List;

import org.fabric3.api.binding.jms.model.Destination;
import org.fabric3.binding.jms.runtime.resolver.DestinationStrategy;
import org.fabric3.binding.jms.spi.runtime.provider.DestinationResolver;
import org.fabric3.binding.jms.spi.runtime.provider.JmsResolutionException;
import org.oasisopen.sca.annotation.Reference;

/**
 * Implementation that always resolves a destination against JNDI and never attempts to create it.
 */
public class NeverDestinationStrategy implements DestinationStrategy {
    private List<DestinationResolver> resolvers;

    @Reference(required = false)
    public void setResolvers(List<DestinationResolver> resolvers) {
        this.resolvers = resolvers;
    }

    public javax.jms.Destination getDestination(Destination definition, ConnectionFactory factory) throws JmsResolutionException {
        for (DestinationResolver resolver : resolvers) {
            javax.jms.Destination destination = resolver.resolve(definition);
            if (destination != null) {
                return destination;
            }
        }
        throw new JmsResolutionException("Destination not found: " + definition.getName());
    }

}
