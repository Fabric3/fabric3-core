/*
* Fabric3
* Copyright (c) 2009-2012 Metaform Systems
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

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.easymock.IMocksControl;

import org.fabric3.spi.objectfactory.ObjectFactory;

/**
 * Creates mock instances by delegating to the IMocksControl.
 *
 * @version $Rev$ $Date$
 */
public class MockObjectFactory<T> implements ObjectFactory<T> {

    private final Map<Class<?>, Object> mocks = new HashMap<Class<?>, Object>();
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
