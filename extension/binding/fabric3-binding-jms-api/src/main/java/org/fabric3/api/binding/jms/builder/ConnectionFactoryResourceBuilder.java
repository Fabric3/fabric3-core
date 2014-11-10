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

import org.fabric3.api.binding.jms.resource.ConnectionFactoryConfiguration;
import org.fabric3.api.binding.jms.resource.ConnectionFactoryResource;
import org.fabric3.api.binding.jms.resource.ConnectionFactoryType;
import org.fabric3.api.model.type.builder.AbstractBuilder;

/**
 * Builds a connection factory configuration.
 */
public class ConnectionFactoryResourceBuilder extends AbstractBuilder {
    private ConnectionFactoryResource definition;

    /**
     * Creates a builder.
     *
     * @return the builder
     */
    public static ConnectionFactoryResourceBuilder newBuilder(String name) {
        return new ConnectionFactoryResourceBuilder(name);
    }

    private ConnectionFactoryResourceBuilder(String name) {
        definition = new ConnectionFactoryResource(new ConnectionFactoryConfiguration(name));
    }

    public ConnectionFactoryResourceBuilder property(String key, String value) {
        checkState();
        definition.getConfiguration().setFactoryProperty(key, value);
        return this;
    }

    /**
     * Sets the client id. The runtime name can be used via the <code>{runtime}</code> parameter.
     *
     * @param clientId the client id
     * @return the builder
     */
    public ConnectionFactoryResourceBuilder clientId(String clientId) {
        checkState();
        definition.getConfiguration().setClientId(clientId);
        return this;
    }

    public ConnectionFactoryResourceBuilder attribute(String key, Object value) {
        checkState();
        definition.getConfiguration().addAttribute(key, value);
        return this;
    }

    public ConnectionFactoryResourceBuilder provider(String provider) {
        checkState();
        definition.getConfiguration().setProvider(provider);
        return this;
    }

    public ConnectionFactoryResourceBuilder type(ConnectionFactoryType type) {
        checkState();
        definition.getConfiguration().setType(type);
        return this;
    }

    public ConnectionFactoryResourceBuilder username(String username) {
        checkState();
        definition.getConfiguration().setUsername(username);
        return this;
    }

    public ConnectionFactoryResourceBuilder password(String password) {
        checkState();
        definition.getConfiguration().setPassword(password);
        return this;
    }

    public ConnectionFactoryResource build() {
        checkState();
        freeze();
        return definition;
    }
}