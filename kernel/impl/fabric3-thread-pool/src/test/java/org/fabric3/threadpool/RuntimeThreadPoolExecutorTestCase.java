package org.fabric3.threadpool;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.RejectedExecutionException;

import junit.framework.TestCase;
import org.easymock.EasyMock;

/**
 *
 */
public class RuntimeThreadPoolExecutorTestCase extends TestCase {

    private RuntimeThreadPoolExecutor executor;

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

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        ExecutorMonitor monitor = EasyMock.createMock(ExecutorMonitor.class);
        executor = new RuntimeThreadPoolExecutor(monitor);
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
