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
package org.fabric3.jpa.runtime;

import java.net.URI;
import javax.transaction.TransactionManager;

import org.osoa.sca.annotations.Reference;

import org.fabric3.jpa.provision.PersistenceContextTargetDefinition;
import org.fabric3.jpa.spi.EmfBuilderException;
import org.fabric3.jpa.spi.classloading.EmfClassLoaderService;
import org.fabric3.spi.ObjectFactory;
import org.fabric3.spi.builder.WiringException;
import org.fabric3.spi.builder.component.TargetWireAttacher;
import org.fabric3.spi.model.physical.PhysicalSourceDefinition;
import org.fabric3.spi.wire.Wire;

/**
 * Attaches the target side of entity manager factories.
 *
 * @version $Rev$ $Date$
 */
public class PersistenceContextWireAttacher implements TargetWireAttacher<PersistenceContextTargetDefinition> {
    private EmfBuilder emfBuilder;
    private EmfClassLoaderService classLoaderService;
    private TransactionManager tm;
    private EntityManagerService emService;

    /**
     * Constructor.
     *
     * @param emService          the service for creating EntityManagers
     * @param tm                 the transaction manager
     * @param emfBuilder         the EMF builder
     * @param classLoaderService the classloader service for returning EMF classloaders
     */
    public PersistenceContextWireAttacher(@Reference EntityManagerService emService,
                                          @Reference TransactionManager tm,
                                          @Reference EmfBuilder emfBuilder,
                                          @Reference EmfClassLoaderService classLoaderService) {
        this.emfBuilder = emfBuilder;
        this.classLoaderService = classLoaderService;
        this.emService = emService;
        this.tm = tm;
    }

    public ObjectFactory<?> createObjectFactory(PersistenceContextTargetDefinition definition) throws WiringException {
        String unitName = definition.getUnitName();
        boolean extended = definition.isExtended();
        URI classLoaderId = definition.getClassLoaderId();
        ClassLoader oldCl = Thread.currentThread().getContextClassLoader();
        try {
            // get the classloader for the entity manager factory
            ClassLoader appCl = classLoaderService.getEmfClassLoader(classLoaderId);
            Thread.currentThread().setContextClassLoader(appCl);
            // eagerly build the the EntityManagerFactory
            emfBuilder.build(unitName, appCl);
            if (definition.isMultiThreaded()) {
                return new MultiThreadedEntityManagerProxyFactory(unitName, extended, emService, tm);
            } else {
                return new StatefulEntityManagerProxyFactory(unitName, extended, emService, tm);
            }
        } catch (EmfBuilderException e) {
            throw new WiringException(e);
        } finally {
            Thread.currentThread().setContextClassLoader(oldCl);
        }
    }

    public void attach(PhysicalSourceDefinition source, PersistenceContextTargetDefinition target, Wire wire) throws WiringException {
        throw new UnsupportedOperationException();
    }

    public void detach(PhysicalSourceDefinition source, PersistenceContextTargetDefinition target) throws WiringException {
        throw new UnsupportedOperationException();
    }

}