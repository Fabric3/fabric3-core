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
import java.util.TimeZone;

import org.fabric3.api.annotation.monitor.Monitor;
import org.fabric3.api.annotation.monitor.MonitorLevel;
import org.fabric3.monitor.impl.router.MonitorEventEntry;
import org.fabric3.monitor.impl.router.ParameterEntry;
import org.fabric3.monitor.spi.writer.EventWriter;
import org.oasisopen.sca.ServiceRuntimeException;
import org.oasisopen.sca.annotation.Init;
import org.oasisopen.sca.annotation.Property;

/**
 *
 */
public class EventWriterImpl implements EventWriter {
    private static final byte[] NEWLINE = "\n".getBytes();

    private EventWriterMonitor monitor;

    private String timestampType = "formatted";
    private String pattern = "%d:%m:%Y %H:%i:%s.%F";
    private TimeZone timeZone = TimeZone.getDefault();

    private TimestampWriter timestampWriter;

    public EventWriterImpl(@Monitor EventWriterMonitor monitor) {
        this.monitor = monitor;
    }

    @Property(required = false)
    public void setPattern(String pattern) {
        this.pattern = pattern;
    }

    @Property(required = false)
    public void setTimeZone(String id) {
        this.timeZone = TimeZone.getTimeZone(id);
    }

    @Property(required = false)
    public void setTimestampFormat(String type) {
        this.timestampType = type;
    }

    @Init
    public void init() {
        initializeTimestampWriter();
    }

    public void write(MonitorLevel level, long timestamp, String template, ByteBuffer buffer, Object[] args) {
        int bytesWritten = 0;
        bytesWritten = bytesWritten + writePrefix(level, timestamp, buffer);
        bytesWritten = bytesWritten + writeTemplate(template, args, buffer);
        buffer.put(NEWLINE);
        bytesWritten++;
        buffer.limit(bytesWritten);
    }

    public int writePrefix(MonitorLevel level, long timestamp, ByteBuffer buffer) {
        int bytesWritten = 0;
        buffer.put((byte) '[');
        bytesWritten++;
        int written = MonitorLevelWriter.write(level, buffer);
        bytesWritten = bytesWritten + written;
        buffer.put((byte) ' ');
        bytesWritten++;

        written = timestampWriter.write(timestamp, buffer);
        if (written == 0) {
            buffer.position(buffer.position() - 1);
        } else {
            bytesWritten = bytesWritten + written;
        }

        buffer.put((byte) ']');
        bytesWritten++;

        buffer.put((byte) ' ');
        bytesWritten++;
        return bytesWritten;
    }

    public int writeTemplate(MonitorEventEntry entry) {
        String template = entry.getTemplate();
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

    private void initializeTimestampWriter() {
        if (timestampType.equals("formatted")) {
            timestampWriter = new FormattingTimestampWriter(pattern, timeZone);
        } else if (timestampType.equals("unformatted")) {
            timestampWriter = new LongTimestampWriter();
        } else if (timestampType.equals("none")) {
            timestampWriter = new NoOpTimestampWriter();
        } else {
            timestampWriter = new FormattingTimestampWriter(pattern, timeZone);
            monitor.invalidTimestampType(timestampType);
        }
    }


    private int writeTemplate(String template, Object[] args, ByteBuffer buffer) {
        if (template == null) {
            return 0;
        }

        int bytesWritten = 0;
        int counter = 0;
        for (int i = 0; i < template.length(); i++) {
            char current = template.charAt(i);
            if ('{' == current) {
                if (args == null || counter >= args.length) {
                    throw new ServiceRuntimeException("Monitor message contains more parameters than are supplied by the method interface: " + template);
                }
                bytesWritten = bytesWritten + writeParameter(args[counter], buffer);
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


    private int writeParameter(Object arg, ByteBuffer buffer) {
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
