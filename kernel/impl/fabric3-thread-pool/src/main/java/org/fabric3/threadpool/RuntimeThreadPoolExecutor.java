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
package org.fabric3.threadpool;

import java.util.List;
import java.util.concurrent.AbstractExecutorService;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

import org.fabric3.api.annotation.Source;
import org.fabric3.api.annotation.management.Management;
import org.fabric3.api.annotation.management.ManagementOperation;
import org.fabric3.api.annotation.monitor.Monitor;
import org.fabric3.spi.threadpool.ExecutionContext;
import org.fabric3.spi.threadpool.ExecutionContextTunnel;
import org.fabric3.spi.threadpool.LongRunnable;
import org.oasisopen.sca.annotation.Destroy;
import org.oasisopen.sca.annotation.EagerInit;
import org.oasisopen.sca.annotation.Init;
import org.oasisopen.sca.annotation.Property;

/**
 * Processes work using a delegate {@link ThreadPoolExecutor}. This executor records processing statistics as well as monitors for stalled threads. When a
 * stalled thread is encountered (i.e. when the processing time for a runnable has exceeded a threshold), an event is sent to the monitor.
 * <p/>
 * The default configuration uses a bounded queue to accept work. If the queue size is exceeded, work will be rejected. This allows the runtime to degrade
 * gracefully under load by pushing requests back to the client and avoid out-of-memory conditions.
 */
@EagerInit
@Management(name = "RuntimeThreadPoolExecutor",
            path = "/runtime/threadpool",
            group = "kernel",
            description = "Manages the runtime thread pool")
public class RuntimeThreadPoolExecutor extends AbstractExecutorService {
    private int coreSize = 100;
    private long keepAliveTime = 60000;
    private boolean allowCoreThreadTimeOut = true;
    private int maximumSize = 100;
    private int queueSize = 10000;
    private int stallThreshold = 600000;
    private boolean checkStalledThreads = true;
    private long stallCheckPeriod = 60000;
    private RejectedExecutionHandler rejectedExecutionHandler = new ThreadPoolExecutor.CallerRunsPolicy();

    private ThreadPoolExecutor delegate;
    private LinkedBlockingQueue<Runnable> queue;
    private StalledThreadMonitor stalledMonitor;
    private ExecutorMonitor monitor;

    private boolean statisticsOff = true;

    // queue of in-flight work
    private ConcurrentLinkedQueue<Runnable> inFlight = new ConcurrentLinkedQueue<>();

    // statistics
    private AtomicLong totalExecutionTime = new AtomicLong();
    private AtomicLong completedWorkCount = new AtomicLong();

    /**
     * Sets the number of threads always available to service the executor queue.
     *
     * @param size the number of threads.
     */
    @Property(required = false)
    @Source("$systemConfig//f3:thread.pool/@coreSize")
    public void setCoreSize(int size) {
        if (size < 0) {
            throw new IllegalArgumentException("Core pool size must be greater than or equal to 0");
        }
        this.coreSize = size;
    }

    /**
     * Sets the maximum number of threads to service the executor queue.
     *
     * @param size the number of threads.
     */
    @Property(required = false)
    @Source("$systemConfig//f3:thread.pool/@size")
    public void setMaximumSize(int size) {
        if (size < 0) {
            throw new IllegalArgumentException("The MaximumSize pool size must be greater than or equal to 0");
        }
        this.maximumSize = size;
    }

    /**
     * Sets the maximum number of work items that can be queued before the executor rejects additional work.
     *
     * @param size the maximum number of work items
     */
    @Property(required = false)
    @Source("$systemConfig//f3:thread.pool/@queueSize")
    public void setQueueSize(int size) {
        this.queueSize = size;
    }

    /**
     * Sets the period between checks for stalled threads.
     *
     * @param period the period between checks for stalled threads.
     */
    @Property(required = false)
    @Source("$systemConfig//f3:thread.pool/@stallCheckPeriod")
    public void setStallCheckPeriod(long period) {
        this.stallCheckPeriod = period;
    }

    @Property(required = false)
    @Source("$systemConfig//f3:thread.pool/@rejected.execution.handler")
    public void setRejectedExecutionHandler(String handler) {
        if ("abort".equals(handler)) {
            this.rejectedExecutionHandler = new ThreadPoolExecutor.AbortPolicy();
        } else if ("discard".equals(handler)) {
            this.rejectedExecutionHandler = new ThreadPoolExecutor.DiscardPolicy();
        } else if ("discard.oldest".equals(handler)) {
            this.rejectedExecutionHandler = new ThreadPoolExecutor.DiscardOldestPolicy();
        } else if (!"caller.runs".equals(handler)) {
            monitor.error("Invalid rejected execution handler configuration - setting to caller.runs: " + handler);
        }
    }

