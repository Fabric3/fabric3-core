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

import org.fabric3.monitor.spi.buffer.ResizableByteBuffer;

/**
 * Writes a CharSequence to a ByteBuffer without creating objects on the heap.
 */
public final class CharSequenceWriter {

    private CharSequenceWriter() {
    }

    public static int write(CharSequence value, ResizableByteBuffer buffer) {
        return write(value, 0, buffer);
    }

    public static int write(CharSequence value, int pos, ResizableByteBuffer buffer) {
        for (int i = pos; i < value.length(); i++) {
            buffer.put((byte) value.charAt(i));
        }
        return value.length();
    }

}
