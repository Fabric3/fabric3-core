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
import javax.persistence.spi.PersistenceUnitInfo;
import java.net.URI;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

import org.fabric3.api.host.Fabric3Exception;
import org.fabric3.api.host.Names;
import org.fabric3.jpa.api.EntityManagerFactoryResolver;
import org.fabric3.jpa.api.F3TransactionManagerLookup;
import org.fabric3.jpa.api.PersistenceOverrides;
import org.fabric3.spi.classloader.MultiParentClassLoader;
import org.hibernate.jpa.boot.internal.EntityManagerFactoryBuilderImpl;
import org.hibernate.jpa.boot.internal.PersistenceUnitInfoDescriptor;
import org.hibernate.jpa.boot.spi.EntityManagerFactoryBuilder;
import org.oasisopen.sca.annotation.Reference;

/**
 * An {@link EntityManagerFactoryResolver} implementation that caches EntityManagerFactory instances.
 */
public class CachingEntityManagerFactoryResolver implements EntityManagerFactoryResolver {
    private static final String HIBERNATE_LOOKUP = "hibernate.transaction.jta.platform";

    private PersistenceContextParser parser;
    private EntityManagerFactoryCache cache;

    public CachingEntityManagerFactoryResolver(@Reference PersistenceContextParser parser, @Reference EntityManagerFactoryCache cache) {
        this.parser = parser;
        this.cache = cache;
    }

    public synchronized EntityManagerFactory resolve(String unitName, PersistenceOverrides overrides, ClassLoader classLoader) throws Fabric3Exception {
        EntityManagerFactory resolvedEmf = cache.get(unitName);
        if (resolvedEmf != null) {
            return resolvedEmf;
        }

        EntityManagerFactory factory = createEntityManagerFactory(overrides, classLoader);
        URI key;
        if (classLoader instanceof MultiParentClassLoader) {
            key = ((MultiParentClassLoader) classLoader).getName();
        } else {
            key = Names.HOST_CONTRIBUTION;
        }
        cache.put(key, unitName, factory);
        return factory;
    }

    /**
     * Creates an EntityManagerFactory for a persistence unit specified by the property overrides. The EMF is created by parsing all persistence.xml files on
     * the classpath.
     *
     * @param overrides   persistence unit property overrides
     * @param classLoader the persistence unit classloader
     * @return the entity manager factory
     * @throws Fabric3Exception if there is an error creating the factory
     */
    private EntityManagerFactory createEntityManagerFactory(PersistenceOverrides overrides, ClassLoader classLoader) throws Fabric3Exception {
        List<PersistenceUnitInfo> infos = parser.parse(classLoader);
        String unitName = overrides.getUnitName();
        for (PersistenceUnitInfo info : infos) {
            if (!unitName.equals(info.getPersistenceUnitName())) {
                // Not the most efficient approach: parse all of the persistence units and only keep the one we are requested in, potentially
                // resulting in parsing the units multiple times.
                // This must be done since the overrides may not be loaded for all units
                continue;
            }
            Properties unitProperties = info.getProperties();
            unitProperties.setProperty(HIBERNATE_LOOKUP, F3TransactionManagerLookup.class.getName());
            unitProperties.putAll(overrides.getProperties());

            PersistenceUnitInfoDescriptor descriptor = new PersistenceUnitInfoDescriptor(info);
            EntityManagerFactoryBuilder builder = new EntityManagerFactoryBuilderImpl(descriptor, Collections.emptyMap(), classLoader);
            return builder.build();
        }
        throw new Fabric3Exception("Persistence unit not defined for: " + unitName);
    }

}
