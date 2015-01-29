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
package org.fabric3.security.authorization;

import javax.xml.namespace.QName;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.fabric3.api.model.type.component.ComponentType;
import org.fabric3.spi.domain.generator.GenerationException;
import org.fabric3.spi.domain.generator.wire.InterceptorGenerator;
import org.fabric3.spi.model.instance.LogicalComponent;
import org.fabric3.spi.model.instance.LogicalOperation;
import org.fabric3.spi.model.physical.PhysicalInterceptorDefinition;
import org.oasisopen.sca.annotation.EagerInit;

/**
 * Generates interceptors that perform role-based authorization checks for a service invocation.
 */
@EagerInit
public class AuthorizationInterceptorGenerator implements InterceptorGenerator {
    private static final QName AUTHORIZATION = new QName("urn:fabric3.org", "authorization");

    public Optional<PhysicalInterceptorDefinition> generate(LogicalOperation source, LogicalOperation target) throws GenerationException {
        List<String> operationPolicies = target.getDefinition().getPolicies();
        ComponentType componentType = target.getParent().getParent().getDefinition().getComponentType();
        List<String> targetPolicies = componentType.getPolicies();
        if ((operationPolicies.isEmpty() || !operationPolicies.contains("authorization")) && (targetPolicies.isEmpty() || !operationPolicies.contains(
                "authorization"))) {
            return Optional.empty();
        }

        String[] roles = target.getDefinition().getMetadata(AUTHORIZATION, String[].class);
        if (roles == null) {
            roles = target.getDefinition().getMetadata(AUTHORIZATION, String[].class);
            if (roles == null) {
                LogicalComponent component = source.getParent().getParent();
                throw new GenerationException("No roles specified for authorization annotation on component: " + component.getUri());
            }
        }
        return Optional.of(new AuthorizationInterceptorDefinition(Arrays.asList(roles)));
    }

}
