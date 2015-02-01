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
package org.fabric3.monitor.spi.appender;

import java.nio.ByteBuffer;

import org.fabric3.api.host.Fabric3Exception;

/**
 * Writes monitor events to a sink such as a file or stream.
 */
public interface Appender {

    /**
     * Initializes the appender to record events.
     *
     * @throws Fabric3Exception if an initialization error occurs
     */
    void start();

    /**
     * Closes any open resources used by the appender.
     *
     * @throws Fabric3Exception if an exception occurs closing resources
     */
    void stop();

    /**
     * Writes an event to the sink.
     *
     * @param buffer the event buffer
     * @throws Fabric3Exception if a writePrefix error occurs
     */
    void write(ByteBuffer buffer) throws Fabric3Exception;

}
