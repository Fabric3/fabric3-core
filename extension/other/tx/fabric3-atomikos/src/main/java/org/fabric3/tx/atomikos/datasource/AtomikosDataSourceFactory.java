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

package org.fabric3.tx.atomikos.datasource;

import javax.xml.stream.XMLStreamReader;
import java.sql.SQLException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import com.atomikos.jdbc.AbstractDataSourceBean;
import com.atomikos.jdbc.AtomikosDataSourceBean;
import com.atomikos.jdbc.nonxa.AtomikosNonXADataSourceBean;
import org.fabric3.api.model.type.resource.datasource.DataSourceConfiguration;
import org.fabric3.api.model.type.resource.datasource.DataSourceType;
import org.fabric3.datasource.spi.DataSourceFactory;
import org.fabric3.datasource.spi.DataSourceRegistry;
import org.fabric3.api.host.ContainerException;
import org.fabric3.spi.management.ManagementService;
import org.oasisopen.sca.annotation.Destroy;
import org.oasisopen.sca.annotation.EagerInit;
import org.oasisopen.sca.annotation.Init;
import org.oasisopen.sca.annotation.Property;
import org.oasisopen.sca.annotation.Reference;

/**
 * Initializes configured data sources and provides facilities for creating datasources dynamically.
 */
@EagerInit
public class AtomikosDataSourceFactory implements DataSourceFactory {
    private DataSourceRegistry registry;
    private ManagementService managementService;
    private List<DataSourceConfiguration> configurations = Collections.emptyList();
    private Map<String, AbstractDataSourceBean> beans;
    private DataSourceConfigParser parser = new DataSourceConfigParser();

    public AtomikosDataSourceFactory(@Reference DataSourceRegistry registry, @Reference ManagementService managementService) {
        this.registry = registry;
        this.managementService = managementService;
    }

    @Property(required = false)
    public void setDataSources(XMLStreamReader reader) throws ContainerException {
        configurations = parser.parse(reader);
    }

    @Init
    public void init() throws ContainerException {
        beans = new HashMap<>();
        for (DataSourceConfiguration configuration : configurations) {
            create(configuration);
        }
    }

    @Destroy
    public void destroy() throws ContainerException {
        for (Map.Entry<String, AbstractDataSourceBean> entry : beans.entrySet()) {
            AbstractDataSourceBean bean = entry.getValue();
            registry.unregister(entry.getKey());
            unRegisterManagement(bean);
            bean.close();
        }
    }

    public void create(DataSourceConfiguration configuration) throws ContainerException {
        String name = configuration.getName();
        if (registry.getDataSource(name) != null) {
            throw new ContainerException("Datasource already registered with name: " + name);
        }
        for (String alias : configuration.getAliases()) {
            if (registry.getDataSource(alias) != null) {
                throw new ContainerException("Datasource already registered with name: " + name);
            }
        }
        if (DataSourceType.XA == configuration.getType()) {
            AtomikosDataSourceBean bean = new AtomikosDataSourceBean();
            bean.setUniqueResourceName(name);
            Properties properties = configuration.getProperties();
            bean.setXaProperties(properties);
            bean.setXaDataSourceClassName(configuration.getDriverClass());
            setBeanProperties(configuration, bean);
            registerManagement(bean, configuration.getAliases());
            beans.put(name, bean);
            registry.register(name, bean);
            for (String alias : configuration.getAliases()) {
                registry.register(alias, bean);
            }
        } else {
            AtomikosNonXADataSourceBean bean = new AtomikosNonXADataSourceBean();
            bean.setUniqueResourceName(name);
            bean.setDriverClassName(configuration.getDriverClass());
            bean.setUrl(configuration.getUrl());
            bean.setUser(configuration.getUsername());
            bean.setPassword(configuration.getPassword());
            setBeanProperties(configuration, bean);
            registerManagement(bean, configuration.getAliases());
            beans.put(name, bean);
            registry.register(name, bean);
            for (String alias : configuration.getAliases()) {
                registry.register(alias, bean);
            }
        }
    }

    public void remove(DataSourceConfiguration configuration) throws ContainerException {
        String name = configuration.getName();
        AbstractDataSourceBean bean = beans.remove(name);
        if (bean == null) {
            throw new ContainerException("DataSource not registered: " + name);
        }
        for (String alias : configuration.getAliases()) {
            registry.unregister(alias);
        }
        registry.unregister(name);
        unRegisterManagement(bean);
        bean.close();
    }

    private void registerManagement(AbstractDataSourceBean bean, List<String> aliases) throws ContainerException {
        String name = bean.getUniqueResourceName();
        DataSourceWrapper wrapper = new DataSourceWrapper(bean, aliases);
        managementService.export(encode(name), "datasources", "Configured datasources", wrapper);
    }

    private void unRegisterManagement(AbstractDataSourceBean bean) throws ContainerException {
        String name = bean.getUniqueResourceName();
        managementService.remove(encode(name), "datasources");
    }

    private String encode(String name) {
        return "datasources/" + name.toLowerCase();
    }

    private void setBeanProperties(DataSourceConfiguration configuration, AbstractDataSourceBean bean) throws ContainerException {
        int connectionTimeout = configuration.getConnectionTimeout();
        if (connectionTimeout != -1) {
            bean.setBorrowConnectionTimeout(connectionTimeout);
        }
        try {
            int loginTimeout = configuration.getLoginTimeout();
            if (loginTimeout != -1) {
                bean.setLoginTimeout(loginTimeout);
            }
        } catch (SQLException e) {
            throw new ContainerException(e);
        }

        int interval = configuration.getMaintenanceInterval();
        if (interval != -1) {
            bean.setMaintenanceInterval(interval);
        }
        int idleTime = configuration.getMaxIdle();
        if (idleTime != -1) {
            bean.setMaxIdleTime(idleTime);
        }
        int maxPoolSize = configuration.getMaxPoolSize();
        if (maxPoolSize != -1) {
            bean.setMaxPoolSize(maxPoolSize);
        }
        int minPoolSize = configuration.getMinPoolSize();
        if (minPoolSize != -1) {
            bean.setMinPoolSize(minPoolSize);
        }
        int poolSize = configuration.getPoolSize();
        if (poolSize != -1) {
            bean.setPoolSize(poolSize);
        }
        int reapTimeout = configuration.getReap();
        if (reapTimeout != -1) {
            bean.setReapTimeout(reapTimeout);
        }
        String query = configuration.getQuery();
        if (query != null) {
            bean.setTestQuery(query);
        }
    }
}
