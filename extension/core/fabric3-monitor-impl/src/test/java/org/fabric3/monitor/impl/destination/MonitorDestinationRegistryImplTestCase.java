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
package org.fabric3.monitor.impl.destination;

import junit.framework.TestCase;
import org.easymock.EasyMock;
import org.fabric3.monitor.spi.buffer.ResizableByteBufferMonitor;
import org.fabric3.monitor.spi.destination.MonitorDestination;
import org.fabric3.monitor.spi.event.MonitorEventEntry;
import org.fabric3.spi.runtime.event.EventService;

/**
 *
 */
public class MonitorDestinationRegistryImplTestCase extends TestCase {

    public void testRegister() throws Exception {
        MonitorDestination destination = EasyMock.createMock(MonitorDestination.class);
        destination.write(EasyMock.isA(MonitorEventEntry.class));

        EasyMock.replay(destination);

        MonitorDestinationRegistryImpl registry = new MonitorDestinationRegistryImpl(EasyMock.createNiceMock(EventService.class));
        registry.init();

        registry.register(destination);
        registry.write(new MonitorEventEntry(1, EasyMock.createNiceMock(ResizableByteBufferMonitor.class)));

        EasyMock.verify(destination);
    }
}
