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

import java.util.List;

import org.osoa.sca.ConversationEndedException;

import org.fabric3.model.type.component.Scope;
import org.fabric3.spi.Lifecycle;
import org.fabric3.spi.ObjectFactory;
import org.fabric3.spi.invocation.WorkContext;
import org.fabric3.spi.invocation.F3Conversation;


/**
 * Manages the lifecycle and visibility of instances associated with an {@link AtomicComponent}.
 *
 * @version $Rev$ $Date$
 */
public interface ScopeContainer extends Lifecycle {

    /**
     * Returns the Scope that this container supports.
     *
     * @return the Scope that this container supports
     */
    Scope<?> getScope();

    /**
     * Registers a component with the scope.
     *
     * @param component the component to register
     */
    void register(AtomicComponent<?> component);

    /**
     * Unregisters a component with the scope.
     *
     * @param component the component to unregister
     */
    void unregister(AtomicComponent<?> component);

    /**
     * Registers a callback object to receive notification when a conversation has expired.
     *
     * @param conversation the conversation to listen to
     * @param callback     the callback instance that receives notifications
     */
    void registerCallback(F3Conversation conversation, ConversationExpirationCallback callback);

    /**
     * Start a new, non-expiring context. The context will remain active until explicitly stopped.
     *
     * @param workContext the current WorkContext
     * @throws GroupInitializationException if an exception was thrown by any eagerInit component
     */
    void startContext(WorkContext workContext) throws GroupInitializationException;

    /**
     * Start a new context which expires according to the given ExpirationPolicy. The context will remain active until it is explicitly stopped or it
     * expires.
     *
     * @param workContext the current WorkContext
     * @param policy      determines when the context expires
     * @throws GroupInitializationException if an exception was thrown by any eagerInit component
     */
    public void startContext(WorkContext workContext, ExpirationPolicy policy) throws GroupInitializationException;

    /**
     * Joins an existing context. Since a scope context may exist accross multiple JVMs (for example, when conversational context is propagated), this
     * operation may result in the creation of a local context associated with the distributed scope context. When the scope context is contained in a
     * single JVM, a new context will not need to be created.
     *
     * @param workContext the current WorkContext
     * @throws GroupInitializationException if an exception was thrown by any eagerInit component
     */
    void joinContext(WorkContext workContext) throws GroupInitializationException;

    /**
     * Joins an existing context. Since a scope context may exist accross multiple JVMs (for example, when conversational context is propagated), this
     * operation may result in the creation of a local context associated with the distributed scope context. This variant of joinContext sets an
     * expiration policy for local contexts, if one needs to be created.
     *
     * @param workContext the current WorkContext
     * @param policy      determines when the local context expires
     * @throws GroupInitializationException if an exception was thrown by any eagerInit component
     */
    void joinContext(WorkContext workContext, ExpirationPolicy policy) throws GroupInitializationException;

    /**
     * Stop the context associated with the current work context.
     *
     * @param workContext the current WorkContext
     */
    void stopContext(WorkContext workContext);

    /**
     * Stops all active contexts.
     *
     * @param workContext the current work context
     */
    void stopAllContexts(WorkContext workContext);

    /**
     * Initialise an ordered list of components. The list is traversed in order and the getWrapper() method called for each to associate an instance
     * with the supplied context.
     *
     * @param components  the components to be initialized
     * @param workContext the work context in which to initialize the components
     * @throws GroupInitializationException if one or more components threw an exception during initialization
     */
    void initializeComponents(List<AtomicComponent<?>> components, WorkContext workContext) throws GroupInitializationException;

    /**
     * Returns an instance wrapper associated with the current scope context, creating one if necessary
     *
     * @param component   the component
     * @param workContext the work context in which the instance should be obtained
     * @return the wrapper for the target instance
     * @throws InstanceLifecycleException if there was a problem instantiating the target instance
     * @throws ConversationEndedException if the instance is conversational and the associated has ended or expired
     */
    <T> InstanceWrapper<T> getWrapper(AtomicComponent<T> component, WorkContext workContext)
            throws InstanceLifecycleException, ConversationEndedException;

    /**
     * Return a wrapper after use (for example, after invoking the instance).
     *
     * @param component   the component
     * @param workContext the work context returning the instance
     * @param wrapper     the wrapper for the target instance being returned
     * @throws InstanceDestructionException if there was a problem returning the target instance
     */
    <T> void returnWrapper(AtomicComponent<T> component, WorkContext workContext, InstanceWrapper<T> wrapper) throws InstanceDestructionException;

    /**
     * Adds an object factory to references of active instances for a component.
     *
     * @param component     Component with active instances, whose references need to be updated.
     * @param factory       Object factory for the reference.
     * @param referenceName Name of the reference.
     * @param key           the component key
     */
    void addObjectFactory(AtomicComponent<?> component, ObjectFactory<?> factory, String referenceName, Object key);

    /**
     * Removes an object factory from references of active instances for a component.
     *
     * @param component     Component with active instances, whose references need to be updated.
     * @param referenceName Name of the reference.
     */
    void removeObjectFactory(AtomicComponent<?> component, String referenceName);

    /**
     * Re-injects all live instances with updated wires.
     *
     * @throws InstanceLifecycleException if an error occurs during reinjection.
     */
    void reinject() throws InstanceLifecycleException;
}
