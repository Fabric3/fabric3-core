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

import org.fabric3.api.annotation.wire.Key;
import org.fabric3.api.model.type.contract.ServiceContract;
import org.fabric3.spi.domain.generator.wire.WireBindingGenerator;
import org.fabric3.spi.model.instance.LogicalBinding;
import org.fabric3.spi.model.instance.LogicalOperation;
import org.fabric3.spi.model.physical.PhysicalWireTarget;
import org.oasisopen.sca.annotation.EagerInit;

/**
 * Implementation of the test binding generator.
 */
@EagerInit
@Key("org.fabric3.binding.test.TestBinding")
public class TestWireBindingGenerator implements WireBindingGenerator<TestBinding> {

    public TestBindingWireSource generateSource(LogicalBinding<TestBinding> logicalBinding, ServiceContract contract, List<LogicalOperation> operations) {
        TestBindingWireSource source = new TestBindingWireSource();
        source.setUri(logicalBinding.getDefinition().getTargetUri());
        return source;
    }

    public TestBindingWireTarget generateTarget(LogicalBinding<TestBinding> binding, ServiceContract contract, List<LogicalOperation> operations) {
        TestBindingWireTarget target = new TestBindingWireTarget();
        target.setUri(binding.getDefinition().getTargetUri());
        return target;
    }

    public PhysicalWireTarget generateServiceBindingTarget(LogicalBinding<TestBinding> binding, ServiceContract contract, List<LogicalOperation> operations) {
        TestBindingWireTarget target = new TestBindingWireTarget();
        URI path = binding.getDefinition().getTargetUri();
        target.setUri(path);
        return target;
    }

}
