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
package org.fabric3.implementation.bytecode.proxy.wire;

import org.fabric3.implementation.bytecode.proxy.common.ProxyDispatcher;
import org.fabric3.spi.container.invocation.CallbackReference;
import org.fabric3.spi.container.invocation.Message;
import org.fabric3.spi.container.invocation.MessageCache;
import org.fabric3.spi.container.invocation.WorkContext;
import org.fabric3.spi.container.invocation.WorkContextCache;
import org.fabric3.spi.container.wire.Interceptor;
import org.fabric3.spi.container.wire.InvocationChain;
import org.oasisopen.sca.ServiceReference;
import org.oasisopen.sca.ServiceRuntimeException;
import org.oasisopen.sca.ServiceUnavailableException;

/**
 * Dispatches from a proxy to a wire.
 */
public class WireProxyDispatcher<B> implements ProxyDispatcher, ServiceReference<B> {
    private static final long serialVersionUID = -3766594738137530257L;

    private Class<B> interfaze;
    private String callbackUri;
    private transient InvocationChain[] chains;

    public void init(Class<B> interfaze, String callbackUri, InvocationChain[] chains) {
        this.interfaze = interfaze;
        this.callbackUri = callbackUri;
        this.chains = chains;
    }

    public B getService() {
        throw new UnsupportedOperationException();
    }

    public Class<B> getBusinessInterface() {
        return interfaze;
    }

    public Object _f3_invoke(int index, Object args) throws Throwable {
        InvocationChain chain = chains[index];

        Interceptor headInterceptor = chain.getHeadInterceptor();

        WorkContext workContext = WorkContextCache.getThreadWorkContext();

        if (callbackUri != null) {
            initializeCallbackReference(workContext);
        }

        Message message = MessageCache.getAndResetMessage();
        message.setBody(args);
        message.setWorkContext(workContext);
        try {
            // dispatch the invocation down the chain and get the response
            Message response;
            try {
                response = headInterceptor.invoke(message);
            } catch (ServiceRuntimeException e) {
                // simply rethrow ServiceRuntimeException
                throw e;
            } catch (RuntimeException e) {
                // wrap other exceptions raised by the runtime
                throw new ServiceUnavailableException(e);
            }

            // handle response from the application, returning or throwing an exception as appropriate
            Object body = response.getBody();
            boolean fault = response.isFault();

            if (fault) {
                throw (Throwable) body;
            } else {
                return body;
            }
        } finally {
            if (callbackUri != null) {
                // no callback reference was created as the wire is unidrectional
                workContext.popCallbackReference();
            }
            message.reset();
        }

    }

    private void initializeCallbackReference(WorkContext workContext) {
        CallbackReference callbackReference = new CallbackReference(callbackUri, null);
        workContext.addCallbackReference(callbackReference);
    }

}
