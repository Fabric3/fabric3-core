package org.fabric3.resource.runtime;

import java.util.function.Supplier;

/**
 * Manages application resources.
 */
public interface ApplicationResourceRegistry {

    /**
     * Returns the resource factory for the given name or null.
     *
     * @param name the name
     * @return the factory or null
     */
    Supplier<?> getResourceFactory(String name);
}
