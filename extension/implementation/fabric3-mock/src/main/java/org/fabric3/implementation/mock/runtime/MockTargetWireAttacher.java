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

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import org.easymock.IMocksControl;
import org.fabric3.api.host.ContainerException;
import org.fabric3.implementation.mock.provision.MockWireTargetDefinition;
import org.fabric3.spi.classloader.ClassLoaderRegistry;
import org.fabric3.spi.container.builder.component.TargetWireAttacher;
import org.fabric3.spi.container.objectfactory.ObjectFactory;
import org.fabric3.spi.container.objectfactory.SingletonObjectFactory;
import org.fabric3.spi.container.wire.InvocationChain;
import org.fabric3.spi.container.wire.Wire;
import org.fabric3.spi.model.physical.PhysicalOperationDefinition;
import org.fabric3.spi.model.physical.PhysicalWireSourceDefinition;
import org.oasisopen.sca.annotation.Reference;

/**
 *
 */
public class MockTargetWireAttacher implements TargetWireAttacher<MockWireTargetDefinition> {
    private final ClassLoaderRegistry classLoaderRegistry;
    private final IMocksControl control;

    public MockTargetWireAttacher(@Reference ClassLoaderRegistry classLoaderRegistry, @Reference IMocksControl control) {
        this.classLoaderRegistry = classLoaderRegistry;
        this.control = control;
    }

    public void attach(PhysicalWireSourceDefinition sourceDefinition, MockWireTargetDefinition targetDefinition, Wire wire) throws ContainerException {

        Class<?> mockedInterface = loadInterface(targetDefinition);
        Object mock = createMock(mockedInterface);

        for (InvocationChain chain : wire.getInvocationChains()) {
            PhysicalOperationDefinition operation = chain.getPhysicalOperation();

            //Each invocation chain has a single physical operation associated with it. This physical operation needs a
            //single interceptor to re-direct the invocation to the mock 
            Method operationMethod = getOperationMethod(mockedInterface, operation, sourceDefinition, targetDefinition);
            MockTargetInterceptor interceptor = new MockTargetInterceptor(mock, operationMethod);
            chain.addInterceptor(interceptor);
        }

    }

    public void detach(PhysicalWireSourceDefinition source, MockWireTargetDefinition target) throws ContainerException {
        // no-op
    }

    public ObjectFactory<?> createObjectFactory(MockWireTargetDefinition target) throws ContainerException {
        Class<?> mockedInterface = loadInterface(target);
        Object mock = createMock(mockedInterface);
        return new SingletonObjectFactory<>(mock);
    }

    private Method getOperationMethod(Class<?> mockedInterface,
                                      PhysicalOperationDefinition op,
                                      PhysicalWireSourceDefinition sourceDefinition,
                                      MockWireTargetDefinition wireTargetDefinition) throws ContainerException {
        List<String> parameters = op.getTargetParameterTypes();
        for (Method method : mockedInterface.getMethods()) {
            if (method.getName().equals(op.getName())) {
                Class<?>[] parameterTypes = method.getParameterTypes();
                if (parameterTypes.length == parameters.size()) {
                    List<String> methodParameters = new ArrayList<>();
                    for (Class<?> parameter : parameterTypes) {
                        methodParameters.add(parameter.getName());
                    }

                    if (parameters.equals(methodParameters)) {
                        return method;
                    }
                }
            }
        }

        throw new ContainerException("Failed to match method: " + op.getName() + " " + op.getSourceParameterTypes());
    }

    private Object createMock(Class<?> mockedInterface) {
        if (IMocksControl.class.isAssignableFrom(mockedInterface)) {
            return control;
        } else {
            return control.createMock(mockedInterface);
        }
    }

    private Class<?> loadInterface(MockWireTargetDefinition target) throws ContainerException {
        String interfaceClass = target.getMockedInterface();
        try {
            ClassLoader classLoader = classLoaderRegistry.getClassLoader(target.getClassLoaderId());
            return classLoader.loadClass(interfaceClass);
        } catch (ClassNotFoundException e) {
            throw new ContainerException("Unable to load interface " + interfaceClass);
        }
    }

}
