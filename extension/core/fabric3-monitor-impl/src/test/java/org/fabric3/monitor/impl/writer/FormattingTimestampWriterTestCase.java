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
import java.util.Calendar;
import java.util.TimeZone;

import junit.framework.TestCase;
import org.fabric3.monitor.spi.buffer.ResizableByteBuffer;

/**
 *
 */
public class FormattingTimestampWriterTestCase extends TestCase {

    public void testWriteTimestamp() throws Exception {
        FormattingTimestampWriter writer = new FormattingTimestampWriter("%Y:%m:%d %H:%i:%s.%F", TimeZone.getDefault());

        ResizableByteBuffer buffer = new ResizableByteBuffer(ByteBuffer.allocateDirect(100));


        Calendar calendar = Calendar.getInstance();
        calendar.set(2013, Calendar.JANUARY, 12, 11, 11, 11);

        int written = writer.write(calendar.getTimeInMillis(), buffer);

        buffer.flip();
        byte[] serialized = new byte[buffer.limit()];
        buffer.get(serialized);

        assertTrue(new String(serialized).contains("2013:01:12 11:11:11."));
        assertEquals(serialized.length, written);
    }
}
