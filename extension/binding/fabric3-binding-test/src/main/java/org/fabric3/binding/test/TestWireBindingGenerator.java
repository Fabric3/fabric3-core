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
package org.fabric3.binding.test;

import java.net.URI;
import java.util.List;

import org.fabric3.spi.domain.generator.wire.WireBindingGenerator;
import org.oasisopen.sca.annotation.EagerInit;

import org.fabric3.api.model.type.contract.ServiceContract;
import org.fabric3.spi.domain.generator.policy.EffectivePolicy;
import org.fabric3.spi.domain.generator.GenerationException;
import org.fabric3.spi.model.instance.LogicalBinding;
import org.fabric3.spi.model.instance.LogicalOperation;
import org.fabric3.spi.model.physical.PhysicalWireTargetDefinition;

/**
 * Implementation of the test binding generator.
 */
@EagerInit
public class TestWireBindingGenerator implements WireBindingGenerator<TestBindingDefinition> {

    public TestBindingWireSourceDefinition generateSource(LogicalBinding<TestBindingDefinition> logicalBinding,
                                                      ServiceContract contract,
                                                      List<LogicalOperation> operations,
                                                      EffectivePolicy policy) throws GenerationException {
        TestBindingWireSourceDefinition definition = new TestBindingWireSourceDefinition();
        definition.setUri(logicalBinding.getDefinition().getTargetUri());
        return definition;
    }

    public TestBindingWireTargetDefinition generateTarget(LogicalBinding<TestBindingDefinition> logicalBinding,
                                                      ServiceContract contract,
                                                      List<LogicalOperation> operations,
                                                      EffectivePolicy policy) throws GenerationException {

        TestBindingWireTargetDefinition definition = new TestBindingWireTargetDefinition();
        definition.setUri(logicalBinding.getDefinition().getTargetUri());
        return definition;
    }

    public PhysicalWireTargetDefinition generateServiceBindingTarget(LogicalBinding<TestBindingDefinition> serviceBinding,
                                                                 ServiceContract contract,
                                                                 List<LogicalOperation> operations,
                                                                 EffectivePolicy policy) throws GenerationException {

        TestBindingWireTargetDefinition definition = new TestBindingWireTargetDefinition();

        URI path = serviceBinding.getDefinition().getTargetUri();
        definition.setUri(path);
        return definition;
    }


}
