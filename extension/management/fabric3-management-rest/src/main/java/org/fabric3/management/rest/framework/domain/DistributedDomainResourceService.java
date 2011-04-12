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
package org.fabric3.management.rest.framework.domain;

import java.net.URL;
import javax.servlet.http.HttpServletRequest;

import org.osoa.sca.annotations.EagerInit;
import org.osoa.sca.annotations.Reference;

import org.fabric3.api.annotation.management.Management;
import org.fabric3.api.annotation.management.ManagementOperation;
import org.fabric3.host.RuntimeMode;
import org.fabric3.host.runtime.HostInfo;
import org.fabric3.management.rest.framework.ResourceHelper;
import org.fabric3.management.rest.model.Link;
import org.fabric3.management.rest.model.Resource;
import org.fabric3.management.rest.model.SelfLink;

import static org.fabric3.management.rest.model.Link.EDIT_LINK;

/**
 * Produces the /domain resource.
 * <p/>
 * Note this resource is only present on the controller.
 *
 * @version $Rev: 9923 $ $Date: 2011-02-03 17:11:06 +0100 (Thu, 03 Feb 2011) $
 */
@EagerInit
@Management(path = "/domain")
public class DistributedDomainResourceService {
    private HostInfo info;

    public DistributedDomainResourceService(@Reference HostInfo info) {
        this.info = info;
    }

    @ManagementOperation(path = "/")
    public Resource getDomainResource(HttpServletRequest request) {
        SelfLink selfLink = ResourceHelper.createSelfLink(request);
        Resource resource = new Resource(selfLink);

        createZoneLinks(request, resource);
        createRuntimeLinks(request, resource);
        createContributionsLink(request, resource);
        createDeploymentsLink(request, resource);
        createComponentsLink(request, resource);

        return resource;
    }

    private void createZoneLinks(HttpServletRequest request, Resource resource) {
        if (info.getRuntimeMode() == RuntimeMode.VM) {
            // running in single-VM mode, return
            return;
        }
        URL url = ResourceHelper.createUrl(request.getRequestURL().toString() + "/zones");
        Link link = new Link("zones", EDIT_LINK, url);
        resource.setProperty("zones", link);
    }

    private void createRuntimeLinks(HttpServletRequest request, Resource resource) {
        if (info.getRuntimeMode() == RuntimeMode.VM) {
            // running in single-VM mode, return
            return;
        }
        URL url = ResourceHelper.createUrl(request.getRequestURL().toString() + "/runtimes");
        Link link = new Link("runtimes", EDIT_LINK, url);
        resource.setProperty("runtimes", link);
    }

    private void createContributionsLink(HttpServletRequest request, Resource resource) {
        URL url = ResourceHelper.createUrl(ResourceHelper.getRequestUrl(request) + "/contributions");
        Link link = new Link("contributions", EDIT_LINK, url);
        resource.setProperty("contributions", link);
    }

    private void createDeploymentsLink(HttpServletRequest request, Resource resource) {
        URL url = ResourceHelper.createUrl(ResourceHelper.getRequestUrl(request) + "/deployments");
        Link link = new Link("deployments", EDIT_LINK, url);
        resource.setProperty("deployments", link);
    }

    private void createComponentsLink(HttpServletRequest request, Resource resource) {
        URL url = ResourceHelper.createUrl(ResourceHelper.getRequestUrl(request) + "/components");
        Link link = new Link("components", EDIT_LINK, url);
        resource.setProperty("components", link);
    }


}
