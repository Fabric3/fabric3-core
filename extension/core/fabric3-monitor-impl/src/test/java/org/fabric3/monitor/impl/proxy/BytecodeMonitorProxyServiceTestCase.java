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
import org.fabric3.api.annotation.monitor.MonitorLevel;
import org.fabric3.api.annotation.monitor.Severe;
import org.fabric3.host.monitor.Monitorable;
import org.fabric3.host.runtime.HostInfo;
import org.fabric3.monitor.impl.router.MonitorEventEntry;
import org.fabric3.monitor.impl.router.RingBufferDestinationRouter;

/**
 *
 */
public class BytecodeMonitorProxyServiceTestCase extends TestCase {
    private BytecodeMonitorProxyService proxyService;

    private RingBufferDestinationRouter router;
    private Monitorable monitorable;
    private MonitorEventEntry entry;

    public void testInvokeTwoStrings() throws Exception {
        EasyMock.replay(router, monitorable);

        ParamsMonitor monitor = proxyService.createMonitor(ParamsMonitor.class, monitorable, "destination");
        monitor.monitor("foo", "bar");

        assertTrue(getStringContents().contains("] Monitor event foo, bar"));
        EasyMock.verify(router, monitorable);
    }

    public void testInvokeString() throws Exception {
        EasyMock.replay(router, monitorable);

        ParamsMonitor monitor = proxyService.createMonitor(ParamsMonitor.class, monitorable, "destination");
        monitor.monitor("foo");

        assertTrue(getStringContents().contains("] Monitor event foo"));
        EasyMock.verify(router, monitorable);
    }

    public void testInvokeInt() throws Exception {
        EasyMock.replay(router, monitorable);

        ParamsMonitor monitor = proxyService.createMonitor(ParamsMonitor.class, monitorable, "destination");
        monitor.monitor(1);

        assertTrue(getStringContents().contains("] Monitor event 1"));
        EasyMock.verify(router, monitorable);
    }

    public void testInvokeNoParams() throws Exception {
        EasyMock.replay(router, monitorable);

        ParamsMonitor monitor = proxyService.createMonitor(ParamsMonitor.class, monitorable, "destination");
        monitor.monitor();

        assertTrue(getStringContents().contains("] Monitor event"));
        EasyMock.verify(router, monitorable);
    }

    public void testInvokeObject() throws Exception {
        EasyMock.replay(router, monitorable);

        ParamsMonitor monitor = proxyService.createMonitor(ParamsMonitor.class, monitorable, "destination");
        Foo foo = new Foo();
        monitor.monitor(foo);

        assertTrue(getStringContents().contains("] Monitor event The Foo Object"));
        EasyMock.verify(router, monitorable);
    }

    protected void setUp() throws Exception {
        super.setUp();
        entry = new MonitorEventEntry(2000);

        router = EasyMock.createMock(RingBufferDestinationRouter.class);
        EasyMock.expect(router.getDestinationIndex(EasyMock.isA(String.class))).andReturn(1);
        EasyMock.expect(router.get()).andReturn(entry);
        router.publish(entry);

        monitorable = EasyMock.createMock(Monitorable.class);
        monitorable.getName();
        EasyMock.expectLastCall().andReturn("test").atLeastOnce();
        EasyMock.expect(monitorable.getLevel()).andReturn(MonitorLevel.SEVERE);

        HostInfo hostInfo = EasyMock.createMock(HostInfo.class);
        hostInfo.getRuntimeName();
        EasyMock.expectLastCall().andReturn("runtime");
        EasyMock.replay(hostInfo);

        proxyService = new BytecodeMonitorProxyService(router, monitorable, hostInfo);
        proxyService.setEnabled(true);
        proxyService.init();
    }

    public void testInvokeLong() throws Exception {
        EasyMock.replay(router, monitorable);

        ParamsMonitor monitor = proxyService.createMonitor(ParamsMonitor.class, monitorable, "destination");
        monitor.monitor(Long.MAX_VALUE);

        assertTrue(getStringContents().contains("] Monitor event " + Long.MAX_VALUE));
        EasyMock.verify(router, monitorable);
    }

    public void testInvokeBoolean() throws Exception {
        EasyMock.replay(router, monitorable);

        ParamsMonitor monitor = proxyService.createMonitor(ParamsMonitor.class, monitorable, "destination");
        monitor.monitor(true);

        assertTrue(getStringContents().contains("] Monitor event true"));
        EasyMock.verify(router, monitorable);
    }

    private String getStringContents() {
        entry.getBuffer().flip();
        byte[] bytes = new byte[entry.getBuffer().limit()];
        entry.getBuffer().get(bytes);
        return new String(bytes);
    }

    public interface ParamsMonitor {

        @Severe("Monitor event")
        void monitor();

        @Severe("Monitor event {0}, {1}")
        void monitor(String arg1, String arg2);

        @Severe("Monitor event {0}")
        void monitor(String arg1);

        @Severe("Monitor event {0}")
        void monitor(int arg1);

        @Severe("Monitor event {0}")
        void monitor(Foo arg1);

        @Severe("Monitor event {0}")
        void monitor(Long arg1);

        @Severe("Monitor event {0}")
        void monitor(boolean arg1);

    }

    // Used to verify bytecode
    //    public void testVerify() throws Exception {
    //        ClassReader cr = new ClassReader(proxyService.createClass(ParamsMonitor.class, "foo"));
    //        CheckClassAdapter.verify(cr, true, new PrintWriter(System.out));
    //
    //    }

    private class Foo {

        public String toString() {
            return "The Foo Object";
        }
    }

}
