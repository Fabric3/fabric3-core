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
public class LongWriterTestCase extends TestCase {

    public void testWritePositiveLong() throws Exception {
        ResizableByteBuffer buffer = new ResizableByteBuffer(ByteBuffer.allocateDirect(2));

        LongWriter.write(10l, buffer);

        buffer.flip();
        byte[] serialized = new byte[buffer.limit()];
        buffer.get(serialized);
        assertEquals("10", new String(serialized));

    }

    public void testWriteNegativeLong() throws Exception {
        ResizableByteBuffer buffer = new ResizableByteBuffer(ByteBuffer.allocateDirect(3));

        LongWriter.write(-10l, buffer);

        buffer.flip();
        byte[] serialized = new byte[buffer.limit()];
        buffer.get(serialized);
        assertEquals("-10", new String(serialized));

    }

    public void testWriteMinLong() throws Exception {
        ResizableByteBuffer buffer = new ResizableByteBuffer(ByteBuffer.allocateDirect(20));

        LongWriter.write(Long.MIN_VALUE, buffer);

        buffer.flip();
        byte[] serialized = new byte[buffer.limit()];
        buffer.get(serialized);
        assertEquals(String.valueOf(Long.MIN_VALUE), new String(serialized));
    }

    public void testWriteMaxLong() throws Exception {
        ResizableByteBuffer buffer = new ResizableByteBuffer(ByteBuffer.allocateDirect(19));

        LongWriter.write(Long.MAX_VALUE, buffer);

        buffer.flip();
        byte[] serialized = new byte[buffer.limit()];
        buffer.get(serialized);
        assertEquals(String.valueOf(Long.MAX_VALUE), new String(serialized));

    }

}
