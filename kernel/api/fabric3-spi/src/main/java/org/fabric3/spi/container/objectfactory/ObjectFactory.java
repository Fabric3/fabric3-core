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

import org.fabric3.spi.container.ContainerException;

/**
 * Creates new instances of a type.
 */
public interface ObjectFactory<T> {
    /**
     * Return a instance of the type that this factory creates.
     *
     * @return a instance from this factory
     * @throws ContainerException if there was a problem creating the instance
     */
    T getInstance() throws ContainerException;
}
