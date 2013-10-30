/*
 * Fabric3 Copyright (c) 2009-2013 Metaform Systems
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

import junit.framework.TestCase;
import org.easymock.EasyMock;
import org.zeromq.ZMQ;

import org.fabric3.api.binding.zeromq.model.ZeroMQMetadata;
import org.fabric3.binding.zeromq.runtime.JDK7WorkaroundHelper;

/**
 * Implementations dispatch messages over a ZeroMQ socket.
 */
public final class SocketHelperTestCase extends TestCase {
    private ZMQ.Socket socket;
    private ZeroMQMetadata metadata;

    public void testSetNone() throws Exception {
        socket.setHWM(1000);
        EasyMock.replay(socket);

        SocketHelper.configure(socket, metadata);
        JDK7WorkaroundHelper.workaroundLinuxJDK7Assertion(socket);
    }

    public void testHighWater() throws Exception {
        metadata.setHighWater(1);
        socket.setHWM(1);
        EasyMock.replay(socket);

        SocketHelper.configure(socket, metadata);
        JDK7WorkaroundHelper.workaroundLinuxJDK7Assertion(socket);
    }

    public void testMulticastRate() throws Exception {
        metadata.setMulticastRate(1);
        socket.setRate(1);
        socket.setHWM(1000);
        EasyMock.replay(socket);

        SocketHelper.configure(socket, metadata);
        JDK7WorkaroundHelper.workaroundLinuxJDK7Assertion(socket);
    }

    public void testMulticastRecovery() throws Exception {
        metadata.setMulticastRecovery(1);
        socket.setHWM(1000);
        socket.setRecoveryInterval(1);
        EasyMock.replay(socket);

        SocketHelper.configure(socket, metadata);
        JDK7WorkaroundHelper.workaroundLinuxJDK7Assertion(socket);
    }

    public void testReceiveBuffer() throws Exception {
        metadata.setReceiveBuffer(1);
        socket.setHWM(1000);
        socket.setReceiveBufferSize(1);
        EasyMock.replay(socket);

        SocketHelper.configure(socket, metadata);
        JDK7WorkaroundHelper.workaroundLinuxJDK7Assertion(socket);
    }

    public void testSendBuffer() throws Exception {
        metadata.setSendBuffer(1);
        socket.setHWM(1000);
        socket.setSendBufferSize(1);
        EasyMock.replay(socket);

        SocketHelper.configure(socket, metadata);
        JDK7WorkaroundHelper.workaroundLinuxJDK7Assertion(socket);
    }

    @Override
    public void setUp() throws Exception {
        super.setUp();
        socket = EasyMock.createMock(ZMQ.Socket.class);
        socket.setLinger(0);
        metadata = new ZeroMQMetadata();
    }
}
