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
package org.fabric3.jpa.runtime.builder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.naming.NamingException;
import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;

import org.hibernate.ejb.Ejb3Configuration;
import org.oasisopen.sca.annotation.Init;
import org.osoa.sca.annotations.Constructor;
import org.osoa.sca.annotations.Property;
import org.osoa.sca.annotations.Reference;
import org.osoa.sca.annotations.Service;

import org.fabric3.jpa.runtime.proxy.EmfCache;
import org.fabric3.resource.jndi.proxy.jdbc.DataSourceProxy;
import org.fabric3.spi.builder.classloader.ClassLoaderListener;
import org.fabric3.spi.resource.DataSourceRegistry;
import org.fabric3.spi.synthesize.ComponentRegistrationException;
import org.fabric3.spi.synthesize.ComponentSynthesizer;

/**
 * EmfBuilder that uses an EmfCache to cache created EntityManagerFactory instances.
 *
 * @version $Rev$ $Date$
 */
@Service(interfaces = {EmfBuilder.class, ClassLoaderListener.class})
public class CachingEmfBuilder implements EmfBuilder, ClassLoaderListener {
    private PersistenceUnitScanner scanner;
    private EmfCache cache;
    private Map<ClassLoader, Set<String>> classLoaderCache = new HashMap<ClassLoader, Set<String>>();

    private DataSourceRegistry dataSourceRegistry;
    private ComponentSynthesizer synthesizer;
    private Level logLevel = Level.WARNING;

    @Property(required = false)
    public void setLogLevel(String logLevel) {
        this.logLevel = Level.parse(logLevel);
    }

    public CachingEmfBuilder(PersistenceUnitScanner scanner, EmfCache cache) {
        this.scanner = scanner;
        this.cache = cache;
    }

    @Constructor
    public CachingEmfBuilder(@Reference PersistenceUnitScanner scanner,
                             @Reference EmfCache cache,
                             @Reference ComponentSynthesizer synthesizer,
                             @Reference DataSourceRegistry dataSourceRegistry) {
        this.scanner = scanner;
        this.cache = cache;
        this.synthesizer = synthesizer;
        this.dataSourceRegistry = dataSourceRegistry;
    }

    @Init
    public void init() {
        // Hibernate defauklt level is INFO which is verbose. Only log warnings by default
        Logger.getLogger("org.hibernate").setLevel(logLevel);
    }

    public void onBuild(ClassLoader loader) {
        // no-op
    }

    public void onDestroy(ClassLoader loader) {
        Set<String> names = classLoaderCache.remove(loader);
        if (names != null) {
            for (String name : names) {
                scanner.release(name);
            }
        }
    }

    public synchronized EntityManagerFactory build(String unitName, ClassLoader classLoader) throws EmfBuilderException {
        EntityManagerFactory emf = cache.getEmf(unitName);
        if (emf != null) {
            return emf;
        }
        emf = createEntityManagerFactory(unitName, classLoader);
        cache.putEmf(unitName, emf);
        Set<String> names = classLoaderCache.get(classLoader);
        if (names == null) {
            names = new HashSet<String>();
            classLoaderCache.put(classLoader, names);
        }
        names.add(unitName);
        return emf;
    }

    /**
     * Creates the entity manager factory using the JPA provider API.
     *
     * @param unitName    the persistence unit name
     * @param classLoader the persistence unit classloader
     * @return the entity manager factory
     * @throws EmfBuilderException if there is an error creating the factory
     */
    private EntityManagerFactory createEntityManagerFactory(String unitName, ClassLoader classLoader) throws EmfBuilderException {
        F3PersistenceUnitInfo info = scanner.getPersistenceUnitInfo(unitName, classLoader);
        String dataSourceName = info.getDataSourceName();
        Ejb3Configuration cfg = new Ejb3Configuration();

        if (dataSourceName != null) {
            DataSource dataSource = dataSourceRegistry.getDataSource(dataSourceName);
            if (dataSource == null) {
                dataSource = mapDataSource(dataSourceName, dataSourceName);
            }
            cfg.setDataSource(dataSource);
        }
        cfg.getProperties().setProperty("hibernate.transaction.manager_lookup_class",
                                        org.fabric3.jpa.runtime.tx.F3TransactionManagerLookup.class.getName());
        cfg.configure(info, Collections.emptyMap());
        return cfg.buildEntityManagerFactory();
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
