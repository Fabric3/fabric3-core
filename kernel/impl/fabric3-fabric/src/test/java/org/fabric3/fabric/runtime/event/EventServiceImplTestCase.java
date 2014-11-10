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
package org.fabric3.fabric.runtime.event;

import junit.framework.TestCase;
import org.easymock.EasyMock;
import org.fabric3.spi.runtime.event.EventService;
import org.fabric3.spi.runtime.event.Fabric3Event;
import org.fabric3.spi.runtime.event.Fabric3EventListener;

/**
 *
 */
public class EventServiceImplTestCase extends TestCase {
    private EventService service;
    private Fabric3EventListener<MockEvent> listener;

    public void testSubscribeUnsubscribe() throws Exception {
        service.subscribe(MockEvent.class, listener);
        service.publish(new MockEvent());
        service.unsubscribe(MockEvent.class, listener);
        service.publish(new MockEvent());
        EasyMock.verify(listener);
    }


    @SuppressWarnings({"unchecked"})
    protected void setUp() throws Exception {
        super.setUp();
        service = new EventServiceImpl();
        listener = EasyMock.createStrictMock(Fabric3EventListener.class);
        listener.onEvent(EasyMock.isA(MockEvent.class));
        EasyMock.replay(listener);
    }

    private class MockEvent implements Fabric3Event {

    }
}
