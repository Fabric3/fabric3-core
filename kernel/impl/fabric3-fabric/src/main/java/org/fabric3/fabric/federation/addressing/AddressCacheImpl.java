/*
 * Fabric3 Copyright (c) 2009-2013 Metaform Systems
 * 
 * Fabric3 is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version, with the following exception:
 * 
 * Linking this software statically or dynamically with other modules is making
 * a combined work based on this software. Thus, the terms and conditions of the
 * GNU General Public License cover the whole combination.
 * 
 * As a special exception, the copyright holders of this software give you
 * permission to link this software with independent modules to produce an
 * executable, regardless of the license terms of these independent modules, and
 * to copy and distribute the resulting executable under terms of your choice,
 * provided that you also meet, for each linked independent module, the terms
 * and conditions of the license of that module. An independent module is a
 * module which is not derived from or based on this software. If you modify
 * this software, you may extend this exception to your version of the software,
 * but you are not obligated to do so. If you do not wish to do so, delete this
 * exception statement from your version.
 * 
 * Fabric3 is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * Fabric3. If not, see <http://www.gnu.org/licenses/>.
 */
package org.fabric3.fabric.federation.addressing;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executor;

import org.fabric3.api.annotation.monitor.Monitor;
import org.fabric3.host.runtime.HostInfo;
import org.fabric3.spi.runtime.event.EventService;
import org.fabric3.spi.runtime.event.Fabric3EventListener;
import org.fabric3.spi.runtime.event.JoinDomainCompleted;
import org.fabric3.spi.federation.addressing.AddressAnnouncement;
import org.fabric3.spi.federation.addressing.AddressCache;
import org.fabric3.spi.federation.addressing.AddressEvent;
import org.fabric3.spi.federation.addressing.AddressListener;
import org.fabric3.spi.federation.addressing.AddressMonitor;
import org.fabric3.spi.federation.addressing.AddressRequest;
import org.fabric3.spi.federation.addressing.AddressUpdate;
import org.fabric3.spi.federation.addressing.SocketAddress;
import org.fabric3.spi.federation.topology.MessageException;
import org.fabric3.spi.federation.topology.MessageReceiver;
import org.fabric3.spi.federation.topology.ParticipantTopologyService;
import org.fabric3.spi.federation.topology.TopologyListener;
import org.fabric3.spi.federation.topology.ZoneChannelException;
import org.oasisopen.sca.annotation.Destroy;
import org.oasisopen.sca.annotation.Init;
import org.oasisopen.sca.annotation.Reference;
import org.oasisopen.sca.annotation.Service;

/**
 *
 */
@Service(AddressCache.class)
public class AddressCacheImpl implements AddressCache, TopologyListener, MessageReceiver, Fabric3EventListener<JoinDomainCompleted> {
    private static final String ADDRESS_CHANNEL = "F3AddressChannel";

    private ParticipantTopologyService topologyService;
    private Executor executor;
    private EventService eventService;
    private HostInfo info;
    private AddressMonitor monitor;

    private String qualifiedChannelName;

    protected Map<String, List<SocketAddress>> addresses = new ConcurrentHashMap<String, List<SocketAddress>>();
    protected Map<String, List<AddressListener>> listeners = new ConcurrentHashMap<String, List<AddressListener>>();

    public AddressCacheImpl(@Reference Executor executor, @Reference EventService eventService, @Reference HostInfo info, @Monitor AddressMonitor monitor) {
        this.executor = executor;
        this.eventService = eventService;
        this.info = info;
        this.monitor = monitor;
        this.qualifiedChannelName = ADDRESS_CHANNEL + "." + info.getDomain().getAuthority();
    }

    @Reference(required = false)
    public void setTopologyService(ParticipantTopologyService topologyService) {
        this.topologyService = topologyService;
    }

    @Init
    public void init() throws MessageException {
        eventService.subscribe(JoinDomainCompleted.class, this);
        if (topologyService != null) {
            topologyService.register(this);
        }
    }

    @Destroy
    public void destroy() throws ZoneChannelException {
        if (topologyService != null) {
            topologyService.closeChannel(qualifiedChannelName);
            topologyService.deregister(this);
        }
    }

    public List<SocketAddress> getActiveAddresses(String endpointId) {
        List<SocketAddress> list = addresses.get(endpointId);
        if (list == null) {
            return Collections.emptyList();
        }
        return list;
    }

    public void publish(AddressEvent event) {
        publish(event, true);
    }

    public void subscribe(String endpointId, AddressListener listener) {
        List<AddressListener> list = listeners.get(endpointId);
        if (list == null) {
            list = new CopyOnWriteArrayList<AddressListener>();
            this.listeners.put(endpointId, list);
        }
        list.add(listener);
    }

