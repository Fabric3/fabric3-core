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
import java.util.List;
import java.util.Set;

import junit.framework.TestCase;
import org.easymock.EasyMock;
import org.fabric3.api.model.type.RuntimeMode;
import org.fabric3.api.host.runtime.HostInfo;
import org.fabric3.management.rest.model.Link;
import org.fabric3.spi.federation.addressing.AddressCache;
import org.fabric3.spi.federation.addressing.EndpointConstants;
import org.fabric3.spi.federation.addressing.SocketAddress;
import org.fabric3.spi.host.Port;

/**
 *
 */
public class RuntimesResourceServiceTestCase extends TestCase {

    private HostInfo info;
    private RuntimesResourceService service;
    private HttpServletRequest request;
    private AddressCache addressCache;
    private Port port;

    public void testDistributedGetRuntimes() throws Exception {

        EasyMock.expect(info.getRuntimeMode()).andReturn(RuntimeMode.NODE);

        EasyMock.expect(port.getNumber()).andReturn(8080);
        SocketAddress address = new SocketAddress("runtime1", "zone", "http", "localhost", port);
        List<SocketAddress> addresses = Collections.singletonList(address);

        EasyMock.expect(addressCache.getActiveAddresses(EasyMock.eq(EndpointConstants.HTTP_SERVER))).andReturn(addresses);

        EasyMock.replay(info, addressCache, request, port);

        Set<Link> links = service.getRuntimes(request);
        Link link = links.iterator().next();
        assertEquals("runtime1", link.getName());
        URL url = new URL("http://localhost:8080/management/runtime");
        assertEquals(url, link.getHref());
        EasyMock.verify(info, addressCache, request, port);
    }

    public void testLocalGetRuntimes() throws Exception {

        EasyMock.expect(info.getRuntimeMode()).andReturn(RuntimeMode.VM);
        EasyMock.expect(info.getRuntimeName()).andReturn("vm");

        EasyMock.expect(request.getRequestURL()).andReturn(new StringBuffer("http:/localhost/management/domain/contributions")).atLeastOnce();

        EasyMock.replay(info, request);

        Set<Link> links = service.getRuntimes(request);
        Link link = links.iterator().next();
        assertEquals("vm", link.getName());
        URL url = new URL("http:/localhost/management/runtime");
        assertEquals(url, link.getHref());
        EasyMock.verify(info, request);
    }

    protected void setUp() throws Exception {
        super.setUp();

        info = EasyMock.createMock(HostInfo.class);

        addressCache = EasyMock.createMock(AddressCache.class);
        service = new RuntimesResourceService(info, addressCache);

        port = EasyMock.createMock(Port.class);

        request = EasyMock.createMock(HttpServletRequest.class);
    }

}
