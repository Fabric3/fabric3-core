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
package org.fabric3.fabric.container.builder.classloader;

import java.net.URI;

import org.fabric3.spi.model.physical.PhysicalClassLoaderDefinition;

/**
 * Instantiates a classloader on a runtime node.
 */
public interface ClassLoaderBuilder {

    /**
     * Creates or updates a classloader based on the classloader definition.
     *
     * @param definition the classloader definition
     * @throws ClassLoaderBuilderException if the classloader cannot be created
     */
    void build(PhysicalClassLoaderDefinition definition) throws ClassLoaderBuilderException;


    /**
     * Removes a classloader if it is not referenced by any other registered classloader.
     *
     * @param uri the classloader uri
     * @throws ClassLoaderBuilderException if the classloader cannot be released
     */
    void destroy(URI uri) throws ClassLoaderBuilderException;
}
