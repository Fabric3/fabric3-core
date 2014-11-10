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

import org.fabric3.spi.container.ContainerException;
import org.oasisopen.sca.annotation.Reference;

import org.fabric3.spi.container.builder.component.SourceWireAttacher;
import org.fabric3.spi.model.physical.PhysicalWireTargetDefinition;
import org.fabric3.spi.container.objectfactory.ObjectFactory;
import org.fabric3.spi.container.wire.Wire;

/**
 *
 */
public class TestBindingSourceWireAttacher implements SourceWireAttacher<TestBindingWireSourceDefinition> {
    private final BindingChannel channel;

    public TestBindingSourceWireAttacher(@Reference BindingChannel channel) {
        this.channel = channel;
    }

    public void attach(TestBindingWireSourceDefinition source, PhysicalWireTargetDefinition target, Wire wire) throws ContainerException {
        // register the wire to the bound service so it can be invoked through the channel from a bound reference
        URI callbackUri = target.getCallbackUri();
        channel.registerDestinationWire(source.getUri(), wire, callbackUri);
    }

    public void detach(TestBindingWireSourceDefinition source, PhysicalWireTargetDefinition target) throws ContainerException {
    }

    public void detachObjectFactory(TestBindingWireSourceDefinition source, PhysicalWireTargetDefinition target) throws ContainerException {
    }

    public void attachObjectFactory(TestBindingWireSourceDefinition source, ObjectFactory<?> objectFactory, PhysicalWireTargetDefinition definition)
            throws ContainerException {
        throw new AssertionError();
    }
}
