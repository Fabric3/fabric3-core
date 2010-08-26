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
package org.fabric3.jpa.runtime.emf;

import java.net.URI;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import javax.persistence.EntityManagerFactory;

import org.hibernate.ejb.HibernateEntityManagerFactory;
import org.hibernate.jmx.StatisticsService;
import org.osoa.sca.annotations.Destroy;
import org.osoa.sca.annotations.Reference;
import org.osoa.sca.annotations.Service;

import org.fabric3.api.annotation.monitor.Monitor;
import org.fabric3.host.Names;
import org.fabric3.jpa.api.JpaResolutionException;
import org.fabric3.spi.builder.classloader.ClassLoaderListener;
import org.fabric3.spi.classloader.MultiParentClassLoader;
import org.fabric3.spi.management.ManagementException;
import org.fabric3.spi.management.ManagementService;

/**
 * Creates and caches entity manager factories.
 *
 * @version $Rev: 8837 $ $Date: 2010-04-08 14:05:46 +0200 (Thu, 08 Apr 2010) $
 */
@Service(interfaces = {EntityManagerFactoryCache.class, ClassLoaderListener.class})
public class DefaultEntityManagerFactoryCache implements EntityManagerFactoryCache, ClassLoaderListener {
    private CacheMonitor monitor;

    public DefaultEntityManagerFactoryCache(@Monitor CacheMonitor monitor) {
        this.monitor = monitor;
    }

    private ManagementService managementService;

    private Map<String, EntityManagerFactory> cache = new HashMap<String, EntityManagerFactory>();
    private Map<URI, Set<String>> contributionCache = new HashMap<URI, Set<String>>();

    @Reference(required = false)
    public void setManagementService(ManagementService managementService) {
        this.managementService = managementService;
    }

    @Destroy
    public void destroy() {
        // the runtime is being shutdown, close any open factories
        for (EntityManagerFactory factory : cache.values()) {
            if (factory != null) {
                factory.close();
            }
        }
    }

    public void onDeploy(ClassLoader loader) {
        // no-op
    }

    public void onUndeploy(ClassLoader classLoader) {
        URI key;
        if (classLoader instanceof MultiParentClassLoader) {
            key = ((MultiParentClassLoader) classLoader).getName();
        } else {
            key = Names.BOOT_CONTRIBUTION;
        }
        Set<String> names = contributionCache.remove(key);
        if (names != null) {
            for (String name : names) {
                EntityManagerFactory factory = cache.remove(name);
                factory.close();
                remove(name);
            }
        }
    }

    public EntityManagerFactory get(String unitName) {
        return cache.get(unitName);
    }

    public void put(URI uri, String unitName, EntityManagerFactory factory) throws JpaResolutionException {
        cache.put(unitName, factory);
        Set<String> names = contributionCache.get(uri);
        if (names == null) {
            names = new HashSet<String>();
            contributionCache.put(uri, names);
        }
        names.add(unitName);
        export(unitName, factory);
    }

    private void export(String unitName, EntityManagerFactory factory) throws JpaResolutionException {
        if (managementService == null) {
            // management not enabled
            return;
        }
        StatisticsService statistics = new StatisticsService();
        // TODO make configurable
        if (!(factory instanceof HibernateEntityManagerFactory)) {
            throw new AssertionError("Expected " + HibernateEntityManagerFactory.class.getName() + " but was " + factory.getClass().getName());
        }
        statistics.setSessionFactory(((HibernateEntityManagerFactory) factory).getSessionFactory());
        statistics.setStatisticsEnabled(true);
        try {
            managementService.export(unitName, "Hibernate", "Hibernate session factory MBeans", statistics);
        } catch (ManagementException e) {
            throw new JpaResolutionException("Error exporting management bean for persistence unit: " + unitName, e);
        }
    }

    private void remove(String unitName) {
        if (managementService == null) {
            // management not enabled
            return;
        }
        try {
            managementService.remove(unitName, "Hibernate");
        } catch (ManagementException e) {
            monitor.error(unitName, e);
        }
    }

}