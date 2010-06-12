/*
 * Fabric3
 * Copyright (c) 2009 Metaform Systems
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
package org.fabric3.timer.impl;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import junit.framework.TestCase;

import org.fabric3.timer.spi.Task;

/**
 * @version $Rev$ $Date$
 */
public class ExecutorTimerServiceTestCase extends TestCase {
    private ExecutorTimerService timerService = new ExecutorTimerService();

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
