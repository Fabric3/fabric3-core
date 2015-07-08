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
package org.fabric3.implementation.web.generator;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import org.fabric3.api.host.Fabric3Exception;
import org.fabric3.api.host.runtime.HostInfo;
import org.fabric3.api.model.type.component.Component;
import org.fabric3.api.model.type.component.ComponentType;
import org.fabric3.api.model.type.component.Producer;
import org.fabric3.api.model.type.component.Property;
import org.fabric3.api.model.type.component.Reference;
import org.fabric3.api.model.type.component.ResourceReference;
import org.fabric3.api.model.type.contract.ServiceContract;
import org.fabric3.api.model.type.java.Injectable;
import org.fabric3.api.model.type.java.InjectionSite;
import org.fabric3.implementation.web.model.WebComponentType;
import org.fabric3.implementation.web.model.WebImplementation;
import org.fabric3.implementation.web.provision.PhysicalWebComponent;
import org.fabric3.implementation.web.provision.WebConnectionSource;
import org.fabric3.implementation.web.provision.WebContextInjectionSite;
import org.fabric3.implementation.web.provision.WebWireSource;
import org.fabric3.spi.domain.generator.ComponentGenerator;
import org.fabric3.spi.model.instance.LogicalComponent;
import org.fabric3.spi.model.instance.LogicalConsumer;
import org.fabric3.spi.model.instance.LogicalProducer;
import org.fabric3.spi.model.instance.LogicalProperty;
import org.fabric3.spi.model.instance.LogicalReference;
import org.fabric3.spi.model.instance.LogicalResourceReference;
import org.fabric3.spi.model.instance.LogicalService;
import org.fabric3.spi.model.physical.PhysicalComponent;
import org.fabric3.spi.model.physical.PhysicalConnectionSource;
import org.fabric3.spi.model.physical.PhysicalConnectionTarget;
import org.fabric3.spi.model.physical.PhysicalProperty;
import org.fabric3.spi.model.physical.PhysicalWireSource;
import org.fabric3.spi.model.physical.PhysicalWireTarget;
import org.oasisopen.sca.ComponentContext;
import org.oasisopen.sca.annotation.EagerInit;
import org.w3c.dom.Document;
import static org.fabric3.container.web.spi.WebApplicationActivator.OASIS_CONTEXT_ATTRIBUTE;
import static org.fabric3.implementation.web.provision.WebConstants.SERVLET_CONTEXT_SITE;
import static org.fabric3.implementation.web.provision.WebConstants.SESSION_CONTEXT_SITE;
import static org.fabric3.implementation.web.provision.WebContextInjectionSite.ContextType.SERVLET_CONTEXT;
import static org.fabric3.implementation.web.provision.WebContextInjectionSite.ContextType.SESSION_CONTEXT;

/**
 * Generates commands to provision a web component.
 */
@EagerInit
public class WebComponentGenerator implements ComponentGenerator<LogicalComponent<WebImplementation>> {
    private HostInfo info;

    public WebComponentGenerator(@org.oasisopen.sca.annotation.Reference HostInfo info) {
        this.info = info;
    }

    public PhysicalComponent generate(LogicalComponent<WebImplementation> component) throws Fabric3Exception {
        Component<WebImplementation> definition = component.getDefinition();
        WebComponentType componentType = definition.getImplementation().getComponentType();
        String contextUrl = calculateContextUrl(component);

        PhysicalWebComponent physical = new PhysicalWebComponent();
        physical.setContextUrl(contextUrl);
        Map<String, Map<String, InjectionSite>> sites = generateInjectionMapping(componentType);
        physical.setInjectionMappings(sites);
        processPropertyValues(component, physical);
        return physical;
    }

    public WebWireSource generateSource(LogicalReference reference) throws Fabric3Exception {
        WebWireSource source = new WebWireSource();
        source.setUri(reference.getUri());
        source.setOptimizable(true);
        return source;
    }

    public PhysicalWireSource generateCallbackSource(LogicalService service) throws Fabric3Exception {
        throw new UnsupportedOperationException();
    }

