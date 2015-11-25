package org.fabric3.hazelcast.discovery;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;
import com.hazelcast.core.Member;
import com.hazelcast.core.MemberAttributeEvent;
import com.hazelcast.core.MembershipAdapter;
import com.hazelcast.core.MembershipEvent;
import com.hazelcast.core.MembershipListener;
import org.fabric3.api.MonitorChannel;
import org.fabric3.api.annotation.monitor.Monitor;
import org.fabric3.api.host.Fabric3Exception;
import org.fabric3.api.host.runtime.HostInfo;
import org.fabric3.hazelcast.impl.HazelcastService;
import org.fabric3.spi.discovery.AbstractEntry;
import org.fabric3.spi.discovery.ChannelEntry;
import org.fabric3.spi.discovery.DiscoveryAgent;
import org.fabric3.spi.discovery.EntryChange;
import org.fabric3.spi.discovery.ServiceEntry;
import org.oasisopen.sca.annotation.EagerInit;
import org.oasisopen.sca.annotation.Init;
import org.oasisopen.sca.annotation.Reference;

/**
 * Implements discovery and clustering based on Hazelcast.
 */
@EagerInit
public class HazelcastAgent implements DiscoveryAgent {
    private HazelcastInstance hazelcast;
    private HostInfo info;
    private MonitorChannel monitor;

    private ObjectMapper mapper;
    private volatile boolean leaderStatus;

    private Map<Consumer, String> leadershipListeners = new HashMap<>();
    private Map<String, String> serviceListeners = new HashMap<>();
    private Map<String, String> channelListeners = new HashMap<>();

    public HazelcastAgent(@Reference HazelcastService hazelcastService, @Reference HostInfo info, @Monitor MonitorChannel monitor) {
        this.info = info;
        this.monitor = monitor;

        this.hazelcast = hazelcastService.getInstance();
        mapper = new ObjectMapper();
    }

    @Init
    public void init() {
        monitor.info("Joined domain {0} as [zone: {1}, runtime: {2}]", info.getDomain(), info.getZoneName(), info.getRuntimeName());
        leaderStatus = isLeader();
        if (leaderStatus) {
            monitor.debug("Node is leader in zone {0}", info.getZoneName());
        }

        hazelcast.getCluster().addMembershipListener(new MembershipListener() {

            public void memberAdded(MembershipEvent event) {
                String zone = event.getMember().getStringAttribute("zone");
                if (zone == null) {
                    return; // ignore, not a runtime
                }
                String runtime = event.getMember().getStringAttribute("runtime");
                monitor.debug("Node joined domain {0} [zone: {1}, runtime: {2}]", info.getDomain(), zone, runtime);
            }

            public void memberRemoved(MembershipEvent event) {
                String zone = event.getMember().getStringAttribute("zone");
                if (zone == null) {
                    return; // ignore, not a runtime
                }
                String runtime = event.getMember().getStringAttribute("runtime");
                monitor.debug("Node left domain {0} [zone: {1}, runtime: {2}]", info.getDomain(), zone, runtime);
            }

            public void memberAttributeChanged(MemberAttributeEvent event) {
            }
        });
    }

    public boolean isLeader() {
        boolean leader = false;
        for (Member member : hazelcast.getCluster().getMembers()) {
            String zone = member.getStringAttribute("zone");
            if (zone == null) {
                continue;
            }
            if (info.getZoneName().equals(zone)) {
                if (member.localMember()) {
                    leader = true;
                }
                break;
            }
        }
        return leader;
    }

    public List<ServiceEntry> getServiceEntries(String name) {
        return getEntries(name, ServiceEntry.class);
    }

    public List<ChannelEntry> getChannelEntries(String name) {
        return getEntries(name, ChannelEntry.class);
    }

    public void register(ServiceEntry entry) {
        registerEntry(entry);
    }

    public void unregisterService(String name) {
        unregisterEntry(name);
    }

    public void register(ChannelEntry entry) {
        registerEntry(entry);

    }

    public void unregisterChannel(String name) {
        unregisterEntry(name);
    }

    public void registerLeadershipListener(Consumer<Boolean> consumer) {
        String id = hazelcast.getCluster().addMembershipListener(new MembershipAdapter() {
            public void memberAdded(MembershipEvent membershipEvent) {
                reportStatus();
            }

            public void memberRemoved(MembershipEvent membershipEvent) {
                reportStatus();
            }

            private void reportStatus() {
                boolean status = HazelcastAgent.this.leaderStatus;
                if (isLeader() != status) {
                    monitor.debug("Node is now leader in zone {0}", info.getZoneName());
                    leaderStatus = status;
                    consumer.accept(status);
                }
            }

        });
        leadershipListeners.put(consumer, id);
        if (isLeader()) {
            consumer.accept(true);
        }
    }

    public void unRegisterLeadershipListener(Consumer<Boolean> consumer) {
        String id = leadershipListeners.get(consumer);
        if (id == null) {
            return;
        }
        hazelcast.getCluster().removeMembershipListener(id);
    }

    public void registerServiceListener(String name, BiConsumer<EntryChange, ServiceEntry> listener) {
        IMap<String, String> map = hazelcast.getMap(name);
        String id = map.addEntryListener(new EntryListenerAdapter<>(ServiceEntry.class, listener, mapper, monitor), true);
        serviceListeners.put(name, id);
    }

    public void unregisterServiceListener(String name, BiConsumer<EntryChange, ServiceEntry> listener) {
        IMap<String, Map<String, String>> map = hazelcast.getMap(name);
        String id = serviceListeners.remove(name);
        if (id == null) {
            throw new Fabric3Exception("Listener not found: " + name);
        }
        map.removeEntryListener(id);
    }

    public void registerChannelListener(String name, BiConsumer<EntryChange, ChannelEntry> listener) {
        IMap<String, String> map = hazelcast.getMap(name);
        String id = map.addEntryListener(new EntryListenerAdapter<>(ChannelEntry.class, listener, mapper, monitor), true);
        channelListeners.put(name, id);

    }

    public void unregisterChannelListener(String name, BiConsumer<EntryChange, ChannelEntry> listener) {
        IMap<String, String> map = hazelcast.getMap(name);
        String id = channelListeners.remove(name);
        if (id == null) {
            throw new Fabric3Exception("Listener not found: " + name);
        }
        map.removeEntryListener(id);
    }

    private <T> List<T> getEntries(String name, Class<T> type) {
        IMap<String, String> map = hazelcast.getMap(name);
        Collection<String> collection = map.values();
        return collection.stream().map(s -> {
            try {
                return mapper.readValue(s, type);
            } catch (IOException e) {
                monitor.severe("Error deserializing service entry {0}", name, e);
                return null;
            }
        }).filter(out -> out != null).collect(Collectors.toList());
    }

    private void registerEntry(AbstractEntry entry) {
        try {
            IMap<String, String> entries = hazelcast.getMap(entry.getName());
            String serialized = mapper.writeValueAsString(entry);
            entries.put(getRuntimeKey(), serialized);
        } catch (JsonProcessingException e) {
            monitor.severe("Error serializing entry: {0}", entry.getName(), e);
        }
    }

    private void unregisterEntry(String name) {
        IMap<String, String> entries = hazelcast.getMap(name);
        if (entries == null) {
            return;
        }
        entries.remove(name);
    }

    private String getRuntimeKey() {
        return info.getDomain().toString() + ":" + info.getZoneName() + ":" + info.getRuntimeName();
    }

}
