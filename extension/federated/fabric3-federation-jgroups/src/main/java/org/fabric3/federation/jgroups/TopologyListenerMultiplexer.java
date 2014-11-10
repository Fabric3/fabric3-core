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
 */
package org.fabric3.federation.jgroups;

import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;

import org.fabric3.spi.federation.topology.TopologyListener;
import org.jgroups.Address;
import org.jgroups.MembershipListener;
import org.jgroups.View;
import org.jgroups.util.UUID;

/**
 *
 */
public class TopologyListenerMultiplexer implements MembershipListener {
    private JGroupsHelper helper;
    private final Object viewLock;

    private View previousView;

    private List<TopologyListener> topologyListeners = new CopyOnWriteArrayList<>();

    public TopologyListenerMultiplexer(JGroupsHelper helper, Object viewLock) {
        this.helper = helper;
        this.viewLock = viewLock;
    }

    public TopologyListenerMultiplexer(JGroupsHelper helper, Object viewLock, List<TopologyListener> listeners) {
        this.helper = helper;
        this.viewLock = viewLock;
        topologyListeners.addAll(listeners);
    }

    public void add(TopologyListener listener) {
        topologyListeners.add(listener);
    }

    public void addAll(List<TopologyListener> listeners) {
        topologyListeners.addAll(listeners);
    }

    public void remove(TopologyListener listener) {
        topologyListeners.remove(listener);
    }

    public void viewAccepted(View newView) {
        synchronized (viewLock) {
            try {
                Set<Address> newZoneLeaders = helper.getNewZoneLeaders(previousView, newView);
                Set<Address> newRuntimes = helper.getNewRuntimes(previousView, newView);
                previousView = newView;
                if (newZoneLeaders.isEmpty() && newRuntimes.isEmpty()) {
                    return;
                }
                for (Address address : newRuntimes) {
                    String name = UUID.get(address);
                    for (TopologyListener listener : topologyListeners) {
                        listener.onJoin(name);
                    }
                }
                for (Address address : newZoneLeaders) {
                    String name = UUID.get(address);
                    for (TopologyListener listener : topologyListeners) {
                        listener.onLeaderElected(name);
                    }
                }
            } finally {
                viewLock.notifyAll();
            }
        }
    }

    public void suspect(Address suspected) {
        String runtimeName = UUID.get(suspected);
        for (TopologyListener listener : topologyListeners) {
            listener.onLeave(runtimeName);
        }
    }

    public void block() {
        // no-op
    }

    public void unblock() {
        // no-op
    }

}
