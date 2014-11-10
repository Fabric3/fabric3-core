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
package org.fabric3.monitor.impl.generator;

import java.util.Collections;
import java.util.Map;

import junit.framework.TestCase;
import org.easymock.EasyMock;
import org.fabric3.monitor.spi.destination.MonitorDestinationGenerator;
import org.fabric3.monitor.spi.model.physical.PhysicalMonitorDestinationDefinition;
import org.fabric3.monitor.spi.model.type.MonitorDestinationDefinition;
import org.fabric3.monitor.spi.model.type.MonitorResourceDefinition;
import org.fabric3.spi.model.instance.LogicalResource;

/**
 *
 */
public class MonitorGeneratorTestCase extends TestCase {

    @SuppressWarnings("unchecked")
    public void testBuild() throws Exception {
        MonitorDestinationGenerator destinationGenerator = EasyMock.createMock(MonitorDestinationGenerator.class);
        destinationGenerator.generateResource(EasyMock.isA(MockDefinition.class));
        EasyMock.expectLastCall().andReturn(new MockPhysicalDefinition());

        EasyMock.replay(destinationGenerator);

        MonitorResourceGenerator generator = new MonitorResourceGenerator();
        Map map = Collections.singletonMap(MockDefinition.class, destinationGenerator);
        generator.setDestinationGenerators(map);

        MockDefinition destinationDefinition = new MockDefinition();
        MonitorResourceDefinition resourceDefinition = new MonitorResourceDefinition("test");
        resourceDefinition.setDestinationDefinition(destinationDefinition);
        LogicalResource<MonitorResourceDefinition> resource = new LogicalResource<>(resourceDefinition, null);

        generator.generateResource(resource);

        EasyMock.verify(destinationGenerator);
    }

    private class MockDefinition extends MonitorDestinationDefinition {
        private static final long serialVersionUID = -3620668496377933326L;
    }

    private class MockPhysicalDefinition extends PhysicalMonitorDestinationDefinition {
        private static final long serialVersionUID = 6515557610052777779L;

        public MockPhysicalDefinition() {
            super("test");
        }
    }
}
