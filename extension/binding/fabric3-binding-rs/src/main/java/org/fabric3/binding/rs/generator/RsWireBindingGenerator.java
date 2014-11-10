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
package org.fabric3.binding.rs.generator;

import java.net.URI;
import java.util.List;
import java.util.Set;
import javax.xml.namespace.QName;

import org.fabric3.binding.rs.provision.RsWireSourceDefinition;
import org.fabric3.binding.rs.provision.RsWireTargetDefinition;
import org.fabric3.spi.domain.generator.wire.WireBindingGenerator;
import org.oasisopen.sca.Constants;
import org.oasisopen.sca.annotation.EagerInit;

import org.fabric3.api.binding.rs.model.RsBindingDefinition;
import org.fabric3.binding.rs.provision.AuthenticationType;
import org.fabric3.api.model.type.contract.ServiceContract;
import org.fabric3.spi.domain.generator.policy.EffectivePolicy;
import org.fabric3.spi.domain.generator.GenerationException;
import org.fabric3.spi.model.instance.LogicalBinding;
import org.fabric3.spi.model.instance.LogicalOperation;
import org.fabric3.spi.model.physical.PhysicalWireTargetDefinition;

/**
 * Implementation of the REST binding generator.
 */
@EagerInit
public class RsWireBindingGenerator implements WireBindingGenerator<RsBindingDefinition> {
    private static final QName F3_AUTHORIZATION = new QName(org.fabric3.api.Namespaces.F3, "authorization");
    private static final QName SCA_AUTHORIZATION = new QName(Constants.SCA_NS, "authorization");
    private static final QName SCA_AUTHENTICATION = new QName(Constants.SCA_NS, "clientAuthentication");
    private static final QName F3_BASIC_AUTHENTICATION = new QName(org.fabric3.api.Namespaces.F3, "clientAuthentication");
    private static final QName F3_DIGEST_AUTHENTICATION = null;

    public RsWireSourceDefinition generateSource(LogicalBinding<RsBindingDefinition> binding,
                                             ServiceContract contract,
                                             List<LogicalOperation> operations,
                                             EffectivePolicy policy) throws GenerationException {
        String interfaze = contract.getQualifiedInterfaceName();
        URI uri = binding.getDefinition().getTargetUri();

        AuthenticationType type = calculateAuthenticationType(binding, operations);
        return new RsWireSourceDefinition(interfaze, uri, type);
    }

    public RsWireTargetDefinition generateTarget(LogicalBinding<RsBindingDefinition> binding,
                                             ServiceContract contract,
                                             List<LogicalOperation> operations,
                                             EffectivePolicy policy) throws GenerationException {
        return new RsWireTargetDefinition(binding.getDefinition().getTargetUri(), contract.getQualifiedInterfaceName());
    }

    public PhysicalWireTargetDefinition generateServiceBindingTarget(LogicalBinding<RsBindingDefinition> binding,
                                                                 ServiceContract contract,
                                                                 List<LogicalOperation> operations,
                                                                 EffectivePolicy policy) throws GenerationException {
        return generateTarget(binding, contract, operations, policy);
    }

    private AuthenticationType calculateAuthenticationType(LogicalBinding<RsBindingDefinition> binding, List<LogicalOperation> operations) {
        if (binding.getIntents().contains(SCA_AUTHENTICATION)) {
            return AuthenticationType.BASIC;
        } else if (binding.getIntents().contains(F3_BASIC_AUTHENTICATION)) {
            return AuthenticationType.BASIC;
        } else if (binding.getIntents().contains(F3_DIGEST_AUTHENTICATION)) {
            return AuthenticationType.BASIC;
        }
        Set<QName> intents = binding.getParent().getIntents();
        if (intents.contains(F3_AUTHORIZATION) || intents.contains(SCA_AUTHORIZATION)) {
            return AuthenticationType.BASIC;
        }
        intents = binding.getParent().getParent().getDefinition().getImplementation().getIntents();
        if (intents.contains(F3_AUTHORIZATION) || intents.contains(SCA_AUTHORIZATION)) {
            return AuthenticationType.BASIC;
        }
        for (LogicalOperation operation : operations) {
            intents = operation.getIntents();
            if (intents.contains(F3_AUTHORIZATION) || intents.contains(SCA_AUTHORIZATION)) {
                // default to basic authentication
                return AuthenticationType.BASIC;
            }
        }

        return AuthenticationType.NONE;
    }

}
