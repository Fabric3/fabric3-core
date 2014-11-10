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
package org.fabric3.transport.jetty;

import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;

import org.fabric3.spi.host.ServletHost;

/**
 * Implementations provide a Jetty transport service to the runtime.
 */
public interface JettyService extends ServletHost {

    /**
     * Returns the active Jetty server
     *
     * @return the active Jetty server
     */
    Server getServer();

    /**
     * Registers a handler with the Jetty service.
     *
     * @param handler the handler to register
     */
    void registerHandler(Handler handler);

    /**
     * Registers a registered handler.
     *
     * @param handler the handler to remove
     */
    void removeHandler(Handler handler);

}
