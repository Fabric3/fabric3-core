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
package org.fabric3.implementation.web.introspection;

import javax.servlet.Filter;
import javax.servlet.Servlet;
import java.net.URL;
import java.util.Map;

import org.fabric3.api.model.type.component.AbstractReference;
import org.fabric3.api.model.type.component.ReferenceDefinition;
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
import org.oasisopen.sca.annotation.Reference;

/**
 * Introspects a web application to create a component type. One component type per web contribution is created and stored as a resource to be used to configure
 * the web component.
 * <p/>
 * Since introspection is performed during contribution indexing, it is guaranteed to be available when the web component configuration is created during the
 * contribution processing phase.
 */
public class WebArtifactIntrospector implements JavaArtifactIntrospector {
    private ClassVisitor classVisitor;
    private ContractMatcher matcher;
    private IntrospectionHelper helper;

    public WebArtifactIntrospector(@Reference ClassVisitor classVisitor, @Reference ContractMatcher matcher, @Reference IntrospectionHelper helper) {
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

        if (!(Servlet.class.isAssignableFrom(clazz) && !(Filter.class.isAssignableFrom(clazz)))) {
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
        InjectingComponentType tempType = new InjectingComponentType(clazz.getName());
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
        for (Map.Entry<String, ReferenceDefinition> entry : tempType.getReferences().entrySet()) {
            String name = entry.getKey();
            AbstractReference reference = componentType.getReferences().get(name);
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
        // apply all injection sites
        for (Map.Entry<InjectionSite, Injectable> entry : tempType.getInjectionSites().entrySet()) {
            componentType.addMapping(tempType.getImplClass(), entry.getKey(), entry.getValue());
        }
    }

}
