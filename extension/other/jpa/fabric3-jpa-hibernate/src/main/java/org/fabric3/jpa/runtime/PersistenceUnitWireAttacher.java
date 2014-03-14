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
import javax.persistence.EntityManagerFactory;

import org.fabric3.jpa.provision.PersistenceUnitWireTargetDefinition;
import org.oasisopen.sca.annotation.Reference;

import org.fabric3.jpa.api.EntityManagerFactoryResolver;
import org.fabric3.jpa.api.JpaResolutionException;
import org.fabric3.jpa.api.PersistenceOverrides;
import org.fabric3.spi.container.builder.WiringException;
import org.fabric3.spi.container.builder.component.TargetWireAttacher;
import org.fabric3.spi.classloader.ClassLoaderRegistry;
import org.fabric3.spi.model.physical.PhysicalWireSourceDefinition;
import org.fabric3.spi.container.objectfactory.ObjectFactory;
import org.fabric3.spi.container.objectfactory.SingletonObjectFactory;
import org.fabric3.spi.container.wire.Wire;

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

    public void attach(PhysicalWireSourceDefinition source, PersistenceUnitWireTargetDefinition target, Wire wire) throws WiringException {
        throw new AssertionError();
    }

    public void detach(PhysicalWireSourceDefinition source, PersistenceUnitWireTargetDefinition target) throws WiringException {
        throw new AssertionError();
    }

    public ObjectFactory<?> createObjectFactory(PersistenceUnitWireTargetDefinition target) throws WiringException {
        String unitName = target.getUnitName();
        URI classLoaderUri = target.getClassLoaderId();
        ClassLoader classLoader = registry.getClassLoader(classLoaderUri);
        ClassLoader oldCl = Thread.currentThread().getContextClassLoader();

        try {
            Thread.currentThread().setContextClassLoader(classLoader);
            PersistenceOverrides overrides = target.getOverrides();
            EntityManagerFactory entityManagerFactory = emfResolver.resolve(unitName, overrides, classLoader);
            return new SingletonObjectFactory<>(entityManagerFactory);
        } catch (JpaResolutionException e) {
            throw new WiringException(e);
        } finally {
            Thread.currentThread().setContextClassLoader(oldCl);
        }

    }

}
