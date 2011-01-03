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
import java.net.URL;
import java.security.SecureClassLoader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.xml.namespace.QName;
import javax.xml.ws.WebServiceFeature;

import com.sun.xml.ws.api.BindingID;
import org.osoa.sca.annotations.Reference;

import org.fabric3.binding.ws.metro.provision.MetroJavaSourceDefinition;
import org.fabric3.binding.ws.metro.provision.ServiceEndpointDefinition;
import org.fabric3.binding.ws.metro.runtime.core.EndpointConfiguration;
import org.fabric3.binding.ws.metro.runtime.core.EndpointException;
import org.fabric3.binding.ws.metro.runtime.core.EndpointService;
import org.fabric3.binding.ws.metro.runtime.core.JaxbInvoker;
import org.fabric3.binding.ws.metro.runtime.policy.FeatureResolver;
import org.fabric3.binding.ws.metro.util.BindingIdResolver;
import org.fabric3.spi.artifact.ArtifactCache;
import org.fabric3.spi.artifact.CacheException;
import org.fabric3.spi.builder.WiringException;
import org.fabric3.spi.classloader.ClassLoaderRegistry;
import org.fabric3.spi.model.physical.PhysicalTargetDefinition;
import org.fabric3.spi.wire.InvocationChain;
import org.fabric3.spi.wire.Wire;

/**
 * Source wire attacher that provisions Java-based web service endpoints.
 *
 * @version $Rev$ $Date$
 */
public class MetroJavaSourceWireAttacher extends AbstractMetroSourceWireAttacher<MetroJavaSourceDefinition> {
    private ClassLoaderRegistry classLoaderRegistry;
    private FeatureResolver featureResolver;
    private BindingIdResolver bindingIdResolver;
    private WireAttacherHelper wireAttacherHelper;
    private ArtifactCache artifactCache;

    public MetroJavaSourceWireAttacher(@Reference ClassLoaderRegistry classLoaderRegistry,
                                       @Reference FeatureResolver featureResolver,
                                       @Reference BindingIdResolver bindingIdResolver,
                                       @Reference WireAttacherHelper wireAttacherHelper,
                                       @Reference ArtifactCache artifactCache,
                                       @Reference EndpointService endpointService) {
        super(endpointService);
        this.classLoaderRegistry = classLoaderRegistry;
        this.featureResolver = featureResolver;
        this.bindingIdResolver = bindingIdResolver;
        this.wireAttacherHelper = wireAttacherHelper;
        this.artifactCache = artifactCache;
    }

    public void attach(MetroJavaSourceDefinition source, PhysicalTargetDefinition target, Wire wire) throws WiringException {
        try {
            ServiceEndpointDefinition endpointDefinition = source.getEndpointDefinition();
            QName serviceName = endpointDefinition.getServiceName();
            QName portName = endpointDefinition.getPortName();
            URI servicePath = endpointDefinition.getServicePath();
            List<InvocationChain> invocationChains = wire.getInvocationChains();
            URI classLoaderId = source.getSEIClassLoaderUri();
            URL wsdlLocation = source.getWsdlLocation();
            List<QName> requestedIntents = source.getIntents();

            ClassLoader classLoader = classLoaderRegistry.getClassLoader(classLoaderId);

            String interfaze = source.getInterface();
            byte[] bytes = source.getGeneratedInterface();

            if (!(classLoader instanceof SecureClassLoader)) {
                throw new WiringException("Classloader for " + interfaze + " must be a SecureClassLoader");
            }
            Class<?> seiClass = wireAttacherHelper.loadSEI(interfaze, bytes, (SecureClassLoader) classLoader);

            ClassLoader old = Thread.currentThread().getContextClassLoader();

            try {
                // SAAJ classes are needed from the TCCL
                Thread.currentThread().setContextClassLoader(classLoader);

                BindingID bindingId = bindingIdResolver.resolveBindingId(requestedIntents);
                WebServiceFeature[] features = featureResolver.getFeatures(requestedIntents);

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
                JaxbInvoker invoker = new JaxbInvoker(invocationChains);
                EndpointConfiguration configuration = new EndpointConfiguration(seiClass,
                                                                                serviceName,
                                                                                portName,
                                                                                path,
                                                                                wsdlLocation,
                                                                                invoker,
                                                                                features,
                                                                                bindingId,
                                                                                generatedWsdl,
                                                                                generatedSchemas);

                endpointService.registerService(configuration);
            } finally {
                Thread.currentThread().setContextClassLoader(old);
            }
        } catch (EndpointException e) {
            throw new WiringException(e);
        } catch (CacheException e) {
            throw new WiringException(e);
        }
    }

    public void detach(MetroJavaSourceDefinition source, PhysicalTargetDefinition target) throws WiringException {
        try {
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
        } catch (CacheException e) {
            throw new WiringException(e);
        } catch (EndpointException e) {
            throw new WiringException(e);
        }
    }


    private List<URL> cacheSchemas(URI servicePath, MetroJavaSourceDefinition source) throws CacheException {
        List<URL> schemas = new ArrayList<URL>();
        for (Map.Entry<String, String> entry : source.getSchemas().entrySet()) {
            URI uri = URI.create(servicePath + "/" + entry.getKey());
            ByteArrayInputStream bas = new ByteArrayInputStream(entry.getValue().getBytes());
            URL url = artifactCache.cache(uri, bas);
            schemas.add(url);
        }
        return schemas;
    }

    private void removeCachedSchemas(URI servicePath, MetroJavaSourceDefinition source) throws CacheException {
        for (Map.Entry<String, String> entry : source.getSchemas().entrySet()) {
            URI uri = URI.create(servicePath + "/" + entry.getKey());
            artifactCache.remove(uri);
        }
    }

}

