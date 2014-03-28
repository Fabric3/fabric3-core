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
package org.fabric3.binding.jms.spi.runtime.connection;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * A base connection factory configuration.
 */
public class ConnectionFactoryConfiguration {
    private String name;
    private ConnectionFactoryType type = ConnectionFactoryType.XA;
    private String username;
    private String password;
    private String provider;

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
     * Returns the connection factory name.
     *
     * @return the connection factory name.
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the JMS provider name.
     *
     * @return the JMS provider name
     */
    public String getProvider() {
        return provider;
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
        return type.cast(attributes.get(key));
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
