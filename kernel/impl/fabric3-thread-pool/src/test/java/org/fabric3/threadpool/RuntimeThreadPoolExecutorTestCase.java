package org.fabric3.threadpool;

import junit.framework.TestCase;
import org.easymock.EasyMock;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.RejectedExecutionException;

/**
 *
 */
public class RuntimeThreadPoolExecutorTestCase extends TestCase {

    private RuntimeThreadPoolExecutor executor;
    private ExecutorMonitor monitor;

    public void testExecutionTimes() throws Exception {
        executor.setStatisticsOff(false);
        executor.init();
        assertEquals(0.0, executor.getMeanExecutionTime());
        CountDownLatch latch = new CountDownLatch(1);
        executor.execute(new MockWork(latch));
        latch.await();
        Thread.sleep(10);
        assertEquals(1, executor.getCompletedWorkCount());
        assertTrue(executor.getMeanExecutionTime() > 1);
        assertTrue(executor.getTotalExecutionTime() > 1);
    }

    public void testStallNotificationIsSent() throws Exception {
        monitor.stalledThread(EasyMock.isA(String.class), EasyMock.anyLong(), EasyMock.isA(String.class));
        EasyMock.expectLastCall().atLeastOnce();
        EasyMock.replay(monitor);
        executor.setStallCheckPeriod(100);
        executor.setStallThreshold(200);
        executor.setStatisticsOff(false);
        executor.init();
        CountDownLatch latch = new CountDownLatch(1);
        executor.execute(new MockStalledWork(latch));
        Thread.sleep(500);
        latch.countDown();
        EasyMock.verify(monitor);
    }

    public void testRejectWork() throws Exception {
        executor.setQueueSize(1);
        executor.setCoreSize(1);
        executor.setMaximumSize(1);
        executor.setRejectedExecutionHandler("abort");
        executor.init();
        CountDownLatch latch1 = new CountDownLatch(1);
        CountDownLatch latch2 = new CountDownLatch(0);
        // stall the single thread so work piles up in the receive queue
        executor.execute(new MockStalledWork(latch1));
        try {
            executor.execute(new MockStalledWork(latch2));
            executor.execute(new MockStalledWork(latch2));
            fail();
        } catch (RejectedExecutionException e) {
            // expected
        }
        latch1.countDown();
    }

    public void testLongestRunning() throws Exception {
        executor.setStatisticsOff(false);
        executor.init();
        CountDownLatch latch1 = new CountDownLatch(1);
        executor.execute(new MockStalledWork(latch1));
        Thread.sleep(10);
        assertTrue(executor.getLongestRunning() > 1);
        latch1.countDown();
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        monitor = EasyMock.createMock(ExecutorMonitor.class);
        executor = new RuntimeThreadPoolExecutor(monitor);
    }

    private class MockWork implements Runnable {
        private CountDownLatch latch;

        private MockWork(CountDownLatch latch) {
            this.latch = latch;
        }

        public void run() {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Thread.interrupted();
            }
            latch.countDown();
        }
    }

    private class MockStalledWork implements Runnable {
        private CountDownLatch latch;

        private MockStalledWork(CountDownLatch latch) {
            this.latch = latch;
        }

        public void run() {
            try {
                latch.await();
            } catch (InterruptedException e) {
                Thread.interrupted();
            }
        }
    }

}
