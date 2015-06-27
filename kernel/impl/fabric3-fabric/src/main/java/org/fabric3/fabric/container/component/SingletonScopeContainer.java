/*
 * Fabric3
 * Copyright (c) 2009-2015 Metaform Systems
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Portions originally based on Apache Tuscany 2007
 * licensed under the Apache 2.0 license.
 */
package org.fabric3.fabric.container.component;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.fabric3.api.annotation.monitor.Monitor;
import org.fabric3.api.host.Fabric3Exception;
import org.fabric3.api.model.type.component.Scope;
import org.fabric3.spi.container.component.GroupInitializationException;
import org.fabric3.spi.container.component.ScopedComponent;
import org.oasisopen.sca.annotation.Destroy;

/**
 * Abstract container for components that have only one implementation instance.  Components deployed via a deployable composite are associated with the
 * same context. When a context starts and stops, components will receive initialization and destruction callbacks. Eager initialization is also supported.
 */
public abstract class SingletonScopeContainer extends AbstractScopeContainer {
    private static final Object EMPTY = new Object();

    private final Map<ScopedComponent, Object> instances;
    // The map of instance/component pairs to destroy keyed by the component contribution URI.
    // The queues of instance/component pairs are ordered by the sequence in which the contribution were deployed.
    // The instance/component pairs themselves are ordered by the sequence in which they were instantiated.
    private final Map<URI, List<Pair>> destroyQueues;

    // the queue of components to eagerly initialize in each group
    private final Map<URI, List<ScopedComponent>> initQueues = new HashMap<>();

    // components that are in the process of being created
    private final Map<ScopedComponent, CountDownLatch> pending;

    protected SingletonScopeContainer(Scope scope, @Monitor ScopeContainerMonitor monitor) {
        super(scope, monitor);
        instances = new ConcurrentHashMap<>();
        pending = new ConcurrentHashMap<>();
        destroyQueues = new LinkedHashMap<>();
    }

    public void register(ScopedComponent component) {
        super.register(component);
        if (component.isEagerInit()) {
            URI uri = component.getContributionUri();
            synchronized (initQueues) {
                List<ScopedComponent> initQueue = initQueues.get(uri);
                if (initQueue == null) {
                    initQueue = new ArrayList<>();
                    initQueues.put(uri, initQueue);
                }
                initQueue.add(component);
            }
        }
        instances.put(component, EMPTY);
    }

    public void unregister(ScopedComponent component) {
        super.unregister(component);
        instances.remove(component);
        if (component.isEagerInit()) {
            URI uri = component.getContributionUri();
            synchronized (initQueues) {
                List<ScopedComponent> initQueue = initQueues.get(uri);
                initQueue.remove(component);
                if (initQueue.isEmpty()) {
                    initQueues.remove(uri);
                }
            }
        }
    }

    public void startContext(URI contribution) throws GroupInitializationException {
        eagerInitialize(contribution);
        // Destroy queues must be updated *after* components have been eagerly initialized since the latter may have dependencies from other
        // contexts. These other contexts need to be put into the destroy queue ahead of the current initializing context so the dependencies
        // are destroyed after the eagerly initialized components (the destroy queues are iterated in reverse order).
        synchronized (destroyQueues) {
            if (!destroyQueues.containsKey(contribution)) {
                destroyQueues.put(contribution, new ArrayList<>());
            }
        }
    }

