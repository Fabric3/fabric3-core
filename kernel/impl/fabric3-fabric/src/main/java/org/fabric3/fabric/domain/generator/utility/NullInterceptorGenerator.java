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
 */
package org.fabric3.fabric.domain.generator.utility;

import org.w3c.dom.Element;

import org.fabric3.spi.domain.generator.GenerationException;
import org.fabric3.spi.domain.generator.wire.InterceptorGenerator;
import org.fabric3.spi.domain.generator.policy.PolicyMetadata;
import org.fabric3.spi.model.instance.LogicalOperation;
import org.fabric3.spi.model.physical.PhysicalInterceptorDefinition;

/**
 * Utility InterceptorGenerator that can be used to avoid generating an interceptor definition for interception-phase policy.
 */
public class NullInterceptorGenerator implements InterceptorGenerator {
    public PhysicalInterceptorDefinition generate(Element policy, PolicyMetadata metadata, LogicalOperation operation) throws GenerationException {
        return null;
    }
}
