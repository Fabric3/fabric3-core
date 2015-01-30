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

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.easymock.IMocksControl;
import org.fabric3.spi.container.objectfactory.ObjectFactory;

/**
 * Creates mock instances by delegating to the IMocksControl.
 */
public class MockObjectFactory<T> implements ObjectFactory<T> {

    private final Map<Class<?>, Object> mocks = new HashMap<>();
    private final T proxy;
    private final IMocksControl control;

    /**
     * Eager initiates the proxy.
     *
     * @param interfaces  the proxy interfaces
     * @param classLoader th classloader for creating the dynamic proxies
     * @param control     the mock control
     */
    public MockObjectFactory(List<Class<?>> interfaces, ClassLoader classLoader, IMocksControl control) {

        this.control = control;

        for (Class<?> interfaze : interfaces) {
            if (!interfaze.getName().equals(IMocksControl.class.getName())) {
                mocks.put(interfaze, control.createMock(interfaze));
            }
        }

        this.proxy = createProxy(interfaces, classLoader);

    }

    @SuppressWarnings("unchecked")
    public T getInstance() {
        return proxy;
    }

    @SuppressWarnings("unchecked")
    private T createProxy(List<Class<?>> interfaces, ClassLoader classLoader) {

        Class<?>[] mockInterfaces = new Class[interfaces.size() + 1];
        interfaces.toArray(mockInterfaces);
        mockInterfaces[mockInterfaces.length - 1] = IMocksControl.class;

        return (T) Proxy.newProxyInstance(classLoader, mockInterfaces, new InvocationHandler() {

            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {

                Class<?> interfaze = method.getDeclaringClass();
                if (interfaze.getName().equals(IMocksControl.class.getName())) {
                    return method.invoke(control, args);
                } else {
                    Object mock = mocks.get(interfaze);
                    if (mock == null) {
                        for (Class<?> mockInterface : mocks.keySet()) {
                            if (interfaze.isAssignableFrom(mockInterface)) {
                                mock = mocks.get(mockInterface);
                                break;
                            }
                        }
                    }
                    assert mock != null && interfaze.isInstance(mock);
                    return method.invoke(mock, args);
                }
            }

        });

    }

}
