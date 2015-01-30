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
package org.fabric3.api.binding.jms.builder;

import org.fabric3.api.binding.jms.model.CreateOption;
import org.fabric3.api.binding.jms.model.Destination;
import org.fabric3.api.binding.jms.model.DestinationType;
import org.fabric3.api.binding.jms.model.ResponseDefinition;
import org.fabric3.api.model.type.builder.AbstractBuilder;

/**
 *
 */
public class ResponseDefinitionBuilder extends AbstractBuilder {
    private ResponseDefinition responseDefinition;

    public static ResponseDefinitionBuilder newBuilder() {
        return new ResponseDefinitionBuilder();
    }

    private ResponseDefinitionBuilder() {
        responseDefinition = new ResponseDefinition();
    }

    public ResponseDefinition build() {
        checkState();
        freeze();
        if (responseDefinition.getDestination() == null) {
            throw new IllegalArgumentException("Response destination not defined for JMS binding");
        }
        return responseDefinition;
    }

    public ResponseDefinitionBuilder destination(String name, DestinationType type, CreateOption option) {
        checkState();
        Destination definition = new Destination();
        definition.setName(name);
        definition.setType(type);
        definition.setCreate(option);
        responseDefinition.setDestination(definition);
        return this;
    }

    public ResponseDefinitionBuilder destination(String name, DestinationType type) {
        checkState();
        Destination definition = new Destination();
        definition.setName(name);
        definition.setType(type);
        responseDefinition.setDestination(definition);
        return this;
    }

    public ResponseDefinitionBuilder connectionFactoryName(String name) {
        checkState();
        responseDefinition.getConnectionFactory().setName(name);
        return this;
    }

    public ResponseDefinitionBuilder connectionFactoryCreate(CreateOption option) {
        checkState();
        responseDefinition.getConnectionFactory().setCreate(option);
        return this;
    }

}
