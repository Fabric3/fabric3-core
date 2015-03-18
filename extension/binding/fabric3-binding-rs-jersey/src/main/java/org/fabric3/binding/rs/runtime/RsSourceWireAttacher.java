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
 * Portions originally based on Apache Tuscany 2007
 * licensed under the Apache 2.0 license.
 */
package org.fabric3.binding.rs.runtime;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.fabric3.api.annotation.Source;
import org.fabric3.api.annotation.monitor.Monitor;
import org.fabric3.api.annotation.wire.Key;
import org.fabric3.api.host.Fabric3Exception;
import org.fabric3.binding.rs.provision.RsWireSourceDefinition;
import org.fabric3.binding.rs.runtime.container.F3ResourceHandler;
import org.fabric3.binding.rs.runtime.container.RsContainer;
import org.fabric3.binding.rs.runtime.container.RsContainerManager;
import org.fabric3.binding.rs.runtime.provider.NameBindingFilterProvider;
import org.fabric3.binding.rs.runtime.provider.ProviderRegistry;
import org.fabric3.spi.container.builder.component.SourceWireAttacher;
import org.fabric3.spi.container.wire.InvocationChain;
import org.fabric3.spi.container.wire.Wire;
import org.fabric3.spi.host.ServletHost;
import org.fabric3.spi.model.physical.PhysicalOperationDefinition;
import org.fabric3.spi.model.physical.PhysicalWireTargetDefinition;
import org.glassfish.jersey.server.model.Resource;
import org.glassfish.jersey.server.model.ResourceMethod;
import org.oasisopen.sca.annotation.EagerInit;
import org.oasisopen.sca.annotation.Property;
import org.oasisopen.sca.annotation.Reference;

/**
 *
 */
@EagerInit
@Key("org.fabric3.binding.rs.provision.RsWireSourceDefinition")
public class RsSourceWireAttacher implements SourceWireAttacher<RsWireSourceDefinition> {
    private ServletHost servletHost;
    private RsContainerManager containerManager;
    private ProviderRegistry providerRegistry;
    private NameBindingFilterProvider provider;
    private RsWireAttacherMonitor monitor;
    private Level logLevel = Level.WARNING;

    public RsSourceWireAttacher(@Reference ServletHost servletHost,
                                @Reference RsContainerManager containerManager,
                                @Reference ProviderRegistry providerRegistry,
                                @Reference NameBindingFilterProvider provider,
                                @Monitor RsWireAttacherMonitor monitor) {
        this.servletHost = servletHost;
        this.containerManager = containerManager;
        this.providerRegistry = providerRegistry;
        this.provider = provider;
        this.monitor = monitor;
        setDebugLevel();
    }

    @Property(required = false)
    @Source("$systemConfig/f3:binding.rs/@log.level")
    public void setLogLevel(String level) {
        this.logLevel = Level.parse(level);
    }

    public void attach(RsWireSourceDefinition source, PhysicalWireTargetDefinition target, Wire wire) throws Fabric3Exception {
        URI sourceUri = source.getUri();
        RsContainer container = containerManager.get(sourceUri);
        if (container == null) {
            // each resource defined with the same binding URI will be deployed to the same container
            container = new RsContainer(sourceUri.toString(), providerRegistry, provider);
            containerManager.register(sourceUri, container);
            String mapping = creatingMappingUri(sourceUri);
            if (servletHost.isMappingRegistered(mapping)) {
                // wire reprovisioned
                servletHost.unregisterMapping(mapping);
            }
            servletHost.registerMapping(mapping, container);
        }

        provision(source, wire, container);
        monitor.provisionedEndpoint(sourceUri);
    }

    public void detach(RsWireSourceDefinition source, PhysicalWireTargetDefinition target) {
        URI sourceUri = source.getUri();
        String mapping = creatingMappingUri(sourceUri);
        servletHost.unregisterMapping(mapping);
        containerManager.unregister(sourceUri);
        monitor.removedEndpoint(sourceUri);
    }

    private String creatingMappingUri(URI sourceUri) {
        String servletMapping = sourceUri.getPath();
        if (!servletMapping.endsWith("/*")) {
            servletMapping = servletMapping + "/*";
        }
        return servletMapping;
    }

    private void provision(RsWireSourceDefinition sourceDefinition, Wire wire, RsContainer container) {
        Map<String, InvocationChain> invocationChains = new HashMap<>();
        for (InvocationChain chain : wire.getInvocationChains()) {
            PhysicalOperationDefinition operation = chain.getPhysicalOperation();
            invocationChains.put(operation.getName(), chain);
        }

        Class<?> interfaze = sourceDefinition.getRsClass();
        F3ResourceHandler handler = new F3ResourceHandler(interfaze, invocationChains);

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
        if (template == null) {
            throw new Fabric3Exception("Interface is not a JAX-RS resource: " + handler.getInterface().getName());
        }
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
        if (template.isSuspendDeclared()) {
            methodBuilder.suspended(template.getSuspendTimeout(), template.getSuspendTimeoutUnit());
        }
    }

    private void setDebugLevel() {
        Logger logger = Logger.getLogger("org.glassfish.jersey.");
        logger.setLevel(logLevel);
    }

}
