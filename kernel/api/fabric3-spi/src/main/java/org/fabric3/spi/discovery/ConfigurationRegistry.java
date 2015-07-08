package org.fabric3.spi.discovery;

import java.util.function.Consumer;

/**
 * Delegates to {@link ConfigurationAgent}s to provide external configuration values.
 */
public interface ConfigurationRegistry {

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
     * @param listener the callback to invoke when a value changes
     */
    void registerListener(String key, Consumer<String> listener);

    /**
     * Un-Registers a value change listener
     *
     * @param key the key to listen for
     * @param listener the listener
     */
    void unregisterListener(String key, Consumer<String> listener);

}
