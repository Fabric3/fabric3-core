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
package org.fabric3.async.runtime;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;

import org.fabric3.api.SecuritySubject;
import org.fabric3.spi.container.invocation.CallbackReference;
import org.fabric3.spi.container.invocation.Message;
import org.fabric3.spi.container.invocation.WorkContext;
import org.fabric3.spi.container.wire.Interceptor;

/**
 * Adds non-blocking behavior to an invocation chain
 */
public class NonBlockingInterceptor implements Interceptor {
    private static final Message RESPONSE = new ImmutableMessage();

    private final ExecutorService executorService;
    private NonBlockingMonitor monitor;
    private Interceptor next;

    public NonBlockingInterceptor(ExecutorService executorService, NonBlockingMonitor monitor) {
        this.executorService = executorService;
        this.monitor = monitor;
    }

    public Message invoke(final Message msg) {
        WorkContext workContext = msg.getWorkContext();
        List<CallbackReference> newStack = null;
        List<CallbackReference> stack = workContext.getCallbackReferences();
        if (stack != null && !stack.isEmpty()) {
            // clone the callstack to avoid multiple threads seeing changes
            newStack = new ArrayList<>(stack);
        }
        Map<String, Object> newHeaders = null;
        Map<String, Object> headers = workContext.getHeaders();
        if (headers != null && !headers.isEmpty()) {
            // clone the headers to avoid multiple threads seeing changes
            newHeaders = new HashMap<>(headers);
        }
        SecuritySubject subject = workContext.getSubject();
        Object payload = msg.getBody();
        AsyncRequest request = new AsyncRequest(next, payload, subject, newStack, newHeaders, monitor);
        executorService.execute(request);
        return RESPONSE;
    }

    public Interceptor getNext() {
        return next;
    }

    public void setNext(Interceptor next) {
        this.next = next;
    }

    /**
     * A dummy message passed back on an invocation
     */
    private static class ImmutableMessage implements Message {

        public Object getBody() {
            return null;
        }

        public void setBody(Object body) {
            if (body != null) {
                throw new UnsupportedOperationException();
            }
        }

        public WorkContext getWorkContext() {
            throw new UnsupportedOperationException();
        }

        public void setWorkContext(WorkContext workContext) {
            throw new UnsupportedOperationException();
        }

        public void reset() {
        }

        public boolean isFault() {
            return false;
        }

        public void setBodyWithFault(Object fault) {
            throw new UnsupportedOperationException();
        }

    }

}
