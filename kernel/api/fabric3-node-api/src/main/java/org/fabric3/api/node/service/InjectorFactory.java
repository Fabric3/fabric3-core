package org.fabric3.api.node.service;

import java.lang.reflect.AccessibleObject;
import java.util.Map;
import java.util.function.Supplier;

/**
 * Creates suppliers that provide injectable proxies for references, producers, and consumers.
 */
public interface InjectorFactory {

    /**
     * Introspects the class and creates suppliers for references, producers, and consumers.
     *
     * @param clazz the class to introspect
     * @return the mapping from field or method to supplier; constructors are not supported
     */
    Map<AccessibleObject, Supplier<Object>> getInjectors(Class<?> clazz);

}
