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
package org.fabric3.jpa.runtime;

import javax.persistence.EntityManagerFactory;
import java.net.URI;
import java.util.function.Supplier;

import org.fabric3.api.host.Fabric3Exception;
import org.fabric3.jpa.api.EntityManagerFactoryResolver;
import org.fabric3.jpa.api.PersistenceOverrides;
import org.fabric3.jpa.provision.PersistenceUnitWireTargetDefinition;
import org.fabric3.spi.classloader.ClassLoaderRegistry;
import org.fabric3.spi.container.builder.component.TargetWireAttacher;
import org.fabric3.spi.container.wire.Wire;
import org.fabric3.spi.model.physical.PhysicalWireSourceDefinition;
import org.oasisopen.sca.annotation.Reference;

/**
 * Attaches the target side of entity manager factories.
 */
public class PersistenceUnitWireAttacher implements TargetWireAttacher<PersistenceUnitWireTargetDefinition> {
    private EntityManagerFactoryResolver emfResolver;
    private ClassLoaderRegistry registry;

    /**
     * Constructor.
     *
     * @param emfResolver EntityManagerFactory builder.
     * @param registry    the classloader registry
     */
    public PersistenceUnitWireAttacher(@Reference EntityManagerFactoryResolver emfResolver, @Reference ClassLoaderRegistry registry) {
        this.emfResolver = emfResolver;
        this.registry = registry;
    }

    public void attach(PhysicalWireSourceDefinition source, PersistenceUnitWireTargetDefinition target, Wire wire) throws Fabric3Exception {
        throw new AssertionError();
    }

    public void detach(PhysicalWireSourceDefinition source, PersistenceUnitWireTargetDefinition target) throws Fabric3Exception {
        throw new AssertionError();
    }

    public Supplier<?> createSupplier(PersistenceUnitWireTargetDefinition target) throws Fabric3Exception {
        String unitName = target.getUnitName();
        URI classLoaderUri = target.getClassLoaderId();
        ClassLoader classLoader = registry.getClassLoader(classLoaderUri);
        ClassLoader oldCl = Thread.currentThread().getContextClassLoader();

        try {
            Thread.currentThread().setContextClassLoader(classLoader);
            PersistenceOverrides overrides = target.getOverrides();
            EntityManagerFactory entityManagerFactory = emfResolver.resolve(unitName, overrides, classLoader);
            return () -> entityManagerFactory;
        } finally {
            Thread.currentThread().setContextClassLoader(oldCl);
        }

    }

}
