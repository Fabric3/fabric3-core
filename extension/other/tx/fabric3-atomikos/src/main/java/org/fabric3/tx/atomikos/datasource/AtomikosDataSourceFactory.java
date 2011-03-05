/*
 * Fabric3
 * Copyright (c) 2009-2011 Metaform Systems
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

package org.fabric3.tx.atomikos.datasource;

import java.sql.SQLException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import javax.xml.stream.XMLStreamReader;

import com.atomikos.jdbc.AbstractDataSourceBean;
import com.atomikos.jdbc.AtomikosDataSourceBean;
import com.atomikos.jdbc.nonxa.AtomikosNonXADataSourceBean;
import org.osoa.sca.annotations.Destroy;
import org.osoa.sca.annotations.EagerInit;
import org.osoa.sca.annotations.Init;
import org.osoa.sca.annotations.Property;
import org.osoa.sca.annotations.Reference;

import org.fabric3.datasource.spi.DataSourceConfiguration;
import org.fabric3.datasource.spi.DataSourceFactory;
import org.fabric3.datasource.spi.DataSourceFactoryException;
import org.fabric3.datasource.spi.DataSourceRegistry;
import org.fabric3.datasource.spi.DataSourceType;
import org.fabric3.spi.management.ManagementException;
import org.fabric3.spi.management.ManagementService;

/**
 * Initializes configured data sources and provides facilities for creating datasources dynamically.
 *
 * @version $Rev$ $Date$
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
    public void setDataSources(XMLStreamReader reader) throws DataSourceParseException {
        configurations = parser.parse(reader);
    }

    @Init
    public void init() throws DataSourceFactoryException {
        beans = new HashMap<String, AbstractDataSourceBean>();
        for (DataSourceConfiguration configuration : configurations) {
            create(configuration);
        }
    }

    @Destroy
    public void destroy() {
        for (Map.Entry<String, AbstractDataSourceBean> entry : beans.entrySet()) {
            registry.unregister(entry.getKey());
            entry.getValue().close();
        }
    }

    public void create(DataSourceConfiguration configuration) throws DataSourceFactoryException {
        String name = configuration.getName();
        if (registry.getDataSource(name) != null) {
            throw new DuplicateDataSourceException("Datasource already registered with name: " + name);
        }
        if (DataSourceType.XA == configuration.getType()) {
            AtomikosDataSourceBean bean = new AtomikosDataSourceBean();
            bean.setUniqueResourceName(name);
            Properties properties = configuration.getProperties();
            bean.setXaProperties(properties);
            bean.setXaDataSourceClassName(configuration.getDriverClass());
            setBeanProperties(configuration, bean);
            registerManagement(bean);
            beans.put(name, bean);
            registry.register(name, bean);
        } else {
            AtomikosNonXADataSourceBean bean = new AtomikosNonXADataSourceBean();
            bean.setUniqueResourceName(name);
            bean.setDriverClassName(configuration.getDriverClass());
            bean.setUrl(configuration.getUrl());
            bean.setUser(configuration.getUsername());
            bean.setPassword(configuration.getPassword());
            setBeanProperties(configuration, bean);
            registerManagement(bean);
            beans.put(name, bean);
            registry.register(name, bean);
        }
    }

    private void setBeanProperties(DataSourceConfiguration configuration, AbstractDataSourceBean bean) throws DataSourceFactoryException {
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
            throw new DataSourceFactoryException(e);
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

    public void remove(DataSourceConfiguration configuration) throws DataSourceFactoryException {
        String name = configuration.getName();
        AbstractDataSourceBean bean = beans.remove(name);
        if (bean == null) {
            throw new DataSourceFactoryException("DataSource not registered: " + name);
        }
        registry.unregister(name);
        unRegisterManagement(bean);
        bean.close();
    }

    private void registerManagement(AbstractDataSourceBean bean) throws DataSourceFactoryException {
        String name = bean.getUniqueResourceName();
        try {
            DataSourceWrapper wrapper = new DataSourceWrapper(bean);
            managementService.export(name, "datasources", "Configured datasources", wrapper);
        } catch (ManagementException e) {
            throw new DataSourceFactoryException(e);
        }
    }

    private void unRegisterManagement(AbstractDataSourceBean bean) throws DataSourceFactoryException {
        try {
            String name = bean.getUniqueResourceName();
            managementService.remove(name, "datasources");
        } catch (ManagementException e) {
            throw new DataSourceFactoryException(e);
        }
    }

}
