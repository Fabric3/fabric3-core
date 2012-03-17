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
import java.lang.reflect.Method;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.security.SecureClassLoader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLInputFactory;
import javax.xml.ws.WebServiceFeature;

import com.sun.xml.wss.SecurityEnvironment;
import org.oasisopen.sca.annotation.Reference;

import org.fabric3.api.annotation.monitor.Monitor;
import org.fabric3.binding.ws.metro.provision.ConnectionConfiguration;
import org.fabric3.binding.ws.metro.provision.MetroJavaTargetDefinition;
import org.fabric3.binding.ws.metro.provision.ReferenceEndpointDefinition;
import org.fabric3.binding.ws.metro.provision.SecurityConfiguration;
import org.fabric3.binding.ws.metro.runtime.core.InterceptorMonitor;
import org.fabric3.binding.ws.metro.runtime.core.MetroJavaTargetInterceptor;
import org.fabric3.binding.ws.metro.runtime.core.MetroProxyObjectFactory;
import org.fabric3.binding.ws.metro.runtime.policy.FeatureResolver;
import org.fabric3.spi.artifact.ArtifactCache;
import org.fabric3.spi.artifact.CacheException;
import org.fabric3.spi.binding.handler.BindingHandlerRegistry;
import org.fabric3.spi.builder.WiringException;
import org.fabric3.spi.builder.component.TargetWireAttacher;
import org.fabric3.spi.classloader.ClassLoaderRegistry;
import org.fabric3.spi.model.physical.PhysicalSourceDefinition;
import org.fabric3.spi.objectfactory.ObjectFactory;
import org.fabric3.spi.wire.InvocationChain;
import org.fabric3.spi.wire.Wire;
import org.fabric3.spi.xml.XMLFactory;

/**
 * Creates an interceptor for invoking a web service endpoint.
 *
 * @version $Rev$ $Date$
 */
public class MetroJavaTargetWireAttacher implements TargetWireAttacher<MetroJavaTargetDefinition> {

    private ClassLoaderRegistry registry;
    private FeatureResolver resolver;
    private WireAttacherHelper wireAttacherHelper;
    private ArtifactCache artifactCache;
    private SecurityEnvironment securityEnvironment;
    private ExecutorService executorService;
    private XMLInputFactory xmlInputFactory;
    private InterceptorMonitor monitor;
	private BindingHandlerRegistry handlerRegistry;


    public MetroJavaTargetWireAttacher(@Reference ClassLoaderRegistry registry,
                                       @Reference FeatureResolver resolver,
                                       @Reference WireAttacherHelper wireAttacherHelper,
                                       @Reference ArtifactCache artifactCache,
                                       @Reference SecurityEnvironment securityEnvironment,
                                       @Reference ExecutorService executorService,
                                       @Reference XMLFactory xmlFactory,
                                       @Reference BindingHandlerRegistry handlerRegistry,
                                       @Monitor InterceptorMonitor monitor) {
        this.registry = registry;
        this.resolver = resolver;
        this.wireAttacherHelper = wireAttacherHelper;
        this.artifactCache = artifactCache;
        this.securityEnvironment = securityEnvironment;
        this.executorService = executorService;
        this.xmlInputFactory = xmlFactory.newInputFactoryInstance();
        this.monitor = monitor;
        this.handlerRegistry = handlerRegistry;
    }

    public void attach(PhysicalSourceDefinition source, MetroJavaTargetDefinition target, Wire wire) throws WiringException {

        try {
            ReferenceEndpointDefinition endpointDefinition = target.getEndpointDefinition();
            URI classLoaderId = target.getSEIClassLoaderUri();
            List<QName> requestedIntents = target.getIntents();

            ClassLoader classLoader = registry.getClassLoader(classLoaderId);

            String interfaze = target.getInterface();
            byte[] bytes = target.getGeneratedInterface();

            if (!(classLoader instanceof SecureClassLoader)) {
                throw new WiringException("Classloader for " + interfaze + " must be a SecureClassLoader");
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

                WebServiceFeature[] features = resolver.getFeatures(requestedIntents);

                ObjectFactory<?> proxyFactory = new MetroProxyObjectFactory(endpointDefinition,
                                                                            wsdlLocation,
                                                                            generatedWsdl,
                                                                            seiClass,
                                                                            features,
                                                                            executorService,
                                                                            securityEnvironment,
                                                                            xmlInputFactory);

                attacheInterceptors(seiClass, target, wire, proxyFactory);
            } finally {
                Thread.currentThread().setContextClassLoader(old);
            }
        } catch (CacheException e) {
            throw new WiringException(e);
        } catch (URISyntaxException e) {
            throw new WiringException(e);
        }

    }

    public ObjectFactory<?> createObjectFactory(MetroJavaTargetDefinition target) throws WiringException {
        return null;
    }

    public void detach(PhysicalSourceDefinition source, MetroJavaTargetDefinition target) throws WiringException {
        // no-op
    }

    private List<URL> cacheSchemas(URI servicePath, MetroJavaTargetDefinition target) throws CacheException {
        List<URL> schemas = new ArrayList<URL>();
        for (Map.Entry<String, String> entry : target.getSchemas().entrySet()) {
            URI uri = URI.create(servicePath + "/" + entry.getKey());
            ByteArrayInputStream bas = new ByteArrayInputStream(entry.getValue().getBytes());
            URL url = artifactCache.cache(uri, bas);
            schemas.add(url);
        }
        return schemas;
    }

    private void attacheInterceptors(Class<?> seiClass, MetroJavaTargetDefinition target, Wire wire, ObjectFactory<?> proxyFactory) {
        Method[] methods = seiClass.getMethods();
        SecurityConfiguration securityConfiguration = target.getSecurityConfiguration();
        ConnectionConfiguration connectionConfiguration = target.getConnectionConfiguration();
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
            MetroJavaTargetInterceptor targetInterceptor =
                    new MetroJavaTargetInterceptor(proxyFactory, method, oneWay, securityConfiguration, connectionConfiguration, retries, handlerRegistry, monitor);
            chain.addInterceptor(targetInterceptor);
        }
    }

}