    public void unsubscribe(String endpointId, String listenerId) {
        List<AddressListener> list = listeners.get(endpointId);
        if (list == null) {
            return;
        }
        List<AddressListener> deleted = new ArrayList<AddressListener>();
        for (AddressListener listener : list) {
            if (listenerId.equals(listener.getId())) {
                deleted.add(listener);
                if (list.isEmpty()) {
                    listeners.remove(endpointId);
                }
                break;
            }
        }
        for (AddressListener listener : deleted) {
            list.remove(listener);
        }
    }

    protected void notifyChange(String endpointId) {
        List<SocketAddress> addresses = this.addresses.get(endpointId);
        if (addresses == null) {
            addresses = Collections.emptyList();
        }
        List<AddressListener> list = listeners.get(endpointId);
        if (list == null) {
            return;
        }
        for (AddressListener listener : list) {
            listener.onUpdate(addresses);
        }
    }

    public void onMessage(Object object) {
        if (object instanceof AddressAnnouncement) {
            AddressAnnouncement announcement = (AddressAnnouncement) object;
            publish(announcement, false);
        } else if (object instanceof AddressUpdate) {
            AddressUpdate update = (AddressUpdate) object;
            for (AddressAnnouncement announcement : update.getAnnouncements()) {
                publish(announcement, false);
            }
        } else if (object instanceof AddressRequest) {
            handleAddressRequest((AddressRequest) object);
        }
    }

    public void onLeave(String name) {
        for (Map.Entry<String, List<SocketAddress>> entry : addresses.entrySet()) {
            List<SocketAddress> toDelete = new ArrayList<SocketAddress>();
            List<SocketAddress> list = entry.getValue();
            for (SocketAddress address : list) {
                if (name.equals(address.getRuntimeName())) {
                    toDelete.add(address);
                }
            }
            for (SocketAddress address : toDelete) {
                monitor.removed(name, address.toString());
                list.remove(address);
            }
            if (list.isEmpty()) {
                addresses.remove(entry.getKey());
            }
            notifyChange(entry.getKey());
        }
    }

    public void onJoin(String name) {
        // no-op
    }

    public void onLeaderElected(String name) {
        // no-op
    }

    /**
     * Broadcasts address requests after the runtime has joined the domain to synchronize the cache.
     *
     * @param event the event the event signalling the runtime has joined the domain
     */
    public void onEvent(JoinDomainCompleted event) {
        try {
            if (topologyService != null) {
                topologyService.openChannel(qualifiedChannelName, null, this);
                AddressRequest request = new AddressRequest(info.getRuntimeName());
                topologyService.sendAsynchronous(qualifiedChannelName, request);
            }
        } catch (ZoneChannelException e) {
            monitor.error(e);
        } catch (MessageException e) {
            monitor.error(e);
        }
    }

    private void publish(AddressEvent event, boolean propagate) {
        if (event instanceof AddressAnnouncement) {
            AddressAnnouncement announcement = (AddressAnnouncement) event;
            String endpointId = announcement.getEndpointId();
            List<SocketAddress> addresses = this.addresses.get(endpointId);
            SocketAddress address = announcement.getAddress();
            if (AddressAnnouncement.Type.ACTIVATED == announcement.getType()) {
                // add the new address
                if (addresses == null) {
                    addresses = new CopyOnWriteArrayList<SocketAddress>();
                    this.addresses.put(endpointId, addresses);
                }
                monitor.added(endpointId, address.toString());
                addresses.add(address);
            } else {
                // remove the address
                if (addresses != null) {
                    monitor.removed(endpointId, address.toString());
                    addresses.remove(address);
                    if (addresses.isEmpty()) {
                        this.addresses.remove(endpointId);
                    }
                }
            }

            if (propagate && topologyService != null && event instanceof AddressAnnouncement) {
                try {
                    topologyService.sendAsynchronous(qualifiedChannelName, event);
                } catch (MessageException e) {
                    monitor.error(e);
                }
            }
            // notify listeners of a change
            notifyChange(endpointId);
        }
    }

    private void handleAddressRequest(final AddressRequest request) {
        monitor.receivedRequest(request.getRuntimeName());
        final AddressUpdate update = new AddressUpdate();
        for (Map.Entry<String, List<SocketAddress>> entry : addresses.entrySet()) {
            for (SocketAddress address : entry.getValue()) {
                if (info.getRuntimeName().equals(address.getRuntimeName())) {
                    AddressAnnouncement announcement = new AddressAnnouncement(entry.getKey(), AddressAnnouncement.Type.ACTIVATED, address);
                    update.addAnnouncement(announcement);
                }
            }
        }
        if (!update.getAnnouncements().isEmpty()) {
            // send response from a separate thread to avoid blocking on the federation callback
            executor.execute(new Runnable() {
                public void run() {
                    try {
                        topologyService.sendAsynchronous(request.getRuntimeName(), qualifiedChannelName, update);
                    } catch (MessageException e) {
                        monitor.error(e);
                    }
                }
            });
        }
    }

}