    public PhysicalWireTarget generateTarget(LogicalService service) throws Fabric3Exception {
        return null;
    }

    public PhysicalWireSource generateResourceSource(LogicalResourceReference<?> resourceReference) throws Fabric3Exception {
        WebWireSource source = new WebWireSource();
        source.setUri(resourceReference.getUri());
        return source;
    }

    public PhysicalConnectionSource generateConnectionSource(LogicalProducer producer) {
        WebConnectionSource source = new WebConnectionSource();
        source.setUri(producer.getUri());
        return source;
    }

    public PhysicalConnectionTarget generateConnectionTarget(LogicalConsumer consumer) throws Fabric3Exception {
        throw new UnsupportedOperationException();
    }

    private Map<String, Map<String, InjectionSite>> generateInjectionMapping(WebComponentType type) {
        Map<String, Map<String, InjectionSite>> mappings = new HashMap<>();
        for (Reference<ComponentType> definition : type.getReferences().values()) {
            generateReferenceInjectionMapping(definition, type, mappings);
        }
        for (ResourceReference definition : type.getResourceReferences().values()) {
            generateResourceInjectionMapping(definition, type, mappings);
        }
        for (Producer definition : type.getProducers().values()) {
            generateProducerInjectionMapping(definition, type, mappings);
        }
        for (Property property : type.getProperties().values()) {
            generatePropertyInjectionMapping(property, mappings);
        }
        generateContextInjectionMapping(type, mappings);
        return mappings;
    }

    private void generateReferenceInjectionMapping(Reference<ComponentType> definition,
                                                   WebComponentType type,
                                                   Map<String, Map<String, InjectionSite>> mappings) {
        Map<String, InjectionSite> mapping = mappings.get(definition.getName());
        if (mapping == null) {
            mapping = new HashMap<>();
            mappings.put(definition.getName(), mapping);
        }
        for (Map.Entry<String, Map<InjectionSite, Injectable>> entry : type.getInjectionSites().entrySet()) {
            for (Map.Entry<InjectionSite, Injectable> siteMap : entry.getValue().entrySet()) {
                if (siteMap.getValue().getName().equals(definition.getName())) {
                    mapping.put(entry.getKey(), siteMap.getKey());
                }
            }
        }
        ServiceContract contract = definition.getServiceContract();
        Class<?> interfaceClass = contract.getInterfaceClass();
        // inject the reference into the session context
        WebContextInjectionSite site = new WebContextInjectionSite(interfaceClass, SESSION_CONTEXT);
        mapping.put(SESSION_CONTEXT_SITE, site);
        // also inject the reference into the servlet context
        WebContextInjectionSite servletContextSite = new WebContextInjectionSite(interfaceClass, SERVLET_CONTEXT);
        mapping.put(SERVLET_CONTEXT_SITE, servletContextSite);
    }

    private void generateResourceInjectionMapping(ResourceReference definition, WebComponentType type, Map<String, Map<String, InjectionSite>> mappings) {
        Map<String, InjectionSite> mapping = mappings.get(definition.getName());
        if (mapping == null) {
            mapping = new HashMap<>();
            mappings.put(definition.getName(), mapping);
        }
        for (Map.Entry<String, Map<InjectionSite, Injectable>> entry : type.getInjectionSites().entrySet()) {
            for (Map.Entry<InjectionSite, Injectable> siteMap : entry.getValue().entrySet()) {
                if (siteMap.getValue().getName().equals(definition.getName())) {
                    mapping.put(entry.getKey(), siteMap.getKey());
                }
            }
        }
        ServiceContract contract = definition.getServiceContract();
        Class<?> interfaceClass = contract.getInterfaceClass();

        // also inject the reference into the servlet context
        WebContextInjectionSite servletContextSite = new WebContextInjectionSite(interfaceClass, SERVLET_CONTEXT);
        mapping.put(SERVLET_CONTEXT_SITE, servletContextSite);
    }

