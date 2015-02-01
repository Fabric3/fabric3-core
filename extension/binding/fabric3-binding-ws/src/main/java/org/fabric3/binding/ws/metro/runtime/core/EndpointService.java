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
package org.fabric3.binding.ws.metro.runtime.core;

import java.net.URI;
import java.net.URL;

import org.fabric3.api.host.Fabric3Exception;

/**
 * Activates and de-activates web service endpoints.
 */
public interface EndpointService {

    /**
     * Registers a web service endpoint.
     *
     * @param configuration the endpoint configuration
     * @throws Fabric3Exception if there is an error registering the endpoint
     */
    void registerService(EndpointConfiguration configuration) throws Fabric3Exception;

    /**
     * Unregisters a web service endpoint.
     *
     * @param path the endpoint path
     * @throws Fabric3Exception if there is an error unregistering the endpoint
     */
    void unregisterService(String path) throws Fabric3Exception;

    /**
     * Returns the endpoint URL for the service.
     *
     * @param serviceUri the service structural URI
     * @return the endpoint URL
     */
    URL getEndpointUrl(URI serviceUri);

}
