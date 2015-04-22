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
 */
package org.fabric3.binding.ws.metro.runtime.core;

import javax.xml.namespace.QName;
import javax.xml.soap.SOAPMessage;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.handler.soap.SOAPHandler;
import javax.xml.ws.handler.soap.SOAPMessageContext;
import java.util.Set;

import org.fabric3.binding.ws.metro.runtime.MetroConstants;
import org.fabric3.spi.container.binding.BindingHandler;
import org.fabric3.spi.container.invocation.Message;
import org.fabric3.spi.container.invocation.MessageCache;
import org.fabric3.spi.container.invocation.WorkContext;
import org.fabric3.spi.container.invocation.WorkContextCache;
import org.oasisopen.sca.ServiceRuntimeException;

/**
 * A {@link BindingHandler} adapter for JAX-WS.
 */
public class SOAPMessageHandlerAdapter implements SOAPHandler<SOAPMessageContext> {
    private BindingHandler<SOAPMessage> delegateHandler;

    public SOAPMessageHandlerAdapter(BindingHandler<SOAPMessage> handler) {
        delegateHandler = handler;
    }

    public boolean handleMessage(SOAPMessageContext smc) {
        Boolean outbound = (Boolean) smc.get(MessageContext.MESSAGE_OUTBOUND_PROPERTY);
        WorkContext workContext = WorkContextCache.getThreadWorkContext();
        workContext = (WorkContext) (workContext == null ? smc.get(MetroConstants.WORK_CONTEXT) : workContext);
        if (workContext == null) {
            throw new ServiceRuntimeException("Work context not set");
        }
        if (outbound) {
            // reference proxy outbound or service invocation return
            Message msg = MessageCache.getMessage();
            if (msg.getWorkContext() == null) {
                // service invocation return
                msg.setBody(smc.getMessage());
                msg.setWorkContext(workContext);
            }
            delegateHandler.handleOutbound(msg, smc.getMessage());
        } else {
            // reference proxy invocation return or service invocation
            Message msg = MessageCache.getMessage();
            if (msg.getWorkContext() == null) {
                // reference proxy return
                msg.setBody(smc.getMessage());
                msg.setWorkContext(workContext);
            }
            delegateHandler.handleInbound(smc.getMessage(), msg);
            msg.reset();
        }
        return true;
    }

    public Set<QName> getHeaders() {
        return null;
    }

    public void close(MessageContext mc) {
    }

    public boolean handleFault(SOAPMessageContext smc) {
        return true;
    }

}
