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
package org.fabric3.host.monitor;

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
        cache = new ArrayList<Entry>();
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

