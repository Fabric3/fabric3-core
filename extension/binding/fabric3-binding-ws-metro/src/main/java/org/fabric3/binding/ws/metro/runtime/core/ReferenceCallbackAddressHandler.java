/*
 * Fabric3
 * Copyright (c) 2009-2013 Metaform Systems
 *
 * Fabric3 is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of
 * the License, or (at your option) any later version, with the
 * following exception:
 *
 * Linking this software statically or dynamically with other
 * modules is making a combined work based on this software.
 * Thus, the terms and conditions of the GNU General Public
 * License cover the whole combination.
 *
 * As a special exception, the copyright holders of this software
 * give you permission to link this software with independent
 * modules to produce an executable, regardless of the license
 * terms of these independent modules, and to copy and distribute
 * the resulting executable under terms of your choice, provided
 * that you also meet, for each linked independent module, the
 * terms and conditions of the license of that module. An
 * independent module is a module which is not derived from or
 * based on this software. If you modify this software, you may
 * extend this exception to your version of the software, but
 * you are not obligated to do so. If you do not wish to do so,
 * delete this exception statement from your version.
 *
 * Fabric3 is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the
 * GNU General Public License along with Fabric3.
 * If not, see <http://www.gnu.org/licenses/>.
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
