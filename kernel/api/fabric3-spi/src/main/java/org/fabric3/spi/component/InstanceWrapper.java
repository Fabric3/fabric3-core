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
 *
 * ----------------------------------------------------
 *
 * Portions originally based on Apache Tuscany 2007
 * licensed under the Apache 2.0 license.
 *
 */
package org.fabric3.spi.component;

import org.fabric3.spi.ObjectFactory;
import org.fabric3.spi.invocation.WorkContext;


/**
 * Provides lifecycle management for an implementation instance associated with an {@link org.fabric3.spi.component.AtomicComponent} for use by the
 * atomic component's associated {@link org.fabric3.spi.component.ScopeContainer}
 *
 * @version $Rev$ $Date$
 */
public interface InstanceWrapper<T> {

    /**
     * Returns the instance managed by this wrapper.
     *
     * @return the instance managed by this wrapper.
     */
    T getInstance();

    /**
     * Returns true if the instance is started.
     *
     * @return true if the instance is started.
     */
    boolean isStarted();

    /**
     * Starts the instance,issuing an initialization callback if the instance is configured to receive one.
     *
     * @param context the current work context
     * @throws InstanceInitializationException
     *          if an error occured starting the instance
     */
    void start(WorkContext context) throws InstanceInitializationException;

    /**
     * Stops the instance, issuing a destruction callback if the instance is configured to receive one..
     *
     * @param context the current work context
     * @throws InstanceDestructionException if an error stopping the instance occurs
     */
    void stop(WorkContext context) throws InstanceDestructionException;

    /**
     * Reinjects updated references on an instance.
     *
     * @throws InstanceLifecycleException if an error occurs during reinjection
     */
    void reinject() throws InstanceLifecycleException;

    /**
     * Adds an object factory for the given reference.
     *
     * @param referenceName the reference
     * @param factory       the object factory
     * @param key           the key associated with the object factory
     */
    void addObjectFactory(String referenceName, ObjectFactory<?> factory, Object key);

    /**
     * Removes an object factory for the given reference. Used to clear multiplicity references during reinjection.
     *
     * @param referenceName the reference
     */
    void removeObjectFactory(String referenceName);
}
