/*
 * Fabric3
 * Copyright (c) 2009-2013 Metaform Systems
 *
 * Fabric3 is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of
 * the License, or (at your option) any later version, with the
 * following exception:
 *
 * Linking this software statically or dynamically with other
 * modules is making a combined work based on this software.
 * Thus, the terms and conditions of the GNU General Public
 * License cover the whole combination.
 *
 * As a special exception, the copyright holders of this software
 * give you permission to link this software with independent
 * modules to produce an executable, regardless of the license
 * terms of these independent modules, and to copy and distribute
 * the resulting executable under terms of your choice, provided
 * that you also meet, for each linked independent module, the
 * terms and conditions of the license of that module. An
 * independent module is a module which is not derived from or
 * based on this software. If you modify this software, you may
 * extend this exception to your version of the software, but
 * you are not obligated to do so. If you do not wish to do so,
 * delete this exception statement from your version.
 *
 * Fabric3 is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the
 * GNU General Public License along with Fabric3.
 * If not, see <http://www.gnu.org/licenses/>.
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
import org.fabric3.monitor.impl.destination.MonitorDestination;
import org.fabric3.monitor.impl.destination.MonitorDestinationRegistry;

/**
 *
 */
public class RingBufferDestinationRouterImplTestCase extends TestCase {
    private ExecutorService executorService;
    private MonitorDestinationRegistry registry;
    private RingBufferDestinationRouterImpl router;
    private ByteBuffer buffer;
    private CountDownLatch latch;

    public void testRingBufferRoute() throws Exception {
        router.setMode("production");

        registry.write(0, buffer);

        router.init();
        MonitorEventEntry entry = router.get();
        entry.setBuffer(buffer);
        entry.setLevel(MonitorLevel.SEVERE);
        router.publish(entry);
        router.destroy();

        assertTrue(latch.await(10, TimeUnit.SECONDS));
    }

    public void testSynchronousRoute() throws Exception {
        router.setMode("development");

        registry.write(0, buffer);

        router.init();
        router.send(MonitorLevel.SEVERE, 0, System.currentTimeMillis(), "source", "this is a test {0}", "test");
        router.destroy();

        assertTrue(latch.await(10, TimeUnit.SECONDS));
    }

    public void setUp() throws Exception {
        super.setUp();
        executorService = Executors.newCachedThreadPool();

        latch = new CountDownLatch(1);
        registry = new MockRegistry(latch);

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

        public void write(int index, ByteBuffer buffer) throws IOException {
            latch.countDown();
        }
    }
}
