/*
 * Fabric3
 * Copyright (c) 2009-2015 Metaform Systems
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Portions originally based on Apache Tuscany 2007
 * licensed under the Apache 2.0 license.
 */
package org.fabric3.implementation.pojo.manager;

import java.lang.reflect.Type;
import java.util.function.Supplier;

import org.fabric3.api.model.type.java.Injectable;
import org.fabric3.spi.container.injection.InjectionAttributes;

/**
 * Creates {@link ImplementationManager}s.
 */
public interface ImplementationManagerFactory {

    /**
     * Creates an instance manager that can be used to create component instances.
     *
     * @return a new instance factory
     */
    ImplementationManager createManager();

    /**
     * Return the implementation class.
     *
     * @return the implementation class
     */
    Class<?> getImplementationClass();

    /**
     * Signals the start of a component configuration update.
     */
    void startUpdate();

    /**
     * Signals when a component configuration update is complete.
     */
    void endUpdate();

    /**
     * Returns a previously added Supplier for the injectable site.
     *
     * @param attribute the injection site
     * @return the Supplier or null
     */
    Supplier<?> getObjectSupplier(Injectable attribute);

    /**
     * Sets a Supplier for an injectable.
     *
     * @param injectable the injection site name
     * @param supplier   the Supplier
     */
    void setSupplier(Injectable injectable, Supplier<?> supplier);

    /**
     * Sets a Supplier that is associated with a key for an injectable.
     *
     * @param injectable the injection site
     * @param supplier   the Supplier
     * @param attributes the injection attributes
     */
    void setSupplier(Injectable injectable, Supplier<?> supplier, InjectionAttributes attributes);

    /**
     * Removes a Supplier for an injection site.
     *
     * @param injectable the injection site name
     */
    void removeSupplier(Injectable injectable);

    /**
     * Returns the type for the injection site
     *
     * @param injectable the injection site
     * @return the required type
     */
    Class<?> getMemberType(Injectable injectable);

    /**
     * Returns the generic type for the injection site
     *
     * @param injectable the injection site
     * @return the required type
     */
    Type getGenericType(Injectable injectable);

}
