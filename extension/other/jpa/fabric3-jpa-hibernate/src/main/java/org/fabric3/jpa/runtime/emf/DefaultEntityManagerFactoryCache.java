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
 * Portions originally based on Apache Tuscany 2007
 * licensed under the Apache 2.0 license.
 */
package org.fabric3.jpa.runtime.emf;

import javax.persistence.EntityManagerFactory;
import java.net.URI;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.fabric3.api.annotation.monitor.Monitor;
import org.fabric3.api.host.ContainerException;
import org.fabric3.api.host.Names;
import org.fabric3.spi.classloader.ClassLoaderRegistry;
import org.fabric3.spi.classloader.MultiParentClassLoader;
import org.fabric3.spi.contribution.Contribution;
import org.fabric3.spi.contribution.ContributionServiceListener;
import org.fabric3.spi.management.ManagementService;
import org.hibernate.jpa.HibernateEntityManagerFactory;
import org.hibernate.stat.Statistics;
import org.oasisopen.sca.annotation.Destroy;
import org.oasisopen.sca.annotation.Reference;
import org.oasisopen.sca.annotation.Service;

/**
 * Creates and caches entity manager factories.
 */
@Service({EntityManagerFactoryCache.class, ContributionServiceListener.class})
public class DefaultEntityManagerFactoryCache implements EntityManagerFactoryCache, ContributionServiceListener {
    private ClassLoaderRegistry classLoaderRegistry;
    private CacheMonitor monitor;

    public DefaultEntityManagerFactoryCache(@Reference ClassLoaderRegistry classLoaderRegistry, @Monitor CacheMonitor monitor) {
        this.classLoaderRegistry = classLoaderRegistry;
        this.monitor = monitor;
    }

    private ManagementService managementService;

    private Map<String, EntityManagerFactory> cache = new HashMap<>();
    private Map<URI, Set<String>> contributionCache = new HashMap<>();

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

    public void onUninstall(Contribution contribution) {
        URI key;
        URI uri = contribution.getUri();
        ClassLoader classLoader = classLoaderRegistry.getClassLoader(uri);
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

    public void put(URI uri, String unitName, EntityManagerFactory factory) throws ContainerException {
        if (factory == null) {
            throw new IllegalArgumentException("EntityManagerFactory was null");
        }
        cache.put(unitName, factory);
        Set<String> names = contributionCache.get(uri);
        if (names == null) {
            names = new HashSet<>();
            contributionCache.put(uri, names);
        }
        names.add(unitName);
        export(unitName, factory);
    }

    private void export(String unitName, EntityManagerFactory factory) throws ContainerException {
        if (managementService == null) {
            // management not enabled
            return;
        }
        if (!(factory instanceof HibernateEntityManagerFactory)) {
            throw new AssertionError("Expected " + HibernateEntityManagerFactory.class.getName() + " but was " + factory.getClass().getName());
        }
        Statistics statistics = ((HibernateEntityManagerFactory) factory).getSessionFactory().getStatistics();
        statistics.setStatisticsEnabled(true);
        managementService.export(encodeName(unitName), "Hibernate", "Hibernate session factory MBeans", statistics);
    }

    private void remove(String unitName) {
        if (managementService == null) {
            // management not enabled
            return;
        }
        try {
            managementService.remove(encodeName(unitName), "Hibernate");
        } catch (ContainerException e) {
            monitor.error(unitName, e);
        }
    }

    private String encodeName(String name) {
        return "hibernate/sessions/" + name;
    }

    public void onStore(Contribution contribution) {

    }

    public void onProcessManifest(Contribution contribution) {

    }

    public void onInstall(Contribution contribution) {

    }

    public void onUpdate(Contribution contribution) {

    }

    public void onRemove(Contribution contribution) {

    }
}