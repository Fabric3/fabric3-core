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
 * A reference to the current runtime execution context. An execution context can be used to mark the beginning and end of a unit-of-work. This is
 * useful for threads that handle long-running processes and will avoid the runtime marking such threads as stalled.
 */
public interface ExecutionContext {

    /**
     * Signals the start of a request.
     */
    public void start();

    /**
     * Signals the end of a request.
     */
    public void stop();

    /**
     * Clears the current request.
     */
    public void clear();

}
