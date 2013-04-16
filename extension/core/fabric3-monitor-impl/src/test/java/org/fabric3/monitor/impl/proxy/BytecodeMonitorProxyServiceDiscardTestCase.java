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
package org.fabric3.monitor.impl.proxy;

import junit.framework.TestCase;
import org.easymock.EasyMock;
import org.fabric3.api.annotation.monitor.Debug;
import org.fabric3.api.annotation.monitor.MonitorLevel;
import org.fabric3.host.monitor.Monitorable;
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
        proxyService.init();
    }

    public interface ParamsMonitor {

        @Debug("Monitor event")
        void monitor();

    }

}
