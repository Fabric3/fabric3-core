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

import java.util.TimeZone;

import org.fabric3.api.annotation.monitor.Monitor;
import org.fabric3.api.annotation.monitor.MonitorLevel;
import org.fabric3.monitor.spi.buffer.ResizableByteBuffer;
import org.fabric3.monitor.spi.event.MonitorEventEntry;
import org.fabric3.monitor.spi.event.ParameterEntry;
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
    private String pattern = "%d.%m.%Y %H:%i:%s.%F";
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

    public void write(MonitorLevel level, long timestamp, String template, ResizableByteBuffer buffer, Object[] args) {
        int bytesWritten = 0;
        bytesWritten = bytesWritten + writePrefix(level, timestamp, buffer);
        bytesWritten = bytesWritten + writeTemplate(template, args, buffer);
        buffer.put(NEWLINE);
        bytesWritten++;
        buffer.limit(bytesWritten);
    }

    public int writePrefix(MonitorLevel level, long timestamp, ResizableByteBuffer buffer) {
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
        ResizableByteBuffer buffer = entry.getBuffer();
        int bytesWritten = 0;
        int counter = 0;
        ParameterEntry[] entries = entry.getEntries();
        for (int i = 0; i < template.length(); i++) {
            char current = template.charAt(i);
            if (entry.isParse() && '{' == current) {
                if (counter > entry.getLimit()) {
                    throw new ServiceRuntimeException("Monitor message contains more parameters than are supplied by the method interface: " + template);
                }
                ParameterEntry parameterEntry = entries[counter];
                bytesWritten = bytesWritten + writeParameter(parameterEntry, buffer);
                int skip = 0;
                while (template.charAt(i + skip) != '}') {
                    skip++;   // skip the formatting information contained in {..}
                }
                i = i + skip;
                counter++;
            } else {
                bytesWritten++;
                buffer.put((byte) current);
            }
        }
        if (counter < entry.getLimit()) {
            ParameterEntry last = entries[entry.getLimit() - 1];
            if (ParameterEntry.Slot.OBJECT == last.getSlot() && last.getObjectValue(Object.class) instanceof Throwable) {
                bytesWritten = bytesWritten + ObjectWriter.write(last.getObjectValue(Object.class), buffer);
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

    private int writeTemplate(String template, Object[] args, ResizableByteBuffer buffer) {
        if (template == null) {
            return 0;
        }

        int bytesWritten = 0;
        int counter = 0;
        for (int i = 0; i < template.length(); i++) {
            char current = template.charAt(i);
            if ('{' == current && args.length > 0) {
                if (args == null || counter >= args.length) {
                    throw new ServiceRuntimeException("Monitor message contains more parameters than are supplied by the method interface: " + template);
                }
                bytesWritten = bytesWritten + writeParameter(args[counter], buffer);
                int skip = 0;
                while (template.charAt(i + skip) != '}') {
                    skip++;   // skip the formatting information contained in {..}
                }
                i = i + skip;
                counter++;
            } else {
                bytesWritten++;
                buffer.put((byte) current);
            }
        }
        if (args != null && counter < args.length) {
            // case where an exception is passed as a param without a param {} marker
            bytesWritten = bytesWritten + writeParameter(args[counter], buffer);
        }
        return bytesWritten;
    }

    private int writeParameter(ParameterEntry parameterEntry, ResizableByteBuffer buffer) {
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
            case NONE:
                break;
        }
        return count;
    }

    private int writeParameter(Object arg, ResizableByteBuffer buffer) {
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
            return ObjectWriter.write(arg, buffer);
        }
    }

}
