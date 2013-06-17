/*
 * Fabric3
 * Copyright (c) 2009-2013 Metaform Systems
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

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import commonj.work.Work;
import commonj.work.WorkException;
import commonj.work.WorkManager;
import org.oasisopen.sca.annotation.EagerInit;
import org.oasisopen.sca.annotation.Init;
import weblogic.logging.LoggingHelper;

/**
 * Delegates to a WebLogic <code>WorkManager</code>.
 * <p/>
 * If no WorkManager is configured for the runtime, the default WebLogic WorkManager will be used.
 * <p/>
 * TODO Implement lookup of configured work managers
 */
@EagerInit
public class WebLogicExecutorService implements ExecutorService {
    private WorkManager workManager;
    private Logger logger;

    public WebLogicExecutorService() {
        // can't use a monitor since this is a primordial service
        logger = LoggingHelper.getServerLogger();
    }

    @Init
    public void init() throws ExecutorInitException {
        // returns the default work manager
        workManager = getDefaultWorkManager();
    }

    private WorkManager getDefaultWorkManager() throws ExecutorInitException {
        WorkManager wm = null;
        ClassLoader classLoader = WorkManager.class.getClassLoader();
        try {
            // prior 10.3.5 version
            Class<?> clazz = Class.forName("com.bea.core.workmanager.WorkManagerFactory", true, classLoader);
            Method defaultMethod = clazz.getMethod("getDefault", new Class[0]);
            wm = (WorkManager) defaultMethod.invoke(null, new Object[0]);
        } catch (ClassNotFoundException e) {
            logger.log(Level.FINEST, "Did not initialize com.bea.core.workmanager.WorkManagerFactory", e);
        } catch (NoSuchMethodException e) {
            logger.log(Level.FINEST, "Did not initialize com.bea.core.workmanager.WorkManagerFactory", e);
        } catch (InvocationTargetException e) {
            logger.log(Level.FINEST, "Did not initialize com.bea.core.workmanager.WorkManagerFactory", e);
        } catch (IllegalAccessException e) {
            logger.log(Level.FINEST, "Did not initialize com.bea.core.workmanager.WorkManagerFactory", e);
        }
        if (wm != null) {
            return wm;
        }
        Object wmObj;
        try {
            // 10.3.5 and upper version
            Class<?> wmWLSFactoryClass = Class.forName("weblogic.work.WorkManagerFactory", true, classLoader);
            Method instanceMethod = wmWLSFactoryClass.getMethod("getInstance", new Class[0]);
            Method defaultMethod = wmWLSFactoryClass.getMethod("getDefault", new Class[0]);
            Object wmWLSFactory = instanceMethod.invoke(null, new Object[0]);
            wmObj = defaultMethod.invoke(wmWLSFactory, new Object[0]);
            Class<?> wmCommonJClass = Class.forName("weblogic.work.commonj.CommonjWorkManagerImpl", true, classLoader);
            Class<?> wmWLSInterface = Class.forName("weblogic.work.WorkManager", true, classLoader);
            Constructor<?> wmCommonJConstructor = wmCommonJClass.getConstructor(new Class[]{wmWLSInterface});
            wm = (WorkManager) wmCommonJConstructor.newInstance(new Object[]{wmObj});
        } catch (NoSuchMethodException e) {
            throw new ExecutorInitException(e);
        } catch (IllegalAccessException e) {
            throw new ExecutorInitException(e);
        } catch (InstantiationException e) {
            throw new ExecutorInitException(e);
        } catch (InvocationTargetException e) {
            throw new ExecutorInitException(e);
        } catch (ClassNotFoundException e) {
            throw new ExecutorInitException(e);
        }
        return wm;
    }

    public void execute(Runnable command) {
        try {
            if (workManager == null) {
                workManager = getDefaultWorkManager();
            }
            workManager.schedule(new CommonJWorkWrapper(command));
        } catch (WorkException e) {
            LogRecord record = new LogRecord(Level.SEVERE, "Error submitting work");
            record.setThrown(e);
            logger.log(record);
        } catch (ExecutorInitException e) {
            LogRecord record = new LogRecord(Level.SEVERE, "Error submitting work");
            record.setThrown(e);
            logger.log(record);
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

    /**
     * Wrapper for work requests to be processed by a WebLogic WorkManager.
     */
    private class CommonJWorkWrapper implements Work {
        private Runnable work;

        private CommonJWorkWrapper(Runnable work) {
            this.work = work;
        }

        public void release() {

        }

        public boolean isDaemon() {
            return false;
        }

        public void run() {
            work.run();
        }
    }
}
