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
package org.fabric3.management.rest.framework.domain.runtime;

import javax.servlet.http.HttpServletRequest;
import java.net.URL;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.fabric3.api.annotation.management.Management;
import org.fabric3.api.annotation.management.ManagementOperation;
import org.fabric3.api.model.type.RuntimeMode;
import org.fabric3.api.host.runtime.HostInfo;
import org.fabric3.management.rest.framework.ResourceHelper;
import org.fabric3.management.rest.model.Link;
import org.fabric3.spi.federation.addressing.AddressCache;
import org.fabric3.spi.federation.addressing.SocketAddress;
import org.oasisopen.sca.annotation.EagerInit;
import org.oasisopen.sca.annotation.Reference;
import static org.fabric3.management.rest.model.Link.EDIT_LINK;
import static org.fabric3.spi.federation.addressing.EndpointConstants.HTTP_SERVER;

/**
 * Produces the /domain/runtimes resource. This is a collection of links to active runtime resources in the domain.
 * <p/>
 * Note this resource is only present on the controller.
 */
@EagerInit
@Management(path = "/domain/runtimes")
public class RuntimesResourceService {
    private HostInfo info;
    private AddressCache addressCache;

    public RuntimesResourceService(@Reference HostInfo info, @Reference AddressCache addressCache) {
        this.info = info;
        this.addressCache = addressCache;
    }

    @ManagementOperation(path = "/")
    public Set<Link> getRuntimes(HttpServletRequest request) {
        if (info.getRuntimeMode() == RuntimeMode.VM) {
            return createLocalRuntimeLink(request);
        }
        return createDistributedRuntimesLink();
    }

    private Set<Link> createLocalRuntimeLink(HttpServletRequest request) {
        StringBuffer requestUrl = request.getRequestURL();
        URL runtimeUrl = ResourceHelper.createUrl(requestUrl.substring(0, requestUrl.toString().indexOf("/management/") + 12) + "runtime");
        Link link = new Link(info.getRuntimeName(), EDIT_LINK, runtimeUrl);
        return Collections.singleton(link);
    }

    private Set<Link> createDistributedRuntimesLink() {
        Set<Link> list = new HashSet<Link>();
        List<SocketAddress> addresses = addressCache.getActiveAddresses(HTTP_SERVER);
        for (SocketAddress address : addresses) {
            int httpPort = address.getPort().getNumber();
            String host = address.getAddress();
            URL runtimeUrl = ResourceHelper.createUrl("http://" + host + ":" + httpPort + "/management/runtime");
            String runtimeName = address.getRuntimeName();
            Link link = new Link(runtimeName, EDIT_LINK, runtimeUrl);
            list.add(link);
        }

        return list;
    }

}
