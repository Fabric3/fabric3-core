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
 *
 * ----------------------------------------------------
 *
 * Portions originally based on Apache Tuscany 2007
 * licensed under the Apache 2.0 license.
 *
 */
package org.fabric3.fabric.host;

import junit.framework.TestCase;

import org.fabric3.spi.host.PortAllocationException;
import org.fabric3.spi.host.PortAllocator;

/**
 * @version $Rev: 10029 $ $Date: 2011-02-21 16:56:40 -0500 (Mon, 21 Feb 2011) $
 */
public class PortAllocatorImplTestCase extends TestCase {

    public void testPortAllocation() throws Exception {
        PortAllocatorImpl allocator = new PortAllocatorImpl();
        allocator.setRange("8900-8901");
        allocator.init();
        int port = allocator.allocate("http");
        assertTrue(port != PortAllocator.NOT_ALLOCATED);
        assertTrue(allocator.getAllocatedPorts().containsKey("http"));
        allocator.release("http");
        assertFalse(allocator.getAllocatedPorts().containsKey("http"));

        allocator.allocate("http");
        assertTrue(allocator.getAllocatedPorts().containsKey("http"));
    }

    public void testPortReserve() throws Exception {
        PortAllocatorImpl allocator = new PortAllocatorImpl();
        allocator.setRange("8900-8901");
        allocator.init();
        allocator.reserve("http", 8900);
        try {
            allocator.allocate("http");
            fail();
        } catch (PortTypeAllocatedException e) {
            //expected
        }
    }

    public void testPortAllocationNotConfigured() throws Exception {
        PortAllocatorImpl allocator = new PortAllocatorImpl();
        allocator.init();
        try {
            allocator.allocate("http");
            fail();
        } catch (PortAllocationException e) {
            // expected
        }
        assertFalse(allocator.getAllocatedPorts().containsKey("http"));
    }

    public void testPortReserveNotConfigured() throws Exception {
        PortAllocatorImpl allocator = new PortAllocatorImpl();
        allocator.init();
        allocator.reserve("http", 8900);
        assertTrue(PortAllocator.NOT_ALLOCATED != allocator.getAllocatedPort("http"));
    }

    public void testNoPortAvailable() throws Exception {
        PortAllocatorImpl allocator = new PortAllocatorImpl();
        allocator.setRange("8900-8900");
        allocator.init();
        int port = allocator.allocate("http");
        assertTrue(port != PortAllocator.NOT_ALLOCATED);
        assertTrue(allocator.getAllocatedPorts().containsKey("http"));
        try {
            allocator.allocate("https");
            fail();
        } catch (PortAllocationException e) {
            // expected
        }

        assertFalse(allocator.getAllocatedPorts().containsKey("https"));
    }


    public void testIllegalPortRange() throws Exception {
        PortAllocatorImpl allocator = new PortAllocatorImpl();
        try {
            allocator.setRange("8901-8900");
            allocator.init();
            fail();
        } catch (IllegalArgumentException e) {
            //expected
        }
    }

    public void testInvalidRangeSyntax() throws Exception {
        PortAllocatorImpl allocator = new PortAllocatorImpl();
        try {
            allocator.setRange("-8900-8901");
            allocator.init();
            fail();
        } catch (IllegalArgumentException e) {
            //expected
        }
    }

    public void testIsPoolEnabled() throws Exception {
        PortAllocatorImpl allocator = new PortAllocatorImpl();
        allocator.setRange("8900-8901");
        allocator.init();
        assertTrue(allocator.isPoolEnabled());
    }

    public void testIsPoolNotEnabled() throws Exception {
         PortAllocatorImpl allocator = new PortAllocatorImpl();
         allocator.init();
         assertFalse(allocator.isPoolEnabled());
     }

}
