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
package org.fabric3.monitor.impl.builder;

import java.util.Collections;
import java.util.Map;

import junit.framework.TestCase;
import org.easymock.EasyMock;
import org.fabric3.monitor.spi.destination.MonitorDestinationBuilder;
import org.fabric3.monitor.spi.model.physical.PhysicalMonitor;
import org.fabric3.monitor.spi.model.physical.PhysicalMonitorDestination;

/**
 *
 */
public class MonitorBuilderTestCase extends TestCase {

    @SuppressWarnings("unchecked")
    public void testBuild() throws Exception {
        MonitorDestinationBuilder destinationBuilder = EasyMock.createMock(MonitorDestinationBuilder.class);
        destinationBuilder.build(EasyMock.isA(MockDestination.class));
        EasyMock.replay(destinationBuilder);

        MonitorBuilder builder = new MonitorBuilder();

        Map map = Collections.singletonMap(MockDestination.class, destinationBuilder);
        builder.setBuilders(map);

        PhysicalMonitor physicalMonitor = new PhysicalMonitor("test");
        physicalMonitor.setDestination(new MockDestination());

        builder.build(physicalMonitor);

        EasyMock.verify(destinationBuilder);

    }

    private class MockDestination extends PhysicalMonitorDestination {

        public MockDestination() {
            super("test");
        }
    }
}
