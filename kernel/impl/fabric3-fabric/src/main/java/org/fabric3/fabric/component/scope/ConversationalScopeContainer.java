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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.osoa.sca.ConversationEndedException;
import org.osoa.sca.annotations.Destroy;
import org.osoa.sca.annotations.EagerInit;
import org.osoa.sca.annotations.Init;
import org.osoa.sca.annotations.Property;
import org.osoa.sca.annotations.Reference;
import org.osoa.sca.annotations.Service;

import org.fabric3.api.annotation.monitor.Monitor;
import org.fabric3.model.type.component.Scope;
import org.fabric3.spi.component.AtomicComponent;
import org.fabric3.spi.component.ComponentException;
import org.fabric3.spi.component.ConversationExpirationCallback;
import org.fabric3.spi.component.ExpirationPolicy;
import org.fabric3.spi.component.GroupInitializationException;
import org.fabric3.spi.component.InstanceInitializationException;
import org.fabric3.spi.component.InstanceLifecycleException;
import org.fabric3.spi.component.InstanceWrapper;
import org.fabric3.spi.component.ScopeContainer;
import org.fabric3.spi.invocation.CallFrame;
import org.fabric3.spi.invocation.ConversationContext;
import org.fabric3.spi.invocation.F3Conversation;
import org.fabric3.spi.invocation.WorkContext;
import org.fabric3.spi.objectfactory.ObjectCreationException;

/**
 * Scope container for the standard CONVERSATIONAL scope.
 *
 * @version $Rev$ $Date$
 */
@Service(ScopeContainer.class)
@EagerInit
public class ConversationalScopeContainer extends AbstractScopeContainer {
    private final Map<F3Conversation, ExpirationPolicy> expirationPolicies;
    private final Map<F3Conversation, List<ConversationExpirationCallback>> expirationCallbacks;
    private final InstanceWrapperStore<F3Conversation> store;
    private ScheduledExecutorService executor;
    // TODO this should be part of the system configuration
    private long delay = 600;  // reap every 600 seconds

    // the queue of instanceWrappers to destroy, in the order that their instances were created
    private final Map<F3Conversation, List<InstanceWrapper>> destroyQueues = new ConcurrentHashMap<F3Conversation, List<InstanceWrapper>>();


    public ConversationalScopeContainer(@Monitor ScopeContainerMonitor monitor,
                                        @Reference(name = "store") InstanceWrapperStore<F3Conversation> store) {
        super(Scope.CONVERSATION, monitor);
        this.store = store;
        expirationPolicies = new ConcurrentHashMap<F3Conversation, ExpirationPolicy>();
        expirationCallbacks = new ConcurrentHashMap<F3Conversation, List<ConversationExpirationCallback>>();
    }

    /**
     * Optional property to set the delay for executing the reaper to clear expired conversation contexts
     *
     * @param delay the delay in seconds
     */
    @Property
    public void setDelay(long delay) {
        this.delay = delay;
    }

    @Init
    public void start() {
        super.start();
        executor = Executors.newSingleThreadScheduledExecutor();
        Runnable reaper = new Reaper();
        executor.scheduleWithFixedDelay(reaper, delay, delay, TimeUnit.SECONDS);
    }

    @Destroy
    public void stop() {
        executor.shutdownNow();
        destroyQueues.clear();
        super.stop();
    }

    public void registerCallback(F3Conversation conversation, ConversationExpirationCallback callback) {
        List<ConversationExpirationCallback> callbacks = expirationCallbacks.get(conversation);
        if (callbacks == null) {
            callbacks = new ArrayList<ConversationExpirationCallback>();
            expirationCallbacks.put(conversation, callbacks);
        }
        synchronized (callbacks) {
            callbacks.add(callback);
        }
    }

    public void startContext(WorkContext workContext) throws ComponentException {
        startContext(workContext, null);
    }

    public void startContext(WorkContext workContext, ExpirationPolicy policy) throws ComponentException {
        F3Conversation conversation = workContext.peekCallFrame().getConversation();
        assert conversation != null;
        store.startContext(conversation);
        destroyQueues.put(conversation, new ArrayList<InstanceWrapper>());
        if (policy != null) {
            expirationPolicies.put(conversation, policy);
        }
    }

    public void joinContext(WorkContext workContext) throws GroupInitializationException {
        joinContext(workContext, null);
    }

    public void joinContext(WorkContext workContext, ExpirationPolicy policy) {
        F3Conversation conversation = workContext.peekCallFrame().getConversation();
        assert conversation != null;
        if (!destroyQueues.containsKey(conversation)) {
            destroyQueues.put(conversation, new ArrayList<InstanceWrapper>());
            if (policy != null) {
                expirationPolicies.put(conversation, policy);
            }
        }
    }

