/*
* Fabric3
* Copyright (c) 2009 Metaform Systems
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
package org.fabric3.implementation.mock;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import org.easymock.IMocksControl;
import org.osoa.sca.annotations.Reference;

import org.fabric3.spi.builder.WiringException;
import org.fabric3.spi.builder.component.TargetWireAttacher;
import org.fabric3.spi.builder.component.WireAttachException;
import org.fabric3.spi.classloader.ClassLoaderRegistry;
import org.fabric3.spi.invocation.Message;
import org.fabric3.spi.invocation.MessageImpl;
import org.fabric3.spi.model.physical.PhysicalOperationDefinition;
import org.fabric3.spi.model.physical.PhysicalSourceDefinition;
import org.fabric3.spi.objectfactory.ObjectFactory;
import org.fabric3.spi.objectfactory.SingletonObjectFactory;
import org.fabric3.spi.wire.Interceptor;
import org.fabric3.spi.wire.InvocationChain;
import org.fabric3.spi.wire.Wire;

/**
 * @version $Rev$ $Date$
 */
public class MockTargetWireAttacher implements TargetWireAttacher<MockTargetDefinition> {

    private final ClassLoaderRegistry classLoaderRegistry;
    private final IMocksControl control;

    public MockTargetWireAttacher(@Reference ClassLoaderRegistry classLoaderRegistry, @Reference IMocksControl control) {
        this.classLoaderRegistry = classLoaderRegistry;
        this.control = control;
    }

    public void attach(PhysicalSourceDefinition sourceDefinition,
                       MockTargetDefinition wireTargetDefinition,
                       Wire wire) throws WireAttachException {

        Class<?> mockedInterface = loadInterface(wireTargetDefinition);
        Object mock = createMock(mockedInterface);

        for (InvocationChain chain : wire.getInvocationChains()) {
            PhysicalOperationDefinition operation = chain.getPhysicalOperation();

            //Each invocation chain has a single physical operation associated with it. This physical operation needs a
            //single interceptor to re-direct the invocation to the mock 
            Method operationMethod = getOperationMethod(mockedInterface, operation, sourceDefinition, wireTargetDefinition);
            chain.addInterceptor(new MockTargetInterceptor(mock, operationMethod));
        }

    }

    public void detach(PhysicalSourceDefinition source, MockTargetDefinition target) throws WiringException {
        // no-op
    }

    public ObjectFactory<?> createObjectFactory(MockTargetDefinition target) throws WiringException {
        Class<?> mockedInterface = loadInterface(target);
        Object mock = createMock(mockedInterface);
        return new SingletonObjectFactory<Object>(mock);
    }

    private Method getOperationMethod(Class<?> mockedInterface, PhysicalOperationDefinition op,
                                      PhysicalSourceDefinition sourceDefinition,
                                      MockTargetDefinition wireTargetDefinition) throws WireAttachException {
        List<String> parameters = op.getTargetParameterTypes();
        for (Method method : mockedInterface.getMethods()) {
            if (method.getName().equals(op.getName())) {
                Class<?>[] parameterTypes = method.getParameterTypes();
                if (parameterTypes.length == parameters.size()) {
                    List<String> methodParameters = new ArrayList<String>();
                    for (Class<?> parameter : parameterTypes) {
                        methodParameters.add(parameter.getName());
                    }

                    if (parameters.equals(methodParameters)) {
                        return method;
                    }
                }
            }
        }

        throw new WireAttachException("Failed to match method: " + op.getName() + " " + op.getSourceParameterTypes(),
                                      sourceDefinition.getUri(),
                                      wireTargetDefinition.getUri(),
                                      null);
    }

    private Object createMock(Class<?> mockedInterface) {
        if (IMocksControl.class.isAssignableFrom(mockedInterface)) {
            return control;
        } else {
            return control.createMock(mockedInterface);
        }
    }

    private Class<?> loadInterface(MockTargetDefinition target) throws WireAttachException {
        String interfaceClass = target.getMockedInterface();
        try {
            ClassLoader classLoader = classLoaderRegistry.getClassLoader(target.getClassLoaderId());
            return classLoader.loadClass(interfaceClass);
        } catch (ClassNotFoundException e) {
            URI targetUri = target.getUri();
            throw new WireAttachException("Unable to load interface " + interfaceClass, null, targetUri, e);
        }
    }

    private class MockTargetInterceptor implements Interceptor {

        private Interceptor next;
        private Object mock;
        private Method method;

        private MockTargetInterceptor(Object mock, Method method) {
            this.mock = mock;
            this.method = method;
        }

        public Interceptor getNext() {
            return next;
        }

        public Message invoke(Message message) {

            try {

                Object[] args = (Object[]) message.getBody();
                Object ret = method.invoke(mock, args);
                Message out = new MessageImpl();
                out.setBody(ret);

                return out;

            } catch (IllegalAccessException e) {
                throw new AssertionError(e);
            } catch (InvocationTargetException e) {
                throw new AssertionError(e);
            }

        }

        public void setNext(Interceptor next) {
            this.next = next;
        }

    }
}
