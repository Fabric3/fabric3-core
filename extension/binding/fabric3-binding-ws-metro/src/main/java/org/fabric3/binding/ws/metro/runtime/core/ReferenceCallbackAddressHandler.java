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
import javax.xml.soap.SOAPEnvelope;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPHeader;
import javax.xml.soap.SOAPMessage;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.handler.soap.SOAPHandler;
import javax.xml.ws.handler.soap.SOAPMessageContext;
import java.net.URI;
import java.net.URL;
import java.util.Set;

import org.oasisopen.sca.ServiceRuntimeException;

/**
 *
 */
public class ReferenceCallbackAddressHandler implements SOAPHandler<SOAPMessageContext> {
    private static final String WSA = "http://www.w3.org/2005/08/addressing";

    private URI callbackUri;
    private EndpointService endpointService;
    private URL callbackAddress;

    public ReferenceCallbackAddressHandler(URI callbackUri, EndpointService endpointService) {
        this.callbackUri = callbackUri;
        this.endpointService = endpointService;
    }

    public Set<QName> getHeaders() {
        return null;
    }

    public boolean handleMessage(SOAPMessageContext context) {
        if (callbackAddress == null) {
            callbackAddress = endpointService.getEndpointUrl(callbackUri);
        }

        try {

            SOAPMessage soapMessage = context.getMessage();
            SOAPHeader header = soapMessage.getSOAPHeader();
            SOAPEnvelope envelope = soapMessage.getSOAPPart().getEnvelope();
            SOAPElement element = header.addHeaderElement(envelope.createName("From", "wsa", WSA));
   	        element.addChildElement(envelope.createName("Address", "wsa", WSA)).addTextNode(callbackAddress.toString());
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
}