    /**
     * Sets the time a thread can be processing work before it is considered stalled. The default is ten minutes.
     *
     * @param stallThreshold the time a thread can be processing work before it is considered stalled
     */
    @Property(required = false)
    @Source("$systemConfig//f3:thread.pool/@stallThreshold")
    @ManagementOperation(description = "The time a thread can be processing work before it is considered stalled in milliseconds")
    public void setStallThreshold(int stallThreshold) {
        this.stallThreshold = stallThreshold;
    }

    @ManagementOperation(description = "Thread keep alive time in milliseconds")
    public long getKeepAliveTime() {
        return keepAliveTime;
    }

    @ManagementOperation(description = "Thread keep alive time in milliseconds")
    @Property(required = false)
    @Source("$systemConfig//f3:thread.pool/@keepAliveTime")
    public void setKeepAliveTime(long keepAliveTime) {
        if (keepAliveTime < 0) {
            throw new IllegalArgumentException("Keep alive time must be greater than or equal to 0");
        }

        this.keepAliveTime = keepAliveTime;
    }

    @ManagementOperation(description = "True if the thread pool expires core threads")
    public boolean isAllowCoreThreadTimeOut() {
        return allowCoreThreadTimeOut;
    }

    @ManagementOperation(description = "True if the thread pool expires core threads")
    @Property(required = false)
    @Source("$systemConfig//f3:thread.pool/@allowCoreThreadTimeOut")
    public void setAllowCoreThreadTimeOut(boolean allowCoreThreadTimeOut) {
        this.allowCoreThreadTimeOut = allowCoreThreadTimeOut;
    }

    @ManagementOperation(description = "True warnings should be issued for stalled threads")
    public boolean isCheckStalledThreads() {
        return checkStalledThreads;
    }

    @Property(required = false)
    @Source("$systemConfig//f3:thread.pool/@checkStalledThreads")
    public void setCheckStalledThreads(boolean checkStalledThreads) {
        this.checkStalledThreads = checkStalledThreads;
    }

    @Property(required = false)
    @Source("$systemConfig//f3:thread.pool/@statistics.off")
    public void setStatisticsOff(boolean statisticsOff) {
        this.statisticsOff = statisticsOff;
    }

    @ManagementOperation(description = "The time a thread can be processing work before it is considered stalled in milliseconds")
    public int getStallThreshold() {
        return stallThreshold;
    }

    @ManagementOperation(description = "Returns the approximate number of threads actively executing tasks")
    public int getActiveCount() {
        return delegate.getActiveCount();
    }

    @ManagementOperation(description = "The maximum thread pool size")
    public int getMaximumPoolSize() {
        return delegate.getMaximumPoolSize();
    }

    @ManagementOperation(description = "The maximum thread pool size")
    public void setMaximumPoolSize(int size) {
        delegate.setMaximumPoolSize(size);
    }

    @ManagementOperation(description = "The core thread pool size")
    public int getCorePoolSize() {
        return delegate.getCorePoolSize();
    }

    @ManagementOperation(description = "The core thread pool size")
    public void setCorePoolSize(int size) {
        delegate.setCorePoolSize(size);
    }

    @ManagementOperation(description = "Returns the largest size the thread pool reached")
    public int getLargestPoolSize() {
        return delegate.getLargestPoolSize();
    }

    @ManagementOperation(description = "Returns the remaining capacity the receive queue has before additional work will be rejected")
    public int getRemainingCapacity() {
        return queue.remainingCapacity();
    }

    @ManagementOperation(description = "Returns the total time the thread pool has spent executing requests in milliseconds")
    public long getTotalExecutionTime() {
        return totalExecutionTime.get();
    }

    @ManagementOperation(description = "Returns the total number of work items processed by the thread pool")
    public long getCompletedWorkCount() {
        return completedWorkCount.get();
    }

    @ManagementOperation(description = "Returns the average elapsed time to process a work request in milliseconds")
    public double getMeanExecutionTime() {
        long count = completedWorkCount.get();
        long totalTime = totalExecutionTime.get();
        return count == 0 ? 0 : totalTime / count;
    }

    @ManagementOperation(description = "Returns the longest elapsed time for a currently running work request in milliseconds")
    public long getLongestRunning() {
        Runnable runnable = inFlight.peek();
        if (runnable == null || !(runnable instanceof RunnableWrapper)) {
            // no work or statistics turned off
            return -1;
        }
        RunnableWrapper wrapper = (RunnableWrapper) runnable;
        return System.currentTimeMillis() - wrapper.start;
    }

