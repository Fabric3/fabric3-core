/*
 * Fabric3
 * Copyright (c) 2009-2013 Metaform Systems
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
