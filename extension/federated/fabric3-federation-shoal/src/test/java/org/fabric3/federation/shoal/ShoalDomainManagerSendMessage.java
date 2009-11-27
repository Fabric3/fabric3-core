/*
* Fabric3
* Copyright (c) 2009 Metaform Systems
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
package org.fabric3.federation.shoal;

/**
 * @version $Rev$ $Date$
 */
public class ShoalDomainManagerSendMessage {
//    private ShoalDomainManager domainManager;
//
//    public static void main(String[] args) throws Exception {
//        ShoalDomainManagerSendMessage client = new ShoalDomainManagerSendMessage();
//        client.init();
//        while (true) {
//            System.out.println("Press <Enter> to send messages, 'x' to exit...");
//            int key = System.in.read();
//            if (key == 88 || key == 120) {
//                System.exit(0);
//            }
//            client.sendMessages();
//        }
//    }
//
//    public void sendMessages() throws Exception {
//        for (Zone zone : domainManager.getZones()) {
//            String zoneName = zone.getName();
//            System.out.println("Sending message to zone: " + zoneName);
//            Command command = new MockCommand();
//            ByteArrayOutputStream bas = new ByteArrayOutputStream();
//            MultiClassLoaderObjectOutputStream stream = new MultiClassLoaderObjectOutputStream(bas);
//            stream.writeObject(command);
//            domainManager.sendMessage(zoneName, bas.toByteArray());
//        }
//    }
//
//    @SuppressWarnings({"unchecked"})
//    protected void init() throws Exception {
//        EventService eventService = EasyMock.createNiceMock(EventService.class);
//        HostInfo info = EasyMock.createMock(HostInfo.class);
//        EasyMock.expect(info.getDomain()).andReturn(URI.create("fabric3://domain"));
//        EasyMock.replay(info);
//        FederationServiceMonitor monitor = new MockMonitor();
//        FederationServiceImpl federationService = new FederationServiceImpl(eventService, info, monitor);
//        federationService.setController(true);
//        federationService.setRuntimeName("Controller");
//        federationService.init();
//
//        DomainManagerMonitor domainMonitor = EasyMock.createNiceMock(DomainManagerMonitor.class);
//        EasyMock.replay(domainMonitor);
//
//        domainManager = new ShoalDomainManager(federationService, null, null, domainMonitor);
//        domainManager.init();
//
//        federationService.onJoinDomain();
//        Thread.sleep(4000);
//    }
//
//    private static class MockCommand implements Command {
//        private static final long serialVersionUID = -6379269046748362803L;
//
//        public int getOrder() {
//            return 0;
//        }
//
//        public int compareTo(Command o) {
//            return 0;
//        }
//    }
}
