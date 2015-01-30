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
import org.fabric3.spi.container.builder.channel.EventFilter;
import org.fabric3.spi.container.channel.EventStreamHandler;

/**
 *
 */
public class FilterHandlerTestCase extends TestCase {

    public void testFilter() throws Exception {
        EventFilter filter = EasyMock.createMock(EventFilter.class);
        EasyMock.expect(filter.filter(EasyMock.notNull())).andReturn(false);

        EventStreamHandler tailHandler = EasyMock.createMock(EventStreamHandler.class);
        EasyMock.replay(filter, tailHandler);

        FilterHandler handler = new FilterHandler(filter);
        handler.setNext(tailHandler);
        handler.handle(new Object(), true);

        EasyMock.verify(filter, tailHandler);
    }

    public void testPassFilter() throws Exception {
        EventFilter filter = EasyMock.createMock(EventFilter.class);
        EasyMock.expect(filter.filter(EasyMock.notNull())).andReturn(true);

        EventStreamHandler tailHandler = EasyMock.createMock(EventStreamHandler.class);
        tailHandler.handle(EasyMock.notNull(), EasyMock.anyBoolean());
        EasyMock.replay(filter, tailHandler);

        FilterHandler handler = new FilterHandler(filter);
        handler.setNext(tailHandler);
        handler.handle(new Object(), true);

        EasyMock.verify(filter, tailHandler);
    }

}