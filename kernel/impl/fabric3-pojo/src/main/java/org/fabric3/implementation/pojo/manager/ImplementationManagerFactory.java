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

import org.fabric3.api.model.type.java.Injectable;
import org.fabric3.spi.container.objectfactory.InjectionAttributes;
import org.fabric3.spi.container.objectfactory.ObjectFactory;

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
     * Returns a previously added object factory for the injectable site.
     *
     * @param attribute the injection site
     * @return the object factory or null
     */
    ObjectFactory<?> getObjectFactory(Injectable attribute);

    /**
     * Sets an object factory for an injectable.
     *
     * @param injectable    the injection site name
     * @param objectFactory the object factory
     */
    void setObjectFactory(Injectable injectable, ObjectFactory<?> objectFactory);

    /**
     * Sets an object factory that is associated with a key for an injectable.
     *
     * @param injectable    the injection site
     * @param objectFactory the object factory
     * @param attributes    the injection attributes
     */
    void setObjectFactory(Injectable injectable, ObjectFactory<?> objectFactory, InjectionAttributes attributes);

    /**
     * Removes an object factory for an injection site.
     *
     * @param injectable the injection site name
     */
    void removeObjectFactory(Injectable injectable);

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
