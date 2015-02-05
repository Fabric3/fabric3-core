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
package org.fabric3.binding.ws.metro.runtime.wire;

import javax.xml.namespace.QName;
import javax.xml.ws.handler.Handler;
import java.io.ByteArrayInputStream;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.fabric3.api.annotation.wire.Key;
import org.fabric3.api.host.Fabric3Exception;
import org.fabric3.binding.ws.metro.provision.MetroJavaWireSourceDefinition;
import org.fabric3.binding.ws.metro.provision.ServiceEndpointDefinition;
import org.fabric3.binding.ws.metro.runtime.core.EndpointConfiguration;
import org.fabric3.binding.ws.metro.runtime.core.EndpointService;
import org.fabric3.binding.ws.metro.runtime.core.JaxbInvoker;
import org.fabric3.spi.container.binding.handler.BindingHandlerRegistry;
import org.fabric3.spi.container.wire.InvocationChain;
import org.fabric3.spi.container.wire.Wire;
import org.fabric3.spi.model.physical.PhysicalWireTargetDefinition;
import org.fabric3.spi.repository.ArtifactCache;
import org.oasisopen.sca.annotation.Reference;

/**
 * Source wire attacher that provisions Java-based web service endpoints.
 */
@Key("org.fabric3.binding.ws.metro.provision.MetroJavaWireSourceDefinition")
public class MetroJavaSourceWireAttacher extends AbstractMetroSourceWireAttacher<MetroJavaWireSourceDefinition> {
    private ArtifactCache artifactCache;

    public MetroJavaSourceWireAttacher(@Reference ArtifactCache artifactCache,
                                       @Reference EndpointService endpointService,
                                       @Reference BindingHandlerRegistry handlerRegistry) {
        super(endpointService, handlerRegistry);
        this.artifactCache = artifactCache;
    }

    public void attach(MetroJavaWireSourceDefinition source, PhysicalWireTargetDefinition target, Wire wire) throws Fabric3Exception {
        ServiceEndpointDefinition endpointDefinition = source.getEndpointDefinition();
        QName serviceName = endpointDefinition.getServiceName();
        QName portName = endpointDefinition.getPortName();
        URI servicePath = endpointDefinition.getServicePath();
        List<InvocationChain> invocationChains = wire.getInvocationChains();
        URL wsdlLocation = source.getWsdlLocation();

        Class<?> seiClass = source.getInterface();
        ClassLoader classLoader = seiClass.getClassLoader();

        ClassLoader old = Thread.currentThread().getContextClassLoader();

        try {
            // SAAJ classes are needed from the TCCL
            Thread.currentThread().setContextClassLoader(classLoader);

            // cache the WSDL and schemas
            URL generatedWsdl = null;
            List<URL> generatedSchemas = null;
            String wsdl = source.getWsdl();
            if (wsdl != null) {
                wsdlLocation = artifactCache.cache(servicePath, new ByteArrayInputStream(wsdl.getBytes()));
                generatedWsdl = wsdlLocation;
                generatedSchemas = cacheSchemas(servicePath, source);
            }

            String path = servicePath.toString();
            if (!path.startsWith("/")) {
                path = "/" + path;
            }

            List<Handler> handlers = createHandlers(source);

            URI serviceUri = source.getServiceUri();

            JaxbInvoker invoker = new JaxbInvoker(invocationChains);
            EndpointConfiguration configuration = new EndpointConfiguration(serviceUri,
                                                                            seiClass,
                                                                            serviceName,
                                                                            portName,
                                                                            path,
                                                                            wsdlLocation,
                                                                            invoker,
                                                                            generatedWsdl,
                                                                            generatedSchemas,
                                                                            handlers);

            endpointService.registerService(configuration);
        } finally {
            Thread.currentThread().setContextClassLoader(old);
        }
    }

    public void detach(MetroJavaWireSourceDefinition source, PhysicalWireTargetDefinition target) throws Fabric3Exception {
        ServiceEndpointDefinition endpointDefinition = source.getEndpointDefinition();
        URI servicePath = endpointDefinition.getServicePath();
        String path = servicePath.toString();
        if (!path.startsWith("/")) {
            path = "/" + path;
        }
        artifactCache.remove(servicePath);
        String wsdl = source.getWsdl();
        if (wsdl != null) {
            removeCachedSchemas(servicePath, source);
        }

        endpointService.unregisterService(path);
    }

    private List<URL> cacheSchemas(URI servicePath, MetroJavaWireSourceDefinition source) throws Fabric3Exception {
        List<URL> schemas = new ArrayList<>();
        for (Map.Entry<String, String> entry : source.getSchemas().entrySet()) {
            URI uri = URI.create(servicePath + "/" + entry.getKey());
            ByteArrayInputStream bas = new ByteArrayInputStream(entry.getValue().getBytes());
            URL url = artifactCache.cache(uri, bas);
            schemas.add(url);
        }
        return schemas;
    }

    private void removeCachedSchemas(URI servicePath, MetroJavaWireSourceDefinition source) throws Fabric3Exception {
        for (Map.Entry<String, String> entry : source.getSchemas().entrySet()) {
            URI uri = URI.create(servicePath + "/" + entry.getKey());
            artifactCache.remove(uri);
        }
    }

}

