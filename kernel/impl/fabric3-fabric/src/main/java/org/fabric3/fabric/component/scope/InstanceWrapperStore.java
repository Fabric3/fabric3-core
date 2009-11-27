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
package org.fabric3.fabric.component.scope;

import org.fabric3.spi.component.AtomicComponent;
import org.fabric3.spi.component.InstanceWrapper;

/**
 * Interface implemented by services that are able to store InstanceWrappers between invocations. Instances are grouped together into collections
 * identified by the context id. Each collection may contain instances from several components.
 *
 * @version $Rev$ $Date$
 * @param <KEY> the type of key this store uses to identify contexts
 */
public interface InstanceWrapperStore<KEY> {
    /**
     * Notification to the store that a scope context is being started. This must be called before any instances are associated with the context
     *
     * @param contextId the id of the context
     * @throws StoreException if there was a problem initializing the context
     */
    void startContext(KEY contextId) throws StoreException;

    /**
     * Notification to the store that a scope context is ending.
     *
     * @param contextId the id of the context
     * @throws StoreException if there was a problem shutting the context down
     */
    void stopContext(KEY contextId) throws StoreException;

    /**
     * Get the instance of the supplied component that is associated with the supplied context. Returns null if there is no instance currently
     * associated.
     *
     * @param component the component whose instance should be returned
     * @param contextId the context whose instance should be returned
     * @return the wrapped instance associated with the context or null
     * @throws StoreException if there was problem returning the instance
     */
    <T> InstanceWrapper<T> getWrapper(AtomicComponent<T> component, KEY contextId) throws StoreException;

    /**
     * Associated an instance of the supplied component with the supplied context.
     *
     * @param component the component whose instance is being stored
     * @param contextId the context with which the instance is associated
     * @param wrapper   the wrapped instance
     * @throws StoreException if there was a problem storing the instance
     */
    <T> void putWrapper(AtomicComponent<T> component, KEY contextId, InstanceWrapper<T> wrapper) throws StoreException;
}
