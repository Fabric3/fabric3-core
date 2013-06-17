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
package org.fabric3.implementation.web.generator;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import org.oasisopen.sca.ComponentContext;
import org.oasisopen.sca.annotation.EagerInit;
import org.oasisopen.sca.annotation.Reference;
import org.w3c.dom.Document;

import org.fabric3.host.runtime.HostInfo;
import org.fabric3.implementation.web.model.WebComponentType;
import org.fabric3.implementation.web.model.WebImplementation;
import org.fabric3.implementation.web.provision.WebComponentDefinition;
import org.fabric3.implementation.web.provision.WebComponentSourceDefinition;
import org.fabric3.implementation.web.provision.WebContextInjectionSite;
import org.fabric3.model.type.component.AbstractReference;
import org.fabric3.model.type.component.ComponentDefinition;
import org.fabric3.model.type.component.Property;
import org.fabric3.model.type.contract.ServiceContract;
import org.fabric3.spi.generator.ComponentGenerator;
import org.fabric3.spi.generator.EffectivePolicy;
import org.fabric3.spi.generator.GenerationException;
import org.fabric3.spi.model.instance.LogicalComponent;
import org.fabric3.spi.model.instance.LogicalConsumer;
import org.fabric3.spi.model.instance.LogicalProducer;
import org.fabric3.spi.model.instance.LogicalProperty;
import org.fabric3.spi.model.instance.LogicalReference;
import org.fabric3.spi.model.instance.LogicalResourceReference;
import org.fabric3.spi.model.instance.LogicalService;
import org.fabric3.spi.model.physical.PhysicalComponentDefinition;
import org.fabric3.spi.model.physical.PhysicalConnectionSourceDefinition;
import org.fabric3.spi.model.physical.PhysicalConnectionTargetDefinition;
import org.fabric3.spi.model.physical.PhysicalPropertyDefinition;
import org.fabric3.spi.model.physical.PhysicalSourceDefinition;
import org.fabric3.spi.model.physical.PhysicalTargetDefinition;
import org.fabric3.spi.model.type.java.Injectable;
import org.fabric3.spi.model.type.java.InjectionSite;

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

    public WebComponentGenerator(@Reference HostInfo info) {
        this.info = info;
    }

    public PhysicalComponentDefinition generate(LogicalComponent<WebImplementation> component) throws GenerationException {
        ComponentDefinition<WebImplementation> definition = component.getDefinition();
        WebComponentType componentType = definition.getImplementation().getComponentType();

        String contextUrl = calculateContextUrl(component);

        WebComponentDefinition physical = new WebComponentDefinition();
        physical.setContextUrl(contextUrl);
        Map<String, Map<String, InjectionSite>> sites = generateInjectionMapping(componentType);
        physical.setInjectionMappings(sites);
        processPropertyValues(component, physical);
        return physical;
    }

    public WebComponentSourceDefinition generateSource(LogicalReference reference, EffectivePolicy policy) throws GenerationException {
        WebComponentSourceDefinition sourceDefinition = new WebComponentSourceDefinition();
        sourceDefinition.setUri(reference.getUri());
        return sourceDefinition;
    }

    public PhysicalSourceDefinition generateCallbackSource(LogicalService service, EffectivePolicy policy) throws GenerationException {
        throw new UnsupportedOperationException();
    }

    public PhysicalTargetDefinition generateTarget(LogicalService service, EffectivePolicy policy) throws GenerationException {
        return null;
    }

    public PhysicalSourceDefinition generateResourceSource(LogicalResourceReference<?> resourceReference) throws GenerationException {
        return null;
    }

    public PhysicalConnectionSourceDefinition generateConnectionSource(LogicalProducer producer) {
        throw new UnsupportedOperationException();
    }

    public PhysicalConnectionTargetDefinition generateConnectionTarget(LogicalConsumer consumer) throws GenerationException {
        throw new UnsupportedOperationException();
    }

    private Map<String, Map<String, InjectionSite>> generateInjectionMapping(WebComponentType type) {
        Map<String, Map<String, InjectionSite>> mappings = new HashMap<String, Map<String, InjectionSite>>();
        for (AbstractReference definition : type.getReferences().values()) {
            generateReferenceInjectionMapping(definition, type, mappings);
        }
        for (Property property : type.getProperties().values()) {
            generatePropertyInjectionMapping(property, mappings);
        }
        generateContextInjectionMapping(type, mappings);
        return mappings;
    }

    private void generateReferenceInjectionMapping(AbstractReference definition,
                                                   WebComponentType type,
                                                   Map<String, Map<String, InjectionSite>> mappings) {
        Map<String, InjectionSite> mapping = mappings.get(definition.getName());
        if (mapping == null) {
            mapping = new HashMap<String, InjectionSite>();
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
        String interfaceClass = contract.getQualifiedInterfaceName();
        // inject the reference into the session context
        WebContextInjectionSite site = new WebContextInjectionSite(interfaceClass, SESSION_CONTEXT);
        mapping.put(SESSION_CONTEXT_SITE, site);
        // also inject the reference into the servlet context
        WebContextInjectionSite servletContextsite = new WebContextInjectionSite(interfaceClass, SERVLET_CONTEXT);
        mapping.put(SERVLET_CONTEXT_SITE, servletContextsite);
    }

    private void generatePropertyInjectionMapping(Property property, Map<String, Map<String, InjectionSite>> mappings) {
        Map<String, InjectionSite> mapping = mappings.get(property.getName());
        if (mapping == null) {
            mapping = new HashMap<String, InjectionSite>();
            mappings.put(property.getName(), mapping);
        }
        // inject the property into the session context
        // we don't need to do the type mappings from schema to Java so set Object as the type
        WebContextInjectionSite site = new WebContextInjectionSite(Object.class.getName(), SERVLET_CONTEXT);
        mapping.put(SESSION_CONTEXT_SITE, site);
    }

    private void generateContextInjectionMapping(WebComponentType type, Map<String, Map<String, InjectionSite>> mappings) {
        // OASIS API
        Map<String, InjectionSite> oasisMapping = mappings.get(OASIS_CONTEXT_ATTRIBUTE);
        if (oasisMapping == null) {
            oasisMapping = new HashMap<String, InjectionSite>();
            WebContextInjectionSite site = new WebContextInjectionSite(ComponentContext.class.getName(), SESSION_CONTEXT);
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

    private void processPropertyValues(LogicalComponent<?> component, WebComponentDefinition physical) {
        for (LogicalProperty property : component.getAllProperties().values()) {
            Document document = property.getValue();
            if (document != null) {
                String name = property.getName();
                boolean many = property.isMany();
                PhysicalPropertyDefinition definition = new PhysicalPropertyDefinition(name, document, many);
                physical.setPropertyDefinition(definition);
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
