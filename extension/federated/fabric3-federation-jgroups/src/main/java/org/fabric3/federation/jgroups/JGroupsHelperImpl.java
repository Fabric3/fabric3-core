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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.fabric3.api.host.Fabric3Exception;
import org.fabric3.spi.classloader.ClassLoaderRegistry;
import org.fabric3.spi.classloader.MultiClassLoaderObjectInputStream;
import org.fabric3.spi.classloader.MultiClassLoaderObjectOutputStream;
import org.jgroups.Address;
import org.jgroups.View;
import org.jgroups.util.UUID;
import org.oasisopen.sca.annotation.Property;
import org.oasisopen.sca.annotation.Reference;

/**
 *
 */
public class JGroupsHelperImpl implements JGroupsHelper {
    private ClassLoaderRegistry classLoaderRegistry;
    private String runtimeType = "node";

    public JGroupsHelperImpl(@Reference ClassLoaderRegistry classLoaderRegistry) {
        this.classLoaderRegistry = classLoaderRegistry;
    }

    @Property(required = false)
    public void setRuntimeType(String runtimeType) {
        this.runtimeType = runtimeType;
    }

    public Address getZoneLeader(String zoneName, View view) {
        for (Address address : view.getMembers()) {
            String name = UUID.get(address);
            if (name == null) {
                return null;
            }
            int pos = name.indexOf(":" + runtimeType + ":");
            if (pos < 0) {
                continue;
            }
            name = name.substring(pos + runtimeType.length() + 2);
            name = name.substring(0, name.indexOf(":"));
            if (zoneName.equals(name)) {
                return address;
            }
        }
        return null;
    }

    public List<Address> getRuntimeAddressesInZone(String zoneName, View view) {
        List<Address> runtimes = new ArrayList<>();
        for (Address address : view.getMembers()) {
            String name = UUID.get(address);
            if (name == null) {
                continue;
            }
            int pos = name.indexOf(":" + runtimeType);
            if (pos < 0) {
                continue;
            }
            if (name.substring(pos + runtimeType.length() + 1).startsWith(":" + zoneName + ":")) {
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
        int pos = name.indexOf(":" + runtimeType + ":");
        if (pos < 0) {
            return null;
        }
        return name.substring(pos + runtimeType.length() + 2, name.lastIndexOf(":"));
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

    public Object deserialize(byte[] payload) throws Fabric3Exception {
        MultiClassLoaderObjectInputStream ois = null;
        try {
            InputStream stream = new ByteArrayInputStream(payload);
            // Deserialize the command set. As command set classes may be loaded in an extension classloader, use a MultiClassLoaderObjectInputStream
            // to deserialize classes in the appropriate classloader.
            ois = new MultiClassLoaderObjectInputStream(stream, classLoaderRegistry);
            return ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            throw new Fabric3Exception(e);
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

    public byte[] serialize(Serializable object) throws Fabric3Exception {
        try {
            ByteArrayOutputStream bas = new ByteArrayOutputStream();
            MultiClassLoaderObjectOutputStream stream;
            stream = new MultiClassLoaderObjectOutputStream(bas);
            stream.writeObject(object);
            stream.close();
            return bas.toByteArray();
        } catch (IOException e) {
            throw new Fabric3Exception(e);
        }
    }

    public Set<Address> getNewRuntimes(View oldView, View newView) {
        List<Address> members = newView.getMembers();
        if (members == null) {
            return Collections.emptySet();
        }
        Set<Address> newRuntimes = new HashSet<>(members);
        if (oldView != null) {
            newRuntimes.removeAll(oldView.getMembers());
        }
        return newRuntimes;
    }

    public Set<Address> getRemovedRuntimes(View oldView, View newView) {
        if (oldView == null) {
            return Collections.emptySet();
        }
        List<Address> members = oldView.getMembers();
        if (members == null) {
            return Collections.emptySet();
        }
        Set<Address> removedRuntimes = new HashSet<>(members);
        if (oldView != null) {
            removedRuntimes.removeAll(newView.getMembers());
        }
        return removedRuntimes;
    }

    public Set<Address> getNewZoneLeaders(View oldView, View newView) {
        Set<Address> newZoneLeaders = new HashSet<>();
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
