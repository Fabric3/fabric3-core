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
package org.fabric3.binding.rs.runtime;

import java.net.URI;

import org.fabric3.api.annotation.monitor.Info;

/**
 *
 */
public interface RsWireAttacherMonitor {

    /**
     * Callback when a service has been provisioned as a REST endpoint
     *
     * @param address the endpoint address
     */
    @Info("REST endpoint provisioned at {0}")
    void provisionedEndpoint(String address);

    /**
     * Callback when a service endpoint has been de-provisioned
     *
     * @param address the endpoint address
     */
    @Info("REST endpoint removed at {0}")
    void removedEndpoint(String address);

    @Info("Servlet container not configured. REST context disabled: {0}")
    void noServletContainer(URI uri);

}
