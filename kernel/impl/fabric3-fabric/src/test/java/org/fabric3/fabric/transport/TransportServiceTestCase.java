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
package org.fabric3.fabric.transport;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import junit.framework.TestCase;
import org.easymock.EasyMock;

import org.fabric3.spi.transport.Transport;

/**
 *
 */
public class TransportServiceTestCase extends TestCase {

    public void testSuspendResume() throws Exception {
        TransportServiceMonitor monitor = EasyMock.createNiceMock(TransportServiceMonitor.class);
        Transport transport1 = EasyMock.createMock(Transport.class);
        transport1.suspend();
        transport1.resume();
        Transport transport2 = EasyMock.createMock(Transport.class);
        EasyMock.replay(monitor, transport1, transport2);

        TransportService service = new TransportService(monitor);
        Map<String, Transport> transports = new HashMap<>();
        transports.put("transport1", transport1);
        transports.put("transport2", transport2);
        service.setTransports(transports);

        service.suspend("transport1");
        service.resume("transport1");

        EasyMock.verify(monitor, transport1, transport2);
    }

    public void testSuspendResumeAll() throws Exception {
        TransportServiceMonitor monitor = EasyMock.createNiceMock(TransportServiceMonitor.class);
        Transport transport1 = EasyMock.createMock(Transport.class);
        transport1.suspend();
        transport1.resume();
        Transport transport2 = EasyMock.createMock(Transport.class);
        transport2.suspend();
        transport2.resume();
        EasyMock.replay(monitor, transport1, transport2);

        TransportService service = new TransportService(monitor);
        Map<String, Transport> transports = new HashMap<>();
        transports.put("transport1", transport1);
        transports.put("transport2", transport2);
        service.setTransports(transports);

        service.suspendAll();
        service.resumeAll();

        EasyMock.verify(monitor, transport1, transport2);
    }

    public void testGetTransports() throws Exception {
        TransportServiceMonitor monitor = EasyMock.createNiceMock(TransportServiceMonitor.class);
        Transport transport1 = EasyMock.createMock(Transport.class);
        Transport transport2 = EasyMock.createMock(Transport.class);
        EasyMock.replay(monitor, transport1, transport2);

        TransportService service = new TransportService(monitor);
        Map<String, Transport> transports = new HashMap<>();
        transports.put("transport1", transport1);
        transports.put("transport2", transport2);
        service.setTransports(transports);

        assertEquals(2, service.getTransports().size());
        EasyMock.verify(monitor, transport1, transport2);
    }

    public void testSuspendResumeInvalidTransport() throws Exception {
        TransportServiceMonitor monitor = EasyMock.createNiceMock(TransportServiceMonitor.class);
        Transport transport1 = EasyMock.createMock(Transport.class);
        EasyMock.replay(monitor, transport1);

        TransportService service = new TransportService(monitor);
        service.setTransports(Collections.singletonMap("transport1", transport1));

        service.suspend("noTransport");
        service.resume("noTransport");

        EasyMock.verify(monitor, transport1);
    }

}
