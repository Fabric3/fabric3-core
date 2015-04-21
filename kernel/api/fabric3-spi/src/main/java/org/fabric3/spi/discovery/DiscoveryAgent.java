package org.fabric3.spi.discovery;

import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * An agent that interacts with a discovery service.
 */
public interface DiscoveryAgent {

    /**
     * Returns true if the runtime is leader for the current zone.
     *
     * @return true if the runtime is the zone leader
     */
    boolean isLeader();

    /**
     * Returns service entries for a service name.
     *
     * @param name the service name
     * @return service entries
     */
    List<ServiceEntry> getServiceEntries(String name);

    /**
     * Returns channel entries for a channel name.
     *
     * @param name the channel name
     * @return service entries
     */
    List<ChannelEntry> getChannelEntries(String name);

    /**
     * Registers a service.
     *
     * @param entry the entry
     */
    void register(ServiceEntry entry);

    /**
     * Removes a service entry.
     *
     * @param name the service name
     */
    void unregisterService(String name);

    /**
     * Registers a channel.
     *
     * @param entry the entry
     */
    void register(ChannelEntry entry);

    /**
     * Removes a channel entry.
     *
     * @param name the channel name
     */
    void unregisterChannel(String name);

    /**
     * Registers to receive callbacks when the runtime leadership status changes.
     *
     * @param consumer the callback that will receive 'true' if the runtime is leader; otherwise false
     */
    void registerLeadershipListener(Consumer<Boolean> consumer);

    /**
     * Unregisters the leader election listener.
     *
     * @param consumer the listener to unregister.
     */
    void unRegisterLeadershipListener(Consumer<Boolean> consumer);

    /**
     * Registers to receive callbacks when service addresses change.
     *
     * @param name     the service name name
     * @param listener the callback
     */
    void registerServiceListener(String name, BiConsumer<EntryChange, ServiceEntry> listener);

    /**
     * De-registers a listener.
     *
     * @param name     the service address to de-register
     * @param listener the listener
     */
    void unregisterServiceListener(String name, BiConsumer<EntryChange, ServiceEntry> listener);

    /**
     * Registers to receive callbacks when channel addresses change.
     *
     * @param name     the channel name
     * @param listener the callback
     */
    void registerChannelListener(String name, BiConsumer<EntryChange, ChannelEntry> listener);

    /**
     * De-registers a listener.
     *
     * @param name     the channel address to de-register
     * @param listener the listener
     */
    void unregisterChannelListener(String name, BiConsumer<EntryChange, ChannelEntry> listener);

}
