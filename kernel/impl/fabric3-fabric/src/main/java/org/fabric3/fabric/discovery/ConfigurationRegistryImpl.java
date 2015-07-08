package org.fabric3.fabric.discovery;

import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

import org.fabric3.spi.discovery.ConfigurationAgent;
import org.fabric3.spi.discovery.ConfigurationRegistry;
import org.oasisopen.sca.annotation.Reference;

/**
 * Default implementation. Searches any installed agents and as a fallback attempts to resolve configuration values from system and JVM properties
 * respectively.
 */
public class ConfigurationRegistryImpl implements ConfigurationRegistry {
    @Reference(required = false)
    private List<ConfigurationAgent> agents = Collections.emptyList();

    public String getValue(String key) {
        String value = null;
        for (ConfigurationAgent agent : agents) {
            value = agent.getValue(key);
            if (value != null) {
                break;
            }
        }
        if (value == null) {
            value = System.getenv(key);
            if (value == null) {
                value = System.getProperty(key);
            }
        }
        return value;
    }

    public void registerListener(String key, Consumer<String> consumer) {
        agents.forEach(a -> a.registerListener(key, consumer));
    }

    public void unRegisterListener(String key) {
        agents.forEach(a -> a.unRegisterListener(key));
    }
}
