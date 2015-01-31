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

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;

import org.fabric3.api.annotation.monitor.Monitor;
import org.fabric3.binding.ws.metro.MetroBindingMonitor;
import org.fabric3.api.host.ContainerException;
import org.fabric3.spi.host.ServletHost;
import org.oasisopen.sca.annotation.Init;
import org.oasisopen.sca.annotation.Reference;

/**
 *
 */
public class EndpointServiceImpl implements EndpointService {
    private ExecutorService executorService;
    private ServletHost servletHost;
    private MetroBindingMonitor monitor;
    private Map<URI, URL> endpointUrls;
    private MetroServlet metroServlet;

    public EndpointServiceImpl(@Reference(name = "executorService") ExecutorService executorService,
                               @Reference ServletHost servletHost,
                               @Monitor MetroBindingMonitor monitor) {
        this.executorService = executorService;
        this.monitor = monitor;
        this.servletHost = servletHost;
        endpointUrls = new HashMap<>();
    }

    @Init
    public void init() {
        metroServlet = new MetroServlet(executorService);
    }

    public void registerService(EndpointConfiguration configuration) throws ContainerException {
        String servicePath = configuration.getServicePath();
        if (servletHost.isMappingRegistered(servicePath)) {
            // wire re-provisioned
            unregisterService(servicePath);
        }
        servletHost.registerMapping(servicePath, metroServlet);
        // register <endpoint-url/mex> address for serving WS-MEX requests
        servletHost.registerMapping(servicePath + "/mex", metroServlet);
        metroServlet.registerService(configuration);

        try {
            URL endpointUrl = new URL(servletHost.getBaseHttpUrl().toString() + servicePath);
            endpointUrls.put(configuration.getServiceUri(), endpointUrl);
            monitor.endpointProvisioned(servicePath);
        } catch (MalformedURLException e) {
            throw new ContainerException("Error registering service: " + servicePath, e);
        }
    }

    public void unregisterService(String servicePath) throws ContainerException {
        try {
            servletHost.unregisterMapping(servicePath);
            servletHost.unregisterMapping(servicePath + "/mex");
            metroServlet.unregisterService(servicePath);

            URL endpointUrl = new URL(servletHost.getBaseHttpUrl().toString() + servicePath);
            for (Map.Entry<URI, URL> entry : endpointUrls.entrySet()) {
                if (entry.getValue().equals(endpointUrl)) {
                    endpointUrls.remove(entry.getKey());
                    break;
                }
            }

            monitor.endpointRemoved(servicePath);
        } catch (MalformedURLException e) {
            throw new ContainerException("Error registering service: " + servicePath, e);
        }
    }

    public URL getEndpointUrl(URI serviceUri) {
        return endpointUrls.get(serviceUri);
    }
}
