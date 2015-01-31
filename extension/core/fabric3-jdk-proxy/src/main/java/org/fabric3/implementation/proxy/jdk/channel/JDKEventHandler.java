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
 *
 * Portions originally based on Apache Tuscany 2007
 * licensed under the Apache 2.0 license.
 */
package org.fabric3.implementation.proxy.jdk.channel;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

import org.fabric3.api.host.ContainerException;
import org.fabric3.spi.container.channel.EventStream;

/**
 * Dispatches from a proxy to an {@link EventStream}.
 */
public final class JDKEventHandler implements InvocationHandler {
    private EventStream stream;

    public JDKEventHandler(EventStream stream) {
        this.stream = stream;
    }

    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        if (args == null || Object.class.equals(method.getDeclaringClass())) {
            // events have at least one arg
            handleProxyMethod(method);
            return null;
        }
        stream.getHeadHandler().handle(args[0], true);
        return null;
    }

    private Object handleProxyMethod(Method method) throws ContainerException {
        if (method.getParameterTypes().length == 0 && "toString".equals(method.getName())) {
            return "[Proxy - " + Integer.toHexString(hashCode()) + "]";
        } else if (method.getDeclaringClass().equals(Object.class) && "equals".equals(method.getName())) {
            throw new UnsupportedOperationException();
        } else if (Object.class.equals(method.getDeclaringClass()) && "hashCode".equals(method.getName())) {
            return hashCode();
        }
        String op = method.getName();
        throw new ContainerException("Operation not configured: " + op);
    }

}