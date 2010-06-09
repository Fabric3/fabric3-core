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

import java.text.ParseException;
import java.util.Date;
import java.util.List;
import java.util.concurrent.AbstractExecutorService;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import javax.transaction.TransactionManager;

import org.osoa.sca.annotations.Destroy;
import org.osoa.sca.annotations.Init;
import org.osoa.sca.annotations.Property;
import org.osoa.sca.annotations.Reference;
import org.quartz.CronTrigger;
import org.quartz.Job;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerConfigException;
import org.quartz.SchedulerException;
import org.quartz.SimpleTrigger;
import org.quartz.Trigger;
import org.quartz.core.JobRunShellFactory;
import org.quartz.core.QuartzScheduler;
import org.quartz.core.QuartzSchedulerResources;
import org.quartz.core.SchedulingContext;
import org.quartz.impl.SchedulerRepository;
import org.quartz.impl.StdScheduler;
import org.quartz.simpl.RAMJobStore;
import org.quartz.spi.JobFactory;
import org.quartz.spi.JobStore;
import org.quartz.spi.ThreadPool;

import org.fabric3.timer.spi.TimerService;

/**
 * Implementation of the TimerService that is backed by Quartz.
 *
 * @version $Rev$ $Date$
 */
public class QuartzTimerService extends AbstractExecutorService implements TimerService {
    public static final String GROUP = "default";

    private final ExecutorService executorService;
    private TransactionManager tm;
    private RunnableJobFactory jobFactory;
    private Scheduler scheduler;
    private long waitTime = -1;  // default Quartz value
    private boolean transactional = true;
    private String schedulerName = "Fabric3Scheduler";
    private long counter;

    public QuartzTimerService(@Reference ExecutorService executorService, @Reference TransactionManager tm) {
        this.executorService = executorService;
        this.tm = tm;
    }

    @Init
    public void init() throws SchedulerException {
        JobStore store = new RAMJobStore();
        F3ThreadPool pool = new F3ThreadPool();
        jobFactory = new RunnableJobFactoryImpl();
        JobRunShellFactory shellFactory;
        if (transactional) {
            shellFactory = new TrxJobRunShellFactory(tm);
        } else {
            shellFactory = new F3JobRunShellFactory();
        }
        scheduler = createScheduler(schedulerName, "default", store, pool, shellFactory, jobFactory);
        RunnableCleanupListener listener = new RunnableCleanupListener(jobFactory);
        scheduler.addSchedulerListener(listener);
        scheduler.start();
    }

    @Destroy
    public void destroy() throws SchedulerException {
        if (scheduler != null) {
            scheduler.shutdown(false);
        }
    }

    @Property
    public void setWaitTime(long waitTime) {
        this.waitTime = waitTime;
    }

    @Property
    public void setTransactional(boolean transactional) {
        this.transactional = transactional;
    }

    @Property
    public void setSchedulerName(String schedulerName) {
        this.schedulerName = schedulerName;
    }

    public ScheduledFuture<?> schedule(Runnable command, String expression) throws ParseException {
        CronTrigger trigger = new CronTrigger();
        trigger.setCronExpression(expression);
        String id = createId();
        trigger.setName(id);
        return schedule(id, command, trigger);
    }

    public ScheduledFuture<?> schedule(Runnable command, long delay, TimeUnit unit) {
        long time = unit.convert(delay, TimeUnit.MILLISECONDS);
        String id = createId();
        Trigger trigger = new SimpleTrigger(id, GROUP, new Date(System.currentTimeMillis() + time));
        return schedule(id, command, trigger);
    }

    public ScheduledFuture<?> scheduleWithFixedDelay(Runnable command, long initialDelay, long delay, TimeUnit unit) {
        long time = unit.convert(delay, TimeUnit.MILLISECONDS);
        String id = createId();
        SimpleTrigger trigger = new SimpleTrigger();
        trigger.setName(id);
        trigger.setRepeatInterval(time);
        trigger.setRepeatCount(SimpleTrigger.REPEAT_INDEFINITELY);
        trigger.setStartTime(new Date(System.currentTimeMillis() + initialDelay));
        return schedule(id, command, trigger);
    }

