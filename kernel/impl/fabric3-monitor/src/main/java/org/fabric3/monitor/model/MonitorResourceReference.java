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
package org.fabric3.monitor.model;

import org.fabric3.api.model.type.component.ResourceReferenceDefinition;
import org.fabric3.api.model.type.contract.ServiceContract;

/**
 *
 */
public class MonitorResourceReference extends ResourceReferenceDefinition {
    private static final long serialVersionUID = -6723752212878850748L;
    private String destination;

    /**
     * Constructor that uses the default channel.
     *
     * @param name     the resource name
     * @param contract the service contract required of the resource
     */
    public MonitorResourceReference(String name, ServiceContract contract) {
        super(name, contract, false);
    }

    /**
     * Constructor.
     *
     * @param name        the resource name
     * @param contract    the service contract required of the resource
     * @param destination the destination to send monitor events
     */
    public MonitorResourceReference(String name, ServiceContract contract, String destination) {
        super(name, contract, false);
        this.destination = destination;
    }

    /**
     * Returns the destination to send monitor events or null if the channel is not specified and a default should be used.
     *
     * @return the target destination to send monitor events or null
     */
    public String getDestination() {
        return destination;
    }
}
