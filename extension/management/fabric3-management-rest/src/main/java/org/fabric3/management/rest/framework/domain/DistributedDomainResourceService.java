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
package org.fabric3.management.rest.framework.domain;

import javax.servlet.http.HttpServletRequest;
import java.net.URL;

import org.fabric3.api.annotation.management.Management;
import org.fabric3.api.annotation.management.ManagementOperation;
import org.fabric3.api.host.runtime.HostInfo;
import org.fabric3.api.model.type.RuntimeMode;
import org.fabric3.management.rest.framework.ResourceHelper;
import org.fabric3.management.rest.model.Link;
import org.fabric3.management.rest.model.Resource;
import org.fabric3.management.rest.model.SelfLink;
import org.oasisopen.sca.annotation.EagerInit;
import org.oasisopen.sca.annotation.Reference;
import static org.fabric3.management.rest.model.Link.EDIT_LINK;

/**
 * Produces the /domain resource.
 * <p/>
 * Note this resource is only present on the controller.
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
