/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.fabric3.jmx.agent;

import java.rmi.server.ExportException;
import javax.management.MBeanServer;

import junit.framework.TestCase;
import org.easymock.EasyMock;

import org.fabric3.spi.host.Port;
import org.fabric3.spi.host.PortAllocator;

/**
 *
 */
public class RmiAgentTestCase extends TestCase {
    private static final int PORT = 1050;

    public void testConfiguredPort() throws Exception {
        try {
            MBeanServer mBeanServer = EasyMock.createNiceMock(MBeanServer.class);
            DelegatingJmxAuthenticator authenticator = EasyMock.createNiceMock(DelegatingJmxAuthenticator.class);
            RmiAgentMonitor monitor = EasyMock.createNiceMock(RmiAgentMonitor.class);

            Port port = EasyMock.createMock(Port.class);
            EasyMock.expect(port.getNumber()).andReturn(PORT).anyTimes();
            port.bind(Port.TYPE.TCP);

            PortAllocator portAllocator = EasyMock.createMock(PortAllocator.class);
            EasyMock.expect(portAllocator.reserve("JMX", "JMX", PORT)).andReturn(port);
            portAllocator.release("JMX");
            EasyMock.expectLastCall();

            EasyMock.replay(mBeanServer, monitor, portAllocator, port);

            RmiAgent agent = new RmiAgent(mBeanServer, authenticator, portAllocator, monitor);
            agent.setJmxPort(String.valueOf(PORT));
            agent.init();
            agent.destroy();
            EasyMock.verify(portAllocator, port);
        } catch (ManagementException e) {
            if (e.getCause() instanceof ExportException) {
                // ignore
                System.out.println("*******Skipping RMI Agent test as port is bound");
            } else {
                throw e;
            }

        }
    }

    public void testPortRange() throws Exception {
        try {
            MBeanServer mBeanServer = EasyMock.createNiceMock(MBeanServer.class);
            DelegatingJmxAuthenticator authenticator = EasyMock.createNiceMock(DelegatingJmxAuthenticator.class);
            RmiAgentMonitor monitor = EasyMock.createNiceMock(RmiAgentMonitor.class);

            Port port = EasyMock.createMock(Port.class);
            EasyMock.expect(port.getNumber()).andReturn(PORT).anyTimes();
            port.bind(Port.TYPE.TCP);

            PortAllocator portAllocator = EasyMock.createMock(PortAllocator.class);
            EasyMock.expect(portAllocator.isPoolEnabled()).andReturn(true);
            EasyMock.expect(portAllocator.allocate("JMX", "JMX")).andReturn(port);
            portAllocator.release("JMX");
            EasyMock.expectLastCall();

            EasyMock.replay(mBeanServer, monitor, portAllocator, port);

            RmiAgent agent = new RmiAgent(mBeanServer, authenticator, portAllocator, monitor);
            agent.init();
            agent.destroy();
            EasyMock.verify(portAllocator, port);
        } catch (ManagementException e) {
            if (e.getCause() instanceof ExportException) {
                // ignore
                System.out.println("*******Skipping RMI Agent test as port is bound");
            } else {
                throw e;
            }

        }
    }

}
