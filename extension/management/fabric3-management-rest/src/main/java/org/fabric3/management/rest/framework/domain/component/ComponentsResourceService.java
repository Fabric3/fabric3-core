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
import javax.servlet.http.HttpServletRequest;

import org.osoa.sca.annotations.EagerInit;
import org.osoa.sca.annotations.Reference;

import org.fabric3.api.annotation.management.Management;
import org.fabric3.api.annotation.management.ManagementOperation;
import org.fabric3.management.rest.model.HttpStatus;
import org.fabric3.management.rest.model.Response;
import org.fabric3.spi.lcm.LogicalComponentManager;
import org.fabric3.spi.model.instance.LogicalComponent;
import org.fabric3.spi.model.instance.LogicalCompositeComponent;

/**
 * Handles the /domain/components resource and its sub-resources:
 * <pre>
 * <ul>
 *  <li>GET /deployments - Returns deployed contributions</ul>
 * </ul>
 * </pre>
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
        LogicalComponent root;
        String pathInfo = request.getPathInfo();
        if (pathInfo.startsWith(COMPONENT_BASE_PATH)) {
            String name = pathInfo.substring(COMPONENT_BASE_PATH.length());
            String base = lcm.getRootComponent().getUri().toString() + "/";
            URI uri = URI.create(base + name);
            root = lcm.getComponent(uri);
        } else {
            root = lcm.getRootComponent();
        }
        if (root == null) {
            return new Response(HttpStatus.NOT_FOUND);
        }
        if (root instanceof LogicalCompositeComponent) {
            CompositeResource domainResource = new CompositeResource(root.getUri(), root.getZone());
            copy((LogicalCompositeComponent) root, domainResource);
            return new Response(HttpStatus.OK, domainResource);
        } else {
            ComponentResource resource = new ComponentResource(root.getUri(), root.getZone());
            return new Response(HttpStatus.OK, resource);
        }
    }

    /**
     * Performs a deep-copy of the logical component hierarchy to a resource representation.
     *
     * @param composite the root logical component
     * @param resource  the resource
     */
    private void copy(LogicalCompositeComponent composite, CompositeResource resource) {
        for (LogicalComponent<?> component : composite.getComponents()) {
            if (component instanceof LogicalCompositeComponent) {
                CompositeResource childResource = new CompositeResource(component.getUri(), component.getZone());
                copy((LogicalCompositeComponent) component, childResource);
                resource.addComponent(childResource);
            } else {
                ComponentResource childResource = new ComponentResource(component.getUri(), component.getZone());
                resource.addComponent(childResource);
            }
        }
    }


}
