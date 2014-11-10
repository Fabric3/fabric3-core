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
package org.fabric3.monitor.impl.proxy;

import junit.framework.TestCase;
import org.easymock.EasyMock;
import org.fabric3.api.annotation.monitor.Debug;
import org.fabric3.api.annotation.monitor.MonitorLevel;
import org.fabric3.api.host.monitor.Monitorable;
import org.fabric3.monitor.impl.router.RingBufferDestinationRouter;

/**
 *
 */
public class BytecodeMonitorProxyServiceDiscardTestCase extends TestCase {
    private BytecodeMonitorProxyService proxyService;

    private RingBufferDestinationRouter router;
    private Monitorable monitorable;

    public void testFilterLogLevel() throws Exception {
        EasyMock.replay(router, monitorable);

        ParamsMonitor monitor = proxyService.createMonitor(ParamsMonitor.class, monitorable, "destination");

        // monitor event should be discarded an not passed to the router
        monitor.monitor();

        EasyMock.verify(router, monitorable);
    }

    protected void setUp() throws Exception {
        super.setUp();

        router = EasyMock.createMock(RingBufferDestinationRouter.class);
        EasyMock.expect(router.getDestinationIndex(EasyMock.isA(String.class))).andReturn(1);

        monitorable = EasyMock.createMock(Monitorable.class);
        monitorable.getName();
        EasyMock.expectLastCall().andReturn("test").atLeastOnce();
        EasyMock.expect(monitorable.getLevel()).andReturn(MonitorLevel.SEVERE);

        proxyService = new BytecodeMonitorProxyService(router, monitorable);
        proxyService.setEnabled(true);
    }

    public interface ParamsMonitor {

        @Debug("Monitor event")
        void monitor();

    }

}
