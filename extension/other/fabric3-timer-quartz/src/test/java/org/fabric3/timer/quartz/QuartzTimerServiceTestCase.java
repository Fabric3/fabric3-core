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
package org.fabric3.timer.quartz;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import junit.framework.TestCase;

import org.fabric3.host.work.PausableWork;
import org.fabric3.host.work.WorkScheduler;

/**
 * @version $Rev$ $Date$
 */
public class QuartzTimerServiceTestCase extends TestCase {
    private QuartzTimerService timerService;

    public void testNonTransactionalScheduler() throws Exception {
        TestRunnable runnable = new TestRunnable(2);
        timerService.scheduleWithFixedDelay(runnable, 0, 10, TimeUnit.MILLISECONDS);
        runnable.await();
    }

    protected void setUp() throws Exception {
        super.setUp();
        // TODO mock transaction manager
        WorkScheduler workScheduler = new WorkScheduler() {
            public <T extends PausableWork> void scheduleWork(T work) {
                work.run();
            }

            public void shutdown() {

            }

            public List<Runnable> shutdownNow() {
                return null;
            }

            public boolean isShutdown() {
                return false;
            }

            public boolean isTerminated() {
                return false;
            }

            public boolean awaitTermination(long timeout, TimeUnit unit) throws InterruptedException {
                return false;
            }

            public <T> Future<T> submit(Callable<T> task) {
                return null;
            }

            public <T> Future<T> submit(Runnable task, T result) {
                return null;
            }

            public Future<?> submit(Runnable task) {
                return null;
            }

            public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> callables) throws InterruptedException {
                return null;
            }

            public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> callables, long l, TimeUnit timeUnit) throws InterruptedException {
                return null;
            }

            public <T> T invokeAny(Collection<? extends Callable<T>> callables) throws InterruptedException, ExecutionException {
                return null;
            }

            public <T> T invokeAny(Collection<? extends Callable<T>> callables, long l, TimeUnit timeUnit)
                    throws InterruptedException, ExecutionException, TimeoutException {
                return null;
            }

            public void execute(Runnable command) {

            }
        };
        timerService = new QuartzTimerService(workScheduler, null);
        timerService.setTransactional(false);
        timerService.init();
    }


    private class TestRunnable implements Runnable {
        private CountDownLatch latch;

        private TestRunnable(int num) {
            latch = new CountDownLatch(num);
        }

        public void run() {
            latch.countDown();
        }

        public void await() throws InterruptedException {
            latch.await();
        }

    }

}
