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

import org.fabric3.api.model.type.component.Component;
import org.fabric3.api.model.type.contract.ServiceContract;
import org.fabric3.implementation.junit.common.ContextConfiguration;
import org.fabric3.implementation.junit.model.JUnitBinding;
import org.fabric3.implementation.junit.provision.JUnitWireSource;
import org.fabric3.spi.domain.generator.WireBindingGenerator;
import org.fabric3.spi.model.instance.LogicalBinding;
import org.fabric3.spi.model.instance.LogicalOperation;
import org.fabric3.spi.model.physical.PhysicalWireTarget;
import org.oasisopen.sca.annotation.EagerInit;

/**
 * Attaches wires to Junit components to the WireHolder.
 */
@EagerInit
public class JUnitWireBindingGenerator implements WireBindingGenerator<JUnitBinding> {

    public JUnitWireSource generateSource(LogicalBinding<JUnitBinding> binding, ServiceContract contract, List<LogicalOperation> operations) {
        Component<?> definition = binding.getParent().getParent().getDefinition();
        String testName = definition.getImplementation().getImplementationName();
        ContextConfiguration configuration = binding.getDefinition().getConfiguration();
        return new JUnitWireSource(testName, configuration);
    }

    public PhysicalWireTarget generateTarget(LogicalBinding<JUnitBinding> binding, ServiceContract contract, List<LogicalOperation> operations) {
        throw new UnsupportedOperationException();
    }

}
