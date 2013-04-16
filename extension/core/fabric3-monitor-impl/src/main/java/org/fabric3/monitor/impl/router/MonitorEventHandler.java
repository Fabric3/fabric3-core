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
package org.fabric3.monitor.impl.router;

import java.nio.ByteBuffer;

import com.lmax.disruptor.EventHandler;
import org.fabric3.api.annotation.monitor.MonitorLevel;
import org.fabric3.monitor.impl.destination.MonitorDestinationRegistry;
import org.fabric3.monitor.impl.writer.BooleanWriter;
import org.fabric3.monitor.impl.writer.ByteWriter;
import org.fabric3.monitor.impl.writer.CharWriter;
import org.fabric3.monitor.impl.writer.DoubleWriter;
import org.fabric3.monitor.impl.writer.FloatWriter;
import org.fabric3.monitor.impl.writer.IntWriter;
import org.fabric3.monitor.impl.writer.LongWriter;
import org.fabric3.monitor.impl.writer.MonitorEntryWriter;
import org.fabric3.monitor.impl.writer.ObjectWriter;
import org.fabric3.monitor.impl.writer.TimestampWriter;
import org.oasisopen.sca.ServiceRuntimeException;

/**
 * Receives events from the ring buffer and performs the actual routing to a destination.
 */
public class MonitorEventHandler implements EventHandler<MonitorEventEntry> {
    private static final byte[] NEWLINE = "\n".getBytes();
//    public static final int MIN = 100000;
//    public static final int MAX = 200000;

    private MonitorDestinationRegistry registry;
    private TimestampWriter timestampWriter;

//    private int counter;
//    private long elapsedTime;

    public MonitorEventHandler(MonitorDestinationRegistry registry, TimestampWriter timestampWriter) {
        this.registry = registry;
        this.timestampWriter = timestampWriter;
    }

    public void onEvent(MonitorEventEntry entry, long sequence, boolean endOfBatch) throws Exception {
        ByteBuffer buffer = entry.getBuffer();
        int index = entry.getDestinationIndex();
        MonitorLevel level = entry.getLevel();

        int count = MonitorEntryWriter.writePrefix(level, entry.getEntryTimestamp(), buffer, timestampWriter);
        count = count + writeTemplate(entry.getTemplate(), entry);
        buffer.put(NEWLINE) ;
        count++;

        buffer.limit(count);
        registry.write(index, buffer);

//        if (counter >= MIN) {
//            long time = System.nanoTime() - entry.getTimestampNanos();
//            elapsedTime = elapsedTime + time;
//        }
//        counter++;
//        if (counter == MAX) {
//            System.out.println("Time last event: " + (System.nanoTime() - entry.getTimestampNanos()));
//            System.out.println("Elapsed: " + elapsedTime);
//            System.out.println("Avg: " + (double) elapsedTime / (double) (MAX - MIN));
//        }
    }

    private int writeTemplate(String template, MonitorEventEntry entry) {
        if (template == null) {
            return 0;
        }
        ByteBuffer buffer = entry.getBuffer();
        int bytesWritten = 0;
        int counter = 0;
        for (int i = 0; i < template.length(); i++) {
            char current = template.charAt(i);
            if ('{' == current) {
                if (counter > entry.getLimit()) {
                    throw new ServiceRuntimeException("Monitor message contains more parameters than are supplied by the method interface: " + template);
                }
                ParameterEntry parameterEntry = entry.getEntries()[counter];
                bytesWritten = bytesWritten + writeParameter(parameterEntry, buffer);
                i = i + 2;    // skip two places
                counter++;
            } else {
                bytesWritten++;
                buffer.put((byte) current);
            }
        }
        return bytesWritten;
    }

    private int writeParameter(ParameterEntry parameterEntry, ByteBuffer buffer) {
        int count = 0;
        switch (parameterEntry.getSlot()) {
            case SHORT:
                count = count + IntWriter.write(parameterEntry.getShortValue(), buffer);
                break;
            case INT:
                count = count + IntWriter.write(parameterEntry.getIntValue(), buffer);
                break;
            case LONG:
                count = count + LongWriter.write(parameterEntry.getLongValue(), buffer);
                break;
            case DOUBLE:
                count = count + DoubleWriter.write(parameterEntry.getDoubleValue(), buffer);
                break;
            case FLOAT:
                count = count + FloatWriter.write(parameterEntry.getFloatValue(), buffer);
                break;
            case CHAR:
                count = count + CharWriter.write(parameterEntry.getCharValue(), buffer);
                break;
            case BOOLEAN:
                count = count + BooleanWriter.write(parameterEntry.getBooleanValue(), buffer);
                break;
            case BYTE:
                count = count + ByteWriter.write(parameterEntry.getByteValue(), buffer);
                break;
            case OBJECT:
                count = count + ObjectWriter.write(parameterEntry.getObjectValue(Object.class), buffer);
                break;
        }
        return count;
    }

}
