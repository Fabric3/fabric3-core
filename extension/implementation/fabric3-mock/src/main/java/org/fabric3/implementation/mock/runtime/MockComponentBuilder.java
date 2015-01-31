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

import org.easymock.IMocksControl;
import org.fabric3.api.host.ContainerException;
import org.fabric3.implementation.mock.model.MockComponentDefinition;
import org.fabric3.spi.classloader.ClassLoaderRegistry;
import org.fabric3.spi.container.builder.component.ComponentBuilder;
import org.fabric3.spi.container.objectfactory.ObjectFactory;
import org.oasisopen.sca.annotation.EagerInit;
import org.oasisopen.sca.annotation.Reference;

/**
 *
 */
@EagerInit
public class MockComponentBuilder implements ComponentBuilder<MockComponentDefinition, MockComponent> {
    private ClassLoaderRegistry classLoaderRegistry;
    private IMocksControl control;

    public MockComponentBuilder(@Reference ClassLoaderRegistry classLoaderRegistry, @Reference IMocksControl control) {
        this.classLoaderRegistry = classLoaderRegistry;
        this.control = control;
    }

    public MockComponent build(MockComponentDefinition componentDefinition) throws ContainerException {
        List<String> interfaces = componentDefinition.getInterfaces();
        ClassLoader classLoader = classLoaderRegistry.getClassLoader(componentDefinition.getClassLoaderId());

        List<Class<?>> mockedInterfaces = new LinkedList<>();
        for (String interfaze : interfaces) {
            try {
                mockedInterfaces.add(classLoader.loadClass(interfaze));
            } catch (ClassNotFoundException ex) {
                throw new AssertionError(ex);
            }
        }

        ObjectFactory<Object> objectFactory = new MockObjectFactory<>(mockedInterfaces, classLoader, control);
        return new MockComponent(componentDefinition.getComponentUri(), objectFactory);
    }

    public void dispose(MockComponentDefinition definition, MockComponent component) throws ContainerException {
        //no-op
    }

}
