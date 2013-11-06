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
