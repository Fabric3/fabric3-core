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
package org.fabric3.federation.allocator;

import java.net.URI;

import junit.framework.TestCase;
import org.easymock.EasyMock;
import org.fabric3.host.runtime.HostInfo;
import org.fabric3.spi.model.instance.LogicalChannel;
import org.fabric3.spi.model.instance.LogicalComponent;
import org.fabric3.spi.model.instance.LogicalCompositeComponent;
import org.fabric3.spi.model.instance.LogicalResource;

/**
 *
 */
public class NodeAllocatorTestCase extends TestCase {
    private NodeAllocator allocator;
    private LogicalCompositeComponent composite;

    public void testAllocate() throws Exception {
        allocator.allocate(composite, null);

        assertEquals("zone1", composite.getZone());
        for (LogicalComponent<?> component : composite.getComponents()) {
            assertEquals("zone1", component.getZone());
        }
        for (LogicalChannel channel : composite.getChannels()) {
            assertEquals("zone1", channel.getZone());
        }
        for (LogicalResource<?> resource : composite.getResources()) {
            assertEquals("zone1", resource.getZone());
        }
    }

    @SuppressWarnings("unchecked")
    public void setUp() throws Exception {
        super.setUp();
        HostInfo info = EasyMock.createMock(HostInfo.class);
        EasyMock.expect(info.getZoneName()).andReturn("zone1");
        EasyMock.replay(info);

        allocator = new NodeAllocator(info);

        composite = new LogicalCompositeComponent(URI.create("test"), null, null);
        LogicalComponent<?> component = new LogicalComponent(URI.create("test/child"), null, composite);
        composite.addComponent(component);
        LogicalChannel channel = new LogicalChannel(URI.create("test/channel"), null, composite);
        composite.addChannel(channel);
        LogicalResource resource = new LogicalResource(null, composite);
        composite.addResource(resource);
    }
}
