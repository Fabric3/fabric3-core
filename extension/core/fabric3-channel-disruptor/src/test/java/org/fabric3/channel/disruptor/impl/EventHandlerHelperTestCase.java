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
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;

import com.lmax.disruptor.EventHandler;
import junit.framework.TestCase;
import org.easymock.EasyMock;
import org.fabric3.spi.container.channel.ChannelConnection;
import org.fabric3.spi.container.channel.EventStream;

/**
 *
 */
public class EventHandlerHelperTestCase extends TestCase {

    public void testSort() throws Exception {
        EventStream stream = EasyMock.createMock(EventStream.class);
        ChannelConnection conn1 = EasyMock.createMock(ChannelConnection.class);
        EasyMock.expect(conn1.getSequence()).andReturn(0);
        EasyMock.expect(conn1.getEventStream()).andReturn(stream);

        ChannelConnection conn2 = EasyMock.createMock(ChannelConnection.class);
        EasyMock.expect(conn2.getSequence()).andReturn(1);
        EasyMock.expect(conn2.getEventStream()).andReturn(stream);

        ChannelConnection conn3 = EasyMock.createMock(ChannelConnection.class);
        EasyMock.expect(conn3.getSequence()).andReturn(2);
        EasyMock.expect(conn3.getEventStream()).andReturn(stream);

        EasyMock.replay(conn1, conn2, conn3, stream);

        List<ChannelConnection> connections = new ArrayList<>();
        connections.add(conn1);
        connections.add(conn2);
        connections.add(conn3);

        NavigableMap<Integer, List<EventHandler<RingBufferEvent>>> map = EventHandlerHelper.createAndSort(connections);
        assertEquals(3, map.size());
        int last = -1;

        for (Map.Entry<Integer, List<EventHandler<RingBufferEvent>>> entry : map.entrySet()) {
            assertTrue(last < entry.getKey());
            last++;
        }

        EasyMock.verify(conn1, conn2, conn3, stream);
    }

}
