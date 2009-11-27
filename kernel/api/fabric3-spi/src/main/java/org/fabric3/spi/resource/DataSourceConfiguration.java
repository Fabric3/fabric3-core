/*
 * Fabric3
 * Copyright (c) 2009 Metaform Systems
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
package org.fabric3.spi.resource;

import java.util.Properties;

/**
 * Represents a configuration for a datasource specified in the runtime system configuration or dynamically through a management interface.
 *
 * @version $Rev$ $Date$
 */
public class DataSourceConfiguration {
    private String name;
    private String driverClass;
    private DataSourceType type;
    private String url;
    private String username;
    private String password;

    private Properties properties = new Properties();

    public DataSourceConfiguration(String name, String driverClass, DataSourceType type) {
        this.name = name;
        this.driverClass = driverClass;
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public String getDriverClass() {
        return driverClass;
    }

    public DataSourceType getType() {
        return type;
    }

    public Object getProperty(String name) {
        return properties.get(name);
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    /**
     * Sets a driver-specific property.
     *
     * @param name  the property name
     * @param value the property value
     */
    public void setProperty(String name, String value) {
        properties.put(name, value);
    }

    /**
     * Returns the driver-specific properties configured for the datasource
     *
     * @return the properties
     */
    public Properties getProperties() {
        return properties;
    }

}
