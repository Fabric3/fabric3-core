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
package org.fabric3.implementation.web.introspection;

import javax.servlet.Filter;
import javax.servlet.Servlet;
import java.net.URL;
import java.util.Map;

import org.fabric3.api.model.type.component.ComponentType;
import org.fabric3.api.model.type.component.Producer;
import org.fabric3.api.model.type.component.Reference;
import org.fabric3.api.model.type.component.ResourceReference;
import org.fabric3.api.model.type.contract.ServiceContract;
import org.fabric3.api.model.type.java.Injectable;
import org.fabric3.api.model.type.java.InjectingComponentType;
import org.fabric3.api.model.type.java.InjectionSite;
import org.fabric3.implementation.web.model.WebComponentType;
import org.fabric3.spi.contract.ContractMatcher;
import org.fabric3.spi.contract.MatchResult;
import org.fabric3.spi.contribution.Contribution;
import org.fabric3.spi.contribution.JavaArtifactIntrospector;
import org.fabric3.spi.contribution.Resource;
import org.fabric3.spi.contribution.ResourceElement;
import org.fabric3.spi.contribution.ResourceState;
import org.fabric3.spi.introspection.IntrospectionContext;
import org.fabric3.spi.introspection.TypeMapping;
import org.fabric3.spi.introspection.java.IntrospectionHelper;
import org.fabric3.spi.introspection.java.annotation.ClassVisitor;

/**
 * Introspects a web application to create a component type. One component type per web contribution is created and stored as a resource to be used to configure
 * the web component.
 *
 * Since introspection is performed during contribution indexing, it is guaranteed to be available when the web component configuration is created during the
 * contribution processing phase.
 */
public class WebArtifactIntrospector implements JavaArtifactIntrospector {
    private ClassVisitor classVisitor;
    private ContractMatcher matcher;
    private IntrospectionHelper helper;

    public WebArtifactIntrospector(@org.oasisopen.sca.annotation.Reference ClassVisitor classVisitor, @org.oasisopen.sca.annotation.Reference ContractMatcher matcher, @org.oasisopen.sca.annotation.Reference IntrospectionHelper helper) {
        this.classVisitor = classVisitor;
        this.matcher = matcher;
        this.helper = helper;
    }

    public Resource inspect(Class<?> clazz, URL url, Contribution contribution, IntrospectionContext context) {
        String sourceUrl = contribution.getLocation().toString();
        if (!sourceUrl.endsWith(".war")) {
            // not a WAR file
            return null;
        }

        if (contribution.getManifest().isExtension()) {
            return null;
        }

        if (!Servlet.class.isAssignableFrom(clazz) && !Filter.class.isAssignableFrom(clazz)) {
            // skip classes that are not servlets or filters
            return null;
        }
        return introspect(clazz, contribution, context);

    }

    public Resource introspect(Class<?> clazz, Contribution contribution, IntrospectionContext context) {
        ResourceElement<WebComponentTypeSymbol, WebComponentType> element = getTypeElement(contribution);
        WebComponentType componentType = element.getValue();
        // introspect the class and generate a component type that will be merged into the web component type
        WebArtifactImplementation artifactImpl = new WebArtifactImplementation();
        InjectingComponentType tempType = new InjectingComponentType(clazz);
        artifactImpl.setComponentType(tempType);
        TypeMapping mapping = context.getTypeMapping(clazz);
        if (mapping == null) {
            mapping = new TypeMapping();
            context.addTypeMapping(clazz, mapping);
            helper.resolveTypeParameters(clazz, mapping);
        }
        classVisitor.visit(tempType, clazz, context);
        mergeComponentTypes(componentType, tempType, context);
        return element.getResource();
    }

    @SuppressWarnings("unchecked")
    private ResourceElement<WebComponentTypeSymbol, WebComponentType> getTypeElement(Contribution contribution) {
        for (Resource resource : contribution.getResources()) {
            for (ResourceElement<?, ?> element : resource.getResourceElements()) {
                if (element.getSymbol() instanceof WebComponentTypeSymbol) {
                    return (ResourceElement<WebComponentTypeSymbol, WebComponentType>) element;
                }
            }
        }
        WebComponentType componentType = new WebComponentType();
        WebComponentTypeSymbol symbol = new WebComponentTypeSymbol();
        ResourceElement<WebComponentTypeSymbol, WebComponentType> element = new ResourceElement<>(symbol, componentType);
        Resource resource = new Resource(contribution, null, "web");
        resource.addResourceElement(element);
        resource.setState(ResourceState.PROCESSED);
        contribution.addResource(resource);
        return element;
    }

    /**
     * Merges the POJO component type into the web component type.
     *
     * @param componentType the web component type to merge into
     * @param tempType      the component type to merge
     * @param context       the introspection context
     */
    private void mergeComponentTypes(WebComponentType componentType, InjectingComponentType tempType, IntrospectionContext context) {
        for (Map.Entry<String, Reference<ComponentType>> entry : tempType.getReferences().entrySet()) {
            String name = entry.getKey();
            Reference<ComponentType> reference = componentType.getReferences().get(name);
            if (reference != null) {
                ServiceContract source = reference.getServiceContract();
                ServiceContract target = entry.getValue().getServiceContract();
                MatchResult result = matcher.isAssignableFrom(source, target, false);
                if (!result.isAssignable()) {
                    IncompatibleReferenceDefinitions failure = new IncompatibleReferenceDefinitions(name);
                    context.addError(failure);
                }

            } else {
                componentType.add(entry.getValue());
            }
        }
        for (Map.Entry<String, ResourceReference> entry : tempType.getResourceReferences().entrySet()) {
            String name = entry.getKey();
            ResourceReference definition = componentType.getResourceReferences().get(name);
            if (definition != null) {
                ServiceContract source = definition.getServiceContract();
                ServiceContract target = entry.getValue().getServiceContract();
                MatchResult result = matcher.isAssignableFrom(source, target, false);
                if (!result.isAssignable()) {
                    IncompatibleReferenceDefinitions failure = new IncompatibleReferenceDefinitions(name);
                    context.addError(failure);
                }

            } else {
                componentType.add(entry.getValue());
            }
        }
        for (Map.Entry<String, Producer<ComponentType>> entry : tempType.getProducers().entrySet()) {
            String name = entry.getKey();
            Producer definition = componentType.getProducers().get(name);
            if (definition != null) {
                ServiceContract source = definition.getServiceContract();
                ServiceContract target = entry.getValue().getServiceContract();
                MatchResult result = matcher.isAssignableFrom(source, target, false);
                if (!result.isAssignable()) {
                    IncompatibleReferenceDefinitions failure = new IncompatibleReferenceDefinitions(name);
                    context.addError(failure);
                }

            } else {
                componentType.add(entry.getValue());
            }
        }

        // apply all injection sites
        for (Map.Entry<InjectionSite, Injectable> entry : tempType.getInjectionSites().entrySet()) {
            componentType.addMapping(tempType.getImplClass().getName(), entry.getKey(), entry.getValue());
        }
    }

}
