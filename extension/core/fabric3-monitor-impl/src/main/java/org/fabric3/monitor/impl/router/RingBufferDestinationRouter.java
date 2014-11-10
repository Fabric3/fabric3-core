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
package org.fabric3.monitor.impl.router;

import org.fabric3.api.host.monitor.DestinationRouter;
import org.fabric3.monitor.spi.event.MonitorEventEntry;

/**
 * Routes to a destination using a ring buffer.
 */
public interface RingBufferDestinationRouter extends DestinationRouter {

    /**
     * Returns a buffer entry to writePrefix to.
     * <p/>
     * This call should be wrapped in a <code>try..finally</code> block where {@link #publish(MonitorEventEntry)} is called in the finally section.
     *
     * @return the buffer entry
     */
    MonitorEventEntry get();

    /**
     * Publishes the buffer entry.
     *
     * @param entry the buffer entry.
     */
    void publish(MonitorEventEntry entry);


}
