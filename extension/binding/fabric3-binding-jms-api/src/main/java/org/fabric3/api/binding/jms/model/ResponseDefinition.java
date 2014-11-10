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
package org.fabric3.api.binding.jms.model;

import org.fabric3.api.model.type.ModelObject;

/**
 * A response configuration.
 */
public class ResponseDefinition extends ModelObject {
    private static final long serialVersionUID = -3413442748842988653L;
    private DestinationDefinition destination;
    private ConnectionFactoryDefinition connectionFactory = new ConnectionFactoryDefinition();
    private ActivationSpec activationSpec;

    public ConnectionFactoryDefinition getConnectionFactory() {
        return connectionFactory;
    }

    public void setConnectionFactory(ConnectionFactoryDefinition connectionFactory) {
        this.connectionFactory = connectionFactory;
    }

    public DestinationDefinition getDestination() {
        return destination;
    }

    public void setDestination(DestinationDefinition destination) {
        this.destination = destination;
    }

    public ActivationSpec getActivationSpec() {
        return activationSpec;
    }

    public void setActivationSpec(ActivationSpec activationSpec) {
        this.activationSpec = activationSpec;
    }
}
