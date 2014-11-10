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
package org.fabric3.monitor.impl.writer;

import java.nio.ByteBuffer;

import junit.framework.TestCase;
import org.easymock.EasyMock;
import org.fabric3.api.annotation.monitor.MonitorLevel;
import org.fabric3.monitor.spi.buffer.ResizableByteBuffer;
import org.fabric3.monitor.spi.buffer.ResizableByteBufferMonitor;
import org.fabric3.monitor.spi.event.MonitorEventEntry;

/**
 *
 */
public class EventWriterImplTestCase extends TestCase {
    private EventWriterImpl eventWriter;

    private long timestamp;
    private ResizableByteBuffer buffer;

    public void testWriteString() throws Exception {
        eventWriter.write(MonitorLevel.SEVERE, timestamp, "This is a {0}", buffer, new Object[]{"test"});
        String string = new String(buffer.array());
        assertTrue(string.contains("SEVERE"));
        assertTrue(string.contains("This is a test"));
    }

    public void testWriteNumeric() throws Exception {
        eventWriter.write(MonitorLevel.SEVERE, timestamp, "This is a {0}", buffer, new Object[]{1});
        String string = new String(buffer.array());
        assertTrue(string.contains("SEVERE"));
        assertTrue(string.contains("This is a 1"));
    }

    public void testWriteBoolean() throws Exception {
        eventWriter.write(MonitorLevel.SEVERE, timestamp, "This is a {0}", buffer, new Object[]{true});
        String string = new String(buffer.array());
        assertTrue(string.contains("SEVERE"));
        assertTrue(string.contains("This is a true"));
    }

    public void testEmptyTemplate() throws Exception {
        eventWriter.write(MonitorLevel.SEVERE, timestamp, null, buffer, null);
        String string = new String(buffer.array());
        assertTrue(string.contains("SEVERE"));
    }

    public void testWritePrefix() throws Exception {
        eventWriter.writePrefix(MonitorLevel.SEVERE, timestamp, buffer);
        String string = new String(buffer.array());
        assertTrue(string.contains("SEVERE"));
    }

    public void testWriteTemplate() throws Exception {
        MonitorEventEntry entry = new MonitorEventEntry(2000, EasyMock.createNiceMock(ResizableByteBufferMonitor.class));
        entry.setTemplate("This is a {0}");
        entry.getEntries()[0].setObjectValue("test");
        entry.setLimit(0);
        int written = eventWriter.writeTemplate(entry);
        entry.getBuffer().limit(written);
        entry.getBuffer().flip();
        byte[] bytes = new byte[written];
        entry.getBuffer().get(bytes);
        String string = new String(bytes);
        assertTrue(string.contains("This is a test"));
    }

    public void setUp() throws Exception {
        super.setUp();
        EventWriterMonitor writerMonitor = EasyMock.createNiceMock(EventWriterMonitor.class);
        eventWriter = new EventWriterImpl(writerMonitor);
        eventWriter.init();

        timestamp = System.currentTimeMillis();
        buffer = new ResizableByteBuffer(ByteBuffer.allocate(200));

    }

}
