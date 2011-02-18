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
package org.fabric3.management.rest.framework.zone;

import java.lang.reflect.Method;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import javax.servlet.http.HttpServletRequest;

import org.osoa.sca.annotations.Destroy;
import org.osoa.sca.annotations.EagerInit;
import org.osoa.sca.annotations.Reference;

import org.fabric3.api.annotation.management.Management;
import org.fabric3.api.annotation.management.ManagementOperation;
import org.fabric3.api.annotation.monitor.Monitor;
import org.fabric3.host.runtime.HostInfo;
import org.fabric3.management.rest.framework.ResourceHelper;
import org.fabric3.management.rest.model.Link;
import org.fabric3.management.rest.model.Resource;
import org.fabric3.management.rest.model.SelfLink;
import org.fabric3.management.rest.runtime.ManagementMonitor;
import org.fabric3.management.rest.runtime.TransformerPair;
import org.fabric3.management.rest.spi.DuplicateResourceNameException;
import org.fabric3.management.rest.spi.ResourceHost;
import org.fabric3.management.rest.spi.ResourceListener;
import org.fabric3.management.rest.spi.ResourceMapping;
import org.fabric3.management.rest.spi.Verb;
import org.fabric3.spi.federation.ZoneTopologyService;

import static org.fabric3.management.rest.model.Link.EDIT_LINK;

/**
 * Produces the /zone resource.
 *
 * @version $Rev: 9923 $ $Date: 2011-02-03 17:11:06 +0100 (Thu, 03 Feb 2011) $
 */
@EagerInit
@Management(path = "/zone")
public class ZoneResourceService implements ResourceListener {
    private static final String RUNTIME_PATH = "/runtime/";
    private ResourceHost resourceHost;
    private ManagementMonitor monitor;
    private HostInfo info;
    private ZoneTopologyService topologyService;

    private List<ResourceMapping> subresources = new ArrayList<ResourceMapping>();


    public ZoneResourceService(@Reference ResourceHost resourceHost, @Reference HostInfo info, @Monitor ManagementMonitor monitor) {
        this.resourceHost = resourceHost;
        this.info = info;
        this.monitor = monitor;
    }

    @Reference(required = false)
    public void setTopologyService(ZoneTopologyService topologyService) {
        this.topologyService = topologyService;
    }

    @Destroy
    public void destroy() {
        for (ResourceMapping mapping : subresources) {
            resourceHost.unregister(mapping);
        }
    }

    @ManagementOperation(path = "/")
    public Resource getZoneResource(HttpServletRequest request) {
        SelfLink selfLink = ResourceHelper.createSelfLink(request);
        Resource resource = new Resource(selfLink);
        String leaderName = getLeader();
        resource.setProperty("name", info.getRuntimeName());
        resource.setProperty("leader", leaderName);
        createRuntimeLink(request,resource);
        return resource;
    }

    @ManagementOperation(path = "runtime")
    public Resource getZoneRuntimeResource(HttpServletRequest request) {
        SelfLink selfLink = ResourceHelper.createSelfLink(request);
        Resource resource = new Resource(selfLink);
        String requestUrl = ResourceHelper.getRequestUrl(request);
        for (ResourceMapping mapping : subresources) {
            String path = mapping.getRelativePath();
            URL url = ResourceHelper.createUrl(requestUrl + '/' + path);
            Link link = new Link(path, Link.EDIT_LINK, url);
            resource.setProperty(link.getName(), link);
        }
        return resource;
    }

    public void onRootResourceExport(ResourceMapping mapping) {
        if (!mapping.getPath().startsWith(RUNTIME_PATH)) {
            // resource is not under runtime path, return
            return;
        }

        String path = "/zone" + mapping.getPath();
        String relativePath = mapping.getRelativePath().substring(RUNTIME_PATH.length());
        Verb verb = mapping.getVerb();
        Method method = mapping.getMethod();
        Object instance = mapping.getInstance();
        TransformerPair jaxbPair = mapping.getJaxbPair();
        TransformerPair jsonPair = mapping.getJsonPair();
        ResourceMapping newMapping = new ResourceMapping(path, relativePath, verb, method, instance, jsonPair, jaxbPair);
        subresources.add(newMapping);
        try {
            resourceHost.register(newMapping);
        } catch (DuplicateResourceNameException e) {
            monitor.error("Duplicate mapping: " + path, e);
        }

    }

    private String getLeader() {
        if (topologyService == null) {
            // running in single-VM or controller mode, return current runtime
            return info.getRuntimeName();
        }
        return topologyService.getZoneLeaderName();
    }

    private void createRuntimeLink(HttpServletRequest request, Resource resource) {
        URL url = ResourceHelper.createUrl(ResourceHelper.getRequestUrl(request) + "/runtime");
        Link link = new Link("runtime", EDIT_LINK, url);
        resource.setProperty("runtime", link);
    }

}
