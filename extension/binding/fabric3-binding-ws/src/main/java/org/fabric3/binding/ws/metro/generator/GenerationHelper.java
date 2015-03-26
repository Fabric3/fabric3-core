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

package org.fabric3.binding.ws.metro.generator;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.fabric3.api.binding.ws.model.WsBinding;
import org.fabric3.api.host.Fabric3Exception;
import org.fabric3.api.model.type.component.BindingHandler;
import org.fabric3.binding.ws.metro.provision.ConnectionConfiguration;
import org.fabric3.spi.model.physical.PhysicalBindingHandler;

/**
 *
 */
public class GenerationHelper {
    private GenerationHelper() {
    }

    /**
     * Parses HTTP connection information and creates a connection configuration.
     *
     * @param binding the binding definition
     * @return the HTTP configuration
     * @throws Fabric3Exception if a configuration value is invalid
     */
    public static ConnectionConfiguration createConnectionConfiguration(WsBinding binding) throws Fabric3Exception {
        ConnectionConfiguration configuration = new ConnectionConfiguration();
        Map<String, String> configProperties = binding.getConfiguration();
        if (configProperties != null) {
            String connectTimeout = configProperties.get("connectTimeout");
            if (connectTimeout != null) {
                try {
                    configuration.setConnectTimeout(Integer.parseInt(connectTimeout));
                } catch (NumberFormatException e) {
                    throw new Fabric3Exception("Invalid connectTimeout", e);
                }
            }
            String requestTimeout = configProperties.get("requestTimeout");
            if (requestTimeout != null) {
                try {
                    configuration.setRequestTimeout(Integer.parseInt(requestTimeout));
                } catch (NumberFormatException e) {
                    throw new Fabric3Exception("Invalid requestTimeout", e);
                }
            }
            String clientStreamingChunkSize = configProperties.get("clientStreamingChunkSize");
            if (clientStreamingChunkSize != null) {
                try {
                    configuration.setClientStreamingChunkSize(Integer.parseInt(clientStreamingChunkSize));
                } catch (NumberFormatException e) {
                    throw new Fabric3Exception("Invalid clientStreamingChunkSize", e);
                }
            }
        }
        return configuration;
    }

    public static List<PhysicalBindingHandler> generateBindingHandlers(URI domainUri, WsBinding binding) {
        List<PhysicalBindingHandler> handlers = new ArrayList<>();
        for (BindingHandler handlerDefinition : binding.getHandlers()) {
            // URIs specified in handler elements in a composite are relative and must be made absolute
            URI resolvedUri = URI.create(domainUri.toString() + "/" + handlerDefinition.getTarget());
            handlers.add(new PhysicalBindingHandler(resolvedUri));
        }
        return handlers;
    }

}
