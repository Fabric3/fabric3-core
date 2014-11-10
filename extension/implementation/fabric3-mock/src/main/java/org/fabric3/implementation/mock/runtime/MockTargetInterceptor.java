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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.fabric3.spi.container.invocation.Message;
import org.fabric3.spi.container.wire.Interceptor;

/**
 *
 */
public class MockTargetInterceptor implements Interceptor {
    private Interceptor next;
    private Object mock;
    private Method method;

    public MockTargetInterceptor(Object mock, Method method) {
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
            message.reset();
            message.setBody(ret);
            return message;
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new AssertionError(e);
        }
    }

    public void setNext(Interceptor next) {
        this.next = next;
    }

}
