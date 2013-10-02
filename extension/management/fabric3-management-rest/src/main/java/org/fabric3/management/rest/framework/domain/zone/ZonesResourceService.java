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
package org.fabric3.management.rest.framework.domain.zone;

import java.net.URL;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import javax.servlet.http.HttpServletRequest;

import org.fabric3.host.Names;
import org.oasisopen.sca.annotation.EagerInit;
import org.oasisopen.sca.annotation.Reference;

import org.fabric3.api.annotation.management.Management;
import org.fabric3.api.annotation.management.ManagementOperation;
import org.fabric3.management.rest.framework.ResourceHelper;
import org.fabric3.management.rest.model.Link;
import org.fabric3.spi.federation.DomainTopologyService;
import org.fabric3.spi.federation.RuntimeInstance;
import org.fabric3.spi.federation.Zone;

import static org.fabric3.management.rest.model.Link.EDIT_LINK;
import static org.fabric3.spi.federation.FederationConstants.HTTP_HOST_METADATA;
import static org.fabric3.spi.federation.FederationConstants.HTTP_PORT_METADATA;

/**
 * Produces the /domain/zones resource. This is a collection of links to active zone resources in the domain. The links correspond to zone leaders,
 * which provide /zone resources.
 * <p/>
 * Note this resource is only present on the controller.
 */
@EagerInit
@Management(path = "/domain/zones")
public class ZonesResourceService {
    private DomainTopologyService topologyService;

    @Reference(required = false)
    public void setTopologyService(DomainTopologyService topologyService) {
        this.topologyService = topologyService;
    }

    @ManagementOperation(path = "/")
    public Set<Link> getZones(HttpServletRequest request) {
        if (topologyService == null) {
            return createLocalZoneLink(request);
        }
        return createDistributedZonesLink();
    }

    private Set<Link> createLocalZoneLink(HttpServletRequest request) {
        StringBuffer requestUrl = request.getRequestURL();
        URL zoneUrl = ResourceHelper.createUrl(requestUrl.substring(0, requestUrl.toString().indexOf("/management/") + 12) + "zone");
        Link link = new Link(Names.LOCAL_ZONE, EDIT_LINK, zoneUrl);
        return Collections.singleton(link);
    }

    private Set<Link> createDistributedZonesLink() {
        Set<Link> list = new HashSet<Link>();
        Set<Zone> zones = topologyService.getZones();
        for (Zone zone : zones) {
            // calculate links for each zone by using the address of zone leaders
            RuntimeInstance leader = zone.getRuntimes().get(0);
            String httpPort = leader.getMetadata(Integer.class, HTTP_PORT_METADATA).toString();
            String host = leader.getMetadata(String.class, HTTP_HOST_METADATA);
            URL zoneUrl = ResourceHelper.createUrl("http://" + host + ":" + httpPort + "/management/zone");
            Link link = new Link(zone.getName(), EDIT_LINK, zoneUrl);
            list.add(link);
        }
        return list;
    }

}
