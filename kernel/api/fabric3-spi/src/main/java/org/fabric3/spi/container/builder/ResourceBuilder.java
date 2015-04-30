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
package org.fabric3.spi.container.builder;

import org.fabric3.api.host.Fabric3Exception;
import org.fabric3.spi.model.physical.PhysicalResource;

/**
 * Builds a resource on a runtime.
 */
public interface ResourceBuilder<R extends PhysicalResource> {

    /**
     * Builds a resource from its physical resource definition.
     *
     * @param definition the physical resource definition
     * @throws Fabric3Exception If unable to build the resource
     */
    void build(R definition) throws Fabric3Exception;

    /**
     * Removes a resource on a runtime.
     *
     * @param definition the physical resource definition
     * @throws Fabric3Exception If unable to remove the resource
     */
    void remove(R definition) throws Fabric3Exception;

}