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
package org.fabric3.api.model.type.resource.datasource;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

/**
 * Configuration used to create a datasource on a runtime.
 */
public class DataSourceConfiguration implements Serializable {
    private static final long serialVersionUID = -2790525523535863636L;

    private String name;
    private List<String> aliases;
    private String driverClass;
    private DataSourceType type;
    private String url;
    private String username;
    private String password;
    private int maxPoolSize = -1;
    private int minPoolSize = -1;
    private int connectionTimeout = -1;
    private int loginTimeout = -1;
    private int maintenanceInterval = -1;
    private int maxIdle = -1;
    private int poolSize = -1;
    private int reap = -1;
    private String query;

    private Properties properties = new Properties();

    public DataSourceConfiguration(String name, String driverClass, DataSourceType type) {
        this.name = name;
        this.driverClass = driverClass;
        this.type = type;
        this.aliases = Collections.emptyList();
    }

    public String getName() {
        return name;
    }

    public List<String> getAliases() {
        return aliases;
    }

    public void setAliases(List<String> aliases) {
        this.aliases = aliases;
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

    public int getMaxPoolSize() {
        return maxPoolSize;
    }

    public void setMaxPoolSize(int maxPoolSize) {
        this.maxPoolSize = maxPoolSize;
    }

    public int getMinPoolSize() {
        return minPoolSize;
    }

    public void setMinPoolSize(int minPoolSize) {
        this.minPoolSize = minPoolSize;
    }

    public int getConnectionTimeout() {
        return connectionTimeout;
    }

    public void setConnectionTimeout(int connectionTimeout) {
        this.connectionTimeout = connectionTimeout;
    }

    public int getLoginTimeout() {
        return loginTimeout;
    }

    public void setLoginTimeout(int loginTimeout) {
        this.loginTimeout = loginTimeout;
    }

    public int getMaintenanceInterval() {
        return maintenanceInterval;
    }

    public void setMaintenanceInterval(int maintenanceInterval) {
        this.maintenanceInterval = maintenanceInterval;
    }

    public int getMaxIdle() {
        return maxIdle;
    }

    public void setMaxIdle(int maxIdle) {
        this.maxIdle = maxIdle;
    }

    public int getPoolSize() {
        return poolSize;
    }

    public void setPoolSize(int poolSize) {
        this.poolSize = poolSize;
    }

    public int getReap() {
        return reap;
    }

    public void setReap(int reap) {
        this.reap = reap;
    }

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
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
