/*
 * Fabric3
 * Copyright (c) 2009-2013 Metaform Systems
 *
 * Fabric3 is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of
 * the License, or (at your option) any later version, with the
 * following exception:
 *
 * Linking this software statically or dynamically with other
 * modules is making a combined work based on this software.
 * Thus, the terms and conditions of the GNU General Public
 * License cover the whole combination.
 *
 * As a special exception, the copyright holders of this software
 * give you permission to link this software with independent
 * modules to produce an executable, regardless of the license
 * terms of these independent modules, and to copy and distribute
 * the resulting executable under terms of your choice, provided
 * that you also meet, for each linked independent module, the
 * terms and conditions of the license of that module. An
 * independent module is a module which is not derived from or
 * based on this software. If you modify this software, you may
 * extend this exception to your version of the software, but
 * you are not obligated to do so. If you do not wish to do so,
 * delete this exception statement from your version.
 *
 * Fabric3 is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the
 * GNU General Public License along with Fabric3.
 * If not, see <http://www.gnu.org/licenses/>.
*/
package org.fabric3.monitor.impl.destination;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.List;

import org.fabric3.api.annotation.monitor.MonitorLevel;
import org.fabric3.monitor.spi.destination.MonitorDestination;
import org.fabric3.monitor.spi.event.MonitorEventEntry;
import org.fabric3.monitor.spi.appender.Appender;
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

    public void start() throws IOException {
        for (Appender appender : appenders) {
            appender.start();
        }
    }

    public void stop() throws IOException {
        for (Appender appender : appenders) {
            appender.stop();
        }
    }

    public void write(MonitorEventEntry entry) throws IOException {
        ByteBuffer buffer = entry.getBuffer();
        MonitorLevel level = entry.getLevel();

        long entryTimestamp = entry.getEntryTimestamp();
        int count = eventWriter.writePrefix(level, entryTimestamp, buffer);
        count = count + eventWriter.writeTemplate(entry);
        buffer.put(NEWLINE);
        count++;

        buffer.limit(count);
        write(buffer);
    }

    public void write(MonitorLevel level, long timestamp, String source, String template, Object... args) throws IOException {
        ByteBuffer buffer = ByteBuffer.allocate(capacity);
        eventWriter.write(level, timestamp, template, buffer, args);
        write(buffer);
    }

    private void write(ByteBuffer buffer) throws IOException {
        for (Appender appender : appenders) {
            buffer.position(0);
            appender.write(buffer);
        }
    }

}
