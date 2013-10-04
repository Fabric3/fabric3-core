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
 *
 */
package org.fabric3.spi.container.component;

import org.fabric3.spi.container.objectfactory.ObjectCreationException;

/**
 * A component whose implementation instances are managed by a {@link ScopeContainer}. This interface defines callbacks used by the scope container to change
 * the state of an implementation.
 */
public interface ScopedComponent extends AtomicComponent {

    /**
     * Returns true if component instances should be eagerly initialized.
     *
     * @return true if component instances should be eagerly initialized
     */
    boolean isEagerInit();

    /**
     * Create a new implementation instance, fully injected with all property and reference values. The instance's lifecycle callbacks must not have been
     * called.
     *
     * @return a wrapper for a new implementation instance
     * @throws ObjectCreationException if there was a problem instantiating the implementation
     */
    Object createInstance() throws ObjectCreationException;

    /**
     * Starts a component instance. If configured on the implementation, an initialization callback will be performed.
     *
     * @param instance the instance to start
     * @throws InstanceInitException if there is an error initializing the instance
     */
    void startInstance(Object instance) throws InstanceInitException;

    /**
     * Stops a component instance. If configured on the implementation, a destruction callback will be performed.
     *
     * @param instance    the instance to start
     * @throws InstanceDestructionException if there is an error stopping the instance
     */
    void stopInstance(Object instance) throws InstanceDestructionException;

    /**
     * Reinjects the instance with updated references.
     *
     * @param instance the instance
     * @throws InstanceLifecycleException if there is an error reinjecting the instance
     */
    void reinject(Object instance) throws InstanceLifecycleException;

}
