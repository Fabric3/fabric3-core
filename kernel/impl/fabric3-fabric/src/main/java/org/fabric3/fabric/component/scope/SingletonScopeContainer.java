/*
 * Fabric3
 * Copyright (c) 2009-2012 Metaform Systems
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
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import javax.xml.namespace.QName;

import org.oasisopen.sca.annotation.Destroy;

import org.fabric3.api.annotation.monitor.Monitor;
import org.fabric3.model.type.component.Scope;
import org.fabric3.spi.component.GroupInitializationException;
import org.fabric3.spi.component.InstanceDestructionException;
import org.fabric3.spi.component.InstanceInitException;
import org.fabric3.spi.component.InstanceLifecycleException;
import org.fabric3.spi.component.ScopedComponent;
import org.fabric3.spi.invocation.WorkContext;
import org.fabric3.spi.objectfactory.ObjectCreationException;

/**
 * Abstract container for components that have only one implementation instance.
 * <p/>
 * Components deployed via a deployable composite are associated with the same context. When a context starts and stops, components will receive
 * initialization and destruction callbacks. Eager initialization is also supported.
 *
 * @version $Rev: 9039 $ $Date: 2010-05-24 10:22:02 +0200 (Mon, 24 May 2010) $
 */
public abstract class SingletonScopeContainer extends AbstractScopeContainer {
    private static final Object EMPTY = new Object();

    private final Map<ScopedComponent, Object> instances;
    // The map of instance/component pairs to destroy keyed by the deployable composite the component was deployed with.
    // The queues of instance/component pairs are ordered by the sequence in which the deployables were deployed.
    // The instance/component pairs themselves are ordered by the sequence in which they were instantiated.
    private final Map<QName, List<Pair>> destroyQueues;

    // the queue of components to eagerly initialize in each group
    private final Map<QName, List<ScopedComponent>> initQueues = new HashMap<QName, List<ScopedComponent>>();

    // components that are in the process of being created
    private final Map<ScopedComponent, CountDownLatch> pending;

    protected SingletonScopeContainer(Scope scope, @Monitor ScopeContainerMonitor monitor) {
        super(scope, monitor);
        instances = new ConcurrentHashMap<ScopedComponent, Object>();
        pending = new ConcurrentHashMap<ScopedComponent, CountDownLatch>();
        destroyQueues = new LinkedHashMap<QName, List<Pair>>();
    }

    public void register(ScopedComponent component) {
        super.register(component);
        if (component.isEagerInit()) {
            QName deployable = component.getDeployable();
            synchronized (initQueues) {
                List<ScopedComponent> initQueue = initQueues.get(deployable);
                if (initQueue == null) {
                    initQueue = new ArrayList<ScopedComponent>();
                    initQueues.put(deployable, initQueue);
                }
                initQueue.add(component);
            }
        }
        instances.put(component, EMPTY);
    }

    public void unregister(ScopedComponent component) {
        super.unregister(component);
        // FIXME should this component be destroyed already or do we need to stop it?
        instances.remove(component);
        if (component.isEagerInit()) {
            QName deployable = component.getDeployable();
            synchronized (initQueues) {
                List<ScopedComponent> initQueue = initQueues.get(deployable);
                initQueue.remove(component);
                if (initQueue.isEmpty()) {
                    initQueues.remove(deployable);
                }
            }
        }
    }

    public void startContext(QName deployable, WorkContext workContext) throws GroupInitializationException {
        eagerInitialize(workContext, deployable);
        // Destroy queues must be updated *after* components have been eagerly initialized since the latter may have dependencies from other
        // contexts. These other contexts need to be put into the destroy queue ahead of the current initializing context so the dependencies
        // are destroyed after the eagerly initialized components (the destroy queues are iterated in reverse order).
        synchronized (destroyQueues) {
            if (!destroyQueues.containsKey(deployable)) {
                destroyQueues.put(deployable, new ArrayList<Pair>());
            }
        }
    }

    public void stopContext(QName deployable, WorkContext workContext) {
        synchronized (destroyQueues) {
            List<Pair> list = destroyQueues.get(deployable);
            if (list == null) {
                // this can happen with domain scope where a non-leader runtime does not activate a context
                return;
            }
            destroyInstances(list, workContext);
        }
    }

    @Destroy
    public synchronized void stop() {
        super.stop();
        synchronized (destroyQueues) {
            destroyQueues.clear();
        }
        synchronized (initQueues) {
            initQueues.clear();
        }
        instances.clear();
    }

