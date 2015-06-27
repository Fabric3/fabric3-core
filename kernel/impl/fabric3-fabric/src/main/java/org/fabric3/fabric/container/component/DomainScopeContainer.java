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
import java.util.List;

import org.fabric3.api.annotation.monitor.Monitor;
import org.fabric3.api.host.Fabric3Exception;
import org.fabric3.api.host.runtime.HostInfo;
import org.fabric3.api.model.type.RuntimeMode;
import org.fabric3.api.model.type.component.Scope;
import org.fabric3.spi.container.component.GroupInitializationException;
import org.fabric3.spi.container.component.ScopeContainer;
import org.fabric3.spi.container.component.ScopedComponent;
import org.fabric3.spi.container.invocation.WorkContextCache;
import org.fabric3.spi.discovery.DiscoveryAgent;
import org.oasisopen.sca.annotation.Destroy;
import org.oasisopen.sca.annotation.EagerInit;
import org.oasisopen.sca.annotation.Init;
import org.oasisopen.sca.annotation.Reference;
import org.oasisopen.sca.annotation.Service;

/**
 * Manages domain-scoped components. A domain scoped component has only one instance active in a domain. The active instance will be hosted by the zone leader.
 * If a zone is clustered and the zone leader fails, clustered instances will be migrated to the newly elected leader and activated.  During deployment, the
 * container checks if the runtime is a zone leader. If it is, contexts will be started. Otherwise, they will be deferred until the existing leader fails and
 * the current host is elected zone leader.
 */
@EagerInit
@Service(ScopeContainer.class)
public class DomainScopeContainer extends SingletonScopeContainer {
    private HostInfo info;
    private final List<URI> deferredContexts = new ArrayList<>();
    boolean activated;

    @Reference(required = false)
    protected DiscoveryAgent discoveryAgent;

    public DomainScopeContainer(@Reference HostInfo info, @Monitor ScopeContainerMonitor monitor) {
        super(Scope.DOMAIN, monitor);
        this.info = info;
    }

    @Init
    public void start() {
        super.start();
        if (discoveryAgent != null) {
            discoveryAgent.registerLeadershipListener(this::onLeaderChange);
        }
    }

    @Destroy
    public synchronized void stop() {
        synchronized (deferredContexts) {
            deferredContexts.clear();
        }
        super.stop();
    }

    public void startContext(URI uri) throws GroupInitializationException {
        if (RuntimeMode.NODE == info.getRuntimeMode() && discoveryAgent == null) {
            return;
        } else if (RuntimeMode.NODE == info.getRuntimeMode() && !discoveryAgent.isLeader()) {
            // defer instantiation until this node becomes zone leader
            synchronized (deferredContexts) {
                deferredContexts.add(uri);
            }
            return;
        }
        activated = true;
        super.startContext(uri);
    }

    public void stopContext(URI uri) {
        synchronized (deferredContexts) {
            deferredContexts.remove(uri);
        }
        super.stopContext(uri);
    }

    public Object getInstance(ScopedComponent component) {
        if (discoveryAgent != null && !activated) {
            throw new Fabric3Exception("Component instance not active: " + component.getUri());
        }
        return super.getInstance(component);
    }

    public void onLeaderChange(boolean value) {
        if (!value) {
            return;
        }
        activated = true;
        // this runtime was elected leader, start the components
        synchronized (deferredContexts) {
            WorkContextCache.getAndResetThreadWorkContext();
            for (URI contributionUri : deferredContexts) {
                try {
                    super.startContext(contributionUri);
                } catch (GroupInitializationException e) {
                    monitor.leaderElectionError(e);
                }
            }
            deferredContexts.clear();
        }

    }

    public void stopAllContexts() {
        synchronized (deferredContexts) {
            deferredContexts.clear();
        }
        super.stopAllContexts();
    }

}