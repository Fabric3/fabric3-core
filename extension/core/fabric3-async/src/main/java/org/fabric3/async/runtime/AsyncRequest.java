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

import java.util.List;
import java.util.Map;

import org.fabric3.api.SecuritySubject;
import org.fabric3.spi.container.invocation.Message;
import org.fabric3.spi.container.invocation.MessageCache;
import org.fabric3.spi.container.invocation.WorkContext;
import org.fabric3.spi.container.invocation.WorkContextCache;
import org.fabric3.spi.container.wire.Interceptor;

/**
 * Encapsulates an invocation to be processed asynchronously.
 */
public class AsyncRequest implements Runnable {
    private Interceptor next;
    private Object payload;
    private SecuritySubject subject;
    private List<String> stack;
    private Map<String, Object> headers;
    private NonBlockingMonitor monitor;

    public AsyncRequest(Interceptor next,
                        Object payload,
                        SecuritySubject subject,
                        List<String> stack,
                        Map<String, Object> headers,
                        NonBlockingMonitor monitor) {
        this.next = next;
        this.payload = payload;
        this.subject = subject;
        this.stack = stack;
        this.headers = headers;
        this.monitor = monitor;
    }

    public void run() {
        WorkContext workContext = WorkContextCache.getAndResetThreadWorkContext();
        workContext.addCallbackReferences(stack);
        workContext.addHeaders(headers);
        workContext.setSubject(subject);

        Message message = MessageCache.getAndResetMessage();
        message.setBody(payload);
        message.setWorkContext(workContext);

        Message response = next.invoke(message);

        if (response.isFault()) {
            // log the exception
            monitor.onError((Throwable) response.getBody());
        }

        message.reset();
        workContext.reset();
    }

    public Interceptor getNext() {
        return next;
    }

}
