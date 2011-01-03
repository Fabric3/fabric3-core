/*
 * Fabric3
 * Copyright (c) 2009-2011 Metaform Systems
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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.jgroups.Address;
import org.jgroups.View;
import org.jgroups.util.UUID;
import org.osoa.sca.annotations.Reference;

import org.fabric3.spi.classloader.ClassLoaderRegistry;
import org.fabric3.spi.classloader.MultiClassLoaderObjectInputStream;
import org.fabric3.spi.classloader.MultiClassLoaderObjectOutputStream;
import org.fabric3.spi.federation.MessageException;

/**
 * @version $Rev$ $Date$
 */
public class JGroupsHelperImpl implements JGroupsHelper {
    private ClassLoaderRegistry classLoaderRegistry;

    public JGroupsHelperImpl(@Reference ClassLoaderRegistry classLoaderRegistry) {
        this.classLoaderRegistry = classLoaderRegistry;
    }

    // domain:controller:id
    // domain:participant:zone:id

    public Address getController(View view) {
        for (Address address : view.getMembers()) {
            String name = UUID.get(address);
            if (name != null && name.substring(name.indexOf(":")).startsWith(":controller:")) {
                return address;
            }
        }
        return null;
    }

    public Address getZoneLeader(String zoneName, View view) {
        for (Address address : view.getMembers()) {
            String name = UUID.get(address);
            if (name == null) {
                return null;
            }
            int pos = name.indexOf(":participant:");
            if (pos < 0) {
                continue;
            }
            name = name.substring(pos + 13);
            name = name.substring(0, name.indexOf(":"));
            if (zoneName.equals(name)) {
                return address;
            }
        }
        return null;
    }

    public List<Address> getRuntimeAddressesInZone(String zoneName, View view) {
        List<Address> runtimes = new ArrayList<Address>();
        for (Address address : view.getMembers()) {
            String name = UUID.get(address);
            if (name == null) {
                continue;
            }
            int pos = name.indexOf(":participant");
            if (pos < 0) {
                continue;
            }
            if (name.substring(pos + 12).startsWith(":" + zoneName + ":")) {
                runtimes.add(address);
            }
        }
        return runtimes;
    }

    public String getZoneName(Address address) {
        String name = UUID.get(address);
        if (name == null) {
            return null;
        }
        int pos = name.indexOf(":participant:");
        if (pos < 0) {
            return null;
        }
        return name.substring(pos + 13, name.lastIndexOf(":"));
    }

    public Address getRuntimeAddress(String runtimeName, View view) {
        for (Address address : view.getMembers()) {
            String name = UUID.get(address);
            if (runtimeName.equals(name)) {
                return address;
            }
        }
        return null;
    }

    public Object deserialize(byte[] payload) throws MessageException {
        MultiClassLoaderObjectInputStream ois = null;
        try {
            InputStream stream = new ByteArrayInputStream(payload);
            // Deserialize the command set. As command set classes may be loaded in an extension classloader, use a MultiClassLoaderObjectInputStream
            // to deserialize classes in the appropriate classloader.
            ois = new MultiClassLoaderObjectInputStream(stream, classLoaderRegistry);
            return ois.readObject();
        } catch (IOException e) {
            throw new MessageException(e);
        } catch (ClassNotFoundException e) {
            throw new MessageException(e);
        } finally {
            try {
                if (ois != null) {
                    ois.close();
                }
            } catch (IOException e) {
                // ignore;
            }
        }
    }

    public byte[] serialize(Serializable object) throws MessageException {
        try {
            ByteArrayOutputStream bas = new ByteArrayOutputStream();
            MultiClassLoaderObjectOutputStream stream;
            stream = new MultiClassLoaderObjectOutputStream(bas);
            stream.writeObject(object);
            stream.close();
            return bas.toByteArray();
        } catch (IOException e) {
            throw new MessageException(e);
        }
    }

    public Set<Address> getNewRuntimes(View oldView, View newView) {
        Set<Address> newRuntimes = new HashSet<Address>(newView.getMembers());
        if (oldView != null) {
            newRuntimes.removeAll(oldView.getMembers());
        }
        return newRuntimes;
    }

    /**
     * Returns the set of new zone leaders that came online from the previous view
     *
     * @param oldView the old view
     * @param newView the new view
     * @return the new zones
     */
    public Set<Address> getNewZoneLeaders(View oldView, View newView) {
        Set<Address> newZoneLeaders = new HashSet<Address>();
        for (Address address : newView.getMembers()) {
            if (oldView == null) {
                String zone = getZoneName(address);
                if (zone != null && address.equals(getZoneLeader(zone, newView))) {
                    newZoneLeaders.add(address);
                }
            } else if (!oldView.getMembers().contains(address)) {
                String zone = getZoneName(address);
                if (zone != null && address.equals(getZoneLeader(zone, newView))) {
                    newZoneLeaders.add(address);
                }
            } else {
                String zone = getZoneName(address);
                if (zone == null) {
                    continue;
                }
                Address oldLeader = getZoneLeader(zone, oldView);
                Address newLeader = getZoneLeader(zone, newView);
                if (!newLeader.equals(oldLeader)) {
                    newZoneLeaders.add(address);
                }
            }
        }
        return newZoneLeaders;
    }
}
