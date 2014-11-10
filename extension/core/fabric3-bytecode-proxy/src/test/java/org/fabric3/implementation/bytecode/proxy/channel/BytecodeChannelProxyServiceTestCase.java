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
package org.fabric3.implementation.bytecode.proxy.channel;

import junit.framework.TestCase;
import org.easymock.EasyMock;
import org.fabric3.implementation.bytecode.proxy.common.ProxyFactory;
import org.fabric3.spi.container.channel.ChannelConnection;
import org.fabric3.spi.container.channel.EventStream;
import org.fabric3.spi.container.channel.EventStreamHandler;

/**
 * Implementation that delegates to a {@link ProxyFactory} to create channel proxies.
 */
public class BytecodeChannelProxyServiceTestCase extends TestCase {
    private ProxyFactory proxyFactory;
    private BytecodeChannelProxyService proxyService;
    private ChannelConnection connection;
    private EventStream eventStream;

    public void testTest() throws Exception {
        EasyMock.replay(proxyFactory, connection, eventStream);

        assertNotNull(proxyService.createObjectFactory(ProxyService.class, connection));

        EasyMock.verify(proxyFactory, connection, eventStream);
    }

    public void setUp() throws Exception {
        super.setUp();

        proxyFactory = EasyMock.createMock(ProxyFactory.class);

        EventStreamHandler handler = EasyMock.createMock(EventStreamHandler.class);

        eventStream = EasyMock.createMock(EventStream.class);
        EasyMock.expect(eventStream.getHeadHandler()).andReturn(handler);

        connection = EasyMock.createMock(ChannelConnection.class);
        EasyMock.expect(connection.getEventStream()).andReturn(eventStream);

        proxyService = new BytecodeChannelProxyService(proxyFactory);
    }

    private interface ProxyService {

        String invoke(String message);

    }
}
