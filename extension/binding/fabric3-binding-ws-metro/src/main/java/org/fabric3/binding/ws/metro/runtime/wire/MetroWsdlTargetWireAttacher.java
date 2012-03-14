/*
 * Fabric3
 * Copyright (c) 2009-2011 Metaform Systems
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

import java.io.ByteArrayInputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;
import java.util.concurrent.ExecutorService;
import javax.xml.namespace.QName;
import javax.xml.ws.WebServiceFeature;

import com.sun.xml.wss.SecurityEnvironment;
import org.oasisopen.sca.annotation.Reference;

import org.fabric3.binding.ws.metro.provision.ConnectionConfiguration;
import org.fabric3.binding.ws.metro.provision.MetroWsdlTargetDefinition;
import org.fabric3.binding.ws.metro.provision.ReferenceEndpointDefinition;
import org.fabric3.binding.ws.metro.provision.SecurityConfiguration;
import org.fabric3.binding.ws.metro.runtime.core.MetroDispatchObjectFactory;
import org.fabric3.binding.ws.metro.runtime.core.MetroDispatchTargetInterceptor;
import org.fabric3.binding.ws.metro.runtime.policy.FeatureResolver;
import org.fabric3.spi.artifact.ArtifactCache;
import org.fabric3.spi.artifact.CacheException;
import org.fabric3.spi.builder.WiringException;
import org.fabric3.spi.builder.component.TargetWireAttacher;
import org.fabric3.spi.model.physical.PhysicalSourceDefinition;
import org.fabric3.spi.objectfactory.ObjectFactory;
import org.fabric3.spi.wire.InvocationChain;
import org.fabric3.spi.wire.Wire;

/**
 * Creates an interceptor for invoking a web service endpoint.
 *
 * @version $Rev$ $Date$
 */
public class MetroWsdlTargetWireAttacher implements TargetWireAttacher<MetroWsdlTargetDefinition> {
    private FeatureResolver resolver;
    private SecurityEnvironment securityEnvironment;
    private ExecutorService executorService;
    private ArtifactCache cache;

    public MetroWsdlTargetWireAttacher(@Reference FeatureResolver resolver,
                                       @Reference SecurityEnvironment securityEnvironment,
                                       @Reference ExecutorService executorService,
                                       @Reference ArtifactCache cache) {
        this.resolver = resolver;
        this.securityEnvironment = securityEnvironment;
        this.executorService = executorService;
        this.cache = cache;
    }

    public void attach(PhysicalSourceDefinition source, MetroWsdlTargetDefinition target, Wire wire) throws WiringException {
        ReferenceEndpointDefinition endpointDefinition = target.getEndpointDefinition();
        List<QName> requestedIntents = target.getIntents();

        WebServiceFeature[] features = resolver.getFeatures(requestedIntents);
        String wsdl = target.getWsdl();
        URL wsdlLocation;
        try {
            URI servicePath = target.getEndpointDefinition().getUrl().toURI();
            wsdlLocation = cache.cache(servicePath, new ByteArrayInputStream(wsdl.getBytes()));
        } catch (CacheException e) {
            throw new WiringException(e);
        } catch (URISyntaxException e) {
            throw new WiringException(e);
        }


        MetroDispatchObjectFactory proxyFactory = new MetroDispatchObjectFactory(endpointDefinition,
                                                                                 wsdlLocation,
                                                                                 null,
                                                                                 features,
                                                                                 executorService,
                                                                                 securityEnvironment);

        SecurityConfiguration securityConfiguration = target.getSecurityConfiguration();
        ConnectionConfiguration connectionConfiguration = target.getConnectionConfiguration();
        for (InvocationChain chain : wire.getInvocationChains()) {
            boolean oneWay = chain.getPhysicalOperation().isOneWay();
            MetroDispatchTargetInterceptor targetInterceptor =
                    new MetroDispatchTargetInterceptor(proxyFactory, oneWay, securityConfiguration, connectionConfiguration);
            chain.addInterceptor(targetInterceptor);
        }

    }

    public ObjectFactory<?> createObjectFactory(MetroWsdlTargetDefinition target) throws WiringException {
        return null;
    }

    public void detach(PhysicalSourceDefinition source, MetroWsdlTargetDefinition target) throws WiringException {
        try {
            cache.remove(target.getUri());
        } catch (CacheException e) {
            throw new WiringException(e);
        }
    }

}