    private void generateProducerInjectionMapping(Producer definition, WebComponentType type, Map<String, Map<String, InjectionSite>> mappings) {
        Map<String, InjectionSite> mapping = mappings.get(definition.getName());
        if (mapping == null) {
            mapping = new HashMap<>();
            mappings.put(definition.getName(), mapping);
        }
        for (Map.Entry<String, Map<InjectionSite, Injectable>> entry : type.getInjectionSites().entrySet()) {
            for (Map.Entry<InjectionSite, Injectable> siteMap : entry.getValue().entrySet()) {
                if (siteMap.getValue().getName().equals(definition.getName())) {
                    mapping.put(entry.getKey(), siteMap.getKey());
                }
            }
        }
        ServiceContract contract = definition.getServiceContract();
        Class<?> interfaceClass = contract.getInterfaceClass();
        // also inject the reference into the servlet context
        WebContextInjectionSite servletContextSite = new WebContextInjectionSite(interfaceClass, SERVLET_CONTEXT);
        mapping.put(SERVLET_CONTEXT_SITE, servletContextSite);
    }

    private void generatePropertyInjectionMapping(Property property, Map<String, Map<String, InjectionSite>> mappings) {
        Map<String, InjectionSite> mapping = mappings.get(property.getName());
        if (mapping == null) {
            mapping = new HashMap<>();
            mappings.put(property.getName(), mapping);
        }
        // inject the property into the session context
        // we don't need to do the type mappings from schema to Java so set Object as the type
        WebContextInjectionSite site = new WebContextInjectionSite(Object.class, SERVLET_CONTEXT);
        mapping.put(SESSION_CONTEXT_SITE, site);
    }

    private void generateContextInjectionMapping(WebComponentType type, Map<String, Map<String, InjectionSite>> mappings) {
        // OASIS API
        Map<String, InjectionSite> oasisMapping = mappings.get(OASIS_CONTEXT_ATTRIBUTE);
        if (oasisMapping == null) {
            oasisMapping = new HashMap<>();
            WebContextInjectionSite site = new WebContextInjectionSite(ComponentContext.class, SESSION_CONTEXT);
            oasisMapping.put(SESSION_CONTEXT_SITE, site);
            mappings.put(OASIS_CONTEXT_ATTRIBUTE, oasisMapping);
        }
        for (Map.Entry<String, Map<InjectionSite, Injectable>> entry : type.getInjectionSites().entrySet()) {
            for (Map.Entry<InjectionSite, Injectable> siteMap : entry.getValue().entrySet()) {
                if (siteMap.getValue().equals(Injectable.OASIS_COMPONENT_CONTEXT)) {
                    oasisMapping.put(entry.getKey(), siteMap.getKey());
                }
            }
        }

    }

    private void processPropertyValues(LogicalComponent<?> component, PhysicalWebComponent physical) {
        for (LogicalProperty property : component.getAllProperties().values()) {
            String name = property.getName();
            boolean many = property.isMany();
            if (property.getXmlValue() != null) {
                Document document = property.getXmlValue();
                PhysicalProperty physicalProperty =  PhysicalProperty.Builder.newBuilder(name).xmlValue(document).many(many).build();
                physical.setProperty(physicalProperty);
            } else if (property.getInstanceValue() != null) {
                Object value = property.getInstanceValue();
                PhysicalProperty physicalProperty =  PhysicalProperty.Builder.newBuilder(name).instanceValue(value).many(many).build();
                physical.setProperty(physicalProperty);
            }
        }
    }

    /**
     * Derives the context URL for the web application relative to the domain.
     *
     * @param component the component
     * @return the context URL
     */
    private String calculateContextUrl(LogicalComponent<WebImplementation> component) {
        URI contextUri = component.getDefinition().getImplementation().getUri();
        if (contextUri == null) {
            // the context URL for the web application is derived from the component name if a URI is not specified
            contextUri = component.getUri();
        }
        return info.getDomain().relativize(contextUri).toString();

    }

}
