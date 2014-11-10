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
import org.fabric3.api.annotation.monitor.MonitorLevel;
import org.fabric3.monitor.spi.buffer.ResizableByteBuffer;

/**
 *
 */
public class MonitorLevelWriterTestCase extends TestCase {

    public void testWriteSevere() throws Exception {
        ResizableByteBuffer buffer = new ResizableByteBuffer(ByteBuffer.allocateDirect(6));

        MonitorLevelWriter.write(MonitorLevel.SEVERE, buffer);

        buffer.flip();
        byte[] serialized = new byte[buffer.limit()];
        buffer.get(serialized);
        assertEquals("SEVERE", new String(serialized));
    }

    public void testWriteInfo() throws Exception {
        ResizableByteBuffer buffer = new ResizableByteBuffer(ByteBuffer.allocateDirect(4));

        MonitorLevelWriter.write(MonitorLevel.INFO, buffer);

        buffer.flip();
        byte[] serialized = new byte[buffer.limit()];
        buffer.get(serialized);
        assertEquals("INFO", new String(serialized));
    }

    public void testWriteDebug() throws Exception {
        ResizableByteBuffer buffer = new ResizableByteBuffer(ByteBuffer.allocateDirect(5));

        MonitorLevelWriter.write(MonitorLevel.DEBUG, buffer);

        buffer.flip();
        byte[] serialized = new byte[buffer.limit()];
        buffer.get(serialized);
        assertEquals("DEBUG", new String(serialized));
    }

    public void testWriteWarning() throws Exception {
        ResizableByteBuffer buffer = new ResizableByteBuffer(ByteBuffer.allocateDirect(7));

        MonitorLevelWriter.write(MonitorLevel.WARNING, buffer);

        buffer.flip();
        byte[] serialized = new byte[buffer.limit()];
        buffer.get(serialized);
        assertEquals("WARNING", new String(serialized));
    }

    public void testWriteTrace() throws Exception {
        ResizableByteBuffer buffer = new ResizableByteBuffer(ByteBuffer.allocateDirect(5));

        MonitorLevelWriter.write(MonitorLevel.TRACE, buffer);

        buffer.flip();
        byte[] serialized = new byte[buffer.limit()];
        buffer.get(serialized);
        assertEquals("TRACE", new String(serialized));
    }

}
