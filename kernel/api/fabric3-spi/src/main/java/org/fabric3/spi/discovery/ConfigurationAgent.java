package org.fabric3.spi.discovery;

import java.util.function.Consumer;

/**
 * An agent that provides configuration values (e.g. for properties) from an external source such as a key/value store.
 */
public interface ConfigurationAgent {

    /**
     * Returns the value for the key or null if not found.
     *
     * @param key the key
     * @return the value or null
     */
    String getValue(String key);

    /**
     * Registers a value change listener
     *
     * @param key      the key to listen for
     * @param consumer the callback to invoke when a value changes
     */
    void registerListener(String key, Consumer<String> consumer);

    /**
     * Un-Registers a value change listener
     *
     * @param key the key to listen for
     */
    void unRegisterListener(String key);

}
