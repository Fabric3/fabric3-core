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
 *
 * ----------------------------------------------------
 *
 * Portions originally based on Apache Tuscany 2007
 * licensed under the Apache 2.0 license.
 *
 */
package org.fabric3.implementation.pojo.instancefactory;

import org.fabric3.spi.component.InstanceDestructionException;
import org.fabric3.spi.component.InstanceInitException;
import org.fabric3.spi.component.InstanceLifecycleException;
import org.fabric3.spi.invocation.WorkContext;
import org.fabric3.spi.objectfactory.ObjectCreationException;

/**
 * Returns an injected component instance. This is used by a Component implementation to create new instances of application implementation objects as
 * determined by the component scope's lifecycle.
 *
 * @version $Rev$ $Date$
 */
public interface ImplementationManager {
    /**
     * Creates a new instance of the component. All injected values must be set but any @Init methods must not have been invoked.
     *
     * @param workContext the work context in which to create the instance
     * @return A new component instance
     * @throws ObjectCreationException if there was a problem creating the instance
     */
    Object newInstance(WorkContext workContext) throws ObjectCreationException;

    /**
     * Starts the instance, calling an @Init method if one is configured.
     *
     * @param instance the instance
     * @param context  the work context
     * @throws InstanceInitException if there is an error when calling the initialization method
     */
    void start(Object instance, WorkContext context) throws InstanceInitException;

    /**
     * Stops the instance, calling an @Destroy method if one is configured.
     *
     * @param instance the instance
     * @param context  the work context
     * @throws InstanceDestructionException if there is an error when calling the initialization method
     */
    void stop(Object instance, WorkContext context) throws InstanceDestructionException;

    /**
     * Reinjects the instance with any updated references.
     *
     * @param instance the instance
     * @throws InstanceLifecycleException if an error is raised during reinjection
     */
    void reinject(Object instance) throws InstanceLifecycleException;

    /**
     * Updates the instance with a new reference proxy.
     *
     * @param instance      the instance
     * @param referenceName the reference name
     */
    void updated(Object instance, String referenceName);

    /**
     * Updates the instance when a reference has been removed.
     *
     * @param instance      the instance
     * @param referenceName the reference name
     */
    void removed(Object instance, String referenceName);

}
