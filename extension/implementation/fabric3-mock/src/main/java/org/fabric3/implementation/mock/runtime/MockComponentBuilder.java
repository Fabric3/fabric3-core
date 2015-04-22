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
package org.fabric3.implementation.mock.runtime;

import java.util.LinkedList;
import java.util.List;
import java.util.function.Supplier;

import org.easymock.IMocksControl;
import org.fabric3.implementation.mock.model.MockPhysicalComponent;
import org.fabric3.spi.container.builder.ComponentBuilder;
import org.oasisopen.sca.annotation.EagerInit;
import org.oasisopen.sca.annotation.Reference;

/**
 *
 */
@EagerInit
public class MockComponentBuilder implements ComponentBuilder<MockPhysicalComponent, MockComponent> {
    private IMocksControl control;

    public MockComponentBuilder(@Reference IMocksControl control) {
        this.control = control;
    }

    public MockComponent build(MockPhysicalComponent componentDefinition) {
        List<String> interfaces = componentDefinition.getInterfaces();
        ClassLoader classLoader = componentDefinition.getClassLoader();

        List<Class<?>> mockedInterfaces = new LinkedList<>();
        for (String interfaze : interfaces) {
            try {
                mockedInterfaces.add(classLoader.loadClass(interfaze));
            } catch (ClassNotFoundException ex) {
                throw new AssertionError(ex);
            }
        }

        Supplier<Object> supplier = new MockObjectSupplier<>(mockedInterfaces, classLoader, control);
        return new MockComponent(componentDefinition.getComponentUri(), supplier);
    }

}
