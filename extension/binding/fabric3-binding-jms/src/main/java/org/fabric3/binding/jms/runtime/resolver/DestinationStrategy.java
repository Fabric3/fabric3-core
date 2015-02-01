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

import org.fabric3.api.binding.jms.model.Destination;
import org.fabric3.api.host.Fabric3Exception;

/**
 * Strategy for looking up destinations.
 */
public interface DestinationStrategy {

    /**
     * Gets the destination based on SCA JMS binding rules.
     *
     * @param definition Destination definition.
     * @param factory    Connection factory.
     * @return Looked up or created destination.
     * @throws Fabric3Exception if there is an error looking up the destination
     */
    javax.jms.Destination getDestination(Destination definition, ConnectionFactory factory) throws Fabric3Exception;

}
