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
package org.fabric3.api;

/**
 * Typed interface to a monitor channel.
 */
public interface MonitorChannel {

    /**
     * Issues a monitor event with a SEVERE level.
     *
     * @param message the event message
     * @param args    the event arguments
     */
    void severe(String message, Object... args);

    /**
     * Issues a monitor event with a WARN level.
     *
     * @param message the event message
     * @param args    the event arguments
     */
    void warn(String message, Object... args);

    /**
     * Issues a monitor event with a INFO level.
     *
     * @param message the event message
     * @param args    the event arguments
     */
    void info(String message, Object... args);

    /**
     * Issues a monitor event with a DEBUG level.
     *
     * @param message the event message
     * @param args    the event arguments
     */
    void debug(String message, Object... args);

    /**
     * Issues a monitor event with a TRACE level.
     *
     * @param message the event message
     * @param args    the event arguments
     */
    void trace(String message, Object... args);

}
