package org.fabric3.discovery.local;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import org.fabric3.spi.discovery.ChannelEntry;
import org.fabric3.spi.discovery.DiscoveryAgent;
import org.fabric3.spi.discovery.EntryChange;
import org.fabric3.spi.discovery.ServiceEntry;

/**
 * In-memory agent for testing.
 *
 * NB: This implementation supports only one instance (entry) for a service or channel.
 */
public class LocalDiscoveryAgent implements DiscoveryAgent {
    private Map<String, List<ServiceEntry>> serviceEntries = new HashMap<>();
    private Map<String, List<ChannelEntry>> channelEntries = new HashMap<>();

    private List<BiConsumer<EntryChange, ServiceEntry>> serviceListeners = new ArrayList<>();
    private List<BiConsumer<EntryChange, ChannelEntry>> channelListeners = new ArrayList<>();

    public boolean isLeader() {
        return true;
    }

    public List<ServiceEntry> getServiceEntries(String name) {
        return serviceEntries.getOrDefault(name, Collections.emptyList());
    }

    public List<ChannelEntry> getChannelEntries(String name) {
        return channelEntries.getOrDefault(name, Collections.emptyList());
    }

    public void register(ServiceEntry entry) {
        serviceEntries.computeIfAbsent(entry.getName(), n -> Collections.singletonList(entry));
        serviceListeners.forEach(l -> l.accept(EntryChange.SET, entry));
    }

    public void unregisterService(String name) {
        List<ServiceEntry> list = serviceEntries.remove(name);
        if (list != null) {
            serviceListeners.forEach(l -> l.accept(EntryChange.SET, list.get(0)));
        }
    }

    public void register(ChannelEntry entry) {
        channelEntries.computeIfAbsent(entry.getName(), n -> Collections.singletonList(entry));
        channelListeners.forEach(l -> l.accept(EntryChange.SET, entry));
    }

    public void unregisterChannel(String name) {
        List<ChannelEntry> list = channelEntries.remove(name);
        if (list != null) {
            channelListeners.forEach(l -> l.accept(EntryChange.SET, list.get(0)));
        }
    }

    public void registerLeadershipListener(Consumer<Boolean> consumer) {
        // no-op as leadership never changes
    }

    public void unRegisterLeadershipListener(Consumer<Boolean> consumer) {
        // no-op as leadership never changes
    }

    public void registerServiceListener(String name, BiConsumer<EntryChange, ServiceEntry> listener) {
        serviceListeners.add(listener);
    }

    public void unregisterServiceListener(String name, BiConsumer<EntryChange, ServiceEntry> listener) {
        serviceListeners.remove(listener);
    }

    public void registerChannelListener(String name, BiConsumer<EntryChange, ChannelEntry> listener) {
        channelListeners.add(listener);
    }

    public void unregisterChannelListener(String name, BiConsumer<EntryChange, ChannelEntry> listener) {
        channelListeners.remove(listener);
    }
}
