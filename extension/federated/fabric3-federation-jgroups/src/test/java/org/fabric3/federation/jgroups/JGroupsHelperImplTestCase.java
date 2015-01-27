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

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import junit.framework.TestCase;
import org.easymock.EasyMock;
import org.fabric3.spi.classloader.ClassLoaderRegistry;
import org.jgroups.Address;
import org.jgroups.View;
import org.jgroups.ViewId;
import org.jgroups.util.UUID;

/**
 *
 */
public class JGroupsHelperImplTestCase extends TestCase {
    JGroupsHelperImpl helper;

    org.jgroups.stack.IpAddress coord_addr = new org.jgroups.stack.IpAddress(9999);

    public void testZoneName() throws Exception {
        helper.setRuntimeType("node");
        UUID address = UUID.randomUUID();
        UUID.add(address, "domain:node:zone1:1");

        assertEquals("zone1", helper.getZoneName(address));
    }

    public void testZoneLeaderNodeNaming() throws Exception {
        helper.setRuntimeType("node");
        ViewId id = new ViewId(coord_addr, 123);
        List<Address> members = new ArrayList<>();
        UUID address1 = UUID.randomUUID();
        UUID.add(address1, "domain:node:zone1:1");
        UUID address2 = UUID.randomUUID();
        UUID.add(address2, "domain:node:zone1:2");
        UUID address3 = UUID.randomUUID();
        UUID.add(address3, "domain:node:zone2:3");
        members.add(address1);
        members.add(address2);
        members.add(address3);
        View view = new View(id, members);

        Address zone1Leader = helper.getZoneLeader("zone1", view);
        Address zone2Leader = helper.getZoneLeader("zone2", view);
        assertEquals(address1, zone1Leader);
        assertEquals(address3, zone2Leader);
    }

    public void testZoneLeader() throws Exception {
        ViewId id = new ViewId(coord_addr, 123);
        List<Address> members = new ArrayList<>();
        UUID address1 = UUID.randomUUID();
        UUID.add(address1, "domain:node:zone1:1");
        UUID address2 = UUID.randomUUID();
        UUID.add(address2, "domain:node:zone1:2");
        UUID address3 = UUID.randomUUID();
        UUID.add(address3, "domain:node:zone2:3");
        members.add(address1);
        members.add(address2);
        members.add(address3);
        View view = new View(id, members);

        Address zone1Leader = helper.getZoneLeader("zone1", view);
        Address zone2Leader = helper.getZoneLeader("zone2", view);
        assertEquals(address1, zone1Leader);
        assertEquals(address3, zone2Leader);
    }

    public void testRuntimeAddressesInZoneNodeNaming() throws Exception {
        helper.setRuntimeType("node");
        ViewId id = new ViewId(coord_addr, 123);
        List<Address> members = new ArrayList<>();
        UUID address1 = UUID.randomUUID();
        UUID.add(address1, "domain:node:zone1:1");
        UUID address2 = UUID.randomUUID();
        UUID.add(address2, "domain:node:zone1:2");
        UUID address3 = UUID.randomUUID();
        UUID.add(address3, "domain:node:zone2:3");
        members.add(address1);
        members.add(address2);
        members.add(address3);
        View view = new View(id, members);

        List<Address> addresses = helper.getRuntimeAddressesInZone("zone1", view);
        assertEquals(2, addresses.size());
        for (Address leader : addresses) {
            assertTrue(leader.equals(address1) || leader.equals(address2));
        }
    }

    public void testRuntimeAddressesInZone() throws Exception {
        ViewId id = new ViewId(coord_addr, 123);
        List<Address> members = new ArrayList<>();
        UUID address1 = UUID.randomUUID();
        UUID.add(address1, "domain:node:zone1:1");
        UUID address2 = UUID.randomUUID();
        UUID.add(address2, "domain:node:zone1:2");
        UUID address3 = UUID.randomUUID();
        UUID.add(address3, "domain:node:zone2:3");
        members.add(address1);
        members.add(address2);
        members.add(address3);
        View view = new View(id, members);

        List<Address> addresses = helper.getRuntimeAddressesInZone("zone1", view);
        assertEquals(2, addresses.size());
        for (Address leader : addresses) {
            assertTrue(leader.equals(address1) || leader.equals(address2));
        }
    }

    public void testNewRuntimes() throws Exception {
        ViewId oldId = new ViewId(coord_addr, 123);
        ViewId newId = new ViewId(coord_addr, 456);
        List<Address> newMembers = new ArrayList<>();
        List<Address> oldMembers = new ArrayList<>();
        UUID address1 = UUID.randomUUID();
        UUID address2 = UUID.randomUUID();
        oldMembers.add(address1);
        newMembers.add(address2);
        newMembers.add(address1);
        View oldView = new View(oldId, oldMembers);
        View newView = new View(newId, newMembers);

        Set<Address> newRuntimes = helper.getNewRuntimes(oldView, newView);
        assertEquals(1, newRuntimes.size());
        assertEquals(address2, newRuntimes.iterator().next());
    }