    public void stopContext(WorkContext workContext) throws ComponentException {
        F3Conversation conversation = workContext.peekCallFrame().getConversation();
        assert conversation != null;
        stopContext(conversation, workContext);
        expirationPolicies.remove(conversation);
        notifyExpirationCallbacks(conversation);
    }

    private void stopContext(F3Conversation conversation, WorkContext workContext) throws InstanceLifecycleException {
        List<InstanceWrapper> list = destroyQueues.remove(conversation);
        if (list == null) {
            throw new IllegalStateException("Conversation does not exist: " + conversation);
        }
        destroyInstances(list, workContext);
        store.stopContext(conversation);
    }

    public InstanceWrapper getWrapper(AtomicComponent component, WorkContext workContext) throws InstanceLifecycleException {
        CallFrame frame = workContext.peekCallFrame();
        F3Conversation conversation = frame.getConversation();
        assert conversation != null;
        ExpirationPolicy policy = expirationPolicies.get(conversation);
        if (policy != null && !policy.isExpired()) {
            // renew the conversation expiration if one is associated, i.e. it is an expiring conversation
            expirationPolicies.get(conversation).renew();
        }
        ConversationContext context = frame.getConversationContext();
        // if the context is new or propagates a conversation and the target instance has not been created, create it
        boolean create = (context == ConversationContext.NEW || context == ConversationContext.PROPAGATE);
        InstanceWrapper wrapper = getWrapper(component, workContext, conversation, create);
        if (wrapper == null) {
            // conversation has either been ended or timed out, throw an exception
            throw new ConversationEndedException("Conversation ended");
        }
        return wrapper;
    }

    public void reinject() {
    }

    public void updated(AtomicComponent component, String referenceName) {

    }

    public void removed(AtomicComponent component, String referenceName) {

    }

    public void returnWrapper(AtomicComponent component, WorkContext workContext, InstanceWrapper wrapper) {
    }


    private void notifyExpirationCallbacks(F3Conversation conversation) {
        List<ConversationExpirationCallback> callbacks = expirationCallbacks.remove(conversation);
        if (callbacks != null) {
            synchronized (callbacks) {
                for (ConversationExpirationCallback callback : callbacks) {
                    callback.expire(conversation);
                }
            }
        }
    }


    /**
     * Periodically scans and removes expired conversation contexts.
     */
    private class Reaper implements Runnable {
        public void run() {
            for (Iterator<Map.Entry<F3Conversation, ExpirationPolicy>> iterator = expirationPolicies.entrySet().iterator(); iterator.hasNext();) {
                Map.Entry<F3Conversation, ExpirationPolicy> entry = iterator.next();
                if (entry.getValue().isExpired()) {
                    F3Conversation conversation = entry.getKey();
                    iterator.remove();
                    WorkContext workContext = new WorkContext();
                    CallFrame frame = new CallFrame(null, conversation, conversation, null);
                    workContext.addCallFrame(frame);
                    try {
                        stopContext(conversation, workContext);
                    } catch (InstanceLifecycleException e) {
                        monitor.error(e);
                    }
                    notifyExpirationCallbacks(conversation);
                }
            }
        }
    }

    /**
     * Return an instance wrapper containing a component implementation instance associated with the correlation key, optionally creating one if not
     * found.
     *
     * @param component    the component the implementation instance belongs to
     * @param workContext  the current WorkContext
     * @param conversation the conversation key for the component implementation instance
     * @param create       true if an instance should be created
     * @return an instance wrapper or null if not found an create is set to false
     * @throws InstanceLifecycleException if an error occurs returning the wrapper
     */
    private InstanceWrapper getWrapper(AtomicComponent component, WorkContext workContext, F3Conversation conversation, boolean create)
            throws InstanceLifecycleException {
        InstanceWrapper wrapper = store.getWrapper(component, conversation);
        if (wrapper == null && create) {
            try {
                wrapper = component.createInstanceWrapper(workContext);
            } catch (ObjectCreationException e) {
                throw new InstanceInitializationException("Error creating instance for: " + component.getUri(), e);
            }
            wrapper.start(workContext);
            store.putWrapper(component, conversation, wrapper);
            List<InstanceWrapper> queue = destroyQueues.get(conversation);
            if (queue == null) {
                throw new IllegalStateException("Instance context not found for : " + component.getUri());
            }
            queue.add(wrapper);
        }
        return wrapper;
    }


}
