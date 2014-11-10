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
package org.fabric3.api.binding.jms.resource;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * A base connection factory configuration.
 */
public class ConnectionFactoryConfiguration implements Serializable {
    private static final long serialVersionUID = 6041948376851715924L;

    public static final String RUNTIME = "{runtime}";

    private String name;
    private String provider;
    private ConnectionFactoryType type = ConnectionFactoryType.XA;
    private String username;
    private String password;
    private String clientId;

    private Map<String, Object> attributes = new HashMap<>();

    private Properties factoryProperties = new Properties();

    /**
     * Constructor.
     *
     * @param name     the connection factory name
     * @param provider the  JMS provider name
     */
    public ConnectionFactoryConfiguration(String name, String provider) {
        this.name = name;
        this.provider = provider;
    }

    /**
     * Constructor.
     *
     * @param name the connection factory name
     */
    public ConnectionFactoryConfiguration(String name) {
        this.name = name;
    }

    /**
     * Returns the connection factory name.
     *
     * @return the connection factory name.
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the JMS provider name or null if the default provider should be used.
     *
     * @return the JMS provider name
     */
    public String getProvider() {
        return provider;
    }

    /**
     * Sets the JMS provider.
     *
     * @param provider the JMS provider
     */
    public void setProvider(String provider) {
        this.provider = provider;
    }

    /**
     * Returns the connection factory type.
     *
     * @return the connection factory type
     */
    public ConnectionFactoryType getType() {
        return type;
    }

    /**
     * Sets the connection factory type.
     *
     * @param type the connection factory type
     */
    public void setType(ConnectionFactoryType type) {
        this.type = type;
    }

    /**
     * Returns the optional username for accessing the connection factory.
     *
     * @return the username
     */
    public String getUsername() {
        return username;
    }

    /**
     * Sets the optional username for accessing the connection factory.
     *
     * @param username the username
     */
    public void setUsername(String username) {
        this.username = username;
    }

    /**
     * Returns the optional password for accessing the connection factory.
     *
     * @return the password
     */
    public String getPassword() {
        return password;
    }

    /**
     * Sets the optional password for accessing the connection factory.
     *
     * @param password the password
     */
    public void setPassword(String password) {
        this.password = password;
    }

    /**
     * Returns the connection client id.
     *
     * @return the connection client id.
     */
    public String getClientId() {
        return clientId;
    }

    /**
     * Sets the connection client id.
     *
     * @param clientId the connection client id.
     */
    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    /**
     * Sets a factory property.
     *
     * @param key   the key
     * @param value the value
     */
    public void setFactoryProperty(String key, String value) {
        factoryProperties.put(key, value);
    }

    /**
     * Returns the factory properties.
     *
     * @return factory properties
     */
    public Properties getFactoryProperties() {
        return factoryProperties;
    }

    /**
     * Sets a provider-specific attribute such as a connection URL.
     *
     * @param type the attribute type
     * @param key  the key
     * @return the attribute or null if not found
     */
    public <T> T getAttribute(Class<T> type, String key) {
        Object value = attributes.get(key);
        if (value != null && !type.isInstance(value)) {
            throw new IllegalArgumentException(
                    "Attribute " + key + " is expected to be of type " + type.getName() + " but is of type " + value.getClass().getName());
        }
        return type.cast(value);
    }

    /**
     * Adds a provider-specific attribute.
     *
     * @param key   the key
     * @param value the value
     */
    public void addAttribute(String key, Object value) {
        attributes.put(key, value);
    }

    /**
     * Returns the provider attributes.
     *
     * @return the attributes
     */
    public Map<String, Object> getAttributes() {
        return attributes;
    }
}
