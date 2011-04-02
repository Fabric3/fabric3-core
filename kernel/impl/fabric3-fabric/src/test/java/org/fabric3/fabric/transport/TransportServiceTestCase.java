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
package org.fabric3.fabric.transport;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import junit.framework.TestCase;
import org.easymock.EasyMock;

import org.fabric3.spi.transport.Transport;

/**
 * @version $Rev$ $Date$
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
        Map<String, Transport> transports = new HashMap<String, Transport>();
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
        Map<String, Transport> transports = new HashMap<String, Transport>();
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
        Map<String, Transport> transports = new HashMap<String, Transport>();
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
