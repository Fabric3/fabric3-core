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

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

import org.fabric3.spi.container.invocation.Fabric3Thread;

/**
 * Factory that returns named threads and sets an uncaught exception handler that forwards exceptions to a monitor.
 */
public class RuntimeThreadFactory implements ThreadFactory {
    private AtomicInteger number = new AtomicInteger(1);
    private ThreadGroup group;
    private String prefix;
    private RuntimeUncaughtExceptionHandler handler;

    public RuntimeThreadFactory(ExecutorMonitor monitor) {
        SecurityManager s = System.getSecurityManager();
        group = (s != null) ? s.getThreadGroup() : Thread.currentThread().getThreadGroup();
        prefix = "pooled:";
        handler = new RuntimeUncaughtExceptionHandler(monitor);
    }

    public Thread newThread(Runnable r) {
        Thread thread = new Fabric3Thread(group, r, prefix + number.getAndIncrement(), 0);
        if (thread.isDaemon()) {
            thread.setDaemon(false);
        }
        if (thread.getPriority() != Thread.NORM_PRIORITY) {
            thread.setPriority(Thread.NORM_PRIORITY);
        }
        thread.setUncaughtExceptionHandler(handler);
        return thread;
    }

    private class RuntimeUncaughtExceptionHandler implements Thread.UncaughtExceptionHandler {
        private ExecutorMonitor monitor;

        private RuntimeUncaughtExceptionHandler(ExecutorMonitor monitor) {
            this.monitor = monitor;
        }

        public void uncaughtException(Thread t, Throwable e) {
            monitor.threadError(e);
        }
    }
}

