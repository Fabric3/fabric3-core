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
package org.fabric3.implementation.junit.generator;

import java.util.List;

import org.fabric3.spi.domain.generator.wire.WireBindingGenerator;
import org.oasisopen.sca.annotation.EagerInit;

import org.fabric3.implementation.junit.common.ContextConfiguration;
import org.fabric3.implementation.junit.model.JUnitBindingDefinition;
import org.fabric3.implementation.junit.provision.JUnitWireSourceDefinition;
import org.fabric3.api.model.type.component.ComponentDefinition;
import org.fabric3.api.model.type.contract.ServiceContract;
import org.fabric3.spi.domain.generator.policy.EffectivePolicy;
import org.fabric3.spi.domain.generator.GenerationException;
import org.fabric3.spi.model.instance.LogicalBinding;
import org.fabric3.spi.model.instance.LogicalOperation;
import org.fabric3.spi.model.physical.PhysicalWireTargetDefinition;

/**
 * Attaches wires to Junit components to the WireHolder.
 */
@EagerInit
public class JUnitWireBindingGenerator implements WireBindingGenerator<JUnitBindingDefinition> {

    public JUnitWireSourceDefinition generateSource(LogicalBinding<JUnitBindingDefinition> bindingDefinition,
                                                ServiceContract contract,
                                                List<LogicalOperation> operations,
                                                EffectivePolicy policy) throws GenerationException {
        ComponentDefinition<?> definition = bindingDefinition.getParent().getParent().getDefinition();
        String testName = definition.getImplementation().getArtifactName();
        ContextConfiguration configuration = bindingDefinition.getDefinition().getConfiguration();
        return new JUnitWireSourceDefinition(testName, configuration);
    }

    public PhysicalWireTargetDefinition generateTarget(LogicalBinding<JUnitBindingDefinition> bindingDefinition,
                                                   ServiceContract contract,
                                                   List<LogicalOperation> operations,
                                                   EffectivePolicy policy) throws GenerationException {
        throw new UnsupportedOperationException();
    }

    public PhysicalWireTargetDefinition generateServiceBindingTarget(LogicalBinding<JUnitBindingDefinition> serviceBinding,
                                                                 ServiceContract contract,
                                                                 List<LogicalOperation> operations,
                                                                 EffectivePolicy policy) throws GenerationException {
        throw new UnsupportedOperationException();
    }
}
