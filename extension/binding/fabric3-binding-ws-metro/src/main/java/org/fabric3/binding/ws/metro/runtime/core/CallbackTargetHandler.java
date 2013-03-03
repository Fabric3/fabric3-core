/*
 * Fabric3
 * Copyright (c) 2009-2012 Metaform Systems
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
import javax.xml.ws.BindingProvider;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.handler.soap.SOAPHandler;
import javax.xml.ws.handler.soap.SOAPMessageContext;
import java.util.Map;
import java.util.Set;

import org.fabric3.binding.ws.metro.runtime.MetroConstants;
import org.fabric3.spi.invocation.WorkContext;
import org.fabric3.spi.invocation.WorkContextTunnel;
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
        WorkContext workContext = WorkContextTunnel.getThreadWorkContext();
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
