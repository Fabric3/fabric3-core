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
package org.fabric3.monitor.impl.router;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import junit.framework.TestCase;
import org.easymock.EasyMock;
import org.fabric3.api.annotation.monitor.MonitorLevel;
import org.fabric3.monitor.spi.buffer.ResizableByteBuffer;
import org.fabric3.monitor.spi.destination.MonitorDestination;
import org.fabric3.monitor.spi.destination.MonitorDestinationRegistry;
import org.fabric3.monitor.spi.event.MonitorEventEntry;

/**
 *
 */
public class RingBufferDestinationRouterImplTestCase extends TestCase {
    private ExecutorService executorService;
    private RingBufferDestinationRouterImpl router;
    private ByteBuffer buffer;
    private CountDownLatch latch;

    public void testRingBufferRoute() throws Exception {
        router.setMode("asynchronous");

        router.init();
        MonitorEventEntry entry = router.get();
        ResizableByteBuffer wrapper = new ResizableByteBuffer(this.buffer);
        entry.setBuffer(wrapper);
        entry.setLevel(MonitorLevel.SEVERE);
        router.publish(entry);
        router.destroy();

        assertTrue(latch.await(10, TimeUnit.SECONDS));
    }

    public void testSynchronousRoute() throws Exception {
        router.setMode("synchronous");

        router.init();
        router.send(MonitorLevel.SEVERE, 0, System.currentTimeMillis(), "source", "this is a test {0}", true, "test");
        router.destroy();

        assertTrue(latch.await(10, TimeUnit.SECONDS));
    }

    @SuppressWarnings("unchecked")
    public void setUp() throws Exception {
        super.setUp();
        executorService = Executors.newCachedThreadPool();

        latch = new CountDownLatch(1);
        MonitorDestinationRegistry registry = new MockRegistry(latch);

        DestinationMonitor monitor = EasyMock.createNiceMock(DestinationMonitor.class);
        EasyMock.replay(monitor);

        router = new RingBufferDestinationRouterImpl(executorService, registry, monitor);
        router.setCapacity(100);
        router.setRingSize(2);

        buffer = ByteBuffer.allocate(200);
    }

    public void tearDown() throws Exception {
        super.tearDown();
        executorService.shutdownNow();
    }

    private class MockRegistry implements MonitorDestinationRegistry {
        private CountDownLatch latch;

        private MockRegistry(CountDownLatch latch) {
            this.latch = latch;
        }

        public void register(MonitorDestination destination) {
            throw new UnsupportedOperationException();
        }

        public MonitorDestination unregister(String name) {
            throw new UnsupportedOperationException();
        }

        public int getIndex(String name) {
            throw new UnsupportedOperationException();
        }

        public void write(MonitorEventEntry entry) throws IOException {
            latch.countDown();
        }

        public void write(int index, MonitorLevel level, long timestamp, String source, String template, Object... args) {
            latch.countDown();
        }
    }
}
