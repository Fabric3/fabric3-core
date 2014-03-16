/*
* Fabric3
* Copyright (c) 2009-2013 Metaform Systems
*
* Fabric3 is free software: you can redistribute it and/or modify
* it under the terms of the GNU General Public License as
* published by the Free Software Foundation, either version 3 of
* the License, or (at your option) any later version, with the
* following exception:
*
* Linking this software statically or dynamically with other
* modules is making a combined work based on this software.
* Thus, the terms and conditions of the GNU General Public
* License cover the whole combination.
*
* As a special exception, the copyright holders of this software
* give you permission to link this software with independent
* modules to produce an executable, regardless of the license
* terms of these independent modules, and to copy and distribute
* the resulting executable under terms of your choice, provided
* that you also meet, for each linked independent module, the
* terms and conditions of the license of that module. An
* independent module is a module which is not derived from or
* based on this software. If you modify this software, you may
* extend this exception to your version of the software, but
* you are not obligated to do so. If you do not wish to do so,
* delete this exception statement from your version.
*
* Fabric3 is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty
* of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
* See the GNU General Public License for more details.
*
* You should have received a copy of the
* GNU General Public License along with Fabric3.
* If not, see <http://www.gnu.org/licenses/>.
*/
package org.fabric3.implementation.mock.runtime;

import java.lang.reflect.Method;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import org.easymock.IMocksControl;
import org.fabric3.implementation.mock.provision.MockWireTargetDefinition;
import org.fabric3.spi.classloader.ClassLoaderRegistry;
import org.fabric3.spi.container.ContainerException;
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
