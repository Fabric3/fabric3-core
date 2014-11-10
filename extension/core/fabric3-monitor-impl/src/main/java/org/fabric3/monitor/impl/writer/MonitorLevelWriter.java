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

import org.fabric3.api.annotation.monitor.MonitorLevel;
import org.fabric3.monitor.spi.buffer.ResizableByteBuffer;

/**
 * Writes a {@link MonitorLevel} value in a character representation to a ByteBuffer without creating objects on the heap.
 */
public final class MonitorLevelWriter {
    private static final byte[] SEVERE = MonitorLevel.SEVERE.toString().getBytes();
    private static final byte[] DEBUG = MonitorLevel.DEBUG.toString().getBytes();
    private static final byte[] INFO = MonitorLevel.INFO.toString().getBytes();
    private static final byte[] TRACE = MonitorLevel.TRACE.toString().getBytes();
    private static final byte[] WARNING = MonitorLevel.WARNING.toString().getBytes();

    private MonitorLevelWriter() {
    }

    public static int write(MonitorLevel level, ResizableByteBuffer buffer) {
        switch (level) {
            case SEVERE:
                buffer.put(SEVERE);
                return 6;
            case WARNING:
                buffer.put(WARNING);
                return 7;
            case INFO:
                buffer.put(INFO);
                return 4;
            case DEBUG:
                buffer.put(DEBUG);
                return 5;
            case TRACE:
                buffer.put(TRACE);
                return 5;
        }
        throw new AssertionError();
    }

}