    public void stopContext(URI contribution) {
        synchronized (destroyQueues) {
            List<Pair> list = destroyQueues.get(contribution);
            if (list == null) {
                // this can happen with domain scope where a non-leader runtime does not activate a context
                return;
            }
            destroyInstances(list);
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
    public Object getInstance(ScopedComponent component) {
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
                    throw new Fabric3Exception("Error creating instance for: " + component.getUri(), e);
                }
                // an instance wrapper is now available as the instantiation has completed
                return instances.get(component);
            } else {
                latch = new CountDownLatch(1);
                pending.put(component, latch);
            }
        }
        try {
            instance = component.createInstance();
            // some component instances such as system singletons may already be started
            // if (!component.isInstanceStarted()) {
            component.startInstance(instance);
            List<Pair> queue;
            URI uri = component.getContributionUri();
            synchronized (destroyQueues) {
                queue = destroyQueues.get(uri);
                if (queue == null) {
                    // The context has not been initialized. This can happen if two deployable composites are deployed simultaneously and a
                    // component in the first composite to be deployed references a component in the second composite. In this case,
                    // create the destroy queue prior to the context being started.
                    queue = new ArrayList<>();
                    destroyQueues.put(uri, queue);
                }
            }
            queue.add(new Pair(component, instance));
            //}
            instances.put(component, instance);
            latch.countDown();
            return instance;
        } finally {
            pending.remove(component);
        }
    }

    public void releaseInstance(ScopedComponent component, Object instance) {
        // no-op
    }

    public List<Object> getActiveInstances(ScopedComponent component) {
        Object instance = instances.get(component);
        if (instance == null || instance == EMPTY) {
            return Collections.emptyList();
        }
        return Collections.singletonList(instance);
    }

    public void reinject() {
        for (Map.Entry<ScopedComponent, Object> entry : instances.entrySet()) {
            ScopedComponent component = entry.getKey();
            Object instance = entry.getValue();
            component.reinject(instance);
        }
    }

    public void stopAllContexts() {
        synchronized (destroyQueues) {
            // Shutdown all instances by traversing the deployable composites in reverse order they were deployed and interating instances within
            // each composite in the reverse order they were instantiated. This guarantees dependencies are disposed after the dependent instance.
            List<List<Pair>> queues = new ArrayList<>(destroyQueues.values());
            ListIterator<List<Pair>> iter = queues.listIterator(queues.size());
            while (iter.hasPrevious()) {
                List<Pair> queue = iter.previous();
                destroyInstances(queue);
            }
        }
    }

    private void eagerInitialize(URI contextUri) {
        // get and clone initialization queue
        List<ScopedComponent> initQueue;
        synchronized (initQueues) {
            initQueue = initQueues.get(contextUri);
            if (initQueue != null) {
                initQueue = new ArrayList<>(initQueue);
            }
        }
        if (initQueue != null) {
            initializeComponents(initQueue);
        }
    }

    /**
     * Initialize an ordered list of components. The list is traversed in order and the getWrapper() method called for each to associate an instance with the
     * supplied context.
     *
     * @param components the components to be initialized
     * @throws GroupInitializationException if one or more components threw an exception during initialization
     */
    private void initializeComponents(List<ScopedComponent> components) throws GroupInitializationException {
        Set<URI> causes = null;
        for (ScopedComponent component : components) {
            try {
                getInstance(component);
            } catch (Exception e) {
                if (causes == null) {
                    causes = new LinkedHashSet<>();
                }
                URI uri = component.getUri();
                monitor.initializationError(uri, component.getContributionUri(), e);
                causes.add(uri);
            }
        }
        if (causes != null) {
            throw new GroupInitializationException(causes);
        }
    }

    /**
     * Shut down an ordered list of instances. The list passed to this method is treated as a live, mutable list so any instances added to this list as shutdown
     * is occurring will also be shut down.
     *
     * @param instances the list of instances to shutdown
     */
    @SuppressWarnings({"SynchronizationOnLocalVariableOrMethodParameter"})
    private void destroyInstances(List<Pair> instances) {
        while (true) {
            Pair toDestroy;
            synchronized (instances) {
                if (instances.size() == 0) {
                    return;
                }
                toDestroy = instances.remove(instances.size() - 1);
            }
            ScopedComponent component = toDestroy.component;
            try {
                Object instance = toDestroy.instance;
                component.stopInstance(instance);
            } catch (Fabric3Exception e) {
                // log the error from destroy but continue
                monitor.destructionError(component.getUri(), component.getContributionUri(), e);
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