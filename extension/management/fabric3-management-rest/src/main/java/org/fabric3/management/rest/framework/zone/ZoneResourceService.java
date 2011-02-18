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

import org.osoa.sca.annotations.EagerInit;
import org.osoa.sca.annotations.Reference;

import org.fabric3.api.annotation.management.Management;
import org.fabric3.api.annotation.management.ManagementOperation;
import org.fabric3.host.runtime.HostInfo;
import org.fabric3.management.rest.framework.ResourceHelper;
import org.fabric3.management.rest.model.Link;
import org.fabric3.management.rest.model.Resource;
import org.fabric3.management.rest.model.SelfLink;
import org.fabric3.management.rest.runtime.TransformerPair;
import org.fabric3.management.rest.spi.ResourceListener;
import org.fabric3.management.rest.spi.ResourceMapping;
import org.fabric3.management.rest.spi.Verb;
import org.fabric3.spi.federation.ZoneTopologyService;

/**
 * Produces the /zone resource.
 *
 * @version $Rev: 9923 $ $Date: 2011-02-03 17:11:06 +0100 (Thu, 03 Feb 2011) $
 */
@EagerInit
@Management(path = "/zone")
public class ZoneResourceService implements ResourceListener {
    private static final String RUNTIME_PATH = "/runtime";
    private HostInfo info;
    private ZoneTopologyService topologyService;

    private List<ResourceMapping> subresources = new ArrayList<ResourceMapping>();


    public ZoneResourceService(@Reference HostInfo info) {
        this.info = info;
    }

    @Reference(required = false)
    public void setTopologyService(ZoneTopologyService topologyService) {
        this.topologyService = topologyService;
    }

    @ManagementOperation(path = "/")
    public Resource getZoneResource(HttpServletRequest request) {
        SelfLink selfLink = ResourceHelper.createSelfLink(request);
        Resource resource = new Resource(selfLink);
        String leaderName = getLeader();
        resource.setProperty("name", info.getRuntimeName());
        resource.setProperty("leader", leaderName);

        String requestUrl = request.getRequestURL().toString();
        for (ResourceMapping mapping : subresources) {
            String path = mapping.getRelativePath().substring(RUNTIME_PATH.length() + 1); // +1 to remove leading '/' for relative link
            URL url = ResourceHelper.createUrl(requestUrl + '/' + path);
            Link link = new Link(path, Link.EDIT_LINK, url);
            resource.setProperty(link.getName(), link);
        }
        return resource;
    }


    public void onRootResourceExport(ResourceMapping mapping) {
        if (!mapping.getPath().startsWith(RUNTIME_PATH + "/")) {
            // resource is not under runtime path, return
            return;
        }

        String path = "/zone" + mapping.getPath();
        String relativePath = "/zone" + mapping.getRelativePath();
        Verb verb = mapping.getVerb();
        Method method = mapping.getMethod();
        Object instance = mapping.getInstance();
        TransformerPair jaxbPair = mapping.getJaxbPair();
        TransformerPair jsonPair = mapping.getJsonPair();
        ResourceMapping newMapping = new ResourceMapping(path, relativePath, verb, method, instance, jsonPair, jaxbPair);
        subresources.add(newMapping);

    }

    private String getLeader() {
        if (topologyService == null) {
            // running in single-VM or controller mode, return current runtime
            return info.getRuntimeName();
        }
        return topologyService.getZoneLeaderName();
    }


}
