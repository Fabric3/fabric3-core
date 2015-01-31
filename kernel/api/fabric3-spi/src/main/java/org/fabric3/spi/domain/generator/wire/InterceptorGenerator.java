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
package org.fabric3.spi.domain.generator.wire;

import java.util.Optional;

import org.fabric3.api.host.ContainerException;
import org.fabric3.spi.model.instance.LogicalOperation;
import org.fabric3.spi.model.physical.PhysicalInterceptorDefinition;

/**
 * Generates {@link PhysicalInterceptorDefinition}s used to attach policy interceptors to a wire.
 */
public interface InterceptorGenerator {

    /**
     * Generates an interceptor definition from the policy set extension. Implementations may return null if an interceptor should not be added to a wire.
     *
     * @param source the source operation
     * @param target the target operation
     * @return the definition
     * @throws ContainerException if an exception occurs during generation
     */
    Optional<PhysicalInterceptorDefinition> generate(LogicalOperation source, LogicalOperation target) throws ContainerException;

}
