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
package org.fabric3.fabric.domain.instantiator;

import java.util.List;

import org.fabric3.api.model.type.component.Composite;
import org.fabric3.spi.model.instance.LogicalCompositeComponent;

/**
 * Instantiates logical components in a domain that can be used to generate deployment commands.
 */
public interface LogicalModelInstantiator {

    /**
     * Instantiates a composite using SCA inclusion semantics in a domain.
     *
     * @param composite the composite to be included.
     * @param domain    the target composite in which the composite is to be included.
     * @return the instantiation context that results from this include operation
     */
    InstantiationContext include(Composite composite, LogicalCompositeComponent domain);

    /**
     * Instantiates a collection of composites using SCA inclusion semantics in a domain.
     *
     * @param composites the composites to be included.
     * @param domain     the target composite in which the composite is to be included.
     * @return the instantiation context that results from this include operation
     */
    InstantiationContext include(List<Composite> composites, LogicalCompositeComponent domain);

}
