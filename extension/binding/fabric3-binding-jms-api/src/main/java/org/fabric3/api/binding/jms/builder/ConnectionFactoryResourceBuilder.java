/*
 * Fabric3
 * Copyright (c) 2009-2013 Metaform Systems
 *
 * Fabric3 is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of
 * the License, or (at your option) any later version, with the
 * following exception:
 *
 * Linking this software statically or dynamically with other
 * modules is making a combined work based on this software.
 * Thus, the terms and conditions of the GNU General Public
 * License cover the whole combination.
 *
 * As a special exception, the copyright holders of this software
 * give you permission to link this software with independent
 * modules to produce an executable, regardless of the license
 * terms of these independent modules, and to copy and distribute
 * the resulting executable under terms of your choice, provided
 * that you also meet, for each linked independent module, the
 * terms and conditions of the license of that module. An
 * independent module is a module which is not derived from or
 * based on this software. If you modify this software, you may
 * extend this exception to your version of the software, but
 * you are not obligated to do so. If you do not wish to do so,
 * delete this exception statement from your version.
 *
 * Fabric3 is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the
 * GNU General Public License along with Fabric3.
 * If not, see <http://www.gnu.org/licenses/>.
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