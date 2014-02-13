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