    public ScheduledFuture<?> scheduleAtFixedRate(Runnable command, long initialDelay, long period, TimeUnit unit) {
        throw new UnsupportedOperationException("Not implemented");
    }

    public void cancel(String id) throws SchedulerException {
        jobFactory.remove(id);
        scheduler.unscheduleJob(id, GROUP);
    }

    public <V> ScheduledFuture<V> schedule(Callable<V> callable, long delay, TimeUnit unit) {
        throw new UnsupportedOperationException("Not implemented");
    }

    public void shutdown() {
        throw new UnsupportedOperationException("Explicit shutdown not supported");
    }

    public List<Runnable> shutdownNow() {
        throw new UnsupportedOperationException("Explicit shutdown not supported");

    }

    public boolean isShutdown() {
        return false;
    }

    public boolean isTerminated() {
        return false;
    }

    public boolean awaitTermination(long timeout, TimeUnit unit) throws InterruptedException {
        throw new UnsupportedOperationException("Explicit shutdown not supported");
    }

    public <T> Future<T> submit(Callable<T> task) {
        throw new UnsupportedOperationException("Not implemented");

    }

    public <T> Future<T> submit(Runnable task, T result) {
        throw new UnsupportedOperationException("Not implemented");

    }

    public Future<?> submit(Runnable task) {
        throw new UnsupportedOperationException("Not implemented");

    }

    public void execute(Runnable runnable) {
        executorService.execute(runnable);
    }

    private Scheduler createScheduler(String name, String id, JobStore store, ThreadPool pool, JobRunShellFactory shellFactory, JobFactory jobFactory)
            throws SchedulerException {
        SchedulingContext context = new SchedulingContext();
        context.setInstanceId(id);

        QuartzSchedulerResources resources = new QuartzSchedulerResources();
        resources.setName(name);
        resources.setInstanceId(id);
        resources.setJobRunShellFactory(shellFactory);
        resources.setThreadPool(pool);
        resources.setJobStore(store);

        QuartzScheduler quartzScheduler = new QuartzScheduler(resources, context, waitTime, -1);
        quartzScheduler.setJobFactory(jobFactory);
        store.initialize(null, quartzScheduler.getSchedulerSignaler());
        Scheduler scheduler = new StdScheduler(quartzScheduler, context);
        shellFactory.initialize(scheduler, context);
        SchedulerRepository repository = SchedulerRepository.getInstance();
        quartzScheduler.addNoGCObject(repository); // prevents the repository from being garbage collected
        repository.bind(scheduler);    // no need to remove since it is handled in the scheduler shutdown method
        return scheduler;
    }

    private String createId() {
        long id = ++counter;
        return (String.valueOf(id));
    }

    private ScheduledFuture<?> schedule(String id, Runnable command, Trigger trigger) throws RejectedExecutionException {
        JobDetail detail = new JobDetail();
        detail.setName(id);
        detail.setGroup(GROUP);
        detail.setJobClass(Job.class);  // required by Quartz
        RunnableHolder holder = new RunnableHolderImpl(id, command, this);
        jobFactory.register(holder);
        try {
            scheduler.scheduleJob(detail, trigger);
        } catch (SchedulerException e) {
            throw new RejectedExecutionException(e);
        }
        return holder;
    }

    /**
     * Wrapper for the system WorkScheduler.
     */
    private class F3ThreadPool implements ThreadPool {

        public boolean runInThread(final Runnable runnable) {
            executorService.execute(runnable);
            return true;
        }

        public int blockForAvailableThreads() {
            return 5; // TODO WorkScheduler doesn't provide this functionality
        }

        public void initialize() throws SchedulerConfigException {
        }

        public void shutdown(boolean b) {
        }

        public int getPoolSize() {
            return 5;  // TODO WorkScheduler doesn't provide this functionality
        }
    }

}
