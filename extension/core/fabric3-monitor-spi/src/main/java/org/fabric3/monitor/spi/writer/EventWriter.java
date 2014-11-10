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
package org.fabric3.monitor.spi.writer;

import org.fabric3.api.annotation.monitor.MonitorLevel;
import org.fabric3.monitor.spi.buffer.ResizableByteBuffer;
import org.fabric3.monitor.spi.event.MonitorEventEntry;

/**
 * Writes monitor events to a buffer.
 */
public interface EventWriter {

    /**
     * Writes the event in character form into the buffer.
     *
     * @param level     the monitor level
     * @param timestamp the timestamp
     * @param template  the template
     * @param buffer    the buffer to write into
     * @param args      the arguments
     */
    void write(MonitorLevel level, long timestamp, String template, ResizableByteBuffer buffer, Object[] args);

    /**
     * Writes the event in character form into the entry buffer.
     *
     * @param entry the entry
     * @return the number of bytes written
     */
    int writeTemplate(MonitorEventEntry entry);

    /**
     * Writes the monitor even prefix characters.
     *
     * @param level          the monitor level to write
     * @param entryTimestamp the timestamp
     * @param buffer         the buffer to write into
     * @return the number of bytes written
     */
    int writePrefix(MonitorLevel level, long entryTimestamp, ResizableByteBuffer buffer);
}
