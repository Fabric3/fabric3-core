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
package org.fabric3.jpa.runtime.emf;

import java.net.URI;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.persistence.EntityManagerFactory;
import javax.persistence.spi.PersistenceUnitInfo;
import javax.sql.DataSource;

import org.hibernate.ejb.Ejb3Configuration;
import org.oasisopen.sca.annotation.Init;
import org.osoa.sca.annotations.Property;
import org.osoa.sca.annotations.Reference;

import org.fabric3.api.annotation.monitor.MonitorLevel;
import org.fabric3.host.Names;
import org.fabric3.jpa.api.EntityManagerFactoryResolver;
import org.fabric3.jpa.api.F3TransactionManagerLookup;
import org.fabric3.jpa.api.JpaResolutionException;
import org.fabric3.spi.classloader.MultiParentClassLoader;
import org.fabric3.spi.monitor.MonitorService;

/**
 * An {@link EntityManagerFactoryResolver} implementation that caches EntityManagerFactory instances.
 *
 * @version $Rev$ $Date$
 */
public class CachingEntityManagerFactoryResolver implements EntityManagerFactoryResolver {
    private static final String HIBERNATE_LOOKUP = "hibernate.transaction.manager_lookup_class";

    private PersistenceContextParser parser;
    private EntityManagerFactoryCache cache;
    private MonitorService monitorService;

    private MonitorLevel logLevel = MonitorLevel.WARNING;

    @Property(required = false)
    public void setMonitorLevel(String logLevel) {
        this.logLevel = MonitorLevel.valueOf(logLevel);
    }

    public CachingEntityManagerFactoryResolver(@Reference PersistenceContextParser parser,
                                               @Reference EntityManagerFactoryCache cache,
                                               @Reference MonitorService monitorService) {
        this.parser = parser;
        this.cache = cache;
        this.monitorService = monitorService;
    }

    @Init
    public void init() {
        // Hibernate default level is INFO which is verbose. Only log warnings by default
        monitorService.setProviderLevel("org.hibernate", logLevel.toString());
    }

    public synchronized EntityManagerFactory resolve(String unitName, ClassLoader classLoader) throws JpaResolutionException {
        EntityManagerFactory resolvedEmf = cache.get(unitName);
        if (resolvedEmf != null) {
            return resolvedEmf;
        }

        Map<String, EntityManagerFactory> emfs = createEntityManagerFactories(classLoader);
        URI key;
        if (classLoader instanceof MultiParentClassLoader) {
            key = ((MultiParentClassLoader) classLoader).getName();
        } else {
            key = Names.HOST_CONTRIBUTION;
        }
        for (Map.Entry<String, EntityManagerFactory> entry : emfs.entrySet()) {
            String name = entry.getKey();
            EntityManagerFactory emf = entry.getValue();
            cache.put(key, name, emf);
            if (unitName.equals(name)) {
                resolvedEmf = emf;
            }

        }
        return resolvedEmf;
    }

    /**
     * Creates EntityManagerFactory instances for all persistence units defined in the persistence.xml file.
     *
     * @param classLoader the persistence unit classloader
     * @return the entity manager factory
     * @throws JpaResolutionException if there is an error creating the factory
     */
    private Map<String, EntityManagerFactory> createEntityManagerFactories(ClassLoader classLoader) throws JpaResolutionException {
        Map<String, EntityManagerFactory> emfs = new HashMap<String, EntityManagerFactory>();
        List<PersistenceUnitInfo> infos = parser.parse(classLoader);
        for (PersistenceUnitInfo info : infos) {
            Ejb3Configuration cfg = new Ejb3Configuration();
            DataSource dataSource = info.getJtaDataSource();
            if (dataSource == null) {
                dataSource = info.getNonJtaDataSource();
            }
            cfg.setDataSource(dataSource);
            cfg.getProperties().setProperty(HIBERNATE_LOOKUP, F3TransactionManagerLookup.class.getName());
            cfg.configure(info, Collections.emptyMap());
            EntityManagerFactory emf = cfg.buildEntityManagerFactory();
            emfs.put(info.getPersistenceUnitName(), emf);
        }
        return emfs;
    }


}
