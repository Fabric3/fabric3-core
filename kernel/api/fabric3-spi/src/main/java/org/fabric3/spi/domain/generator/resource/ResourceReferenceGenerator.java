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
package org.fabric3.spi.domain.generator.resource;

import org.fabric3.api.model.type.component.ResourceReference;
import org.fabric3.spi.domain.generator.GenerationException;
import org.fabric3.spi.model.instance.LogicalResourceReference;
import org.fabric3.spi.model.physical.PhysicalWireTargetDefinition;

/**
 * Generates metadata to attach a component resource reference to a resource.
 */
public interface ResourceReferenceGenerator<R extends ResourceReference> {

    /**
     * Generate the physical definition for the logical resource reference.
     *
     * @param logicalResourceReference the resource being wired to
     * @return Source wire definition.
     * @throws GenerationException if there was a problem generating the wire
     */
    PhysicalWireTargetDefinition generateWireTarget(LogicalResourceReference<R> logicalResourceReference) throws GenerationException;

}
