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

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.fabric3.spi.federation.topology.MessageException;
import org.fabric3.spi.federation.topology.RuntimeInstance;
import org.fabric3.spi.federation.topology.Zone;
import org.jgroups.Address;
import org.jgroups.View;

/**
 * Helper service for the JGroups messaging layer.
 */
public interface JGroupsHelper {

    /**
     * Returns the controller for the domain view.
     *
     * @param view the current domain view
     * @return the controller or null if not present
     */
    Address getController(View view);

    /**
     * Returns the leader for the zone, which is the first runtime in the given view.
     *
     * @param zoneName the zone name
     * @param view     the domain view
     * @return the leader or null if no runtimes in the zone are present
     */
    Address getZoneLeader(String zoneName, View view);

    /**
     * Returns the addresses for runtimes in a zone.
     *
     * @param zoneName the zone name
     * @param view     the current domain view
     * @return the the addresses. If no runtimes are present, an empty list is returned.
     */
    List<Address> getRuntimeAddressesInZone(String zoneName, View view);

    /**
     * Returns the name of the zone the runtime is a member of.
     *
     * @param address the runtime address
     * @return the zone name or null if the runtime is not part of a zone (e.g. it is a controller)
     */
    String getZoneName(Address address);

    /**
     * Returns the address of the runtime with the given name.
     *
     * @param runtimeName the runtime name
     * @param view        the current domain view
     * @return the runtime address
     */
    Address getRuntimeAddress(String runtimeName, View view);

    /**
     * Deserializes a message payload.
     *
     * @param payload the message
     * @return the message payload
     * @throws MessageException if there is an error deserializing the payload
     */
    Object deserialize(byte[] payload) throws MessageException;

    /**
     * Serializes an object.
     *
     * @param object the object to serialize
     * @return the serialized bytes
     * @throws MessageException if there is an error serializing
     */
    byte[] serialize(Serializable object) throws MessageException;

    /**
     * Calculates the set of new runtimes from the two views.
     *
     * @param oldView the old view
     * @param newView the new view
     * @return the set of new runtimes
     */
    Set<Address> getNewRuntimes(View oldView, View newView);

    /**
     * Calculates the set of runtimes that were removed from the new view.
     *
     * @param oldView the old view
     * @param newView the new view
     * @return the set of removed runtimes
     */
    Set<Address> getRemovedRuntimes(View oldView, View newView);

    /**
     * Returns a list of zones in the given view
     *
     * @param runtimes the active runtimes where the key is the zone and the value is a map of runtime names to instances.
     * @return the list of zones
     */
    Set<Zone> getZones(Map<String, Map<String, RuntimeInstance>> runtimes);

    /**
     * Calculates the set of new zone leaders from the two views.
     *
     * @param oldView the old view
     * @param newView the new view
     * @return the set of new leaders
     */
    Set<Address> getNewZoneLeaders(View oldView, View newView);
}
