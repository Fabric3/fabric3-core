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
package org.fabric3.api.host.monitor;

import java.io.ByteArrayOutputStream;

import junit.framework.TestCase;
import org.easymock.EasyMock;
import org.fabric3.api.annotation.monitor.MonitorLevel;

/**
 *
 */
public class DelegatingDestinationRouterTestCase extends TestCase {
    private DelegatingDestinationRouter router;
    private DestinationRouter delegate;

    public void testDelegate() throws Exception {
        long timestamp = System.currentTimeMillis();
        delegate.send(MonitorLevel.SEVERE, 0, timestamp, "source", "this is a test: {0}", true, "test");
        EasyMock.replay(delegate);

        router.setDestination(delegate);
        router.send(MonitorLevel.SEVERE, 0, timestamp, "source", "this is a test: {0}", true, "test");
        EasyMock.verify(delegate);
    }

    public void testFlush() throws Exception {
        long timestamp = System.currentTimeMillis();
        router.send(MonitorLevel.SEVERE, 0, timestamp, "source", "this is a test: {0}", true, "test");

        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        router.flush(stream);
        assertTrue(new String(stream.toByteArray()).contains("this is a test: test"));
    }

    public void testCacheEventsBeforeDestinationSet() throws Exception {
        long timestamp = System.currentTimeMillis();
        delegate.send(MonitorLevel.SEVERE, 0, timestamp, "source", "this is a test: {0}", true, "test");
        EasyMock.replay(delegate);

        router.send(MonitorLevel.SEVERE, 0, timestamp, "source", "this is a test: {0}", true, "test");

        router.setDestination(delegate);
        EasyMock.verify(delegate);
    }

    public void setUp() throws Exception {
        super.setUp();
        delegate = EasyMock.createMock(DestinationRouter.class);
        router = new DelegatingDestinationRouter();
    }
}
