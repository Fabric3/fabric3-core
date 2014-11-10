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
package org.fabric3.spi.threadpool;

/**
 * Associates the active execution context with the current thread.
 */
public class ExecutionContextTunnel {
    private static final ThreadLocal<ExecutionContext> CONTEXT = new ThreadLocal<>();

    /**
     * Associates the active execution context with the current thread.
     *
     * @param context the active execution context
     * @return the previously associated execution context or null
     */
    public static ExecutionContext setThreadExecutionContext(ExecutionContext context) {
        ExecutionContext old = CONTEXT.get();
        CONTEXT.set(context);
        return old;
    }

    /**
     * Returns the ExecutionContext for the current thread or null if the host environment implements is own thread pooling scheme.
     *
     * @return the ExecutionContext for the current thread or null
     */
    public static ExecutionContext getThreadExecutionContext() {
        return CONTEXT.get();
    }

}