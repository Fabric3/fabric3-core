/*
 * Fabric3
 * Copyright (c) 2009-2013 Metaform Systems
 *
 * Fabric3 is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of
 * the License, or (at your option) any later version, with the
 * following exception:
 *
 * Linking this software statically or dynamically with other
 * modules is making a combined work based on this software.
 * Thus, the terms and conditions of the GNU General Public
 * License cover the whole combination.
 *
 * As a special exception, the copyright holders of this software
 * give you permission to link this software with independent
 * modules to produce an executable, regardless of the license
 * terms of these independent modules, and to copy and distribute
 * the resulting executable under terms of your choice, provided
 * that you also meet, for each linked independent module, the
 * terms and conditions of the license of that module. An
 * independent module is a module which is not derived from or
 * based on this software. If you modify this software, you may
 * extend this exception to your version of the software, but
 * you are not obligated to do so. If you do not wish to do so,
 * delete this exception statement from your version.
 *
 * Fabric3 is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the
 * GNU General Public License along with Fabric3.
 * If not, see <http://www.gnu.org/licenses/>.
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
import org.fabric3.spi.repository.ArtifactCache;
import org.fabric3.spi.repository.CacheException;
import org.fabric3.spi.container.binding.handler.BindingHandlerRegistry;
import org.fabric3.spi.container.builder.WiringException;
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
                                       @Reference ExecutorService executorService,
                                       @Reference BindingHandlerRegistry handlerRegistry,
                                       @Reference ArtifactCache cache) {
        super(handlerRegistry, endpointService);
        this.resolver = resolver;
        this.securityEnvironment = securityEnvironment;
        this.executorService = executorService;
        this.cache = cache;
    }

    public void attach(PhysicalWireSourceDefinition source, MetroWsdlWireTargetDefinition target, Wire wire) throws WiringException {
        ReferenceEndpointDefinition endpointDefinition = target.getEndpointDefinition();
        List<QName> requestedIntents = target.getIntents();

        WebServiceFeature[] features = resolver.getFeatures(requestedIntents);
        String wsdl = target.getWsdl();
        URL wsdlLocation;
        try {
            URI servicePath = target.getEndpointDefinition().getUrl().toURI();
            wsdlLocation = cache.cache(servicePath, new ByteArrayInputStream(wsdl.getBytes()));
        } catch (CacheException | URISyntaxException e) {
            throw new WiringException(e);
        }

        SecurityConfiguration securityConfiguration = target.getSecurityConfiguration();
        ConnectionConfiguration connectionConfiguration = target.getConnectionConfiguration();
        List<Handler> handlers = createHandlers(target);

        MetroDispatchObjectFactory proxyFactory
                = new MetroDispatchObjectFactory(endpointDefinition, wsdlLocation, null, securityConfiguration, connectionConfiguration, handlers, features,
                                                 executorService, securityEnvironment);

        for (InvocationChain chain : wire.getInvocationChains()) {
            boolean oneWay = chain.getPhysicalOperation().isOneWay();
            MetroDispatchTargetInterceptor targetInterceptor = new MetroDispatchTargetInterceptor(proxyFactory, oneWay);
            chain.addInterceptor(targetInterceptor);
        }

    }

    public ObjectFactory<?> createObjectFactory(MetroWsdlWireTargetDefinition target) throws WiringException {
        return null;
    }

    public void detach(PhysicalWireSourceDefinition source, MetroWsdlWireTargetDefinition target) throws WiringException {
        try {
            cache.remove(target.getUri());
        } catch (CacheException e) {
            throw new WiringException(e);
        }
    }

}