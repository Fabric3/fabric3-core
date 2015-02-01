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
package org.fabric3.implementation.system.runtime;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.fabric3.api.host.Fabric3Exception;
import org.fabric3.spi.container.component.AtomicComponent;
import org.fabric3.spi.container.invocation.Message;
import org.fabric3.spi.container.wire.Interceptor;
import org.fabric3.spi.container.wire.InvocationRuntimeException;

/**
 *
 */
public class SystemInvokerInterceptor implements Interceptor {

    private final Method operation;
    private final AtomicComponent component;

    public SystemInvokerInterceptor(Method operation, AtomicComponent component) {
        this.operation = operation;
        this.component = component;
    }

    public void setNext(Interceptor next) {
        throw new UnsupportedOperationException();
    }

    public Interceptor getNext() {
        return null;
    }

    public Message invoke(Message msg) {
        Object body = msg.getBody();
        Object instance;
        try {
            instance = component.getInstance();
        } catch (Fabric3Exception e) {
            throw new InvocationRuntimeException(e);
        }

        try {
            msg.setBody(operation.invoke(instance, (Object[]) body));
        } catch (InvocationTargetException e) {
            msg.setBodyWithFault(e.getCause());
        } catch (IllegalAccessException e) {
            throw new InvocationRuntimeException(e);
        } finally {
            try {
                component.releaseInstance(instance);
            } catch (Fabric3Exception e) {
                throw new InvocationRuntimeException(e);
            }
        }
        return msg;
    }
}
