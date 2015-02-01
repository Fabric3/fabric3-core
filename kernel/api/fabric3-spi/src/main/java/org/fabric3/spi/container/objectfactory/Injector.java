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
package org.fabric3.spi.container.objectfactory;

import org.fabric3.api.host.Fabric3Exception;

/**
 * Implementations inject an ObjectFactory value on an object instance
 */
public interface Injector<T> {

    /**
     * Inject a value on the given instance.
     *
     * @param instance the instance to inject on.
     * @throws Fabric3Exception if an error is raised during injection
     */
    void inject(T instance) throws Fabric3Exception;

    /**
     * Adds or updates the injector with an ObjectFactory used to inject the pre-configured value.
     *
     * @param objectFactory the ObjectFactory
     * @param attributes    the injection attributes
     */
    void setObjectFactory(ObjectFactory<?> objectFactory, InjectionAttributes attributes);

    /**
     * Clears the currently set ObjectFactory. Used when a multiplicity reference is reinjected.
     */
    void clearObjectFactory();

}
