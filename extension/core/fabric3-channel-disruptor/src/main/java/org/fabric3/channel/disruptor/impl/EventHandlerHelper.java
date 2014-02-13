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
package org.fabric3.channel.disruptor.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.NavigableMap;
import java.util.TreeMap;

import com.lmax.disruptor.EventHandler;
import org.fabric3.spi.container.channel.ChannelConnection;

/**
 *
 */
public class EventHandlerHelper {

    /**
     * Creates event handlers from a collection of {@link ChannelConnection}s and returns a Map sorted by connection sequence value.
     *
     * @param connections the connections
     * @return the sorted Map
     */
    public static NavigableMap<Integer, List<EventHandler<RingBufferEvent>>> createAndSort(Collection<ChannelConnection> connections) {
        NavigableMap<Integer, List<EventHandler<RingBufferEvent>>> sorted = new TreeMap<>();

        for (ChannelConnection connection : connections) {
            Integer sequence = connection.getSequence();
            List<EventHandler<RingBufferEvent>> handlers = sorted.get(sequence);
            if (handlers == null) {
                handlers = new ArrayList<>();
                sorted.put(sequence, handlers);
            }
            handlers.add(new ChannelEventHandler(connection));
        }
        return sorted;
    }

    private EventHandlerHelper() {
    }
}
