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
package org.fabric3.timer.impl;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import junit.framework.TestCase;

import org.fabric3.timer.spi.Task;

/**
 *
 */
public class ExecutorTimerServiceTestCase extends TestCase {
    private ExecutorTimerService timerService = new ExecutorTimerService(null, null);

    public void testAllocateDeallocate() throws Exception {
        timerService.allocate("test", 10);
        timerService.deallocate("test");
        timerService.allocate("test", 10);
        try {
            timerService.allocate("test", 10);
            fail();
        } catch (IllegalStateException e) {
            // expected
        }
        timerService.deallocate("test");
    }

    public void testRecurringTask() throws Exception {
        timerService.allocate("test", 10);
        CountDownLatch latch = new CountDownLatch(4);
        IntervalTask task = new IntervalTask(latch);
        timerService.scheduleRecurring("test", task);
        assertTrue(latch.await(1000, TimeUnit.MILLISECONDS));
        assertEquals(3, task.getCounter());
    }

    public void testRecurringTaskEnds() throws Exception {
        timerService.allocate("test", 10);
        CountDownLatch latch = new CountDownLatch(5);
        IntervalTask task = new IntervalTask(latch);
        timerService.scheduleRecurring("test", task);
        // this should timeout as the task will only fire 3 times and the latch is set to countdown 4 times
        assertFalse(latch.await(1000, TimeUnit.MILLISECONDS));
        assertEquals(3, task.getCounter());
    }

    public void testCancel() throws Exception {
        timerService.allocate("test", 10);
        EndlessTask task = new EndlessTask();
        ScheduledFuture<?> future = timerService.scheduleRecurring("test", task);
        long start = System.currentTimeMillis();
        while (task.getCounter() < 3) {
            Thread.sleep(500);
            if (System.currentTimeMillis() - start > 8000) {
                fail("Task did not start in expected amount of time");
            }
        }
        assertTrue(future.cancel(true));
        // wait an iteration for cancel to take effect
        Thread.sleep(600);
        int val = task.getCounter();
        // verify no additional runs were made
        assertEquals(val, task.getCounter());
    }


    private class EndlessTask implements Task {
        private int counter;

        public int getCounter() {
            return counter;
        }

        public long nextInterval() {
            return 500;
        }

        public void run() {
            counter++;
        }
    }

    private class IntervalTask implements Task {
        private CountDownLatch latch;
        private int counter;

        private IntervalTask(CountDownLatch latch) {
            this.latch = latch;
        }

        public int getCounter() {
            return counter;
        }

        public long nextInterval() {
            latch.countDown();
            if (counter == 3) {
                return Task.DONE;
            }
            return 50;
        }

        public void run() {
            counter++;
        }
    }
}
