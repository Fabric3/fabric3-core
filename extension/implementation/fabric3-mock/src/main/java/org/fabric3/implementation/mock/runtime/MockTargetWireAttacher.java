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
import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;

import org.easymock.IMocksControl;
import org.fabric3.api.host.Fabric3Exception;
import org.fabric3.implementation.mock.provision.MockWireTargetDefinition;
import org.fabric3.spi.container.builder.component.TargetWireAttacher;
import org.fabric3.spi.container.wire.InvocationChain;
import org.fabric3.spi.container.wire.Wire;
import org.fabric3.spi.model.physical.PhysicalOperationDefinition;
import org.fabric3.spi.model.physical.PhysicalWireSourceDefinition;
import org.fabric3.spi.util.ClassLoading;
import org.oasisopen.sca.annotation.Reference;

/**
 *
 */
public class MockTargetWireAttacher implements TargetWireAttacher<MockWireTargetDefinition> {
    private final IMocksControl control;

    public MockTargetWireAttacher(@Reference IMocksControl control) {
        this.control = control;
    }

    public void attach(PhysicalWireSourceDefinition sourceDefinition, MockWireTargetDefinition targetDefinition, Wire wire) {

        Class<?> mockedInterface = loadInterface(targetDefinition);
        Object mock = createMock(mockedInterface);

        for (InvocationChain chain : wire.getInvocationChains()) {
            PhysicalOperationDefinition operation = chain.getPhysicalOperation();

            //Each invocation chain has a single physical operation associated with it. This physical operation needs a
            //single interceptor to re-direct the invocation to the mock 
            Method operationMethod = getOperationMethod(mockedInterface, operation);
            MockTargetInterceptor interceptor = new MockTargetInterceptor(mock, operationMethod);
            chain.addInterceptor(interceptor);
        }

    }

    public Supplier<?> createSupplier(MockWireTargetDefinition target) {
        return () -> createMock(loadInterface(target));
    }

    private Method getOperationMethod(Class<?> mockedInterface, PhysicalOperationDefinition op) {
        List<Class<?>> parameters = op.getTargetParameterTypes();
        for (Method method : mockedInterface.getMethods()) {
            if (method.getName().equals(op.getName())) {
                Class<?>[] parameterTypes = method.getParameterTypes();
                if (parameterTypes.length == parameters.size()) {
                    List<Class<?>> methodParameters = new ArrayList<>();
                    Collections.addAll(methodParameters, parameterTypes);
                    if (parameters.equals(methodParameters)) {
                        return method;
                    }
                }
            }
        }

        throw new Fabric3Exception("Failed to match method: " + op.getName() + " " + op.getSourceParameterTypes());
    }

    private Object createMock(Class<?> mockedInterface) {
        if (IMocksControl.class.isAssignableFrom(mockedInterface)) {
            return control;
        } else {
            return control.createMock(mockedInterface);
        }
    }

    private Class<?> loadInterface(MockWireTargetDefinition target) {
        String interfaceClass = target.getMockedInterface();
        ClassLoader classLoader = target.getClassLoader();
        return ClassLoading.loadClass(classLoader, interfaceClass);
    }

}
