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

import org.fabric3.api.binding.rs.model.RsBinding;
import org.fabric3.api.model.type.contract.ServiceContract;
import org.fabric3.binding.rs.provision.RsWireSourceDefinition;
import org.fabric3.binding.rs.provision.RsWireTargetDefinition;
import org.fabric3.spi.domain.generator.wire.WireBindingGenerator;
import org.fabric3.spi.model.instance.LogicalBinding;
import org.fabric3.spi.model.instance.LogicalOperation;
import org.fabric3.spi.model.physical.PhysicalWireTargetDefinition;
import org.oasisopen.sca.annotation.EagerInit;

/**
 * Implementation of the REST binding generator.
 */
@EagerInit
public class RsWireBindingGenerator implements WireBindingGenerator<RsBinding> {
    public RsWireSourceDefinition generateSource(LogicalBinding<RsBinding> binding, ServiceContract contract, List<LogicalOperation> operations) {
        String interfaze = contract.getQualifiedInterfaceName();
        URI uri = binding.getDefinition().getTargetUri();

        return new RsWireSourceDefinition(interfaze, uri);
    }

    public RsWireTargetDefinition generateTarget(LogicalBinding<RsBinding> binding, ServiceContract contract, List<LogicalOperation> operations) {
        return new RsWireTargetDefinition(binding.getDefinition().getTargetUri(), contract.getQualifiedInterfaceName());
    }

    public PhysicalWireTargetDefinition generateServiceBindingTarget(LogicalBinding<RsBinding> binding,
                                                                     ServiceContract contract,
                                                                     List<LogicalOperation> operations) {
        return generateTarget(binding, contract, operations);
    }

}
