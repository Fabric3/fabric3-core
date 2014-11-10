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
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;
import java.util.concurrent.ExecutorService;

import com.sun.xml.wss.SecurityEnvironment;
import org.fabric3.binding.ws.metro.provision.ConnectionConfiguration;
import org.fabric3.binding.ws.metro.provision.MetroWsdlWireTargetDefinition;
import org.fabric3.binding.ws.metro.provision.ReferenceEndpointDefinition;
import org.fabric3.binding.ws.metro.provision.SecurityConfiguration;
import org.fabric3.binding.ws.metro.runtime.core.EndpointService;
import org.fabric3.binding.ws.metro.runtime.core.MetroDispatchObjectFactory;
import org.fabric3.binding.ws.metro.runtime.core.MetroDispatchTargetInterceptor;
import org.fabric3.binding.ws.metro.runtime.policy.FeatureResolver;
import org.fabric3.spi.container.ContainerException;
import org.fabric3.spi.repository.ArtifactCache;
import org.fabric3.spi.repository.CacheException;
import org.fabric3.spi.container.binding.handler.BindingHandlerRegistry;
import org.fabric3.spi.model.physical.PhysicalWireSourceDefinition;
import org.fabric3.spi.container.objectfactory.ObjectFactory;
import org.fabric3.spi.container.wire.InvocationChain;
import org.fabric3.spi.container.wire.Wire;
import org.oasisopen.sca.annotation.Reference;

/**
 * Attaches an interceptor for invoking a web service endpoint based on a WSDL contract to a wire.
 */
public class MetroWsdlTargetWireAttacher extends AbstractMetroTargetWireAttacher<MetroWsdlWireTargetDefinition> {
    private FeatureResolver resolver;
    private SecurityEnvironment securityEnvironment;
    private ExecutorService executorService;
    private ArtifactCache cache;

    public MetroWsdlTargetWireAttacher(@Reference FeatureResolver resolver,
                                       @Reference EndpointService endpointService,
                                       @Reference SecurityEnvironment securityEnvironment,
                                       @Reference(name = "executorService") ExecutorService executorService,
                                       @Reference BindingHandlerRegistry handlerRegistry,
                                       @Reference ArtifactCache cache) {
        super(handlerRegistry, endpointService);
        this.resolver = resolver;
        this.securityEnvironment = securityEnvironment;
        this.executorService = executorService;
        this.cache = cache;
    }

    public void attach(PhysicalWireSourceDefinition source, MetroWsdlWireTargetDefinition target, Wire wire) throws ContainerException {
        ReferenceEndpointDefinition endpointDefinition = target.getEndpointDefinition();
        List<QName> requestedIntents = target.getIntents();

        WebServiceFeature[] features = resolver.getFeatures(requestedIntents);
        String wsdl = target.getWsdl();
        URL wsdlLocation;
        try {
            URI servicePath = target.getEndpointDefinition().getUrl().toURI();
            wsdlLocation = cache.cache(servicePath, new ByteArrayInputStream(wsdl.getBytes()));
        } catch (CacheException | URISyntaxException e) {
            throw new ContainerException(e);
        }

        SecurityConfiguration securityConfiguration = target.getSecurityConfiguration();
        ConnectionConfiguration connectionConfiguration = target.getConnectionConfiguration();
        List<Handler> handlers = createHandlers(target);

        MetroDispatchObjectFactory proxyFactory = new MetroDispatchObjectFactory(endpointDefinition,
                                                                                 wsdlLocation,
                                                                                 null,
                                                                                 securityConfiguration,
                                                                                 connectionConfiguration,
                                                                                 handlers,
                                                                                 features,
                                                                                 executorService,
                                                                                 securityEnvironment);

        for (InvocationChain chain : wire.getInvocationChains()) {
            boolean oneWay = chain.getPhysicalOperation().isOneWay();
            MetroDispatchTargetInterceptor targetInterceptor = new MetroDispatchTargetInterceptor(proxyFactory, oneWay);
            chain.addInterceptor(targetInterceptor);
        }

    }

    public ObjectFactory<?> createObjectFactory(MetroWsdlWireTargetDefinition target) throws ContainerException {
        return null;
    }

    public void detach(PhysicalWireSourceDefinition source, MetroWsdlWireTargetDefinition target) throws ContainerException {
        try {
            cache.remove(target.getUri());
        } catch (CacheException e) {
            throw new ContainerException(e);
        }
    }

}