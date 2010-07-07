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

import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Delayed;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.osoa.sca.annotations.Destroy;
import org.osoa.sca.annotations.EagerInit;
import org.osoa.sca.annotations.Init;
import org.osoa.sca.annotations.Property;
import org.osoa.sca.annotations.Reference;

import org.fabric3.spi.management.ManagementException;
import org.fabric3.spi.management.ManagementService;
import org.fabric3.timer.spi.PoolAllocationException;
import org.fabric3.timer.spi.Task;
import org.fabric3.timer.spi.TimerService;

/**
 * @version $Rev$ $Date$
 */
@EagerInit
public class ExecutorTimerService implements TimerService {
    private ManagementService managementService;
    private Map<String, ScheduledExecutorService> executors = new ConcurrentHashMap<String, ScheduledExecutorService>();
    private Map<String, TimerPoolStatistics> statisticsCache = new ConcurrentHashMap<String, TimerPoolStatistics>();
    private int defaultPoolSize = 2;

    public ExecutorTimerService(@Reference ManagementService managementService) {
        this.managementService = managementService;
    }

    @Property(required = false)
    public void setDefaultPoolSize(int defaultCoreSize) {
        this.defaultPoolSize = defaultCoreSize;
    }

    @Init
    public void init() throws PoolAllocationException {
        allocate(TimerService.DEFAULT_POOL, defaultPoolSize);
    }

    @Destroy
    public void destroy() {
        for (ScheduledExecutorService executor : executors.values()) {
            executor.shutdown();
        }
    }

    public void allocate(String poolName, int coreSize) throws PoolAllocationException {
        if (executors.containsKey(poolName)) {
            throw new IllegalStateException("Pool already allocated: " + poolName);
        }
        ScheduledThreadPoolExecutor executor = new ScheduledThreadPoolExecutor(coreSize);
        executors.put(poolName, executor);
        TimerPoolStatistics statistics = new TimerPoolStatistics(poolName, coreSize);
        statistics.start();
        statisticsCache.put(poolName, statistics);
        if (managementService != null) {
            try {
                managementService.export(poolName, "timer pools", "Timer pools", statistics);
            } catch (ManagementException e) {
                throw new PoolAllocationException("Error allocating pool " + poolName, e);
            }
        }
    }

    public void deallocate(String poolName) throws PoolAllocationException {
        ScheduledExecutorService executor = executors.remove(poolName);
        if (executor == null) {
            throw new IllegalStateException("Pool not allocated: " + poolName);
        }
        if (managementService != null) {
            try {
                managementService.remove(poolName, "timer pools");
            } catch (ManagementException e) {
                throw new PoolAllocationException("Error allocating pool " + poolName, e);
            }
        }
        executor.shutdown();
    }

    public ScheduledFuture<?> scheduleRecurring(String poolName, Task task) {
        TimerPoolStatistics statistics = this.statisticsCache.get(poolName);
        RecurringRunnable recurring = new RecurringRunnable(poolName, task, statistics);
        return recurring.schedule();
    }

    public ScheduledFuture<?> scheduleAtFixedRate(String poolName, Runnable command, long initialDelay, long period, TimeUnit unit) {
        ScheduledExecutorService executor = getExecutor(poolName);
        return executor.scheduleAtFixedRate(command, initialDelay, period, unit);
    }

    public ScheduledFuture<?> scheduleWithFixedDelay(String poolName, Runnable command, long initialDelay, long delay, TimeUnit unit) {
        ScheduledExecutorService executor = executors.get(poolName);
        return executor.scheduleWithFixedDelay(command, initialDelay, delay, unit);
    }

    public ScheduledFuture<?> schedule(String poolName, Runnable command, long delay, TimeUnit unit) {
        ScheduledExecutorService executor = executors.get(poolName);
        return executor.schedule(command, delay, unit);
    }

    public <V> ScheduledFuture<V> schedule(String poolName, Callable<V> callable, long delay, TimeUnit unit) {
        ScheduledExecutorService executor = executors.get(poolName);
        return executor.schedule(callable, delay, unit);
    }

    private ScheduledExecutorService getExecutor(String poolName) {
        ScheduledExecutorService executor = executors.get(poolName);
        if (executor == null) {
            throw new RejectedExecutionException("Pool not allocated: " + poolName);
        }
        return executor;
    }

    /**
     * Implements a recurring task by wrapping an runnable and rescheduling with the executor service after an iteration has completed.
     */
    private class RecurringRunnable implements Runnable {
        private TimerPoolStatistics statistics;

        private String poolName;
        private Task delegate;
        private ScheduledFutureWrapper<?> currentFuture;

        private RecurringRunnable(String poolName, Task delegate, TimerPoolStatistics statistics) {
            this.poolName = poolName;
            this.delegate = delegate;
            this.statistics = statistics;
        }

        @SuppressWarnings({"unchecked"})
        public ScheduledFuture<?> schedule() {
            long interval = delegate.nextInterval();
            if (Task.DONE == interval) {
                return null;
            }
            ScheduledExecutorService executor = getExecutor(poolName);
            ScheduledFuture future = executor.schedule(this, interval, TimeUnit.MILLISECONDS);
            if (currentFuture == null) {
                currentFuture = new ScheduledFutureWrapper(future);
            } else {
                currentFuture.update(future);
            }
            return currentFuture;
        }

        public void run() {
            long start = System.currentTimeMillis();
            delegate.run();
            long elapsed = System.currentTimeMillis() - start;
            statistics.incrementTotalExecutions();
            statistics.incrementExecutionTime(elapsed);
            if (!currentFuture.isCancelled()) {
                schedule();
            }
        }
    }

    /**
     * Returned when a recurring task is scheduled. This class wraps a ScheduledFuture delegate which is updated after the recurring event is
     * rescheduled.
     *
     * @param <V> The result type returned by this Future's <tt>get</tt> method
     */
    private class ScheduledFutureWrapper<V> implements ScheduledFuture<V> {
        private volatile ScheduledFuture<V> delegate;

        private ScheduledFutureWrapper(ScheduledFuture<V> delegate) {
            this.delegate = delegate;
        }

        public void update(ScheduledFuture<V> newDelegate) {
            delegate = newDelegate;
        }

        public boolean cancel(boolean mayInterruptIfRunning) {
            return delegate.cancel(mayInterruptIfRunning);
        }

        public boolean isCancelled() {
            return delegate.isCancelled();
        }

        public boolean isDone() {
            return delegate.isDone();
        }

        public V get() throws InterruptedException, ExecutionException {
            return delegate.get();
        }

        public V get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
            return delegate.get(timeout, unit);
        }

        public long getDelay(TimeUnit unit) {
            return delegate.getDelay(unit);
        }

        public int compareTo(Delayed o) {
            return delegate.compareTo(o);
        }
    }

}