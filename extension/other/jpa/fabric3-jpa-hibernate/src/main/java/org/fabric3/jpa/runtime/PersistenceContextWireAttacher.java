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
package org.fabric3.jpa.runtime;

import java.net.URI;
import javax.transaction.TransactionManager;

import org.oasisopen.sca.annotation.Reference;

import org.fabric3.jpa.api.EntityManagerFactoryResolver;
import org.fabric3.jpa.api.JpaResolutionException;
import org.fabric3.jpa.api.PersistenceOverrides;
import org.fabric3.jpa.provision.PersistenceContextWireTargetDefinition;
import org.fabric3.jpa.runtime.proxy.EntityManagerService;
import org.fabric3.jpa.runtime.proxy.MultiThreadedEntityManagerProxyFactory;
import org.fabric3.jpa.runtime.proxy.StatefulEntityManagerProxyFactory;
import org.fabric3.spi.container.builder.BuilderException;
import org.fabric3.spi.container.builder.component.TargetWireAttacher;
import org.fabric3.spi.classloader.ClassLoaderRegistry;
import org.fabric3.spi.model.physical.PhysicalWireSourceDefinition;
import org.fabric3.spi.container.objectfactory.ObjectFactory;
import org.fabric3.spi.container.wire.Wire;

/**
 * Attaches the target side of entity manager factories.
 */
public class PersistenceContextWireAttacher implements TargetWireAttacher<PersistenceContextWireTargetDefinition> {
    private EntityManagerFactoryResolver emfResolver;
    private ClassLoaderRegistry registry;
    private TransactionManager tm;
    private EntityManagerService emService;

    /**
     * Constructor.
     *
     * @param emService   the service for creating EntityManagers
     * @param tm          the transaction manager
     * @param emfResolver the EMF builder
     * @param registry    the classloader registry
     */
    public PersistenceContextWireAttacher(@Reference EntityManagerService emService,
                                          @Reference TransactionManager tm,
                                          @Reference EntityManagerFactoryResolver emfResolver,
                                          @Reference ClassLoaderRegistry registry) {
        this.emfResolver = emfResolver;
        this.registry = registry;
        this.emService = emService;
        this.tm = tm;
    }

    public ObjectFactory<?> createObjectFactory(PersistenceContextWireTargetDefinition definition) throws BuilderException {
        String unitName = definition.getUnitName();
        URI classLoaderId = definition.getClassLoaderId();
        ClassLoader oldCl = Thread.currentThread().getContextClassLoader();
        try {
            // get the classloader for the entity manager factory
            ClassLoader classLoader = registry.getClassLoader(classLoaderId);
            Thread.currentThread().setContextClassLoader(classLoader);
            // eagerly build the the EntityManagerFactory
            PersistenceOverrides overrides = definition.getOverrides();
            emfResolver.resolve(unitName, overrides, classLoader);
            if (definition.isMultiThreaded()) {
                return new MultiThreadedEntityManagerProxyFactory(unitName, emService, tm);
            } else {
                return new StatefulEntityManagerProxyFactory(unitName, emService, tm);
            }
        } catch (JpaResolutionException e) {
            throw new BuilderException(e);
        } finally {
            Thread.currentThread().setContextClassLoader(oldCl);
        }
    }

    public void attach(PhysicalWireSourceDefinition source, PersistenceContextWireTargetDefinition target, Wire wire) throws BuilderException {
        throw new UnsupportedOperationException();
    }

    public void detach(PhysicalWireSourceDefinition source, PersistenceContextWireTargetDefinition target) throws BuilderException {
        throw new UnsupportedOperationException();
    }

}