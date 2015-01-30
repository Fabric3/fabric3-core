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
import org.fabric3.api.annotation.monitor.MonitorLevel;
import org.fabric3.api.annotation.monitor.Severe;
import org.fabric3.api.host.monitor.Monitorable;
import org.fabric3.monitor.impl.router.RingBufferDestinationRouter;
import org.fabric3.monitor.spi.buffer.ResizableByteBufferMonitor;
import org.fabric3.monitor.spi.event.MonitorEventEntry;
import org.fabric3.monitor.spi.event.ParameterEntry;

/**
 *
 */
public class BytecodeMonitorProxyServiceTestCase extends TestCase {
    private BytecodeMonitorProxyService proxyService;

    private RingBufferDestinationRouter router;
    private Monitorable monitorable;
    private MonitorEventEntry entry;

    // Used to verify bytecode
    //    public void testVerify() throws Exception {
    //        ClassReader cr = new ClassReader(proxyService.generateClass(ParamsMonitor.class, 0));
    //        CheckClassAdapter.verify(cr, true, new PrintWriter(System.out));
    //    }

    public void testInvokeTwoStrings() throws Exception {
        EasyMock.replay(router, monitorable);

        ParamsMonitor monitor = proxyService.createMonitor(ParamsMonitor.class, monitorable, "destination");
        monitor.monitor("foo", "bar");

        assertNotNull(entry.getTemplate());
        assertNotNull(entry.getEntryTimestamp());
        assertNotNull(entry.getTimestampNanos());
        assertEquals(MonitorLevel.SEVERE, entry.getLevel());
        assertEquals(2, entry.getLimit());
        assertEquals(ParameterEntry.Slot.OBJECT, entry.getEntries()[0].getSlot());
        assertEquals("foo", entry.getEntries()[0].getObjectValue(String.class));
        assertEquals(ParameterEntry.Slot.OBJECT, entry.getEntries()[1].getSlot());
        assertEquals("bar", entry.getEntries()[1].getObjectValue(String.class));

        EasyMock.verify(router, monitorable);
    }

    public void testInvokeString() throws Exception {
        EasyMock.replay(router, monitorable);

        ParamsMonitor monitor = proxyService.createMonitor(ParamsMonitor.class, monitorable, "destination");
        monitor.monitor("foo");

        assertNotNull(entry.getTemplate());
        assertNotNull(entry.getEntryTimestamp());
        assertNotNull(entry.getTimestampNanos());
        assertEquals(MonitorLevel.SEVERE, entry.getLevel());
        assertEquals(1, entry.getLimit());
        assertEquals(ParameterEntry.Slot.OBJECT, entry.getEntries()[0].getSlot());
        assertEquals("foo", entry.getEntries()[0].getObjectValue(String.class));

        EasyMock.verify(router, monitorable);
    }

    public void testInvokeInt() throws Exception {
        EasyMock.replay(router, monitorable);

        ParamsMonitor monitor = proxyService.createMonitor(ParamsMonitor.class, monitorable, "destination");
        monitor.monitor(1);

        assertNotNull(entry.getTemplate());
        assertNotNull(entry.getEntryTimestamp());
        assertNotNull(entry.getTimestampNanos());
        assertEquals(MonitorLevel.SEVERE, entry.getLevel());
        assertEquals(1, entry.getLimit());
        assertEquals(ParameterEntry.Slot.INT, entry.getEntries()[0].getSlot());
        assertEquals(1, entry.getEntries()[0].getIntValue());

        EasyMock.verify(router, monitorable);
    }

    public void testInvokeNoParams() throws Exception {
        EasyMock.replay(router, monitorable);

        ParamsMonitor monitor = proxyService.createMonitor(ParamsMonitor.class, monitorable, "destination");
        monitor.monitor();

        assertNotNull(entry.getTemplate());
        assertNotNull(entry.getEntryTimestamp());
        assertNotNull(entry.getTimestampNanos());
        assertEquals(MonitorLevel.SEVERE, entry.getLevel());
        assertEquals(0, entry.getLimit());

        EasyMock.verify(router, monitorable);
    }

    public void testInvokeObject() throws Exception {
        EasyMock.replay(router, monitorable);

        ParamsMonitor monitor = proxyService.createMonitor(ParamsMonitor.class, monitorable, "destination");
        Foo foo = new Foo();
        monitor.monitor(foo);

        assertNotNull(entry.getTemplate());
        assertNotNull(entry.getEntryTimestamp());
        assertNotNull(entry.getTimestampNanos());
        assertEquals(MonitorLevel.SEVERE, entry.getLevel());
        assertEquals(1, entry.getLimit());
        assertEquals(ParameterEntry.Slot.OBJECT, entry.getEntries()[0].getSlot());
        assertEquals(foo, entry.getEntries()[0].getObjectValue(Foo.class));

        EasyMock.verify(router, monitorable);
    }

    public void testInvokeLong() throws Exception {
        EasyMock.replay(router, monitorable);

        ParamsMonitor monitor = proxyService.createMonitor(ParamsMonitor.class, monitorable, "destination");
        monitor.monitor(Long.MAX_VALUE);

        assertNotNull(entry.getTemplate());
        assertNotNull(entry.getEntryTimestamp());
        assertNotNull(entry.getTimestampNanos());
        assertEquals(MonitorLevel.SEVERE, entry.getLevel());
        assertEquals(1, entry.getLimit());
        assertEquals(ParameterEntry.Slot.LONG, entry.getEntries()[0].getSlot());
        assertEquals(Long.MAX_VALUE, entry.getEntries()[0].getLongValue());

        EasyMock.verify(router, monitorable);
    }

