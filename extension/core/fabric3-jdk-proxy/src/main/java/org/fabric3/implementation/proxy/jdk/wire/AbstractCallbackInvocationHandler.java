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
package org.fabric3.implementation.proxy.jdk.wire;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

import org.fabric3.spi.container.ContainerException;
import org.fabric3.spi.container.invocation.Message;
import org.fabric3.spi.container.invocation.MessageCache;
import org.fabric3.spi.container.invocation.WorkContext;
import org.fabric3.spi.container.wire.Interceptor;
import org.fabric3.spi.container.wire.InvocationChain;
import org.oasisopen.sca.ServiceUnavailableException;

/**
 * Abstract callback handler implementation. Concrete classes must implement a strategy for mapping the callback target chain for the invoked callback
 * operation.
 */
public abstract class AbstractCallbackInvocationHandler<T> implements InvocationHandler {

    protected Object invoke(InvocationChain chain, Object[] args, WorkContext workContext) throws Throwable {
        // Pop the call callback reference as we move back in the request stack. When the invocation is made on the callback target,
        // the same call callback reference state will be present as existed when the initial forward request to this proxy's instance was dispatched to.
        // Consequently, CallbackReference#getForwardCorrelaltionId() will return the correlation id for the callback target.
        String callbackReference = workContext.popCallbackReference();

        Interceptor headInterceptor = chain.getHeadInterceptor();
        assert headInterceptor != null;

        // send the invocation down the wire
        Message message = MessageCache.getAndResetMessage();
        message.setBody(args);
        message.setWorkContext(workContext);
        try {
            // dispatch the wire down the chain and get the response
            Message response;
            try {
                response = headInterceptor.invoke(message);
            } catch (ServiceUnavailableException e) {
                // simply rethrow ServiceUnavailableExceptions
                throw e;
            } catch (RuntimeException e) {
                // wrap other exceptions raised by the runtime
                throw new ServiceUnavailableException(e);
            }

            // handle response from the application, returning or throwing is as appropriate
            Object body = response.getBody();
            boolean fault = response.isFault();
            if (fault) {
                throw (Throwable) body;
            } else {
                return body;
            }
        } finally {
            // push the call callbackReference for this component instance back onto the stack
            workContext.addCallbackReference(callbackReference);
            message.reset();
        }
    }

    protected Object handleProxyMethod(Method method) throws ContainerException {
        if (method.getParameterTypes().length == 0 && "toString".equals(method.getName())) {
            return "[Proxy - " + Integer.toHexString(hashCode()) + "]";
        } else if (method.getDeclaringClass().equals(Object.class) && "equals".equals(method.getName())) {
            // TODO implement
            throw new UnsupportedOperationException();
        } else if (Object.class.equals(method.getDeclaringClass()) && "hashCode".equals(method.getName())) {
            return hashCode();
            // TODO better hash algorithm
        }
        String op = method.getName();
        throw new ContainerException("Operation not configured: " + op);
    }

}