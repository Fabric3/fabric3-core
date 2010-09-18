/*
* Fabric3
* Copyright (c) 2009 Metaform Systems
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
package org.fabric3.binding.rs.runtime;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.MethodInterceptor;
import org.osoa.sca.annotations.EagerInit;
import org.osoa.sca.annotations.Reference;

import org.fabric3.api.annotation.monitor.Monitor;
import org.fabric3.binding.rs.provision.AuthenticationType;
import org.fabric3.binding.rs.provision.RsSourceDefinition;
import org.fabric3.binding.rs.runtime.security.Authenticator;
import org.fabric3.binding.rs.runtime.security.BasicAuthenticator;
import org.fabric3.spi.ObjectFactory;
import org.fabric3.spi.builder.WiringException;
import org.fabric3.spi.builder.component.SourceWireAttacher;
import org.fabric3.spi.builder.component.WireAttachException;
import org.fabric3.spi.classloader.ClassLoaderRegistry;
import org.fabric3.spi.classloader.MultiParentClassLoader;
import org.fabric3.spi.host.ServletHost;
import org.fabric3.spi.model.physical.PhysicalOperationDefinition;
import org.fabric3.spi.model.physical.PhysicalTargetDefinition;
import org.fabric3.spi.security.AuthenticationService;
import org.fabric3.spi.wire.InvocationChain;
import org.fabric3.spi.wire.Wire;

/**
 * @version $Rev$ $Date$
 */
@EagerInit
public class RsSourceWireAttacher implements SourceWireAttacher<RsSourceDefinition> {
    private ServletHost servletHost;
    private ClassLoaderRegistry classLoaderRegistry;
    private RsWireAttacherMonitor monitor;
    private Authenticator basicAuthenticator;
    private Map<URI, RsContainer> containers = new ConcurrentHashMap<URI, RsContainer>();

    public RsSourceWireAttacher(@Reference AuthenticationService authenticationService,
                                @Reference ServletHost servletHost,
                                @Reference ClassLoaderRegistry registry,
                                @Monitor RsWireAttacherMonitor monitor) {
        this.servletHost = servletHost;
        this.classLoaderRegistry = registry;
        this.monitor = monitor;
        // TODO make realm configurable
        basicAuthenticator = new BasicAuthenticator(authenticationService, "fabric3");
    }

    public void attach(RsSourceDefinition source, PhysicalTargetDefinition target, Wire wire) throws WireAttachException {
        URI sourceUri = source.getUri();
        RsContainer container = containers.get(sourceUri);
        if (container == null) {
            container = new RsContainer(getClass().getClassLoader());
            containers.put(sourceUri, container);
            String mapping = creatingMappingUri(sourceUri);
            if (servletHost.isMappingRegistered(mapping)) {
                // wire reprovisioned
                servletHost.unregisterMapping(mapping);
            }
            servletHost.registerMapping(mapping, container);
        }

        try {
            provision(source, wire, container);
            monitor.provisionedEndpoint(sourceUri);
        } catch (ClassNotFoundException e) {
            String name = source.getRsClass();
            throw new WireAttachException("Unable to load interface class " + name, sourceUri, null, e);
        }
    }

    public void detach(RsSourceDefinition source, PhysicalTargetDefinition target) throws WiringException {
        URI sourceUri = source.getUri();
        String mapping = creatingMappingUri(sourceUri);
        servletHost.unregisterMapping(mapping);
        containers.remove(sourceUri);
        monitor.removedEndpoint(sourceUri);
    }

    public void attachObjectFactory(RsSourceDefinition source, ObjectFactory<?> objectFactory, PhysicalTargetDefinition target)
            throws WiringException {
        throw new AssertionError();
    }

    public void detachObjectFactory(RsSourceDefinition source, PhysicalTargetDefinition target) throws WiringException {
        throw new AssertionError();
    }

    private String creatingMappingUri(URI sourceUri) {
        String servletMapping = sourceUri.getPath();
        if (!servletMapping.endsWith("/*")) {
            servletMapping = servletMapping + "/*";
        }
        return servletMapping;
    }

    private void provision(RsSourceDefinition sourceDefinition, Wire wire, RsContainer container) throws ClassNotFoundException {
        ClassLoader classLoader = classLoaderRegistry.getClassLoader(sourceDefinition.getClassLoaderId());
        Map<String, InvocationChain> invocationChains = new HashMap<String, InvocationChain>();
        for (InvocationChain chain : wire.getInvocationChains()) {
            PhysicalOperationDefinition operation = chain.getPhysicalOperation();
            invocationChains.put(operation.getName(), chain);
        }

        MethodInterceptor methodInterceptor = createMethodInterceptor(sourceDefinition, invocationChains);

        Class<?> interfaze = classLoader.loadClass(sourceDefinition.getRsClass());
        Enhancer enhancer = new Enhancer();
        enhancer.setSuperclass(interfaze);
        enhancer.setCallback(methodInterceptor);

        // CGLib requires a classloader with access to the application classloader and this extension classloader
        MultiParentClassLoader rsClassLoader = new MultiParentClassLoader(URI.create("RESTClassLoader"), getClass().getClassLoader());
        rsClassLoader.addParent(classLoader);
        enhancer.setClassLoader(rsClassLoader);

        ClassLoader old = Thread.currentThread().getContextClassLoader();
        try {
            // set the TCCL as Jersey uses it to dynamically load classes
            Thread.currentThread().setContextClassLoader(rsClassLoader);
            Object instance = enhancer.create();
            container.addResource(interfaze, instance);
        } finally {
            Thread.currentThread().setContextClassLoader(old);
        }
    }

    private MethodInterceptor createMethodInterceptor(RsSourceDefinition sourceDefinition, Map<String, InvocationChain> invocationChains) {
        MethodInterceptor methodInterceptor;
        if (AuthenticationType.BASIC == sourceDefinition.getAuthenticationType()) {
            methodInterceptor = new RsMethodInterceptor(invocationChains, basicAuthenticator);
            for (InvocationChain chain : invocationChains.values()) {
                RsAuthorizationInterceptor authInterceptor = new RsAuthorizationInterceptor();
                chain.addInterceptor(0, authInterceptor);
            }
        } else if (AuthenticationType.STATEFUL_FORM == sourceDefinition.getAuthenticationType()) {
            throw new UnsupportedOperationException();
        } else if (AuthenticationType.DIGEST == sourceDefinition.getAuthenticationType()) {
            throw new UnsupportedOperationException();
        } else {
            methodInterceptor = new RsMethodInterceptor(invocationChains);
        }
        return methodInterceptor;
    }


}
