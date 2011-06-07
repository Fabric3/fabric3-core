/*
 * Fabric3 Copyright (c) 2009-2011 Metaform Systems
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
package org.fabric3.binding.zeromq.runtime.federation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import org.fabric3.binding.zeromq.runtime.SocketAddress;

/**
 * An implementation which propagates socket changes locally.
 *
 * @version $Revision: 10212 $ $Date: 2011-03-15 18:20:58 +0100 (Tue, 15 Mar 2011) $
 */
public class LocalAddressCache implements AddressCache {
    protected Map<String, List<SocketAddress>> addresses = new ConcurrentHashMap<String, List<SocketAddress>>();
    protected Map<String, List<AddressListener>> listeners = new ConcurrentHashMap<String, List<AddressListener>>();

    public List<SocketAddress> getActiveAddresses(String endpointId) {
        List<SocketAddress> list = addresses.get(endpointId);
        if (list == null) {
            return Collections.emptyList();
        }
        return list;
    }

    public void publish(AddressEvent event) {
        if (event instanceof AddressAnnouncement) {
            AddressAnnouncement announcement = (AddressAnnouncement) event;
            String endpointId = announcement.getEndpointId();
            List<SocketAddress> addresses = this.addresses.get(endpointId);
            if (AddressAnnouncement.Type.ACTIVATED == announcement.getType()) {
                // add the new address
                if (addresses == null) {
                    addresses = new CopyOnWriteArrayList<SocketAddress>();
                    this.addresses.put(endpointId, addresses);
                }
                addresses.add(announcement.getAddress());
            } else {
                // remove the address
                if (addresses != null) {
                    addresses.remove(announcement.getAddress());
                    if (addresses.isEmpty()) {
                        this.addresses.remove(endpointId);
                    }
                }
            }

            // notify listeners of a change
            List<AddressListener> list = listeners.get(endpointId);
            if (list == null) {
                return;
            }
            for (AddressListener listener : list) {
                listener.onUpdate(addresses);
            }
        }
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


}
