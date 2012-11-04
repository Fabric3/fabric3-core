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
*/
package org.fabric3.federation.jgroups;

import java.io.Serializable;
import java.util.List;
import java.util.Set;

import org.jgroups.Address;
import org.jgroups.View;

import org.fabric3.spi.federation.MessageException;

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
     * Calculates the set of new zone leaders from the two views.
     *
     * @param oldView the old view
     * @param newView the new view
     * @return the set of new leaders
     */
    Set<Address> getNewZoneLeaders(View oldView, View newView);
}
