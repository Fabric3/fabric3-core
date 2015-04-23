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
package org.fabric3.spi.domain.generator;

import org.fabric3.api.host.Fabric3Exception;
import org.fabric3.api.model.type.component.Resource;
import org.fabric3.spi.model.instance.LogicalResource;
import org.fabric3.spi.model.physical.PhysicalResource;

/**
 * Generates {@link PhysicalResource}s from a resource definition declared in a composite.
 */
public interface ResourceGenerator<R extends Resource> {

    /**
     * Generate the physical definition for a logical resource.
     *
     * @param resource the logical resource
     * @return the physical resource definition.
     * @throws Fabric3Exception if there was a problem generating the wire
     */
    PhysicalResource generateResource(LogicalResource<R> resource) throws Fabric3Exception;

}