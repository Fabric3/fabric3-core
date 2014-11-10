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
import javax.xml.ws.BindingProvider;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.handler.soap.SOAPHandler;
import javax.xml.ws.handler.soap.SOAPMessageContext;
import java.util.Map;
import java.util.Set;

import org.fabric3.binding.ws.metro.runtime.MetroConstants;
import org.fabric3.spi.container.invocation.WorkContext;
import org.fabric3.spi.container.invocation.WorkContextCache;
import org.oasisopen.sca.ServiceRuntimeException;

/**
 * Sets callback headers and the callback endpoint address using the replyTo address sent with the initial request and stored in the current work context.
 */
public class CallbackTargetHandler implements SOAPHandler<SOAPMessageContext> {
    private static final String WSA = "http://www.w3.org/2005/08/addressing";
    private static final QName WSA_RELATES_TO = new QName(WSA, "RelatesTo");
    private static final QName WSA_RELATIONSHIP_TYPE = new QName(WSA, "RelationshipType");
    private static final String SCA_CALLBACK_RELATIONSHIP = "http://docs.oasis-open.org/opencsa/sca-bindings/ws/callback";

    public Set<QName> getHeaders() {
        return null;
    }

    public boolean handleMessage(SOAPMessageContext soapContext) {
        WorkContext workContext = WorkContextCache.getThreadWorkContext();
        workContext = (WorkContext) (workContext == null ? soapContext.get(MetroConstants.WORK_CONTEXT) : workContext);
        if (workContext == null) {
            throw new ServiceRuntimeException("Work context not set");
        }

        soapContext.put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, workContext.getHeader(String.class, CallbackConstants.ENDPOINT_ADDRESS));

        // set other callback headers
        try {
            handleRelatesToHeader(soapContext, workContext);
            handleReferenceParameters(soapContext, workContext);
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
     * Per the SCA WS Binding spec, sends a RelatesTo based on the original message id if one was sent.
     *
     * @param soapContext the SOAP context
     * @param workContext the work context
     * @throws SOAPException if there is an error setting the relatesTo header.
     */
    private void handleRelatesToHeader(SOAPMessageContext soapContext, WorkContext workContext) throws SOAPException {
        String messageId = workContext.getHeader(String.class, CallbackConstants.MESSAGE_ID);
        if (messageId == null) {
            return;
        }
        SOAPHeader soapHeader = soapContext.getMessage().getSOAPHeader();
        SOAPElement relatesToHeader = soapHeader.addHeaderElement(WSA_RELATES_TO);
        relatesToHeader.setValue(messageId);
        relatesToHeader.addAttribute(WSA_RELATIONSHIP_TYPE, SCA_CALLBACK_RELATIONSHIP);
    }

    /**
     * Per the SCA WS Binding spec, returns WSA ReferenceParameters from the original message if present.
     *
     * @param soapContext the SOAP context
     * @param workContext the work context
     * @throws SOAPException if there is an error setting the parameters header.
     */
    @SuppressWarnings("unchecked")
    private void handleReferenceParameters(SOAPMessageContext soapContext, WorkContext workContext) throws SOAPException {
        Map<QName, String> parameters = workContext.getHeader(Map.class, CallbackConstants.REFERENCE_PARAMETERS);
        if (parameters == null) {
            return;
        }
        SOAPHeader soapHeader = soapContext.getMessage().getSOAPHeader();
        for (Map.Entry<QName, String> entry : parameters.entrySet()) {
            SOAPElement headerElement = soapHeader.addHeaderElement(entry.getKey());
            headerElement.setValue(entry.getValue());

        }
    }

}
