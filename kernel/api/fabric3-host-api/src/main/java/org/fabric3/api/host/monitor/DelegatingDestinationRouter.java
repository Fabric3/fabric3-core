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
package org.fabric3.api.host.monitor;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.fabric3.api.annotation.monitor.MonitorLevel;
import org.oasisopen.sca.annotation.Reference;

/**
 * Enables lazy-loading of a monitor destination by caching messages received before the destination has been loaded.
 * <p/>
 * This is used to allow bootstrap components to send monitor messages before a destination router has been loaded from a runtime extension.
 */
public class DelegatingDestinationRouter implements DestinationRouter {
    private DestinationRouter delegate;
    private List<Entry> cache;

    public DelegatingDestinationRouter() {
        cache = new ArrayList<>();
    }

    @Reference(required = false)
    public void setDestination(DestinationRouter destination) {
        this.delegate = destination;
        for (Entry entry : cache) {
            delegate.send(entry.level, entry.destinationIndex, entry.timestamp, entry.source, entry.message, entry.parse, entry.values);
        }
        cache = null;
    }

    /**
     * Flush the cache to the output stream. Used if the runtime errors during bootstrap.
     *
     * @param stream the output stream to flush to
     */
    public void flush(OutputStream stream) {
        if (cache != null) {
            DateFormat format = new SimpleDateFormat("MM:dd:yyyy HH:mm:ss.SSS");
            for (Entry entry : cache) {
                write(entry.level, entry.timestamp, entry.message, entry.values, stream, format);
            }
            cache.clear();
        }
    }

    public int getDestinationIndex(String name) {
        if (delegate == null) {
            if (DEFAULT_DESTINATION.equals(name)) {
                return DEFAULT_DESTINATION_INDEX;
            } else {
                return -1;
            }
        }
        return delegate.getDestinationIndex(name);
    }

    public void send(MonitorLevel level, int destinationIndex, long timestamp, String source, String message, boolean parse, Object... args) {
        if (delegate != null) {
            delegate.send(level, destinationIndex, timestamp, source, message, parse, args);
        } else {
            cache.add(new Entry(level, destinationIndex, timestamp, source, message, parse, args));
        }
    }

    private class Entry {
        protected MonitorLevel level;
        private int destinationIndex;
        protected long timestamp;
        private String source;
        protected String message;
        private boolean parse;
        protected Object[] values;

        private Entry(MonitorLevel level, int destinationIndex, long timestamp, String source, String message, boolean parse, Object... values) {
            this.level = level;
            this.destinationIndex = destinationIndex;
            this.timestamp = timestamp;
            this.source = source;
            this.message = message;
            this.parse = parse;
            this.values = values;
        }
    }

    private void write(MonitorLevel level, long timestamp, String message, Object[] args, OutputStream stream, DateFormat format) {
        message = MessageFormatter.format(message, args);

        Throwable e = null;
        for (Object o : args) {
            if (o instanceof Throwable) {
                e = (Throwable) o;
            }
        }
        if (e != null) {
            StringWriter writer = new StringWriter();
            PrintWriter pw = new PrintWriter(writer);
            if (message != null) {
                writer.write(message);
            }
            writer.write("\n");
            e.printStackTrace(pw);
            message = writer.toString();
        }

        byte[] bytes = ("[" + level + " " + format.format(new Date(timestamp)) + "] " + message + "\n").getBytes();

        try {
            stream.write(bytes);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

}
