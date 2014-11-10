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
package org.fabric3.api.host.monitor;

/**
 * Creates monitor proxies.
 */
public interface MonitorProxyServiceExtension {

    /**
     * Create a proxy using the runtime as the event source that routes to the default monitor destination.
     *
     * @param type the proxy interface
     * @param <T>  the proxy type
     * @return the proxy
     * @throws MonitorCreationException if an error occurs creating the proxy
     */
    <T> T createMonitor(Class<T> type) throws MonitorCreationException;

    /**
     * Create a proxy using the given monitorable as the event source and the given destination.
     *
     * @param type        the proxy interface
     * @param monitorable the event source
     * @param destination the destination name
     * @param <T>         the proxy type
     * @return the proxy
     * @throws MonitorCreationException if an error occurs creating the proxy
     */
    <T> T createMonitor(Class<T> type, Monitorable monitorable, String destination) throws MonitorCreationException;

}