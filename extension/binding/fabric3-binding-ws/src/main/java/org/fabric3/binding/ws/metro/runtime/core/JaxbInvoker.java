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
package org.fabric3.binding.ws.metro.runtime.core;

import javax.xml.ws.WebServiceContext;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.sun.xml.ws.api.message.Packet;
import com.sun.xml.ws.api.server.Invoker;
import org.fabric3.binding.ws.metro.runtime.MetroConstants;
import org.fabric3.spi.container.invocation.Message;
import org.fabric3.spi.container.invocation.MessageCache;
import org.fabric3.spi.container.invocation.WorkContext;
import org.fabric3.spi.container.wire.Interceptor;
import org.fabric3.spi.container.wire.InvocationChain;

/**
 * Invoker that receives a web service invocation from the Metro transport layer and dispatches it through the interceptor chain to a target service that
 * accepts JAXB parameter types.
 */
public class JaxbInvoker extends Invoker {
    private Map<String, InvocationChain> chains = new HashMap<>();

    /**
     * Constructor.
     *
     * @param chains Invocation chains.
     */
    public JaxbInvoker(List<InvocationChain> chains) {
        for (InvocationChain chain : chains) {
            this.chains.put(chain.getPhysicalOperation().getName(), chain);
        }
    }

    public Object invoke(Packet packet, Method method, Object... args) throws InvocationTargetException {
        // the work context is populated by the current tubeline
        WorkContext workContext = (WorkContext) packet.invocationProperties.get(MetroConstants.WORK_CONTEXT);
        if (workContext == null) {
            // programming error
            throw new AssertionError("Work context not set");
        }

        Message input = MessageCache.getAndResetMessage();
        try {
            input.setWorkContext(workContext);
            input.setBody(args);
            Interceptor head = chains.get(method.getName()).getHeadInterceptor();

            Message ret = head.invoke(input);

            if (!ret.isFault()) {
                return ret.getBody();
            } else {
                Throwable th = (Throwable) ret.getBody();
                throw new InvocationTargetException(th);
            }
        } finally {
            input.reset();
        }
    }

    /**
     * Overridden as the superclass method throws <code>UnsupportedOperationException</code>
     */
    @Override
    public void start(WebServiceContext wsc) {
    }

}
