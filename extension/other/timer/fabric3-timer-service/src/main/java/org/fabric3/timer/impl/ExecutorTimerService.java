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
 * Portions originally based on Apache Tuscany 2007
 * licensed under the Apache 2.0 license.
 */
package org.fabric3.timer.impl;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Delayed;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.fabric3.api.annotation.monitor.Monitor;
import org.fabric3.api.host.Fabric3Exception;
import org.fabric3.spi.management.ManagementService;
import org.fabric3.timer.spi.Task;
import org.fabric3.timer.spi.TimerService;
import org.oasisopen.sca.annotation.Destroy;
import org.oasisopen.sca.annotation.EagerInit;
import org.oasisopen.sca.annotation.Init;
import org.oasisopen.sca.annotation.Property;
import org.oasisopen.sca.annotation.Reference;
import org.oasisopen.sca.annotation.Service;

/**
 *
 */
@EagerInit
@Service({TimerService.class, ScheduledExecutorService.class})
public class ExecutorTimerService implements TimerService, ScheduledExecutorService {
    private ManagementService managementService;
    private TimerServiceMonitor monitor;
    private Map<String, ScheduledExecutorService> executors = new ConcurrentHashMap<>();
    private Map<String, TimerPoolStatistics> statisticsCache = new ConcurrentHashMap<>();
    private int defaultPoolSize = 2;

    public ExecutorTimerService(@Reference ManagementService managementService, @Monitor TimerServiceMonitor monitor) {
        this.managementService = managementService;
        this.monitor = monitor;
    }

    @Property(required = false)
    public void setDefaultPoolSize(int defaultCoreSize) {
        this.defaultPoolSize = defaultCoreSize;
    }

    @Init
    public void init() throws Fabric3Exception {
        allocate(TimerService.DEFAULT_POOL, defaultPoolSize);
    }

    @Destroy
    public void destroy() {
        executors.values().forEach(java.util.concurrent.ScheduledExecutorService::shutdownNow);
    }

    public void allocate(String poolName, int coreSize) throws Fabric3Exception {
        if (executors.containsKey(poolName)) {
            throw new IllegalStateException("Pool already allocated: " + poolName);
        }
        ScheduledThreadPoolExecutor executor = new ScheduledThreadPoolExecutor(coreSize);
        TimerThreadFactory threadFactory = new TimerThreadFactory(poolName, monitor);
        executor.setThreadFactory(threadFactory);
        executors.put(poolName, executor);
        TimerPoolStatistics statistics = new TimerPoolStatistics(poolName, coreSize);
        statistics.start();
        statisticsCache.put(poolName, statistics);
        if (managementService != null) {
            managementService.export(encodeName(poolName), "timer pools", "Timer pools", statistics);
        }
    }

    public void deallocate(String poolName) throws Fabric3Exception {
        ScheduledExecutorService executor = executors.remove(poolName);
        if (executor == null) {
            throw new IllegalStateException("Pool not allocated: " + poolName);
        }
        if (managementService != null) {
            managementService.remove(encodeName(poolName), "timer pools");
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

    public ScheduledFuture<?> schedule(Runnable command, long delay, TimeUnit unit) {
        return schedule(TimerService.DEFAULT_POOL, command, delay, unit);
    }

    public <V> ScheduledFuture<V> schedule(Callable<V> callable, long delay, TimeUnit unit) {
        return schedule(TimerService.DEFAULT_POOL, callable, delay, unit);
    }

    public ScheduledFuture<?> scheduleAtFixedRate(Runnable command, long initialDelay, long period, TimeUnit unit) {
        return scheduleAtFixedRate(TimerService.DEFAULT_POOL, command, initialDelay, period, unit);
    }

    public ScheduledFuture<?> scheduleWithFixedDelay(Runnable command, long initialDelay, long delay, TimeUnit unit) {
        return scheduleWithFixedDelay(TimerService.DEFAULT_POOL, command, initialDelay, delay, unit);
    }

    public void shutdown() {
        throw new UnsupportedOperationException();
    }

    public List<Runnable> shutdownNow() {
        throw new UnsupportedOperationException();
    }

    public boolean isShutdown() {
        return false;
    }

    public boolean isTerminated() {
        return false;
    }

    public boolean awaitTermination(long timeout, TimeUnit unit) throws InterruptedException {
        throw new UnsupportedOperationException();
    }

    public <T> Future<T> submit(Callable<T> task) {
        throw new UnsupportedOperationException();
    }

    public <T> Future<T> submit(Runnable task, T result) {
        throw new UnsupportedOperationException();
    }

    public Future<?> submit(Runnable task) {
        throw new UnsupportedOperationException();
    }

    public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks) throws InterruptedException {
        throw new UnsupportedOperationException();
    }

    public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks, long timeout, TimeUnit unit) throws InterruptedException {
        throw new UnsupportedOperationException();
    }

    public <T> T invokeAny(Collection<? extends Callable<T>> tasks) throws InterruptedException, ExecutionException {
        throw new UnsupportedOperationException();
    }

    public <T> T invokeAny(Collection<? extends Callable<T>> tasks, long timeout, TimeUnit unit)
            throws InterruptedException, ExecutionException, TimeoutException {
        throw new UnsupportedOperationException();
    }

    public void execute(Runnable command) {
        throw new UnsupportedOperationException();
    }

    private ScheduledExecutorService getExecutor(String poolName) {
        ScheduledExecutorService executor = executors.get(poolName);
        if (executor == null) {
            throw new RejectedExecutionException("Pool not allocated: " + poolName);
        }
        return executor;
    }

    private String encodeName(String name) {
        return "timer/pools/" + name.toLowerCase();
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
            try {
                delegate.run();
            } catch (RuntimeException e) {
                monitor.threadError(e);
                throw e;
            } finally {
                long elapsed = System.currentTimeMillis() - start;
                statistics.incrementTotalExecutions();
                statistics.incrementExecutionTime(elapsed);
                if (!currentFuture.isCancelled()) {
                    schedule();
                }
            }
        }
    }

    /**
     * Returned when a recurring task is scheduled. This class wraps a ScheduledFuture delegate which is updated after the recurring event is rescheduled.
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