    public void testRemovedRuntimes() throws Exception {
        ViewId oldId = new ViewId(coord_addr, 123);
        ViewId newId = new ViewId(coord_addr, 456);
        List<Address> newMembers = new ArrayList<>();
        List<Address> oldMembers = new ArrayList<>();
        UUID address1 = UUID.randomUUID();
        UUID address2 = UUID.randomUUID();
        oldMembers.add(address1);
        oldMembers.add(address2);
        newMembers.add(address2);
        View oldView = new View(oldId, oldMembers);
        View newView = new View(newId, newMembers);

        Set<Address> removedRuntimes = helper.getRemovedRuntimes(oldView, newView);
        assertEquals(1, removedRuntimes.size());
        assertEquals(address1, removedRuntimes.iterator().next());
    }

    public void testRemovedRuntimesOnBootstrap() throws Exception {
        ViewId newId = new ViewId(coord_addr, 456);
        List<Address> newMembers = new ArrayList<>();
        UUID address1 = UUID.randomUUID();
        UUID address2 = UUID.randomUUID();
        newMembers.add(address1);
        newMembers.add(address2);
        View newView = new View(newId, newMembers);

        Set<Address> removedRuntimes = helper.getRemovedRuntimes(null, newView);
        assertTrue(removedRuntimes.isEmpty());
    }

    public void testNewRuntimesOnBootstrap() throws Exception {
        ViewId newId = new ViewId(coord_addr, 456);
        List<Address> newMembers = new ArrayList<>();
        UUID address1 = UUID.randomUUID();
        UUID address2 = UUID.randomUUID();
        newMembers.add(address1);
        newMembers.add(address2);
        View newView = new View(newId, newMembers);

        // old view is null as a previous view did not exist
        Set<Address> newRuntimes = helper.getNewRuntimes(null, newView);
        assertEquals(2, newRuntimes.size());
    }

    public void testNoNewRuntimes() throws Exception {
        ViewId oldId = new ViewId(coord_addr, 123);
        ViewId newId = new ViewId(coord_addr, 456);
        List<Address> newMembers = new ArrayList<>();
        List<Address> oldMembers = new ArrayList<>();
        UUID address1 = UUID.randomUUID();
        UUID address2 = UUID.randomUUID();
        oldMembers.add(address1);
        oldMembers.add(address2);
        newMembers.add(address2);
        newMembers.add(address1);
        View oldView = new View(oldId, oldMembers);
        View newView = new View(newId, newMembers);

        Set<Address> newRuntimes = helper.getNewRuntimes(oldView, newView);
        assertEquals(0, newRuntimes.size());
    }

    public void testNewZoneLeaders() throws Exception {
        ViewId oldId = new ViewId(coord_addr, 123);
        ViewId newId = new ViewId(coord_addr, 456);
        List<Address> newMembers = new ArrayList<>();
        List<Address> oldMembers = new ArrayList<>();
        UUID address1 = UUID.randomUUID();
        UUID.add(address1, "domain:node:zone:1");
        UUID address2 = UUID.randomUUID();
        UUID.add(address2, "domain:node:zone:2");
        UUID address3 = UUID.randomUUID();
        UUID.add(address3, "domain:node:zone:3");
        UUID address4 = UUID.randomUUID();
        UUID.add(address4, "domain:node:zone2:3");
        oldMembers.add(address1);
        newMembers.add(address2);
        newMembers.add(address3);
        newMembers.add(address4);
        View oldView = new View(oldId, oldMembers);
        View newView = new View(newId, newMembers);

        Set<Address> leaders = helper.getNewZoneLeaders(oldView, newView);
        assertEquals(2, leaders.size());
        for (Address leader : leaders) {
            assertTrue(leader.equals(address2) || leader.equals(address4));
        }
    }

    public void testNewZoneLeadersOnBootstrap() throws Exception {
        ViewId newId = new ViewId(coord_addr, 456);
        List<Address> newMembers = new ArrayList<>();
        UUID address1 = UUID.randomUUID();
        UUID.add(address1, "domain:node:zone:1");
        UUID address2 = UUID.randomUUID();
        UUID.add(address2, "domain:node:zone:2");
        UUID address3 = UUID.randomUUID();
        UUID.add(address3, "domain:node:zone:3");
        UUID address4 = UUID.randomUUID();
        UUID.add(address4, "domain:node:zone2:3");
        newMembers.add(address2);
        newMembers.add(address3);
        newMembers.add(address4);
        View newView = new View(newId, newMembers);

        // old view is null as a previous view did not exist
        Set<Address> leaders = helper.getNewZoneLeaders(null, newView);
        assertEquals(2, leaders.size());
        for (Address leader : leaders) {
            assertTrue(leader.equals(address2) || leader.equals(address4));
        }
    }

    protected void setUp() throws Exception {
        super.setUp();
        ClassLoaderRegistry registry = EasyMock.createNiceMock(ClassLoaderRegistry.class);
        helper = new JGroupsHelperImpl(registry);
    }

}