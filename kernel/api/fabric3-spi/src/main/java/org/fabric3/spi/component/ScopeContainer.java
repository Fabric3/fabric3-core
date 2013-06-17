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
 * ----------------------------------------------------
 *
 * Portions originally based on Apache Tuscany 2007
 * licensed under the Apache 2.0 license.
 *
 */
package org.fabric3.spi.component;

import javax.xml.namespace.QName;
import java.util.List;

import org.fabric3.model.type.component.Scope;

/**
 * Manages the lifecycle and visibility component implementations instances.
 */
public interface ScopeContainer {

    /**
     * Returns the Scope that this container supports.
     *
     * @return the Scope that this container supports
     */
    Scope getScope();

    /**
     * Registers a component with the scope.
     *
     * @param component the component to register
     */
    void register(ScopedComponent component);

    /**
     * Unregisters a component with the scope.
     *
     * @param component the component to unregister
     */
    void unregister(ScopedComponent component);

    /**
     * Start a new, non-expiring context. The context will remain active until explicitly stopped.
     *
     * @param deployable the deployable to start the context for
     * @throws ComponentException if an exception starting the context was encountered
     */
    void startContext(QName deployable) throws ComponentException;

    /**
     * Stop the context associated with the current work context.
     *
     * @param deployable the deployable to start the context for
     * @throws ComponentException if there is an error stopping the context
     */
    void stopContext(QName deployable) throws ComponentException;

    /**
     * Returns an instance associated with the current scope context, creating one if necessary
     *
     * @param component the component
     * @return the instance
     * @throws InstanceLifecycleException if there was a problem instantiating the target instance
     */
    Object getInstance(ScopedComponent component) throws InstanceLifecycleException;

    /**
     * Return am instance after use (for example, after invoking the instance).
     *
     * @param component the component
     * @param instance  the instance
     * @throws InstanceDestructionException if there was a problem returning the target instance
     */
    void releaseInstance(ScopedComponent component, Object instance) throws InstanceDestructionException;

    /**
     * Returns a snapshot of the component instances that are active and currently managed by the scope container.
     *
     * @param component the component
     * @return the active instances
     */
    List<Object> getActiveInstances(ScopedComponent component);

    /**
     * Reinjects all live instances with updated wires
     *
     * @throws InstanceLifecycleException if an error occurs during reinjection
     */
    void reinject() throws InstanceLifecycleException;

}
