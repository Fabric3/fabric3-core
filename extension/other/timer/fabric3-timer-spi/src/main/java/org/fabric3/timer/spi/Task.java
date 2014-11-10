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
package org.fabric3.timer.spi;

/**
 * Implementations are executed on a recurring basis by the {@link TimerService}.
 */
public interface Task extends Runnable {

    /**
     * Indicates a task has finished its scheduled period and should not be executed again.
     */
    int DONE = -1;

    /**
     * Returns next time in milliseconds as an offset of the current time when the task should next be executed.
     *
     * @return the next execution time offset or {@link #DONE}
     */
    long nextInterval();

}