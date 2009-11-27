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
package org.fabric3.jpa.hibernate;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.naming.NamingException;
import javax.persistence.EntityManagerFactory;
import javax.persistence.spi.PersistenceUnitInfo;
import javax.sql.DataSource;

import org.hibernate.SessionFactory;
import org.hibernate.ejb.Ejb3Configuration;
import org.oasisopen.sca.annotation.Init;
import org.osoa.sca.annotations.Property;
import org.osoa.sca.annotations.Reference;

import org.fabric3.jpa.spi.EmfBuilderException;
import org.fabric3.jpa.spi.delegate.EmfBuilderDelegate;
import org.fabric3.resource.jndi.proxy.jdbc.DataSourceProxy;
import org.fabric3.spi.resource.DataSourceRegistry;
import org.fabric3.spi.synthesize.ComponentRegistrationException;
import org.fabric3.spi.synthesize.ComponentSynthesizer;

/**
 * @version $Rev$ $Date$
 */
public class HibernateDelegate implements EmfBuilderDelegate {

    private DataSourceRegistry dataSourceRegistry;
    private ComponentSynthesizer synthesizer;
    private Level logLevel = Level.WARNING;

    /**
     * Build the entity Manager factory (Hibernate {@link SessionFactory} )
     */
    public EntityManagerFactory build(PersistenceUnitInfo info, ClassLoader classLoader, String dataSourceName) throws EmfBuilderException {

        Ejb3Configuration cfg = new Ejb3Configuration();

        if (dataSourceName != null) {
            DataSource dataSource = dataSourceRegistry.getDataSource(dataSourceName);
            if (dataSource == null) {
                dataSource = mapDataSource(dataSourceName, dataSourceName);
            }
            cfg.setDataSource(dataSource);
        }

        cfg.getProperties().setProperty("hibernate.transaction.manager_lookup_class",
                                        "org.fabric3.jpa.hibernate.F3HibernateTransactionManagerLookup");
        cfg.configure(info, Collections.emptyMap());

        return cfg.buildEntityManagerFactory();
    }

    @Property(required = false)
    public void setLogLevel(String logLevel) {
        this.logLevel = Level.parse(logLevel);
    }

    @Reference
    public void setDataSourceRegistry(DataSourceRegistry dataSourceRegistry) {
        this.dataSourceRegistry = dataSourceRegistry;
    }

    @Reference
    public void setSynthesizer(ComponentSynthesizer synthesizer) {
        this.synthesizer = synthesizer;
    }

    @Init
    public void init() {
        // Hibernate defauklt level is INFO which is verbose. Only log warnings by default
        Logger.getLogger("org.hibernate").setLevel(logLevel);
    }

    /**
     * Maps a datasource from JNDI to a Fabric3 system component. This provides the defaulting behavior where a user does not have to explicitly
     * configure a Fabric3 DataSourceProxy when deploying to a managed environment that provides its own datasources.
     * <p/>
     * This mapping is done by creating a DataSourceProxy component dynamically, registering it with the DataSourceRegistry using the JNDI name as a
     * key, and adding it as a system component. Since the defaulting behavior derives the key from the JNDI name, a datasource is only mapped to a
     * sngle key. If a datasource must be mapped to multiple keys, manual configuration of a DataSourceProxy must be done.
     *
     * @param datasource      the datasource name
     * @param persistenceUnit the persistence unit the datasource is found in
     * @return a proxy to the datasource bound to the JNDI name
     * @throws DataSourceInitException if an error mapping the datasource is encountered
     */
    private DataSource mapDataSource(String datasource, String persistenceUnit) throws DataSourceInitException {
        DataSourceProxy proxy = new DataSourceProxy();
        proxy.setDataSourceRegistry(dataSourceRegistry);
        try {
            proxy.setJndiName(datasource);
            List<String> keys = new ArrayList<String>();
            keys.add(datasource);
            proxy.setDataSourceKeys(keys);
            proxy.init();
            // TODO unregister this when the app is undeployed that uses it
            synthesizer.registerComponent(datasource + "Component", DataSource.class, proxy, false);
            return proxy;
        } catch (NamingException e) {
            throw new DataSourceInitException("Datasource " + datasource + " specified in persistent unit " + persistenceUnit
                    + " was not found. The datasource must either be explicitly declared as part of the Fabric3 system configuration or provided"
                    + " via JNDI using the name of the data source.", e);
        } catch (ComponentRegistrationException e) {
            throw new DataSourceInitException("Error registering datasource " + datasource + " specified in persistent unit " + persistenceUnit, e);
        }
    }

}
