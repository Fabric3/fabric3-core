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
package org.fabric3.api.model.type.builder;

import java.util.ArrayList;

import org.fabric3.api.model.type.resource.datasource.DataSourceConfiguration;
import org.fabric3.api.model.type.resource.datasource.DataSourceType;

/**
 * Builds {@link DataSourceConfiguration}s.
 */
public class DataSourceConfigurationBuilder extends AbstractBuilder {
    private DataSourceConfiguration configuration;

    /**
     * Creates a builder.
     *
     * @return the builder
     */
    public static DataSourceConfigurationBuilder newBuilder(String name, String driverClass, DataSourceType type) {
        return new DataSourceConfigurationBuilder(name, driverClass, type);
    }

    private DataSourceConfigurationBuilder(String name, String driverClass, DataSourceType type) {
        configuration = new DataSourceConfiguration(name, driverClass, type);
        configuration.setAliases(new ArrayList<String>());
    }

    public DataSourceConfigurationBuilder alias(String alias) {
        checkState();
        configuration.getAliases().add(alias);
        return this;
    }

    public DataSourceConfigurationBuilder connectionTimeout(int timeout) {
        checkState();
        configuration.setConnectionTimeout(timeout);
        return this;
    }

    public DataSourceConfigurationBuilder loginTimeout(int timeout) {
        checkState();
        configuration.setLoginTimeout(timeout);
        return this;
    }

    public DataSourceConfigurationBuilder maintenanceInterval(int interval) {
        checkState();
        configuration.setMaintenanceInterval(interval);
        return this;
    }

    public DataSourceConfigurationBuilder maxIdle(int idle) {
        checkState();
        configuration.setMaxIdle(idle);
        return this;
    }

    public DataSourceConfigurationBuilder maxPoolSize(int size) {
        checkState();
        configuration.setMaxPoolSize(size);
        return this;
    }

    public DataSourceConfigurationBuilder minPoolSize(int size) {
        checkState();
        configuration.setMinPoolSize(size);
        return this;
    }

    public DataSourceConfigurationBuilder poolSize(int size) {
        checkState();
        configuration.setPoolSize(size);
        return this;
    }

    public DataSourceConfigurationBuilder password(String password) {
        checkState();
        configuration.setPassword(password);
        return this;
    }

    public DataSourceConfigurationBuilder username(String name) {
        checkState();
        configuration.setUsername(name);
        return this;
    }

    public DataSourceConfigurationBuilder property(String name, String value) {
        checkState();
        configuration.setProperty(name, value);
        return this;
    }

    public DataSourceConfigurationBuilder query(String query) {
        checkState();
        configuration.setQuery(query);
        return this;
    }

    public DataSourceConfigurationBuilder reap(int reap) {
        checkState();
        configuration.setReap(reap);
        return this;
    }

    public DataSourceConfigurationBuilder url(String url) {
        checkState();
        configuration.setUrl(url);
        return this;
    }

    public DataSourceConfiguration build() {
        checkState();
        freeze();
        return configuration;
    }

}
