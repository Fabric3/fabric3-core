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
package org.fabric3.binding.zeromq.runtime.interceptor;

import org.fabric3.binding.zeromq.runtime.message.RequestReplySender;
import org.fabric3.spi.container.invocation.Message;
import org.fabric3.spi.container.invocation.WorkContext;
import org.fabric3.spi.container.wire.Interceptor;

/**
 * Dispatches a message from an invocation chain to a ZeroMQ request-response sender.
 */
public class RequestReplyInterceptor implements Interceptor {
    private int index;
    private RequestReplySender sender;

    public RequestReplyInterceptor(int index, RequestReplySender sender) {
        this.index = index;
        this.sender = sender;
    }

    public Message invoke(Message msg) {
        byte[] body = (byte[]) msg.getBody();
        WorkContext workContext = msg.getWorkContext();
        byte[] value = sender.sendAndReply(body, index, workContext);
        msg.setBody(value);
        return msg;
    }

    public void setNext(Interceptor next) {
        throw new IllegalStateException("This interceptor must be the last one in an target interceptor chain");
    }

    public Interceptor getNext() {
        return null;
    }

}
