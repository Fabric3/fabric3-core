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
package org.fabric3.binding.rs.runtime;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.fabric3.api.annotation.monitor.Monitor;
import org.fabric3.binding.rs.provision.AuthenticationType;
import org.fabric3.binding.rs.provision.RsSourceDefinition;
import org.fabric3.spi.container.builder.WiringException;
import org.fabric3.spi.container.builder.component.SourceWireAttacher;
import org.fabric3.spi.container.builder.component.WireAttachException;
import org.fabric3.spi.classloader.ClassLoaderRegistry;
import org.fabric3.spi.host.ServletHost;
import org.fabric3.spi.model.physical.PhysicalOperationDefinition;
import org.fabric3.spi.model.physical.PhysicalTargetDefinition;
import org.fabric3.spi.container.objectfactory.ObjectFactory;
import org.fabric3.spi.security.BasicAuthenticator;
import org.fabric3.spi.container.wire.InvocationChain;
import org.fabric3.spi.container.wire.Wire;
import org.glassfish.jersey.server.model.Resource;
import org.glassfish.jersey.server.model.ResourceMethod;
import org.oasisopen.sca.annotation.EagerInit;
import org.oasisopen.sca.annotation.Property;
import org.oasisopen.sca.annotation.Reference;

/**
 *
 */
@EagerInit
public class RsSourceWireAttacher implements SourceWireAttacher<RsSourceDefinition> {
    private ServletHost servletHost;
    private ClassLoaderRegistry classLoaderRegistry;
    private RsContainerManager containerManager;
    private BasicAuthenticator authenticator;
    private RsWireAttacherMonitor monitor;
    private Level logLevel = Level.WARNING;

    public RsSourceWireAttacher(@Reference ServletHost servletHost,
                                @Reference ClassLoaderRegistry registry,
                                @Reference RsContainerManager containerManager,
                                @Reference BasicAuthenticator authenticator,
                                @Monitor RsWireAttacherMonitor monitor) throws NoSuchFieldException, IllegalAccessException {
        this.servletHost = servletHost;
        this.classLoaderRegistry = registry;
        this.containerManager = containerManager;
        this.authenticator = authenticator;
        this.monitor = monitor;
        setDebugLevel();
    }

    @Property(required = false)
    public void setLogLevel(String level) {
        this.logLevel = Level.parse(level);
    }

    public void attach(RsSourceDefinition source, PhysicalTargetDefinition target, Wire wire) throws WireAttachException {
        URI sourceUri = source.getUri();
        RsContainer container = containerManager.get(sourceUri);
        if (container == null) {
            // each resource defined with the same binding URI will be deployed to the same container
            container = new RsContainer(sourceUri.toString());
            containerManager.register(sourceUri, container);
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
        } catch (RsContainerException e) {
            throw new WireAttachException("Error attaching source: " + sourceUri, e);
        }
    }

    public void detach(RsSourceDefinition source, PhysicalTargetDefinition target) throws WiringException {
        URI sourceUri = source.getUri();
        String mapping = creatingMappingUri(sourceUri);
        servletHost.unregisterMapping(mapping);
        containerManager.unregister(sourceUri);
        monitor.removedEndpoint(sourceUri);
    }

    public void attachObjectFactory(RsSourceDefinition source, ObjectFactory<?> objectFactory, PhysicalTargetDefinition target) throws WiringException {
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

    private void provision(RsSourceDefinition sourceDefinition, Wire wire, RsContainer container) throws ClassNotFoundException, RsContainerException {
        ClassLoader classLoader = classLoaderRegistry.getClassLoader(sourceDefinition.getClassLoaderId());
        Map<String, InvocationChain> invocationChains = new HashMap<String, InvocationChain>();
        for (InvocationChain chain : wire.getInvocationChains()) {
            PhysicalOperationDefinition operation = chain.getPhysicalOperation();
            invocationChains.put(operation.getName(), chain);
        }

        Class<?> interfaze = classLoader.loadClass(sourceDefinition.getRsClass());
        boolean authenticate = authenticate(sourceDefinition);
        F3ResourceHandler handler = new F3ResourceHandler(interfaze, invocationChains, authenticate, authenticator);

        // Set the class loader to the runtime one so Jersey loads the Resource config properly
        ClassLoader old = Thread.currentThread().getContextClassLoader();
        try {
            Thread.currentThread().setContextClassLoader(getClass().getClassLoader());
            Resource resource = createResource(handler);
            container.addResource(resource);
        } finally {
            Thread.currentThread().setContextClassLoader(old);
        }
    }

    private Resource createResource(F3ResourceHandler handler) {
        Resource template = Resource.from(handler.getInterface());
        Resource.Builder resourceBuilder = Resource.builder(template.getPath());
        for (ResourceMethod resourceMethod : template.getAllMethods()) {
            createMethod(resourceBuilder, resourceMethod, handler);
        }
        for (Resource childTemplate : template.getChildResources()) {
            Resource.Builder childResourceBuilder = Resource.builder(childTemplate.getPath());
            for (ResourceMethod resourceMethod : childTemplate.getAllMethods()) {
                createMethod(childResourceBuilder, resourceMethod, handler);
            }
            resourceBuilder.addChildResource(childResourceBuilder.build());
        }
        return resourceBuilder.build();
    }

    private void createMethod(Resource.Builder resourceBuilder, ResourceMethod template, F3ResourceHandler handler) {
        ResourceMethod.Builder methodBuilder = resourceBuilder.addMethod(template.getHttpMethod());
        methodBuilder.consumes(template.getConsumedTypes());
        methodBuilder.produces(template.getProducedTypes());
        methodBuilder.handledBy(handler, template.getInvocable().getHandlingMethod());
    }

    private boolean authenticate(RsSourceDefinition sourceDefinition) {
        if (AuthenticationType.BASIC == sourceDefinition.getAuthenticationType()) {
            return true;
        } else if (AuthenticationType.STATEFUL_FORM == sourceDefinition.getAuthenticationType()) {
            throw new UnsupportedOperationException();
        } else if (AuthenticationType.DIGEST == sourceDefinition.getAuthenticationType()) {
            throw new UnsupportedOperationException();
        }
        return false;
    }

    private void setDebugLevel() {
        Logger logger = Logger.getLogger("org.glassfish.jersey.");
        logger.setLevel(logLevel);
    }

}
