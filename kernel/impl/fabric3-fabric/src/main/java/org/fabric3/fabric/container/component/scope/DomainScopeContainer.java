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
package org.fabric3.fabric.container.component.scope;

import javax.xml.namespace.QName;
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
import org.fabric3.spi.federation.topology.NodeTopologyService;
import org.fabric3.spi.federation.topology.TopologyListener;
import org.oasisopen.sca.annotation.Destroy;
import org.oasisopen.sca.annotation.EagerInit;
import org.oasisopen.sca.annotation.Init;
import org.oasisopen.sca.annotation.Reference;
import org.oasisopen.sca.annotation.Service;

/**
 * Manages domain-scoped components. A domain scoped component has only one instance active in a domain. The active instance will be hosted by the zone leader.
 * If a zone is clustered and the zone leader fails, clustered instances will be migrated to the newly elected leader and activated. <p/> During deployment, the
 * container checks if the runtime is a zone leader. If it is, contexts will be started. Otherwise, they will be deferred until the existing leader fails and
 * the current host is elected zone leader.
 */
@EagerInit
@Service({ScopeContainer.class, TopologyListener.class})
public class DomainScopeContainer extends SingletonScopeContainer implements TopologyListener {
    private HostInfo info;
    private NodeTopologyService topologyService;
    private final List<QName> deferredContexts = new ArrayList<>();
    boolean activated;

    public DomainScopeContainer(@Reference HostInfo info, @Monitor ScopeContainerMonitor monitor) {
        super(Scope.DOMAIN, monitor);
        this.info = info;
    }

    @Reference(required = false)
    public void setTopologyService(List<NodeTopologyService> topologyServices) {
        if (topologyServices.size() > 0) {
            this.topologyService = topologyServices.get(0);
        }
    }

    @Init
    public void start() {
        super.start();
    }

    @Destroy
    public synchronized void stop() {
        synchronized (deferredContexts) {
            deferredContexts.clear();
        }
        super.stop();
    }

    public void startContext(QName deployable) throws GroupInitializationException {
        if (RuntimeMode.NODE == info.getRuntimeMode() && topologyService == null) {
            return;
        } else if (RuntimeMode.NODE == info.getRuntimeMode() && !topologyService.isZoneLeader()) {
            // defer instantiation until this node becomes zone leader
            synchronized (deferredContexts) {
                deferredContexts.add(deployable);
            }
            return;
        }
        activated = true;
        super.startContext(deployable);
    }

    public void stopContext(QName deployable) {
        synchronized (deferredContexts) {
            deferredContexts.remove(deployable);
        }
        super.stopContext(deployable);
    }

    public Object getInstance(ScopedComponent component) throws Fabric3Exception {
        if (topologyService != null && !activated) {
            throw new Fabric3Exception("Component instance not active: " + component.getUri());
        }
        return super.getInstance(component);
    }

    public void onJoin(String name) {
        // no-op
    }

    public void onLeave(String name) {
        // no-op
    }

    public void onLeaderElected(String name) {
        if (topologyService != null && !topologyService.isZoneLeader()) {
            // this runtime is not the leader, ignore
            return;
        }
        activated = true;
        // this runtime was elected leader, start the components
        synchronized (deferredContexts) {
            WorkContextCache.getAndResetThreadWorkContext();
            for (QName deployable : deferredContexts) {
                try {
                    super.startContext(deployable);
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