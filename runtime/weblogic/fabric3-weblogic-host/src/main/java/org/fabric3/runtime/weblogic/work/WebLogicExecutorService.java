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
            wm = (WorkManager) defaultMethod.invoke(null);
        } catch (ClassNotFoundException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
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
            Object wmWLSFactory = instanceMethod.invoke(null);
            wmObj = defaultMethod.invoke(wmWLSFactory);
            Class<?> wmCommonJClass = Class.forName("weblogic.work.commonj.CommonjWorkManagerImpl", true, classLoader);
            Class<?> wmWLSInterface = Class.forName("weblogic.work.WorkManager", true, classLoader);
            Constructor<?> wmCommonJConstructor = wmCommonJClass.getConstructor(new Class[]{wmWLSInterface});
            wm = (WorkManager) wmCommonJConstructor.newInstance(wmObj);
        } catch (NoSuchMethodException | ClassNotFoundException | InvocationTargetException | InstantiationException | IllegalAccessException e) {
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
        } catch (WorkException | ExecutorInitException e) {
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
