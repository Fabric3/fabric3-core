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
package org.fabric3.management.rest.framework.domain.runtime;

import javax.servlet.http.HttpServletRequest;
import java.net.URL;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.fabric3.api.annotation.management.Management;
import org.fabric3.api.annotation.management.ManagementOperation;
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
        Set<Link> list = new HashSet<>();
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
