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
package org.fabric3.management.rest.framework.domain.zone;

import javax.servlet.http.HttpServletRequest;
import java.net.URL;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.fabric3.api.annotation.management.Management;
import org.fabric3.api.annotation.management.ManagementOperation;
import org.fabric3.api.host.Names;
import org.fabric3.api.host.runtime.HostInfo;
import org.fabric3.api.model.type.RuntimeMode;
import org.fabric3.management.rest.framework.ResourceHelper;
import org.fabric3.management.rest.model.Link;
import org.fabric3.spi.federation.addressing.AddressCache;
import org.fabric3.spi.federation.addressing.SocketAddress;
import org.oasisopen.sca.annotation.EagerInit;
import org.oasisopen.sca.annotation.Reference;
import static org.fabric3.management.rest.model.Link.EDIT_LINK;
import static org.fabric3.spi.federation.addressing.EndpointConstants.HTTP_SERVER;

/**
 * Produces the /domain/zones resource. This is a collection of links to active zone resources in the domain. The links correspond to zone leaders, which
 * provide /zone resources.
 *
 * Note this resource is only present on the controller.
 */
@EagerInit
@Management(path = "/domain/zones")
public class ZonesResourceService {
    private AddressCache addressCache;
    private HostInfo hostInfo;

    public ZonesResourceService(@Reference AddressCache addressCache, @Reference HostInfo hostInfo) {
        this.addressCache = addressCache;
        this.hostInfo = hostInfo;
    }

    @ManagementOperation(path = "/")
    public Set<Link> getZones(HttpServletRequest request) {
        if (RuntimeMode.VM == hostInfo.getRuntimeMode()) {
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
        Set<Link> list = new HashSet<>();
        Set<String> zones = new HashSet<>();
        // calculate the list of zones by taking the first socket encountered in the zone
        for (SocketAddress address : addressCache.getActiveAddresses(HTTP_SERVER)) {
            String zone = address.getZone();
            if (zones.contains(zone)) {
                continue;
            }
            int httpPort = address.getPort().getNumber();
            String host = address.getAddress();
            URL zoneUrl = ResourceHelper.createUrl("http://" + host + ":" + httpPort + "/management/zone");
            Link link = new Link(zone, EDIT_LINK, zoneUrl);
            list.add(link);
            zones.add(zone);
        }

        return list;
    }

}
