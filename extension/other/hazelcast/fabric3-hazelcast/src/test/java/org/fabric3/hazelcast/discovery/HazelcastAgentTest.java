package org.fabric3.hazelcast.discovery;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;

import org.easymock.EasyMock;
import org.fabric3.api.MonitorChannel;
import org.fabric3.api.host.runtime.HostInfo;
import org.fabric3.hazelcast.impl.HazelcastServiceImpl;
import org.fabric3.spi.discovery.ChannelEntry;
import org.fabric3.spi.discovery.EntryChange;
import org.fabric3.spi.discovery.ServiceEntry;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

/**
 *
 */

@Ignore
public class HazelcastAgentTest {
    private static HazelcastServiceImpl SERVICE;
    private static MonitorChannel MONITOR;

    @Test
    public void testIsLeader() throws Exception {
        HostInfo info = EasyMock.createNiceMock(HostInfo.class);
        EasyMock.replay(info);

        HazelcastAgent agent = new HazelcastAgent(SERVICE, info, MONITOR);
        List<Boolean> listenerFired = new ArrayList<>();
        agent.registerLeadershipListener(listenerFired::add);

        Assert.assertTrue(agent.isLeader());
        Assert.assertFalse(listenerFired.isEmpty());
    }

    @Test
    public void testServiceEntries() throws Exception {
        HostInfo info = EasyMock.createNiceMock(HostInfo.class);
        EasyMock.expect(info.getDomain()).andReturn(URI.create("domain")).anyTimes();
        EasyMock.expect(info.getZoneName()).andReturn("zone").anyTimes();
        EasyMock.expect(info.getRuntimeName()).andReturn("runtime1");
        EasyMock.expect(info.getRuntimeName()).andReturn("runtime2");
        EasyMock.expect(info.getRuntimeName()).andReturn("runtime2");
        EasyMock.replay(info);

        HazelcastAgent agent = new HazelcastAgent(SERVICE, info, MONITOR);

        // register from runtime 1
        List<Boolean> listenerFired = new ArrayList<>();
        BiConsumer<EntryChange, ServiceEntry> listener = (change, entry) -> listenerFired.add(true);
        agent.registerServiceListener("test", listener);

        agent.register(new ServiceEntry("test", "123", 80, "http"));

        List<ServiceEntry> entries = agent.getServiceEntries("test");
        Assert.assertEquals(1, entries.size());
        Assert.assertEquals(1, listenerFired.size());

        // register from runtime 2
        agent.register(new ServiceEntry("test", "1234", 80, "http"));

        Thread.sleep(200);
        Assert.assertEquals(2, listenerFired.size());
        entries = agent.getServiceEntries("test");
        Assert.assertEquals(2, entries.size());

        agent.unregisterServiceListener("test", listener);
        agent.register(new ServiceEntry("test2", "1234", 80, "http"));

        // listener was removed so it should not have fired
        Assert.assertEquals(2, listenerFired.size());

    }

    @Test
    public void testChannelEntries() throws Exception {
        HostInfo info = EasyMock.createNiceMock(HostInfo.class);
        EasyMock.expect(info.getDomain()).andReturn(URI.create("domain")).anyTimes();
        EasyMock.expect(info.getZoneName()).andReturn("zone").anyTimes();
        EasyMock.expect(info.getRuntimeName()).andReturn("runtime1");
        EasyMock.expect(info.getRuntimeName()).andReturn("runtime2");
        EasyMock.expect(info.getRuntimeName()).andReturn("runtime2");
        EasyMock.replay(info);

        HazelcastAgent agent = new HazelcastAgent(SERVICE, info, MONITOR);

        // register from runtime 1
        List<Boolean> listenerFired = new ArrayList<>();
        BiConsumer<EntryChange, ChannelEntry> listener = (change, entry) -> listenerFired.add(true);
        agent.registerChannelListener("testChannel", listener);

        agent.register(new ChannelEntry("testChannel", "123", 80, "http"));

        List<ChannelEntry> entries = agent.getChannelEntries("testChannel");
        Assert.assertEquals(1, entries.size());
        Assert.assertEquals(1, listenerFired.size());

        // register from runtime 2
        agent.register(new ChannelEntry("testChannel", "1234", 80, "http"));

        Thread.sleep(200);
        Assert.assertEquals(2, listenerFired.size());
        entries = agent.getChannelEntries("testChannel");
        Assert.assertEquals(2, entries.size());

        agent.unregisterChannelListener("testChannel", listener);
        agent.register(new ChannelEntry("test2", "1234", 80, "http"));

        // listener was removed so it should not have fired
        Assert.assertEquals(2, listenerFired.size());

    }

    @BeforeClass
    public static void setupHazelcast() throws Exception {
        MONITOR = EasyMock.createNiceMock(MonitorChannel.class);

        HostInfo info = EasyMock.createNiceMock(HostInfo.class);
        EasyMock.expect(info.getDomain()).andReturn(URI.create("domain")).anyTimes();
        EasyMock.expect(info.getZoneName()).andReturn("zone").anyTimes();
        EasyMock.expect(info.getRuntimeName()).andReturn("runtime1");

        EasyMock.replay(info, MONITOR);

        SERVICE = new HazelcastServiceImpl(info, MONITOR);
        SERVICE.init();

    }
}