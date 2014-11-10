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

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import org.fabric3.monitor.spi.buffer.ResizableByteBuffer;

/**
 * Writes an Object to a ByteBuffer.
 */
public final class ObjectWriter {
    private static final byte[] NEWLINE = "\n".getBytes();

    private ObjectWriter() {
    }

    public static int write(Object object, ResizableByteBuffer buffer) {
        if (object == null) {
            return 0;
        } else if (object instanceof Throwable) {
            Throwable t = (Throwable) object;
            ByteArrayOutputStream bas = new ByteArrayOutputStream();
            PrintStream printStream = new PrintStream(bas);
            t.printStackTrace(printStream);
            byte[] bytes = bas.toByteArray();
            buffer.put(NEWLINE);
            buffer.put(bytes);
            return bytes.length + NEWLINE.length;
        } else {
            return CharSequenceWriter.write(object.toString(), buffer);
        }
    }

}
