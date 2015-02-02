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

import org.fabric3.api.host.Fabric3Exception;
import org.fabric3.spi.container.builder.component.TargetWireAttacher;
import org.fabric3.spi.container.wire.Interceptor;
import org.fabric3.spi.container.wire.InvocationChain;
import org.fabric3.spi.container.wire.Wire;
import org.fabric3.spi.model.physical.PhysicalOperationDefinition;
import org.fabric3.spi.model.physical.PhysicalWireSourceDefinition;
import org.oasisopen.sca.annotation.EagerInit;
import org.oasisopen.sca.annotation.Reference;

/**
 *
 */
@EagerInit
public class TestBindingTargetWireAttacher implements TargetWireAttacher<TestBindingWireTargetDefinition> {
    private final BindingChannel channel;

    public TestBindingTargetWireAttacher(@Reference BindingChannel channel) {
        this.channel = channel;
    }

    public void attach(PhysicalWireSourceDefinition source, TestBindingWireTargetDefinition target, Wire wire) throws Fabric3Exception {
        for (InvocationChain chain : wire.getInvocationChains()) {
            URI destination = target.getUri();
            PhysicalOperationDefinition operation = chain.getPhysicalOperation();
            String name = operation.getName();
            Interceptor interceptor = new TestBindingInterceptor(channel, destination, name);
            chain.addInterceptor(interceptor);
        }
    }

    public void detach(PhysicalWireSourceDefinition source, TestBindingWireTargetDefinition target) throws Fabric3Exception {
    }

}