    public void testInvokeBoolean() throws Exception {
        EasyMock.replay(router, monitorable);

        ParamsMonitor monitor = proxyService.createMonitor(ParamsMonitor.class, monitorable, "destination");
        monitor.monitor(true);

        assertNotNull(entry.getTemplate());
        assertNotNull(entry.getEntryTimestamp());
        assertNotNull(entry.getTimestampNanos());
        assertEquals(MonitorLevel.SEVERE, entry.getLevel());
        assertEquals(1, entry.getLimit());
        assertEquals(ParameterEntry.Slot.BOOLEAN, entry.getEntries()[0].getSlot());
        assertTrue(entry.getEntries()[0].getBooleanValue());

        EasyMock.verify(router, monitorable);
    }

    public void testInvokeFloat() throws Exception {
        EasyMock.replay(router, monitorable);

        ParamsMonitor monitor = proxyService.createMonitor(ParamsMonitor.class, monitorable, "destination");
        monitor.monitor(1.1f);

        assertNotNull(entry.getTemplate());
        assertNotNull(entry.getEntryTimestamp());
        assertNotNull(entry.getTimestampNanos());
        assertEquals(MonitorLevel.SEVERE, entry.getLevel());
        assertEquals(1, entry.getLimit());
        assertEquals(ParameterEntry.Slot.FLOAT, entry.getEntries()[0].getSlot());
        assertEquals(1.1f, entry.getEntries()[0].getFloatValue());

        EasyMock.verify(router, monitorable);
    }

    public void testInvokeDouble() throws Exception {
        EasyMock.replay(router, monitorable);

        ParamsMonitor monitor = proxyService.createMonitor(ParamsMonitor.class, monitorable, "destination");
        monitor.monitor(1.1d);

        assertNotNull(entry.getTemplate());
        assertNotNull(entry.getEntryTimestamp());
        assertNotNull(entry.getTimestampNanos());
        assertEquals(MonitorLevel.SEVERE, entry.getLevel());
        assertEquals(1, entry.getLimit());
        assertEquals(ParameterEntry.Slot.DOUBLE, entry.getEntries()[0].getSlot());
        assertEquals(1.1d, entry.getEntries()[0].getDoubleValue());

        EasyMock.verify(router, monitorable);
    }

    public void testInvokeShort() throws Exception {
        EasyMock.replay(router, monitorable);

        ParamsMonitor monitor = proxyService.createMonitor(ParamsMonitor.class, monitorable, "destination");
        monitor.monitor((short) 1);

        assertNotNull(entry.getTemplate());
        assertNotNull(entry.getEntryTimestamp());
        assertNotNull(entry.getTimestampNanos());
        assertEquals(MonitorLevel.SEVERE, entry.getLevel());
        assertEquals(1, entry.getLimit());
        assertEquals(ParameterEntry.Slot.SHORT, entry.getEntries()[0].getSlot());
        assertEquals((short) 1, entry.getEntries()[0].getShortValue());

        EasyMock.verify(router, monitorable);
    }

    public void testInvokeByte() throws Exception {
        EasyMock.replay(router, monitorable);

        ParamsMonitor monitor = proxyService.createMonitor(ParamsMonitor.class, monitorable, "destination");
        monitor.monitor((byte) 'x');

        assertNotNull(entry.getTemplate());
        assertNotNull(entry.getEntryTimestamp());
        assertNotNull(entry.getTimestampNanos());
        assertEquals(MonitorLevel.SEVERE, entry.getLevel());
        assertEquals(1, entry.getLimit());
        assertEquals(ParameterEntry.Slot.BYTE, entry.getEntries()[0].getSlot());
        assertEquals((byte) 'x', entry.getEntries()[0].getByteValue());

        EasyMock.verify(router, monitorable);
    }

    public void testInvokeChar() throws Exception {
        EasyMock.replay(router, monitorable);

        ParamsMonitor monitor = proxyService.createMonitor(ParamsMonitor.class, monitorable, "destination");
        monitor.monitor('x');

        assertNotNull(entry.getTemplate());
        assertNotNull(entry.getEntryTimestamp());
        assertNotNull(entry.getTimestampNanos());
        assertEquals(MonitorLevel.SEVERE, entry.getLevel());
        assertEquals(1, entry.getLimit());
        assertEquals(ParameterEntry.Slot.CHAR, entry.getEntries()[0].getSlot());
        assertEquals('x', entry.getEntries()[0].getCharValue());

        EasyMock.verify(router, monitorable);
    }

    protected void setUp() throws Exception {
        super.setUp();
        entry = new MonitorEventEntry(2000, EasyMock.createNiceMock(ResizableByteBufferMonitor.class));

        router = EasyMock.createMock(RingBufferDestinationRouter.class);
        EasyMock.expect(router.getDestinationIndex(EasyMock.isA(String.class))).andReturn(1);
        EasyMock.expect(router.get()).andReturn(entry);
        router.publish(entry);

        monitorable = EasyMock.createMock(Monitorable.class);
        monitorable.getName();
        EasyMock.expectLastCall().andReturn("test").atLeastOnce();
        EasyMock.expect(monitorable.getLevel()).andReturn(MonitorLevel.SEVERE);

        proxyService = new BytecodeMonitorProxyService(router, monitorable);
        proxyService.setEnabled(true);
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
        void monitor(long arg1);

        @Severe("Monitor event {0}")
        void monitor(boolean arg1);

        @Severe("Monitor event {0}")
        void monitor(float arg1);

        @Severe("Monitor event {0}")
        void monitor(double arg1);

        @Severe("Monitor event {0}")
        void monitor(short arg1);

        @Severe("Monitor event {0}")
        void monitor(byte arg1);

        @Severe("Monitor event {0}")
        void monitor(char arg1);

    }

    private class Foo {

        public String toString() {
            return "The Foo Object";
        }
    }

}
