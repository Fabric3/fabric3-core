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
package org.fabric3.monitor.impl.writer;

import java.nio.ByteBuffer;

import org.fabric3.api.annotation.monitor.MonitorLevel;
import org.oasisopen.sca.ServiceRuntimeException;

/**
 *
 */
public final class MonitorEntryWriter {
    private static final byte[] NEWLINE = "\n".getBytes();

    private MonitorEntryWriter() {
    }

    public static void write(MonitorLevel level, long timestamp, String template, ByteBuffer buffer, TimestampWriter timestampWriter, Object[] args) {
        int bytesWritten = 0;
        bytesWritten = bytesWritten + MonitorEntryWriter.writePrefix(level, timestamp, buffer, timestampWriter);
        bytesWritten = bytesWritten + MonitorEntryWriter.writeTemplate(template, args, buffer);
        buffer.put(NEWLINE);
        bytesWritten++;
        buffer.limit(bytesWritten);
    }

    public static int writePrefix(MonitorLevel level, long timestamp, ByteBuffer buffer, TimestampWriter timestampWriter) {
        int bytesWritten = 0;
        buffer.put((byte) '[');
        bytesWritten++;
        int written = MonitorLevelWriter.write(level, buffer);
        bytesWritten = bytesWritten + written;
        buffer.put((byte) ' ');
        bytesWritten++;

        bytesWritten = bytesWritten + timestampWriter.write(timestamp, buffer);

        buffer.put((byte) ']');
        bytesWritten++;

        buffer.put((byte) ' ');
        bytesWritten++;
        return bytesWritten;
    }

    private static int writeTemplate(String template, Object[] args, ByteBuffer buffer) {
        if (template == null) {
            return 0;
        }

        int bytesWritten = 0;
        int counter = 0;
        for (int i = 0; i < template.length(); i++) {
            char current = template.charAt(i);
            if ('{' == current) {
                if (counter >= args.length) {
                    throw new ServiceRuntimeException("Monitor message contains more parameters than are supplied by the method interface: " + template);
                }
                bytesWritten = bytesWritten + MonitorEntryWriter.writeParameter(args[counter], buffer);
                i = i + 2;    // skip two places
                counter++;
            } else {
                bytesWritten++;
                buffer.put((byte) current);
            }
        }
        return bytesWritten;
    }

    private static int writeParameter(Object arg, ByteBuffer buffer) {
        if (arg instanceof CharSequence) {
            return CharSequenceWriter.write((CharSequence) arg, buffer);
        } else if (arg instanceof Long) {
            return LongWriter.write((Long) arg, buffer);
        } else if (arg instanceof Integer) {
            return IntWriter.write((Integer) arg, buffer);
        } else if (arg instanceof Double) {
            return DoubleWriter.write((Double) arg, buffer);
        } else if (arg instanceof Boolean) {
            return BooleanWriter.write((Boolean) arg, buffer);
        } else {
            return 0;
        }
    }

}
