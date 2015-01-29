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
package org.fabric3.tx;

import java.util.List;
import java.util.Optional;

import org.fabric3.api.model.type.component.ComponentType;
import org.fabric3.spi.domain.generator.wire.InterceptorGenerator;
import org.fabric3.spi.model.instance.LogicalOperation;
import org.fabric3.spi.model.physical.PhysicalInterceptorDefinition;
import org.oasisopen.sca.annotation.EagerInit;

/**
 * Generates metadata for creating a TxInterceptor on a wire invocation chain.
 */
@EagerInit
public class TxInterceptorGenerator implements InterceptorGenerator {
    private static final Optional<PhysicalInterceptorDefinition> PHYSICAL_INTERCEPTOR_DEFINITION = Optional.of(new TxInterceptorDefinition(TxAction.BEGIN));

    public Optional<PhysicalInterceptorDefinition> generate(LogicalOperation source, LogicalOperation target) {
        ComponentType componentType = target.getParent().getParent().getDefinition().getImplementation().getComponentType();
        List<String> policies = componentType.getPolicies();
        if (!policies.isEmpty() && containsPolicy(policies)) {
            return PHYSICAL_INTERCEPTOR_DEFINITION;
        }
        return Optional.empty();
    }

    private boolean containsPolicy(List<String> policies) {
        return policies.contains("managedTransaction") || policies.contains("managedTransaction.local") || policies.contains("managedTransaction.global");
    }
}
