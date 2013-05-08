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

import javax.xml.namespace.QName;
import java.util.ArrayList;
import java.util.List;

import org.fabric3.api.annotation.monitor.Monitor;
import org.fabric3.host.RuntimeMode;
import org.fabric3.host.runtime.HostInfo;
import org.fabric3.model.type.component.Scope;
import org.fabric3.spi.component.GroupInitializationException;
import org.fabric3.spi.component.InstanceLifecycleException;
import org.fabric3.spi.component.ScopeContainer;
import org.fabric3.spi.component.ScopedComponent;
import org.fabric3.spi.federation.TopologyListener;
import org.fabric3.spi.federation.ZoneTopologyService;
import org.fabric3.spi.invocation.WorkContextCache;
import org.oasisopen.sca.annotation.Destroy;
import org.oasisopen.sca.annotation.EagerInit;
import org.oasisopen.sca.annotation.Init;
import org.oasisopen.sca.annotation.Reference;
import org.oasisopen.sca.annotation.Service;

/**
 * Manages domain-scoped components. A domain scoped component has only one instance active in a domain. The active instance will be hosted by the zone leader.
 * If a zone is clustered and the zone leader fails, clustered instances will be migrated to the newly elected leader and activated.
 * <p/>
 * During deployment, the container checks if the runtime is a zone leader. If it is, contexts will be started. Otherwise, they will be deferred until the
 * existing leader fails and the current host is elected zone leader.
 */
@EagerInit
@Service(names = {ScopeContainer.class, TopologyListener.class})
public class DomainScopeContainer extends SingletonScopeContainer implements TopologyListener {
    private HostInfo info;
    private ZoneTopologyService topologyService;
    private final List<QName> deferredContexts = new ArrayList<QName>();
    boolean activated;

    public DomainScopeContainer(@Reference HostInfo info, @Monitor ScopeContainerMonitor monitor) {
        super(Scope.DOMAIN, monitor);
        this.info = info;
    }

    @Reference(required = false)
    public void setTopologyService(List<ZoneTopologyService> topologyServices) {
        if (topologyServices.size() > 0) {
            this.topologyService = topologyServices.get(0);
        }
    }

    @Override
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
        if (RuntimeMode.PARTICIPANT == info.getRuntimeMode() && topologyService == null) {
            return;
        } else if (RuntimeMode.PARTICIPANT == info.getRuntimeMode() && !topologyService.isZoneLeader()) {
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

    public Object getInstance(ScopedComponent component) throws InstanceLifecycleException {
        if (topologyService != null && !activated) {
            throw new InstanceLifecycleException("Component instance not active: " + component.getUri());
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
            for (QName deployable : deferredContexts) {
                WorkContextCache.getAndResetThreadWorkContext();
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