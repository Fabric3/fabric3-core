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

import java.nio.ByteBuffer;
import java.util.Collections;
import java.util.List;

import junit.framework.TestCase;
import org.easymock.EasyMock;
import org.fabric3.api.annotation.monitor.MonitorLevel;
import org.fabric3.monitor.spi.appender.Appender;
import org.fabric3.monitor.spi.buffer.ResizableByteBuffer;
import org.fabric3.monitor.spi.buffer.ResizableByteBufferMonitor;
import org.fabric3.monitor.spi.event.MonitorEventEntry;
import org.fabric3.monitor.spi.writer.EventWriter;

/**
 *
 */
public class DefaultMonitorDestinationTestCase extends TestCase {

    public void testWrite() throws Exception {
        MonitorEventEntry entry = new MonitorEventEntry(25, EasyMock.createNiceMock(ResizableByteBufferMonitor.class));
        entry.setLevel(MonitorLevel.SEVERE);
        entry.setTemplate("test");
        long timestamp = System.currentTimeMillis();
        entry.setEntryTimestamp(timestamp);

        EventWriter eventWriter = EasyMock.createMock(EventWriter.class);
        EasyMock.expect(eventWriter.writePrefix(EasyMock.eq(MonitorLevel.SEVERE),
                                                EasyMock.eq(timestamp),
                                                EasyMock.isA(ResizableByteBuffer.class))).andReturn(10);
        EasyMock.expect(eventWriter.writeTemplate(EasyMock.isA(MonitorEventEntry.class))).andReturn(10);

        Appender appender = EasyMock.createMock(Appender.class);
        appender.start();
        appender.write(EasyMock.isA(ByteBuffer.class));
        appender.stop();

        EasyMock.replay(eventWriter, appender);

        List<Appender> appenders = Collections.singletonList(appender);
        DefaultMonitorDestination destination = new DefaultMonitorDestination("test", eventWriter, 2000, appenders);
        destination.start();
        destination.write(entry);
        destination.stop();

        EasyMock.verify(eventWriter, appender);

    }
}
