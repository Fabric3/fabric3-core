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
import java.util.HashMap;
import java.util.Map;

import org.easymock.ConstructorArgs;
import org.easymock.EasyMock;
import org.easymock.IMocksControl;
import org.oasisopen.sca.annotation.EagerInit;
import org.oasisopen.sca.annotation.Init;

/**
 *
 */
@EagerInit
public class IMocksControlProxy implements IMocksControl {

    private IMocksControl delegate;
    private Map<Class<?>, Object> mocks = new HashMap<>();

    @Init
    public void init() {
        delegate = EasyMock.createControl();
    }

    public void checkOrder(boolean state) {
        delegate.checkOrder(state);
    }

    public void makeThreadSafe(boolean threadSafe) {
    }

    public void checkIsUsedInOneThread(boolean shouldBeUsedInOneThread) {
    }

    public <T> T createMock(Class<T> toMock) {
        Object mock = mocks.get(toMock);
        if (mock == null) {
            mock = delegate.createMock(toMock);
            mocks.put(toMock, mock);
        }
        return toMock.cast(mock);
    }

    public <T> T createMock(String name, Class<T> toMock) {
        return delegate.createMock(name, toMock);
    }

    public <T> T createMock(Class<T> toMock, Method... mockedMethods) {
        return delegate.createMock(toMock, mockedMethods);
    }

    public <T> T createMock(Class<T> toMock, ConstructorArgs constructorArgs, Method... mockedMethods) {
        return delegate.createMock(toMock, constructorArgs, mockedMethods);
    }

    public <T> T createMock(String name, Class<T> toMock, Method... mockedMethods) {
        return delegate.createMock(name, toMock, mockedMethods);
    }

    public <T> T createMock(String name, Class<T> toMock, ConstructorArgs constructorArgs, Method... mockedMethods) {
        return delegate.createMock(name, toMock, constructorArgs, mockedMethods);
    }

    public void replay() {
        delegate.replay();
    }

    public void reset() {
        delegate.reset();
        mocks.clear();
    }

    public void resetToNice() {
    }

    public void resetToDefault() {
    }

    public void resetToStrict() {
    }

    public void verify() {
        delegate.verify();
    }

}
