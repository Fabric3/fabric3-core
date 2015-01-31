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

import javax.xml.stream.XMLInputFactory;
import javax.xml.ws.handler.Handler;
import java.io.ByteArrayInputStream;
import java.lang.reflect.Method;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.security.SecureClassLoader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;

import org.fabric3.api.annotation.monitor.Monitor;
import org.fabric3.api.host.ContainerException;
import org.fabric3.binding.ws.metro.provision.ConnectionConfiguration;
import org.fabric3.binding.ws.metro.provision.MetroJavaWireTargetDefinition;
import org.fabric3.binding.ws.metro.provision.ReferenceEndpointDefinition;
import org.fabric3.binding.ws.metro.runtime.core.EndpointService;
import org.fabric3.binding.ws.metro.runtime.core.InterceptorMonitor;
import org.fabric3.binding.ws.metro.runtime.core.MetroJavaTargetInterceptor;
import org.fabric3.binding.ws.metro.runtime.core.MetroProxyObjectFactory;
import org.fabric3.spi.classloader.ClassLoaderRegistry;
import org.fabric3.spi.container.binding.handler.BindingHandlerRegistry;
import org.fabric3.spi.container.objectfactory.ObjectFactory;
import org.fabric3.spi.container.wire.InvocationChain;
import org.fabric3.spi.container.wire.Wire;
import org.fabric3.spi.model.physical.PhysicalWireSourceDefinition;
import org.fabric3.spi.repository.ArtifactCache;
import org.oasisopen.sca.annotation.Reference;

/**
 * Attaches an interceptor for invoking a web service endpoint based on a Java interface contract to a wire.
 */
public class MetroJavaTargetWireAttacher extends AbstractMetroTargetWireAttacher<MetroJavaWireTargetDefinition> {

    private ClassLoaderRegistry registry;
    private WireAttacherHelper wireAttacherHelper;
    private ArtifactCache artifactCache;
    private ExecutorService executorService;
    private XMLInputFactory xmlInputFactory;
    private InterceptorMonitor monitor;

    public MetroJavaTargetWireAttacher(@Reference ClassLoaderRegistry registry,
                                       @Reference EndpointService endpointService,
                                       @Reference WireAttacherHelper wireAttacherHelper,
                                       @Reference ArtifactCache artifactCache,
                                       @Reference(name = "executorService") ExecutorService executorService,
                                       @Reference BindingHandlerRegistry handlerRegistry,
                                       @Monitor InterceptorMonitor monitor) {
        super(handlerRegistry);
        this.registry = registry;
        this.wireAttacherHelper = wireAttacherHelper;
        this.artifactCache = artifactCache;
        this.executorService = executorService;
        this.xmlInputFactory = XMLInputFactory.newFactory();
        this.monitor = monitor;
    }

    public void attach(PhysicalWireSourceDefinition source, MetroJavaWireTargetDefinition target, Wire wire) throws ContainerException {

        try {
            ReferenceEndpointDefinition endpointDefinition = target.getEndpointDefinition();
            URI classLoaderId = target.getSEIClassLoaderUri();

            ClassLoader classLoader = registry.getClassLoader(classLoaderId);

            String interfaze = target.getInterface();
            byte[] bytes = target.getGeneratedInterface();

            if (!(classLoader instanceof SecureClassLoader)) {
                throw new ContainerException("Classloader for " + interfaze + " must be a SecureClassLoader");
            }
            Class<?> seiClass = wireAttacherHelper.loadSEI(interfaze, bytes, (SecureClassLoader) classLoader);

            ClassLoader old = Thread.currentThread().getContextClassLoader();

            try {
                // SAAJ classes are needed from the TCCL
                Thread.currentThread().setContextClassLoader(classLoader);

                // cache WSDL and Schemas
                URL wsdlLocation = target.getWsdlLocation();
                URL generatedWsdl = null;
                URI servicePath = target.getEndpointDefinition().getUrl().toURI();
                String wsdl = target.getWsdl();
                if (wsdl != null) {
                    wsdlLocation = artifactCache.cache(servicePath, new ByteArrayInputStream(wsdl.getBytes()));
                    generatedWsdl = wsdlLocation;
                    cacheSchemas(servicePath, target);
                }

                ConnectionConfiguration connectionConfiguration = target.getConnectionConfiguration();

                List<Handler> handlers = createHandlers(target);

                // if the target service is a callback, add the resolver
                ObjectFactory<?> proxyFactory = new MetroProxyObjectFactory(endpointDefinition,
                                                                            wsdlLocation,
                                                                            generatedWsdl,
                                                                            seiClass,
                                                                            connectionConfiguration,
                                                                            handlers,
                                                                            executorService,
                                                                            xmlInputFactory);

                attachInterceptors(seiClass, target, wire, proxyFactory);
            } finally {
                Thread.currentThread().setContextClassLoader(old);
            }
        } catch (URISyntaxException e) {
            throw new ContainerException(e);
        }

    }

    public ObjectFactory<?> createObjectFactory(MetroJavaWireTargetDefinition target) throws ContainerException {
        return null;
    }

    public void detach(PhysicalWireSourceDefinition source, MetroJavaWireTargetDefinition target) throws ContainerException {
        // no-op
    }

    private List<URL> cacheSchemas(URI servicePath, MetroJavaWireTargetDefinition target) throws ContainerException {
        List<URL> schemas = new ArrayList<>();
        for (Map.Entry<String, String> entry : target.getSchemas().entrySet()) {
            URI uri = URI.create(servicePath + "/" + entry.getKey());
            ByteArrayInputStream bas = new ByteArrayInputStream(entry.getValue().getBytes());
            URL url = artifactCache.cache(uri, bas);
            schemas.add(url);
        }
        return schemas;
    }

    private void attachInterceptors(Class<?> seiClass, MetroJavaWireTargetDefinition target, Wire wire, ObjectFactory<?> factory) {
        Method[] methods = seiClass.getMethods();
        int retries = target.getRetries();
        for (InvocationChain chain : wire.getInvocationChains()) {
            Method method = null;
            for (Method m : methods) {
                if (chain.getPhysicalOperation().getName().equals(m.getName())) {
                    method = m;
                    break;
                }
            }
            boolean oneWay = chain.getPhysicalOperation().isOneWay();
            MetroJavaTargetInterceptor targetInterceptor = new MetroJavaTargetInterceptor(factory, method, oneWay, retries, monitor);
            chain.addInterceptor(targetInterceptor);
        }
    }

}