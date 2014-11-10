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
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPHeader;
import javax.xml.soap.SOAPMessage;
import javax.xml.ws.ProtocolException;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.handler.soap.SOAPHandler;
import javax.xml.ws.handler.soap.SOAPMessageContext;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.fabric3.binding.ws.metro.runtime.MetroConstants;
import org.fabric3.spi.container.invocation.WorkContext;
import org.fabric3.spi.container.invocation.WorkContextCache;
import org.oasisopen.sca.ServiceRuntimeException;

/**
 *
 */
public class ServiceCallbackAddressHandler implements SOAPHandler<SOAPMessageContext> {
    private static final String WSA = "http://www.w3.org/2005/08/addressing";
    private static final QName WSA_REFERENCE_PARAMETERS = new QName(WSA, "ReferenceParameters");
    private static final QName WSA_ADDRESS = new QName(WSA, "Address");
    private static final QName WSA_REPLY_TO = new QName(WSA, "ReplyTo");
    private static final QName WSA_FROM = new QName(WSA, "From");
    private static final String WSA_ANONYMOUS = "http://www.w3.org/2005/08/addressing/anonymous";
    public static final QName WSA_MESSAGE_ID = new QName("http://www.w3.org/2005/08/addressing", "MessageID");

    public ServiceCallbackAddressHandler() {
    }

    public Set<QName> getHeaders() {
        return null;
    }

    @SuppressWarnings("unchecked")
    public boolean handleMessage(SOAPMessageContext soapContext) {
        SOAPMessage soapMessage = soapContext.getMessage();
        if (soapMessage == null) {
            return true;
        }

        WorkContext workContext = WorkContextCache.getThreadWorkContext();
        workContext = (WorkContext) (workContext == null ? soapContext.get(MetroConstants.WORK_CONTEXT) : workContext);
        if (workContext == null) {
            throw new ServiceRuntimeException("Work context not set");
        }

        try {

            SOAPHeader soapHeader = soapMessage.getSOAPHeader();
            if (soapHeader == null) {
                return true;
            }
            Iterator<SOAPElement> fromHeaders = (Iterator<SOAPElement>) soapHeader.getChildElements(WSA_FROM);
            if (fromHeaders.hasNext()) {
                SOAPElement fromElement = fromHeaders.next();
                setReturnAddress(fromElement, workContext);
                setReferenceParameters(fromElement, workContext);
            }

            Iterator<SOAPElement> replyToHeaders = (Iterator<SOAPElement>) soapHeader.getChildElements(WSA_REPLY_TO);
            if (replyToHeaders.hasNext()) {
                SOAPElement replyToElement = replyToHeaders.next();
                setReturnAddress(replyToElement, workContext);
            }

            Iterator<SOAPElement> messageIdHeaders = soapHeader.getChildElements(WSA_MESSAGE_ID);
            if (messageIdHeaders.hasNext()) {
                SOAPElement messageIdHeader = messageIdHeaders.next();
                String messageId = messageIdHeader.getFirstChild().getNodeValue();
                workContext.setHeader(CallbackConstants.MESSAGE_ID, messageId);
            }

        } catch (SOAPException e) {
            throw new ServiceRuntimeException(e);
        }

        return true;
    }

    public boolean handleFault(SOAPMessageContext context) {
        return true;
    }

    public void close(MessageContext context) {
        // no-op
    }

    /**
     * Sets the callback endpoint address specified by the WSA header in the current work context.
     *
     * @param element     the WSA header
     * @param workContext the current work context
     */
    @SuppressWarnings("unchecked")
    private void setReturnAddress(SOAPElement element, WorkContext workContext) {
        Iterator<SOAPElement> addresses = (Iterator<SOAPElement>) element.getChildElements(WSA_ADDRESS);
        if (addresses.hasNext()) {
            String address = addresses.next().getValue();
            if (WSA_ANONYMOUS.equals(address)) {
                throw new ProtocolException("Invalid Callback Address: " + WSA_ANONYMOUS);
            }
            if (address != null) {
                workContext.setHeader(CallbackConstants.ENDPOINT_ADDRESS, address);
            }
        }

    }

    /**
     * Per the SCA spec, sets WSA reference parameters in the current work context so they can be returned as part of a callback message.
     *
     * @param fromElement the WSA from element
     * @param workContext the current work context
     */
    @SuppressWarnings("unchecked")
    private void setReferenceParameters(SOAPElement fromElement, WorkContext workContext) {
        // handle reference parameters
        Iterator<SOAPElement> referenceParameters = fromElement.getChildElements(WSA_REFERENCE_PARAMETERS);
        if (!referenceParameters.hasNext()) {
            return;
        }
        SOAPElement referenceParameter = referenceParameters.next();
        Iterator<SOAPElement> values = referenceParameter.getChildElements();
        if (!values.hasNext()) {
            return;
        }
        Map<QName, String> parameters = new HashMap<>();
        while (values.hasNext()) {
            SOAPElement element = values.next();
            QName name = element.getElementQName();
            String value = element.getValue();
            parameters.put(name, value);
        }
        workContext.setHeader(CallbackConstants.REFERENCE_PARAMETERS, parameters);
    }

}
