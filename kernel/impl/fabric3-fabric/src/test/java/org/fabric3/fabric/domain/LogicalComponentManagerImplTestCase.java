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
 * Portions originally based on Apache Tuscany 2007
 * licensed under the Apache 2.0 license.
 */
package org.fabric3.fabric.domain;

import java.net.URI;

import junit.framework.TestCase;
import org.easymock.EasyMock;
import org.fabric3.api.host.Names;
import org.fabric3.spi.model.instance.LogicalComponent;
import org.fabric3.spi.model.instance.LogicalCompositeComponent;

/**
 *
 */
public class LogicalComponentManagerImplTestCase extends TestCase {

    public void testInitAutowireOn() throws Exception {
        LCMMonitor monitor = EasyMock.createMock(LCMMonitor.class);
        EasyMock.replay(monitor);
        LogicalComponentManagerImpl lcm = new LogicalComponentManagerImpl();
        lcm.setMonitor(monitor);
        lcm.setAutowire("on");
        lcm.init();
        EasyMock.verify(monitor);
    }

    public void testInitAutowireOff() throws Exception {
        LCMMonitor monitor = EasyMock.createMock(LCMMonitor.class);
        EasyMock.replay(monitor);
        LogicalComponentManagerImpl lcm = new LogicalComponentManagerImpl();
        lcm.setMonitor(monitor);
        lcm.setAutowire("off");
        lcm.init();
        EasyMock.verify(monitor);
    }

    public void testInitAutowireInvalid() throws Exception {
        LCMMonitor monitor = EasyMock.createMock(LCMMonitor.class);
        monitor.invalidAutowireValue("invalid");
        EasyMock.replay(monitor);
        LogicalComponentManagerImpl lcm = new LogicalComponentManagerImpl();
        lcm.setMonitor(monitor);
        lcm.setAutowire("invalid");
        lcm.init();
        EasyMock.verify(monitor);
    }

    public void testGetComponent() throws Exception {
        LCMMonitor monitor = EasyMock.createMock(LCMMonitor.class);
        EasyMock.replay(monitor);
        LogicalComponentManagerImpl lcm = new LogicalComponentManagerImpl();

        lcm.setMonitor(monitor);
        lcm.init();

        LogicalCompositeComponent domain = lcm.getDomainComposite();
        URI uri = URI.create(Names.RUNTIME_NAME + "/component");
        LogicalComponent component = new LogicalComponent(uri, null, domain);
        domain.addComponent(component);

        assertEquals(component, lcm.getComponent(uri));
        URI invalidUri = URI.create(Names.RUNTIME_NAME + "/component2");
        assertNull(lcm.getComponent(invalidUri));
        EasyMock.verify(monitor);
    }
}
