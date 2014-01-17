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
package org.fabric3.channel.impl;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import org.fabric3.spi.container.channel.ChannelConnection;
import org.fabric3.spi.container.channel.EventStreamHandler;

/**
 * Base FanOutHandler functionality.
 * <p/>
 * Supports registering a connection multiple times. This is required for producer-side channels where a connection from the channel to a binding may be
 * provisioned multiple times if there is more than one producer connected to the same channel provisioned in a zone.
 */
public abstract class AbstractFanOutHandler implements FanOutHandler {
    protected Map<URI, ChannelConnection> connectionMap = new HashMap<URI, ChannelConnection>();
    protected Map<URI, AtomicInteger> counterMap = new HashMap<URI, AtomicInteger>();

    protected ChannelConnection[] connections = new ChannelConnection[0];

    public synchronized void addConnection(URI uri, ChannelConnection connection) {
        AtomicInteger count = counterMap.get(uri);
        if (count == null) {
            count = new AtomicInteger(1);
            counterMap.put(uri, count);
            connectionMap.put(uri, connection);
            connections = connectionMap.values().toArray(new ChannelConnection[connectionMap.size()]);
        } else {
            count.incrementAndGet();
        }
    }

    public synchronized ChannelConnection removeConnection(URI uri) {
        int count = counterMap.get(uri).decrementAndGet();
        if (count == 0) {
            counterMap.remove(uri);
            ChannelConnection connection = connectionMap.remove(uri);
            connections = connectionMap.values().toArray(new ChannelConnection[connectionMap.size()]);
            return connection;
        } else {
            return connectionMap.get(uri);
        }
    }

    public void setNext(EventStreamHandler next) {
        throw new IllegalStateException("This handler must be the last one in the handler sequence");
    }

    public EventStreamHandler getNext() {
        return null;
    }

}