    @ManagementOperation
    public int getCount() {
        return inFlight.size();
    }

    public RuntimeThreadPoolExecutor(@Monitor ExecutorMonitor monitor) {
        this.monitor = monitor;
    }

    @Init
    public void init() {
        if (maximumSize < coreSize) {
            throw new IllegalArgumentException("Maximum pool size cannot be less than core pool size");
        }
        if (queueSize > 0) {
            // create a bounded queue to accept work
            queue = new LinkedBlockingQueue<>(queueSize);
        } else {
            // create an unbounded queue to accept work
            queue = new LinkedBlockingQueue<>();
        }
        RuntimeThreadFactory factory = new RuntimeThreadFactory(monitor);
        delegate = new ThreadPoolExecutor(coreSize, maximumSize, Long.MAX_VALUE, TimeUnit.SECONDS, queue, factory);
        delegate.setKeepAliveTime(keepAliveTime, TimeUnit.MILLISECONDS);
        delegate.allowCoreThreadTimeOut(allowCoreThreadTimeOut);
        if (checkStalledThreads && !statisticsOff) {
            stalledMonitor = new StalledThreadMonitor();
            delegate.execute(stalledMonitor);
        }

        // set rejection strategy
        delegate.setRejectedExecutionHandler(rejectedExecutionHandler);
    }

    @Destroy
    public void stop() {
        if (stalledMonitor != null) {
            stalledMonitor.stop();
        }
        delegate.shutdown();
    }

    public void execute(Runnable runnable) {
        if (statisticsOff) {
            delegate.execute(runnable);
        } else {
            Runnable wrapper = new RunnableWrapper(runnable);
            delegate.execute(wrapper);
        }
    }

    public void shutdown() {
        delegate.shutdown();
    }

    public List<Runnable> shutdownNow() {
        return delegate.shutdownNow();
    }

    public boolean isShutdown() {
        return delegate.isShutdown();
    }

    public boolean isTerminated() {
        return delegate.isTerminated();
    }

    public boolean awaitTermination(long timeout, TimeUnit unit) throws InterruptedException {
        return false;
    }

    /**
     * Wraps submitted work to record processing statistics.
     */
    private class RunnableWrapper implements Runnable, ExecutionContext {
        private Runnable delegate;
        private boolean autoStart;
        private Thread currentThread;

        private long start = -1;

        private RunnableWrapper(Runnable delegate) {
            this.delegate = delegate;
            autoStart = !(delegate instanceof LongRunnable);
        }

        public void run() {
            ExecutionContext old = ExecutionContextTunnel.setThreadExecutionContext(this);
            try {
                if (autoStart) {
                    start();
                    delegate.run();
                    stop();
                } else {
                    delegate.run();
                }
            } finally {
                ExecutionContextTunnel.setThreadExecutionContext(old);
                clear();
            }
        }

        public void start() {
            if (currentThread != null) {
                // already started, ignore
                return;
            }
            currentThread = Thread.currentThread();
            inFlight.add(this);
            start = System.currentTimeMillis();
        }

        public void clear() {
            currentThread = null;
            inFlight.remove(this);
        }

        public void stop() {
            long elapsed = System.currentTimeMillis() - start;
            totalExecutionTime.addAndGet(elapsed);
            completedWorkCount.incrementAndGet();
        }

    }

    /**
     * Periodically scans in-flight work for threads that have exceeded a processing time threshold.
     */
    private class StalledThreadMonitor implements Runnable {
        private AtomicBoolean active = new AtomicBoolean(true);

        public void run() {
            while (active.get()) {
                try {
                    Thread.sleep(stallCheckPeriod);
                } catch (InterruptedException e) {
                    Thread.interrupted();
                    continue;
                }
                // iterator never throws ConcurrentModificationException and can therefore be used to safely traverse the in-flight work queue
                for (Runnable runnable : inFlight) {
                    if (!(runnable instanceof RunnableWrapper)) {
                        continue;
                    }
                    RunnableWrapper wrapper = (RunnableWrapper) runnable;
                    long elapsed = System.currentTimeMillis() - wrapper.start;
                    if (elapsed >= stallThreshold) {
                        Thread thread = wrapper.currentThread;
                        if (thread != null) {
                            StackTraceElement[] trace = thread.getStackTrace();
                            StringBuilder builder = new StringBuilder();
                            for (StackTraceElement element : trace) {
                                builder.append("\tat ").append(element).append("\n");
                            }
                            monitor.stalledThread(thread.getName(), elapsed, builder.toString());
                        }
                    }
                }
            }
        }

        public void stop() {
            active.set(false);
        }
    }

}
