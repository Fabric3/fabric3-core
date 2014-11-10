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
package org.fabric3.monitor.spi.buffer;

import java.nio.ByteBuffer;

import junit.framework.TestCase;
import org.easymock.EasyMock;

/**
 *
 */
public class ResizableByteBufferTestCase extends TestCase {

    public void testResize() throws Exception {
        ResizableByteBufferMonitor monitor = EasyMock.createNiceMock(ResizableByteBufferMonitor.class);
        monitor.bufferResize();
        EasyMock.replay(monitor);

        ResizableByteBuffer buffer = new ResizableByteBuffer(ByteBuffer.allocateDirect(1), monitor);
        buffer.put(1);
        buffer.put(2);
        buffer.put(3);

        assertEquals(1026, buffer.capacity());   // buffer re-sizes 1024 at a time
        assertEquals(1, buffer.getByteBuffer().get(0));
        assertEquals(2, buffer.getByteBuffer().get(1));
        assertEquals(3, buffer.getByteBuffer().get(2));

        EasyMock.verify(monitor);
    }
}
