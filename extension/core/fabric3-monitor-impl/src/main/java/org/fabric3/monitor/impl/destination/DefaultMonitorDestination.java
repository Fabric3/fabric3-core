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
package org.fabric3.monitor.impl.destination;

import java.nio.ByteBuffer;
import java.util.List;

import org.fabric3.api.annotation.monitor.MonitorLevel;
import org.fabric3.monitor.spi.appender.Appender;
import org.fabric3.monitor.spi.buffer.ResizableByteBuffer;
import org.fabric3.monitor.spi.destination.MonitorDestination;
import org.fabric3.monitor.spi.event.MonitorEventEntry;
import org.fabric3.monitor.spi.writer.EventWriter;

/**
 * Default {@link MonitorDestination} implementation that writes to a collection of {@link Appender}s.
 */
public class DefaultMonitorDestination implements MonitorDestination {
    private static final byte[] NEWLINE = "\n".getBytes();

    private String name;
    private EventWriter eventWriter;
    private Appender[] appenders;
    private int capacity;

    public DefaultMonitorDestination(String name, EventWriter eventWriter, int capacity, List<Appender> appenders) {
        this.name = name;
        this.eventWriter = eventWriter;
        this.capacity = capacity;
        this.appenders = appenders.toArray(new Appender[appenders.size()]);
    }

    public String getName() {
        return name;
    }

    public void start() {
        for (Appender appender : appenders) {
            appender.start();
        }
    }

    public void stop() {
        for (Appender appender : appenders) {
            appender.stop();
        }
    }

    public void write(MonitorEventEntry entry) {
        ResizableByteBuffer buffer = entry.getBuffer();
        MonitorLevel level = entry.getLevel();

        long entryTimestamp = entry.getEntryTimestamp();
        int count = eventWriter.writePrefix(level, entryTimestamp, buffer);
        count = count + eventWriter.writeTemplate(entry);
        buffer.put(NEWLINE);
        count++;

        buffer.limit(count);
        write(buffer);
    }

    public void write(MonitorLevel level, long timestamp, String source, String template, Object... args) {
        ByteBuffer buffer = ByteBuffer.allocate(capacity);
        ResizableByteBuffer wrapper = new ResizableByteBuffer(buffer);
        eventWriter.write(level, timestamp, template, wrapper, args);
        write(wrapper);
    }

    private void write(ResizableByteBuffer buffer)  {
        for (Appender appender : appenders) {
            buffer.position(0);
            appender.write(buffer.getByteBuffer());
        }
    }

}
