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
package org.fabric3.threadpool;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.AbstractExecutorService;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.osoa.sca.annotations.Destroy;
import org.osoa.sca.annotations.EagerInit;
import org.osoa.sca.annotations.Init;
import org.osoa.sca.annotations.Property;

import org.fabric3.host.work.DefaultPausableWork;
import org.fabric3.host.work.PausableWork;
import org.fabric3.host.work.WorkScheduler;
import org.fabric3.management.WorkSchedulerMBean;

/**
 * Thread pool based implementation of the work scheduler.
 */
@EagerInit
public class ThreadPoolWorkScheduler extends AbstractExecutorService implements WorkScheduler, WorkSchedulerMBean {

    private ThreadPoolExecutor executor;
    private final Set<PausableWork> workInProgress = new CopyOnWriteArraySet<PausableWork>();
    private final AtomicBoolean paused = new AtomicBoolean();
    private final ReadWriteLock readWriteLock = new ReentrantReadWriteLock();

    private int size = 20;
    private boolean pauseOnStart = false;

    /**
     * Sets the pool size.
     *
     * @param size Pool size.
     */
    @Property
    public void setSize(int size) {
        this.size = size;
    }

    /**
     * Indicates whether to start this in a paused state.
     *
     * @param pauseOnStart True if we want to start this in a paused state.
     */
    @Property
    public void setPauseOnStart(boolean pauseOnStart) {
        this.pauseOnStart = pauseOnStart;
    }

    /**
     * Initializes the thread-pool. Supports unbounded work with a fixed pool size. If all the workers are busy, work gets queued.
     */
    @Init
    public void init() {
        executor = new ThreadPoolExecutor(size, size, Long.MAX_VALUE, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>());
        paused.set(pauseOnStart);
    }

    @Destroy
    public void stop() {

        Lock lock = readWriteLock.writeLock();
        lock.lock();
        try {
            for (PausableWork pausableWork : workInProgress) {
                pausableWork.stop();
            }
            executor.shutdown();
        } finally {
            lock.unlock();
        }

    }

    public <T extends PausableWork> void scheduleWork(T work) {

        Lock lock = readWriteLock.readLock();
        lock.lock();
        try {
            Runnable runnable = new DecoratingWork(work);
            executor.submit(runnable);
        } finally {
            lock.unlock();
        }

    }

    public void execute(final Runnable runnable) {
        scheduleWork(new DefaultPausableWork() {
            public void execute() {
                runnable.run();
            }
        });
    }

    public void shutdown() {
    }

    public List<Runnable> shutdownNow() {
        return Collections.emptyList();
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

    private class DecoratingWork implements Runnable {

        private PausableWork work;

        public DecoratingWork(PausableWork work) {
            this.work = work;
        }

        public void run() {

            if (paused.get()) {
                work.pause();
            }
            workInProgress.add(work);

            try {
                work.run();
            } finally {
                workInProgress.remove(work);
            }

        }

    }

    // ------------------ Management operations
    public int getActiveCount() {
        return executor.getActiveCount();
    }

    public int getPoolSize() {
        return executor.getCorePoolSize();
    }

    public void pause() {

        if (paused.get()) {
            return;
        }

        Lock lock = readWriteLock.writeLock();
        lock.lock();
        try {
            paused.set(true);
            for (PausableWork pausableWork : workInProgress) {
                pausableWork.pause();
            }
        } finally {
            lock.unlock();
        }

    }

    public void setPoolSize(int poolSize) {
        executor.setCorePoolSize(poolSize);
    }

    public void start() {

        if (!paused.get()) {
            return;
        }

        Lock lock = readWriteLock.writeLock();
        lock.lock();
        try {
            paused.set(false);
            for (PausableWork pausableWork : workInProgress) {
                pausableWork.start();
            }
        } finally {
            lock.unlock();
        }

    }

    public Status getStatus() {
        return paused.get() ? Status.PAUSED : Status.STARTED;
    }

}