    @SuppressWarnings({"SynchronizationOnLocalVariableOrMethodParameter"})
    public Object getInstance(ScopedComponent component, WorkContext workContext) throws InstanceLifecycleException {
        Object instance = instances.get(component);
        if (instance != EMPTY && instance != null) {
            return instance;
        }
        CountDownLatch latch;
        // Avoid race condition where two or more threads attempt to initialize the component instance concurrently.
        // Pending component instantiations are tracked and threads block on a latch until they are complete.
        synchronized (component) {
            latch = pending.get(component);
            if (latch != null) {
                try {
                    // wait on the instantiation
                    latch.await(5, TimeUnit.MINUTES);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    throw new InstanceInitException("Error creating instance for: " + component.getUri(), e);
                }
                // an instance wrapper is now available as the instantiation has completed
                return instances.get(component);
            } else {
                latch = new CountDownLatch(1);
                pending.put(component, latch);
            }
        }
        try {
            instance = component.createInstance(workContext);
            // some component instances such as system singletons may already be started
            // if (!component.isInstanceStarted()) {
            component.startInstance(instance, workContext);
            List<Pair> queue;
            QName deployable = component.getDeployable();
            synchronized (destroyQueues) {
                queue = destroyQueues.get(deployable);
                if (queue == null) {
                    // The context has not been initialized. This can happen if two deployable composites are deployed simultaneously and a
                    // component in the first composite to be deployed references a component in the second composite. In this case,
                    // create the destroy queue prior to the context being started.
                    queue = new ArrayList<Pair>();
                    destroyQueues.put(deployable, queue);
                }
            }
            queue.add(new Pair(component, instance));
            //}
            instances.put(component, instance);
            latch.countDown();
            return instance;
        } catch (ObjectCreationException e) {
            throw new InstanceInitException("Error creating instance for: " + component.getUri(), e);
        } finally {
            pending.remove(component);
        }
    }

    public void releaseInstance(ScopedComponent component, Object instance, WorkContext workContext) {
        // no-op
    }

    public List<Object> getActiveInstances(ScopedComponent component) {
        Object instance = instances.get(component);
        if (instance == null || instance == EMPTY) {
            return Collections.emptyList();
        }
        return Collections.singletonList(instance);
    }

    public void reinject() throws InstanceLifecycleException {
        for (Map.Entry<ScopedComponent, Object> entry : instances.entrySet()) {
            ScopedComponent component = entry.getKey();
            Object instance = entry.getValue();
            component.reinject(instance);
        }
    }

    public void stopAllContexts(WorkContext workContext) {
        synchronized (destroyQueues) {
            // Shutdown all instances by traversing the deployable composites in reverse order they were deployed and interating instances within
            // each composite in the reverse order they were instantiated. This guarantees dependencies are disposed after the dependent instance.
            List<List<Pair>> queues = new ArrayList<List<Pair>>(destroyQueues.values());
            ListIterator<List<Pair>> iter = queues.listIterator(queues.size());
            while (iter.hasPrevious()) {
                List<Pair> queue = iter.previous();
                destroyInstances(queue, workContext);
            }
        }
    }

    private void eagerInitialize(WorkContext workContext, QName contextId) throws GroupInitializationException {
        // get and clone initialization queue
        List<ScopedComponent> initQueue;
        synchronized (initQueues) {
            initQueue = initQueues.get(contextId);
            if (initQueue != null) {
                initQueue = new ArrayList<ScopedComponent>(initQueue);
            }
        }
        if (initQueue != null) {
            initializeComponents(initQueue, workContext);
        }
    }

    /**
     * Initialize an ordered list of components. The list is traversed in order and the getWrapper() method called for each to associate an instance
     * with the supplied context.
     *
     * @param components  the components to be initialized
     * @param workContext the work context in which to initialize the components
     * @throws GroupInitializationException if one or more components threw an exception during initialization
     */
    private void initializeComponents(List<ScopedComponent> components, WorkContext workContext) throws GroupInitializationException {
        List<Exception> causes = null;
        for (ScopedComponent component : components) {
            try {
                getInstance(component, workContext);
            } catch (Exception e) {
                monitor.eagerInitializationError(component.getUri(), e);
                if (causes == null) {
                    causes = new ArrayList<Exception>();
                }
                causes.add(e);
            }
        }
        if (causes != null) {
            throw new GroupInitializationException(causes);
        }
    }

    /**
     * Shut down an ordered list of instances. The list passed to this method is treated as a live, mutable list so any instances added to this list
     * as shutdown is occurring will also be shut down.
     *
     * @param instances   the list of instances to shutdown
     * @param workContext the current work context
     */
    @SuppressWarnings({"SynchronizationOnLocalVariableOrMethodParameter"})
    private void destroyInstances(List<Pair> instances, WorkContext workContext) {
        while (true) {
            Pair toDestroy;
            synchronized (instances) {
                if (instances.size() == 0) {
                    return;
                }
                toDestroy = instances.remove(instances.size() - 1);
            }
            try {
                ScopedComponent component = toDestroy.component;
                Object instance = toDestroy.instance;
                component.stopInstance(instance, workContext);
            } catch (InstanceDestructionException e) {
                // log the error from destroy but continue
                monitor.destructionError(e);
            }
        }
    }


    private class Pair {
        private ScopedComponent component;
        private Object instance;

        private Pair(ScopedComponent component, Object instance) {
            this.component = component;
            this.instance = instance;
        }
    }
}