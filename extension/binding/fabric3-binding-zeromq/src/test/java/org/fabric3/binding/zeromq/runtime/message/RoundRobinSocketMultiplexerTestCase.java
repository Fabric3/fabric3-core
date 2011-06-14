/*
 * Fabric3 Copyright (c) 2009-2011 Metaform Systems
 * 
 * Fabric3 is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version, with the following exception:
 * 
 * Linking this software statically or dynamically with other modules is making
 * a combined work based on this software. Thus, the terms and conditions of the
 * GNU General Public License cover the whole combination.
 * 
 * As a special exception, the copyright holders of this software give you
 * permission to link this software with independent modules to produce an
 * executable, regardless of the license terms of these independent modules, and
 * to copy and distribute the resulting executable under terms of your choice,
 * provided that you also meet, for each linked independent module, the terms
 * and conditions of the license of that module. An independent module is a
 * module which is not derived from or based on this software. If you modify
 * this software, you may extend this exception to your version of the software,
 * but you are not obligated to do so. If you do not wish to do so, delete this
 * exception statement from your version.
 * 
 * Fabric3 is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * Fabric3. If not, see <http://www.gnu.org/licenses/>.
 */
package org.fabric3.binding.zeromq.runtime.message;

import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;
import org.easymock.classextension.EasyMock;
import org.zeromq.ZMQ;

/**
 * @version $Revision: 10212 $ $Date: 2011-03-15 18:20:58 +0100 (Tue, 15 Mar 2011) $
 */
public class RoundRobinSocketMultiplexerTestCase extends TestCase {

    public void testRoundRobin() throws Exception {
        RoundRobinSocketMultiplexer multiplexer = new RoundRobinSocketMultiplexer();
        ZMQ.Socket socket1 = EasyMock.createMock(ZMQ.Socket.class);
        ZMQ.Socket socket2 = EasyMock.createMock(ZMQ.Socket.class);
        ZMQ.Socket socket3 = EasyMock.createMock(ZMQ.Socket.class);

        List<ZMQ.Socket> list = new ArrayList<ZMQ.Socket>();
        list.add(socket1);
        list.add(socket2);
        list.add(socket3);
        multiplexer.update(list);

        assertEquals(socket1, multiplexer.get());
        assertEquals(socket2, multiplexer.get());
        assertEquals(socket3, multiplexer.get());
        assertEquals(socket1, multiplexer.get());
    }

    public void testSingletonIterator() throws Exception {
        RoundRobinSocketMultiplexer multiplexer = new RoundRobinSocketMultiplexer();
        ZMQ.Socket socket1 = EasyMock.createMock(ZMQ.Socket.class);

        List<ZMQ.Socket> list = new ArrayList<ZMQ.Socket>();
        list.add(socket1);
        multiplexer.update(list);

        assertEquals(socket1, multiplexer.get());
        assertEquals(socket1, multiplexer.get());
    }

    public void testUpdate() throws Exception {
        RoundRobinSocketMultiplexer multiplexer = new RoundRobinSocketMultiplexer();
        ZMQ.Socket socket1 = EasyMock.createMock(ZMQ.Socket.class);
        ZMQ.Socket socket2 = EasyMock.createMock(ZMQ.Socket.class);
        ZMQ.Socket socket3 = EasyMock.createMock(ZMQ.Socket.class);

        List<ZMQ.Socket> list1 = new ArrayList<ZMQ.Socket>();
        list1.add(socket1);
        list1.add(socket2);
        list1.add(socket3);
        multiplexer.update(list1);

        assertEquals(socket1, multiplexer.get());

        List<ZMQ.Socket> list2 = new ArrayList<ZMQ.Socket>();
        list2.add(socket1);
        list2.add(socket2);
        multiplexer.update(list2);


        assertEquals(socket1, multiplexer.get());
        assertEquals(socket2, multiplexer.get());
        assertEquals(socket1, multiplexer.get());
        assertEquals(socket2, multiplexer.get());
    }

}
