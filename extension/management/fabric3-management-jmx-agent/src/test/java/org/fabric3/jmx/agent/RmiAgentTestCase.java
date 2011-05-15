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

import javax.management.MBeanServer;

import junit.framework.TestCase;
import org.easymock.classextension.EasyMock;

import org.fabric3.spi.host.PortAllocator;

/**
 * @version $Revision: 9250 $ $Date: 2010-07-30 12:52:01 +0200 (Fri, 30 Jul 2010) $
 */
public class RmiAgentTestCase extends TestCase {

    public void testConfiguredPort() throws Exception {
        MBeanServer mBeanServer = EasyMock.createNiceMock(MBeanServer.class);
        DelegatingJmxAuthenticator authenticator = EasyMock.createNiceMock(DelegatingJmxAuthenticator.class);
        RmiAgentMonitor monitor = EasyMock.createNiceMock(RmiAgentMonitor.class);

        PortAllocator portAllocator = EasyMock.createMock(PortAllocator.class);
        portAllocator.reserve("JMX", "JMX", 8182);
        portAllocator.release("JMX");
        EasyMock.expectLastCall();

        EasyMock.replay(mBeanServer, monitor, portAllocator);

        RmiAgent agent = new RmiAgent(mBeanServer, authenticator, portAllocator, monitor);
        agent.setJmxPort("8182");
        agent.init();
        agent.destroy();
        EasyMock.verify(portAllocator);
    }

    public void testPortRange() throws Exception {
        MBeanServer mBeanServer = EasyMock.createNiceMock(MBeanServer.class);
        DelegatingJmxAuthenticator authenticator = EasyMock.createNiceMock(DelegatingJmxAuthenticator.class);
        RmiAgentMonitor monitor = EasyMock.createNiceMock(RmiAgentMonitor.class);

        PortAllocator portAllocator = EasyMock.createMock(PortAllocator.class);
        EasyMock.expect(portAllocator.isPoolEnabled()).andReturn(true);
        EasyMock.expect(portAllocator.allocate("JMX", "JMX")).andReturn(8182);
        portAllocator.release("JMX");
        EasyMock.expectLastCall();

        EasyMock.replay(mBeanServer, monitor, portAllocator);

        RmiAgent agent = new RmiAgent(mBeanServer, authenticator, portAllocator, monitor);
        agent.init();
        agent.destroy();
        EasyMock.verify(portAllocator);
    }

}
