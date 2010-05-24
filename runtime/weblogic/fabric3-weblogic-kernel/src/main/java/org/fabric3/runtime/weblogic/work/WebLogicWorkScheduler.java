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
package org.fabric3.runtime.weblogic.work;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import com.bea.core.workmanager.WorkManagerFactory;
import commonj.work.Work;
import commonj.work.WorkException;
import commonj.work.WorkManager;
import org.osoa.sca.annotations.EagerInit;
import org.osoa.sca.annotations.Init;

import org.fabric3.api.annotation.monitor.Monitor;
import org.fabric3.host.work.PausableWork;
import org.fabric3.host.work.WorkScheduler;

/**
 * Delegates to a WebLogic <code>WorkManager</code>.
 * <p/>
 * If no WorkManager is configured for the runtime, the default WebLogic WorkManager will be used.
 * <p/>
 * TODO Implement lookup of configured work managers
 *
 * @version $Rev$ $Date$
 */
@EagerInit
public class WebLogicWorkScheduler implements WorkScheduler {
    private WebLogicWorkSchedulerMonitor monitor;
    private WorkManager workManager;

    public WebLogicWorkScheduler(@Monitor WebLogicWorkSchedulerMonitor monitor) {
        this.monitor = monitor;
    }

    @Init
    public void init() {
        // returns the default work manager
        workManager = WorkManagerFactory.getDefault();
    }

    public <T extends PausableWork> void scheduleWork(T work) {
        try {
            workManager.schedule(new CommonJWorkWrapper(work));
        } catch (WorkException e) {
            monitor.scheduleError(e);
        }
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

    /**
     * Wrapper for work requests to be processed by a WebLogic WorkManager.
     */
    private class CommonJWorkWrapper implements Work {
        private PausableWork work;

        private CommonJWorkWrapper(PausableWork work) {
            this.work = work;
        }

        public void release() {
            work.stop();
        }

        public boolean isDaemon() {
            return work.isDaemon();
        }

        public void run() {
            work.run();
        }
    }
}
