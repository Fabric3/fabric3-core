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
 *
 * Portions originally based on Apache Tuscany 2007
 * licensed under the Apache 2.0 license.
 */
package org.fabric3.spi.container.invocation;

/**
 * Cache of {@link WorkContext}s associated with runtime threads.
 * <p/>
 * On runtimes with managed thread pools, the cache uses {@link Fabric3Thread} to store the context; on other runtimes a thread local is used.
 */
public final class WorkContextCache {
    private static final ThreadLocal<WorkContext> CONTEXT = new ThreadLocal<>();

    private WorkContextCache() {
    }

    /**
     * Returns the WorkContext for the current thread.
     *
     * @return the WorkContext for the current thread
     */
    public static WorkContext getThreadWorkContext() {
        Thread thread = Thread.currentThread();
        if (thread instanceof Fabric3Thread) {
            Fabric3Thread fabric3Thread = (Fabric3Thread) thread;
            WorkContext workContext = fabric3Thread.getWorkContext();
            if (workContext == null) {
                workContext = new WorkContext();
                fabric3Thread.setWorkContext(workContext);
            }
            return workContext;
        } else {
            WorkContext workContext = CONTEXT.get();
            if (workContext == null) {
                workContext = new WorkContext();
                CONTEXT.set(workContext);
            }
            return workContext;
        }
    }

    /**
     * Resets and returns the WorkContext for the current thread.
     *
     * @return the WorkContext for the current thread
     */
    public static WorkContext getAndResetThreadWorkContext() {
        WorkContext workContext = getThreadWorkContext();
        workContext.reset();
        return workContext;
    }
}
