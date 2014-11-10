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
public class BooleanWriterTestCase extends TestCase {

    public void testWriteTrue() throws Exception {
        ResizableByteBuffer buffer = new ResizableByteBuffer(ByteBuffer.allocateDirect(4));

        BooleanWriter.write(true, buffer);

        buffer.flip();
        byte[] serialized = new byte[buffer.limit()];
        buffer.get(serialized);
        assertEquals("true", new String(serialized));
    }

    public void testWriteFalse() throws Exception {
        ResizableByteBuffer buffer = new ResizableByteBuffer(ByteBuffer.allocateDirect(5));

        BooleanWriter.write(false, buffer);

        buffer.flip();
        byte[] serialized = new byte[buffer.limit()];
        buffer.get(serialized);
        assertEquals("false", new String(serialized));
    }

}
