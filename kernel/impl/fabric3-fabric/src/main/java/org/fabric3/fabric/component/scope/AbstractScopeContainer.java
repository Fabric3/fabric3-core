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
import java.util.List;

import org.osoa.sca.annotations.Destroy;
import org.osoa.sca.annotations.Init;
import org.osoa.sca.annotations.Reference;

import org.fabric3.model.type.component.Scope;
import org.fabric3.spi.AbstractLifecycle;
import org.fabric3.spi.component.AtomicComponent;
import org.fabric3.spi.component.ConversationExpirationCallback;
import org.fabric3.spi.invocation.F3Conversation;
import org.fabric3.spi.component.GroupInitializationException;
import org.fabric3.spi.component.InstanceDestructionException;
import org.fabric3.spi.component.InstanceWrapper;
import org.fabric3.spi.component.ScopeContainer;
import org.fabric3.spi.component.ScopeRegistry;
import org.fabric3.spi.invocation.WorkContext;

/**
 * Implements functionality common to scope containers.
 *
 * @version $Rev$ $Date$
 */
public abstract class AbstractScopeContainer extends AbstractLifecycle implements ScopeContainer {
    private final Scope scope;
    protected final ScopeContainerMonitor monitor;
    private ScopeRegistry scopeRegistry;

    public AbstractScopeContainer(Scope scope, ScopeContainerMonitor monitor) {
        this.scope = scope;
        this.monitor = monitor;
    }

    @Reference
    public void setScopeRegistry(ScopeRegistry scopeRegistry) {
        this.scopeRegistry = scopeRegistry;
    }

    @Init
    public synchronized void start() {
        int lifecycleState = getLifecycleState();
        if (lifecycleState != UNINITIALIZED && lifecycleState != STOPPED) {
            throw new IllegalStateException("Scope must be in UNINITIALIZED or STOPPED state [" + lifecycleState + "]");
        }
        if (scopeRegistry != null) {
            scopeRegistry.register(this);
        }
        setLifecycleState(RUNNING);
    }

    @Destroy
    public synchronized void stop() {
        int lifecycleState = getLifecycleState();
        if (lifecycleState != RUNNING) {
            throw new IllegalStateException("Scope in wrong state [" + lifecycleState + "]");
        }
        setLifecycleState(STOPPED);
        if (scopeRegistry != null) {
            scopeRegistry.unregister(this);
        }
    }

    public void stopAllContexts(WorkContext workContext) {
        throw new UnsupportedOperationException();
    }

    public Scope getScope() {
        return scope;
    }

    public void register(AtomicComponent<?> component) {
        checkInit();
    }

    public void unregister(AtomicComponent<?> component) {
        checkInit();
    }

    public void registerCallback(F3Conversation conversation, ConversationExpirationCallback callback) {
        throw new UnsupportedOperationException();
    }

    public void initializeComponents(List<AtomicComponent<?>> components, WorkContext workContext) throws GroupInitializationException {
        List<Exception> causes = null;
        for (AtomicComponent<?> component : components) {
            try {
                getWrapper(component, workContext);
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

    public String toString() {
        return "In state [" + super.toString() + ']';
    }

    /**
     * Shut down an ordered list of instances. The list passed to this method is treated as a live, mutable list so any instances added to this list
     * as shutdown is occurring will also be shut down.
     *
     * @param instances   the list of instances to shutdown
     * @param workContext the current work context
     */
    protected void destroyInstances(List<InstanceWrapper<?>> instances, WorkContext workContext) {
        while (true) {
            InstanceWrapper<?> toDestroy;
            synchronized (instances) {
                if (instances.size() == 0) {
                    return;
                }
                toDestroy = instances.remove(instances.size() - 1);
            }
            try {
                toDestroy.stop(workContext);
            } catch (InstanceDestructionException e) {
                // log the error from destroy but continue
                monitor.destructionError(e);
            }
        }
    }

    private void checkInit() {
        if (getLifecycleState() != RUNNING) {
            throw new IllegalStateException("Scope container not running [" + getLifecycleState() + "]");
        }
    }

}
