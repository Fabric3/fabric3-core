package org.fabric3.spi.discovery;

import java.util.List;
import java.util.Map;
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
     * Registers a service or channel.
     *
     * @param entry the entry
     */
    void register(AbstractEntry entry);

    /**
     * Registers to receive callbacks when the runtime leadership status changes.
     *
     * @param consumer the callback that will receive 'true' if the runtime is leader; otherwise false
     */
    void registerLeadershipListener(Consumer<Boolean> consumer);

    /**
     * Registers to receive callbacks when services with a particular binding change.
     *
     * @param binding  the binding type
     * @param consumer the callback
     */
    void registerDiscoveryListener(String binding, Consumer<Map<String, Object>> consumer);

}
