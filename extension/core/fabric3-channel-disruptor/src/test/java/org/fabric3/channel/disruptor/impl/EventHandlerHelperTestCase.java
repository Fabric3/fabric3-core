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
import org.fabric3.spi.util.Cast;

/**
 *
 */
public class EventHandlerHelperTestCase extends TestCase {

    public void testSort() throws Exception {
        EventStream stream = EasyMock.createMock(EventStream.class);
        ChannelConnection conn1 = EasyMock.createMock(ChannelConnection.class);
        EasyMock.expect(conn1.getSequence()).andReturn(0);
        EasyMock.expect(conn1.getEventStream()).andReturn(stream).atLeastOnce();

        EasyMock.expect(stream.getEventType()).andReturn(Cast.cast(Object.class)).atLeastOnce();

        ChannelConnection conn2 = EasyMock.createMock(ChannelConnection.class);
        EasyMock.expect(conn2.getSequence()).andReturn(1);
        EasyMock.expect(conn2.getEventStream()).andReturn(stream).atLeastOnce();

        ChannelConnection conn3 = EasyMock.createMock(ChannelConnection.class);
        EasyMock.expect(conn3.getSequence()).andReturn(2);
        EasyMock.expect(conn3.getEventStream()).andReturn(stream).atLeastOnce();

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
