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
package org.fabric3.fabric.domain;

import java.net.URI;

import junit.framework.TestCase;
import org.easymock.EasyMock;
import org.fabric3.api.host.runtime.HostInfo;
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
        allocator.allocate(composite);

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
