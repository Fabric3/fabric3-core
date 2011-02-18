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
package org.fabric3.management.rest.framework.domain.component;

import java.net.URI;
import java.net.URL;
import javax.servlet.http.HttpServletRequest;

import org.osoa.sca.annotations.EagerInit;
import org.osoa.sca.annotations.Reference;

import org.fabric3.api.annotation.management.Management;
import org.fabric3.api.annotation.management.ManagementOperation;
import org.fabric3.management.rest.framework.ResourceHelper;
import org.fabric3.management.rest.model.HttpStatus;
import org.fabric3.management.rest.model.Response;
import org.fabric3.management.rest.model.SelfLink;
import org.fabric3.spi.lcm.LogicalComponentManager;
import org.fabric3.spi.model.instance.LogicalComponent;
import org.fabric3.spi.model.instance.LogicalCompositeComponent;

/**
 * Handles the /domain/components resource by mapping the HTTP GET URL to the logical component hierarchy for a domain.
 * <p/>
 * Note this resource is only present on the controller.
 *
 * @version $Rev: 9923 $ $Date: 2011-02-03 17:11:06 +0100 (Thu, 03 Feb 2011) $
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
            URL url = ResourceHelper.createUrl(request.getRequestURL().toString());
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
        URL url = ResourceHelper.createUrl(request.getRequestURL().toString());
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
