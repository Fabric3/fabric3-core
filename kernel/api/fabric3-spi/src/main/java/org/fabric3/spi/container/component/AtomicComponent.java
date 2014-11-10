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
package org.fabric3.spi.container.component;

import org.fabric3.spi.container.objectfactory.ObjectFactory;

/**
 * The runtime instantiation of an SCA atomic, or leaf-type, component.
 */
public interface AtomicComponent extends Component {

    /**
     * Create an ObjectFactory that returns an instance of this AtomicComponent.
     *
     * @return an ObjectFactory that returns an instance of this AtomicComponent
     */
    ObjectFactory<Object> createObjectFactory();

    /**
     * Returns a component instance for the current context. After an instance is used, it must be returned by calling {@link #releaseInstance(Object)}.
     *
     * @return a component instance
     * @throws InstanceLifecycleException if there is an error returning an instance
     */
    Object getInstance() throws InstanceLifecycleException;

    /**
     * Signals that an implementation has been released from use.
     *
     * @param instance the instance
     * @throws InstanceDestructionException if there is an error releasing the component
     */
    void releaseInstance(Object instance) throws InstanceDestructionException;
}
