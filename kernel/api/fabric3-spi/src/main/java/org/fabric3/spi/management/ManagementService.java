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
 * Portions originally based on Apache Tuscany 2007
 * licensed under the Apache 2.0 license.
 */
package org.fabric3.spi.management;

import java.net.URI;

import org.fabric3.api.model.type.java.ManagementInfo;
import org.fabric3.spi.container.objectfactory.ObjectFactory;

/**
 * Exposes a component to the underlying runtime management framework.
 */
public interface ManagementService {

    /**
     * Exposes a component for management.
     *
     * @param componentUri  the component URI
     * @param info          the management metadata
     * @param objectFactory the object factory responsible for returning the managed component instance
     * @param classLoader   the classloader
     * @throws ManagementException if an error exposing the component is encountered
     */
    void export(URI componentUri, ManagementInfo info, ObjectFactory<?> objectFactory, ClassLoader classLoader) throws ManagementException;

    /**
     * Exposes an instance for management as a system resource.
     *
     * @param name        the management name
     * @param group       the management group
     * @param description the instance description
     * @param instance    the instance
     * @throws ManagementException if an error exposing the instance is encountered
     */
    void export(String name, String group, String description, Object instance) throws ManagementException;

    /**
     * Removes a component from the underlying management framework.
     *
     * @param componentUri the component URI
     * @param info         the management metadata
     * @throws ManagementException if an error removing the component is encountered
     */
    void remove(URI componentUri, ManagementInfo info) throws ManagementException;

    /**
     * Removes an instance from the underlying management framework.
     *
     * @param name  the management name
     * @param group the management group
     * @throws ManagementException if an error removing the component is encountered
     */
    public void remove(String name, String group) throws ManagementException;

}