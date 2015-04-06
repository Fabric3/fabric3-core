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
package org.fabric3.fabric.container.channel;

import junit.framework.TestCase;
import org.easymock.EasyMock;
import org.fabric3.spi.container.channel.EventStreamHandler;

/**
 *
 */
public class EventStreamImplTestCase extends TestCase {

    public void testAddHandlers() throws Exception {
        EventStreamHandler handler1 = EasyMock.createMock(EventStreamHandler.class);
        EventStreamHandler handler2 = EasyMock.createMock(EventStreamHandler.class);

        handler1.setNext(handler2);
        EasyMock.replay(handler1, handler2);

        EventStreamImpl stream = new EventStreamImpl(Object.class);
        stream.addHandler(handler1);
        assertEquals(handler1, stream.getTailHandler());

        stream.addHandler(handler2);
        assertEquals(handler2, stream.getTailHandler());

        EasyMock.verify(handler1, handler2);
    }

    public void testAddIndexedHandler() throws Exception {
        EventStreamHandler handler3 = EasyMock.createMock(EventStreamHandler.class);
        handler3.setNext(EasyMock.isNull());

        EventStreamHandler handler2 = EasyMock.createMock(EventStreamHandler.class);
        handler2.setNext(EasyMock.eq(handler3));

        EventStreamHandler handler1 = EasyMock.createMock(EventStreamHandler.class);
        EasyMock.expect(handler1.getNext()).andReturn(null);
        handler1.setNext(EasyMock.eq(handler3));
        EasyMock.expectLastCall();
        EasyMock.expect(handler1.getNext()).andReturn(handler3);
        handler1.setNext(EasyMock.eq(handler2));
        EasyMock.expectLastCall();

        EasyMock.replay(handler1, handler2, handler3);

        EventStreamImpl stream = new EventStreamImpl(Object.class);
        stream.addHandler(handler1);
        stream.addHandler(2, handler3);
        stream.addHandler(2, handler2);

        EasyMock.verify(handler1, handler2, handler3);
    }

}