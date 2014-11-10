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
import org.fabric3.monitor.spi.buffer.ResizableByteBuffer;

/**
 *
 */
public class LongTimestampWriterTestCase extends TestCase {

    public void testWrite() throws Exception {
        LongTimestampWriter writer = new LongTimestampWriter();
        long timestamp = System.currentTimeMillis();

        String result = Long.toString(timestamp);

        ResizableByteBuffer buffer = new ResizableByteBuffer(ByteBuffer.allocate(result.length()));

        int written = writer.write(timestamp, buffer);

        buffer.flip();

        assertEquals(result.length(), written);
        assertEquals(result, new String(buffer.array()));
    }

    public void testNegativeWrite() throws Exception {
        LongTimestampWriter writer = new LongTimestampWriter();

        ResizableByteBuffer buffer = new ResizableByteBuffer(ByteBuffer.allocate(2));

        int written = writer.write(-1l, buffer);

        buffer.flip();

        assertEquals(2, written);
        assertEquals("-1", new String(buffer.array()));
    }

}
