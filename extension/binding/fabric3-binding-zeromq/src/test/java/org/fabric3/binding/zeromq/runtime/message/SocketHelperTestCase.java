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
 * Portions originally based on Apache Tuscany 2007
 * licensed under the Apache 2.0 license.
 */
package org.fabric3.binding.zeromq.runtime.message;

import junit.framework.TestCase;
import org.fabric3.api.binding.zeromq.model.ZeroMQMetadata;
import org.zeromq.ZMQ;

/**
 *
 */
public final class SocketHelperTestCase extends TestCase {
    private ZMQ.Socket socket;
    private ZeroMQMetadata metadata;

    public void testHighWater() throws Exception {
        metadata.setHighWater(1);

        SocketHelper.configure(socket, metadata);
        assertEquals(1, socket.getRcvHWM());
    }

    public void testReceiveBuffer() throws Exception {
        metadata.setReceiveBuffer(1);

        SocketHelper.configure(socket, metadata);
        assertEquals(1, socket.getReceiveBufferSize());
    }

    public void testSendBuffer() throws Exception {
        metadata.setSendBuffer(1);

        SocketHelper.configure(socket, metadata);
        assertEquals(1, socket.getSendBufferSize());
    }

    public void setUp() throws Exception {
        super.setUp();
        socket = ZMQ.context(1).socket(ZMQ.PUB);
        socket.setLinger(0);
        metadata = new ZeroMQMetadata();
    }
}
