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
package org.fabric3.management.rest.framework.domain.component;

import javax.servlet.http.HttpServletRequest;
import java.net.URI;
import java.net.URL;

import org.fabric3.api.annotation.management.Management;
import org.fabric3.api.annotation.management.ManagementOperation;
import org.fabric3.management.rest.framework.ResourceHelper;
import org.fabric3.management.rest.model.HttpStatus;
import org.fabric3.management.rest.model.Response;
import org.fabric3.management.rest.model.SelfLink;
import org.fabric3.spi.domain.LogicalComponentManager;
import org.fabric3.spi.model.instance.LogicalComponent;
import org.fabric3.spi.model.instance.LogicalCompositeComponent;
import org.oasisopen.sca.annotation.EagerInit;
import org.oasisopen.sca.annotation.Reference;

/**
 * Handles the /domain/components resource by mapping the HTTP GET URL to the logical component hierarchy for a domain.
 *
 * Note this resource is only present on the controller.
 */
@EagerInit
@Management(path = "/domain/components")
public class ComponentsResourceService {
    private static final String COMPONENT_BASE_PATH = "/domain/components/";
    private LogicalComponentManager lcm;

    public ComponentsResourceService(@Reference(name = "lcm") LogicalComponentManager lcm) {
        this.lcm = lcm;
    }

    @ManagementOperation(path = "/")
    public Response getComponents(HttpServletRequest request) {
        LogicalComponent root = findComponent(request);
        if (root == null) {
            return new Response(HttpStatus.NOT_FOUND);
        }
        if (root instanceof LogicalCompositeComponent) {
            CompositeResource domainResource = createCompositeResource(root, request);
            return new Response(HttpStatus.OK, domainResource);
        } else {
            URL url = ResourceHelper.createUrl(ResourceHelper.getRequestUrl(request));
            ComponentResource resource = createComponentResource(root, url);
            return new Response(HttpStatus.OK, resource);
        }
    }

    /**
     * Resolves a logical component based on the HTTP GET URL. The URL path excluding the {@link #COMPONENT_BASE_PATH} is mapped to the logical
     * component URI.
     *
     * @param request the current HTTP request
     * @return the component or null if not found
     */
    private LogicalComponent findComponent(HttpServletRequest request) {
        LogicalComponent component;
        String pathInfo = request.getPathInfo();
        if (pathInfo.startsWith(COMPONENT_BASE_PATH)) {
            // exclude the base path and map the rest of the path to the domain hierarchy
            String name = pathInfo.substring(COMPONENT_BASE_PATH.length());
            String base = lcm.getRootComponent().getUri().toString() + "/";
            URI uri = URI.create(base + name);
            component = lcm.getComponent(uri);
        } else {
            // only the base path, the root (domain) component was requested
            component = lcm.getRootComponent();
        }
        return component;
    }

    /**
     * Recursively creates a component resource for a root composite component and its children.
     *
     * @param composite the composite component
     * @param request   the current HTTP request
     * @return the composite resource
     */
    private CompositeResource createCompositeResource(LogicalComponent composite, HttpServletRequest request) {
        CompositeResource compositeResource = new CompositeResource(composite.getUri(), composite.getZone());
        URL url = ResourceHelper.createUrl(ResourceHelper.getRequestUrl(request));
        SelfLink selfLink = new SelfLink(url);
        compositeResource.setSelfLink(selfLink);
        String baseUrl = request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort() + "/management" + COMPONENT_BASE_PATH;
        copy((LogicalCompositeComponent) composite, compositeResource, baseUrl);
        return compositeResource;
    }

    /**
     * Creates a component resource representation for an atomic component.
     *
     * @param component the atomic component
     * @param url       the component resource URL
     * @return the component resource representation
     */
    private ComponentResource createComponentResource(LogicalComponent component, URL url) {
        ComponentResource resource = new ComponentResource(component.getUri(), component.getZone());
        SelfLink selfLink = new SelfLink(url);
        resource.setSelfLink(selfLink);
        return resource;
    }

    /**
     * Performs a deep-copy of the logical component hierarchy to a resource representation.
     *
     * @param composite the root logical component
     * @param resource  the resource
     * @param baseUrl   the base URL for calculating self-links
     */
    private void copy(LogicalCompositeComponent composite, CompositeResource resource, String baseUrl) {
        for (LogicalComponent<?> component : composite.getComponents()) {
            URL url = ResourceHelper.createUrl(baseUrl + component.getUri().getPath().substring(1));     // strip leading '/'
            if (component instanceof LogicalCompositeComponent) {
                CompositeResource childResource = new CompositeResource(component.getUri(), component.getZone());
                SelfLink selfLink = new SelfLink(url);
                childResource.setSelfLink(selfLink);
                copy((LogicalCompositeComponent) component, childResource, baseUrl);
                resource.addComponent(childResource);
            } else {
                ComponentResource childResource = createComponentResource(component, url);
                resource.addComponent(childResource);
            }
        }
    }


}
