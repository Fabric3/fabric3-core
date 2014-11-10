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
import javax.xml.ws.WebServiceFeature;
import javax.xml.ws.handler.Handler;
import java.io.ByteArrayInputStream;
import java.net.URI;
import java.net.URL;
import java.util.List;

import com.sun.xml.ws.api.BindingID;
import org.fabric3.binding.ws.metro.provision.MetroWsdlWireSourceDefinition;
import org.fabric3.binding.ws.metro.provision.ServiceEndpointDefinition;
import org.fabric3.binding.ws.metro.runtime.core.DocumentInvoker;
import org.fabric3.binding.ws.metro.runtime.core.EndpointConfiguration;
import org.fabric3.binding.ws.metro.runtime.core.EndpointService;
import org.fabric3.binding.ws.metro.runtime.core.F3Provider;
import org.fabric3.binding.ws.metro.runtime.policy.FeatureResolver;
import org.fabric3.binding.ws.metro.util.BindingIdResolver;
import org.fabric3.spi.container.ContainerException;
import org.fabric3.spi.container.binding.handler.BindingHandlerRegistry;
import org.fabric3.spi.container.wire.InvocationChain;
import org.fabric3.spi.container.wire.Wire;
import org.fabric3.spi.model.physical.PhysicalWireTargetDefinition;
import org.fabric3.spi.repository.ArtifactCache;
import org.fabric3.spi.repository.CacheException;
import org.oasisopen.sca.annotation.Reference;

/**
 * Source wire attacher that provisions WSDL-based web service endpoints.
 */
public class MetroWsdlSourceWireAttacher extends AbstractMetroSourceWireAttacher<MetroWsdlWireSourceDefinition> {
    private FeatureResolver featureResolver;
    private BindingIdResolver bindingIdResolver;
    private ArtifactCache cache;

    public MetroWsdlSourceWireAttacher(@Reference FeatureResolver featureResolver,
                                       @Reference BindingIdResolver bindingIdResolver,
                                       @Reference EndpointService endpointService,
                                       @Reference ArtifactCache cache,
                                       @Reference BindingHandlerRegistry handlerRegistry) {
        super(endpointService, handlerRegistry);
        this.featureResolver = featureResolver;
        this.bindingIdResolver = bindingIdResolver;
        this.cache = cache;
    }

    public void attach(MetroWsdlWireSourceDefinition source, PhysicalWireTargetDefinition target, Wire wire) throws ContainerException {
        ClassLoader old = Thread.currentThread().getContextClassLoader();
        try {
            ServiceEndpointDefinition endpointDefinition = source.getEndpointDefinition();
            QName serviceName = endpointDefinition.getServiceName();
            QName portName = endpointDefinition.getPortName();
            URI servicePath = endpointDefinition.getServicePath();
            List<InvocationChain> invocationChains = wire.getInvocationChains();
            List<QName> requestedIntents = source.getIntents();

            Thread.currentThread().setContextClassLoader(getClass().getClassLoader());

            BindingID bindingId = bindingIdResolver.resolveBindingId(requestedIntents);
            WebServiceFeature[] features = featureResolver.getFeatures(requestedIntents);

            String path = servicePath.toString();
            if (!path.startsWith("/")) {
                path = "/" + path;
            }

            String wsdl = source.getWsdl();
            URL wsdlLocation = cache.cache(servicePath, new ByteArrayInputStream(wsdl.getBytes()));
            List<URL> generatedSchemas = null;

            List<Handler> handlers = createHandlers(source);

            URI serviceUri = source.getServiceUri();

            DocumentInvoker invoker = new DocumentInvoker(invocationChains);

            EndpointConfiguration configuration = new EndpointConfiguration(serviceUri,
                                                                            F3Provider.class,
                                                                            serviceName,
                                                                            portName,
                                                                            path,
                                                                            wsdlLocation,
                                                                            invoker,
                                                                            features,
                                                                            bindingId,
                                                                            null,
                                                                            generatedSchemas,
                                                                            handlers);
            endpointService.registerService(configuration);
        } catch (CacheException e) {
            throw new ContainerException(e);
        } finally {
            Thread.currentThread().setContextClassLoader(old);
        }
    }

    public void detach(MetroWsdlWireSourceDefinition source, PhysicalWireTargetDefinition target) throws ContainerException {
        try {
            URI servicePath = source.getEndpointDefinition().getServicePath();
            cache.remove(servicePath);
        } catch (CacheException e) {
            throw new ContainerException(e);
        }
    }

}