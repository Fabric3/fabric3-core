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
import javax.xml.soap.SOAPMessage;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.handler.soap.SOAPHandler;
import javax.xml.ws.handler.soap.SOAPMessageContext;
import java.util.Iterator;
import java.util.Set;

import org.fabric3.binding.ws.metro.runtime.MetroConstants;
import org.fabric3.spi.invocation.CallFrame;
import org.fabric3.spi.invocation.WorkContext;
import org.fabric3.spi.invocation.WorkContextTunnel;
import org.oasisopen.sca.ServiceRuntimeException;

/**
 *
 */
public class ServiceCallbackAddressHandler implements SOAPHandler<SOAPMessageContext> {
    private static final String WSA = "http://www.w3.org/2005/08/addressing";
    private static final QName WSA_ADDRESS = new QName(WSA, "Address");
    private static final QName WSA_ACTION = new QName(WSA, "Action");
    private static final QName WSA_RELATES_TO = new QName(WSA, "RelatesTo");
    private static final QName WSA_TO = new QName(WSA, "To");
    private static final QName WSA_REPLY_TO = new QName(WSA, "ReplyTo");
    private static final QName WSA_FROM = new QName(WSA, "From");

    public ServiceCallbackAddressHandler() {
    }

    public Set<QName> getHeaders() {
        return null;
    }

    public boolean handleMessage(SOAPMessageContext soapContext) {
        SOAPMessage soapMessage = soapContext.getMessage();
        if (soapMessage == null) {
            return true;
        }

        WorkContext workContext = WorkContextTunnel.getThreadWorkContext();
        workContext = (WorkContext) (workContext == null ? soapContext.get(MetroConstants.WORK_CONTEXT) : workContext);
        if (workContext == null) {
            throw new ServiceRuntimeException("Work context not set");
        }
        workContext.addCallFrame(CallFrame.STATELESS_FRAME);

        try {

            SOAPHeader soapHeader = soapMessage.getSOAPHeader();
            if (soapHeader == null) {
                return true;
            }
//            workContext.setHeader(CallbackConstants.ENDPOINT_ADDRESS, "http://localhost:9081/JAXWS/Service5Callback");
            //            handleWsaToHeader(soapHeader, soapContext, workContext);
            //            handleWsaRelatesToHeader(soapHeader, soapContext, workContext);
            //            handleWsaActionHeader(soapHeader, soapContext, workContext);
            //handleScaPropsHeader(soapHeader, soapContext, workContext);

            Iterator<SOAPElement> fromHeaders = (Iterator<SOAPElement>) soapHeader.getChildElements(WSA_FROM);
            if (fromHeaders.hasNext()) {
                SOAPElement fromElement = fromHeaders.next();
                setReturnAddress(fromElement, workContext);
                return true;
            }

            Iterator<SOAPElement> replyToHeaders = (Iterator<SOAPElement>) soapHeader.getChildElements(WSA_REPLY_TO);
            if (replyToHeaders.hasNext()) {
                SOAPElement replyToElement = replyToHeaders.next();
                setReturnAddress(replyToElement, workContext);
                return true;
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

    private void setReturnAddress(SOAPElement element, WorkContext workContext) {
        Iterator<SOAPElement> addresses = (Iterator<SOAPElement>) element.getChildElements(WSA_ADDRESS);
        if (addresses.hasNext()) {
            String address = addresses.next().getValue();
            if (address != null) {
                workContext.setHeader(CallbackConstants.ENDPOINT_ADDRESS, address);
            }
        }

    }

    private boolean handleWsaToHeader(SOAPHeader soapHeader, SOAPMessageContext context, WorkContext workContext) {
        Iterator<SOAPElement> toHeaders = (Iterator<SOAPElement>) soapHeader.getChildElements(WSA_TO);
        if (toHeaders.hasNext()) {
            SOAPElement toElement = toHeaders.next();
            String address = toElement.getValue();
            if (address != null) {
         //       workContext.setHeader(CallbackConstants.ENDPOINT_ADDRESS, address);
                //                workContext.setHeader();
                //                context.put(TO_ADDRESS_KEY, address);
                //                context.setScope(TO_ADDRESS_KEY, MessageContext.Scope.APPLICATION);
            }
            return true;
        }
        return false;
    }

    private boolean handleWsaRelatesToHeader(SOAPHeader soapHeader, SOAPMessageContext context, WorkContext workContext) {
        Iterator<SOAPElement> relatesHeaders = (Iterator<SOAPElement>) soapHeader.getChildElements(WSA_RELATES_TO);
        if (relatesHeaders.hasNext()) {
            SOAPElement relatesElement = relatesHeaders.next();
            String relationshipType = relatesElement.getAttribute("RelationshipType");
            String relateID = relatesElement.getValue();
            if (relateID != null) {
                System.out.println("wsa:RelatesTo ID found: " + relateID);
                //                context.put(RELATES_KEY, relateID);
                //                context.setScope(RELATES_KEY, MessageContext.Scope.APPLICATION);
                //                context.put(RELATES_TYPE_KEY, relationshipType);
                //                context.setScope(RELATES_TYPE_KEY, MessageContext.Scope.APPLICATION);
            }
            return true;
        }
        return false;
    }

    private boolean handleWsaActionHeader(SOAPHeader soapHeader, SOAPMessageContext context, WorkContext workContext) {
        Iterator<SOAPElement> actionHeaders = (Iterator<SOAPElement>) soapHeader.getChildElements(WSA_ACTION);
        if (actionHeaders.hasNext()) {
            SOAPElement actionElement = actionHeaders.next();
            String address = actionElement.getValue();
            if (address != null) {
                System.out.println("wsa:Action value found: " + address);
                //                context.put(ACTION_KEY, address);
                //                context.setScope(ACTION_KEY, MessageContext.Scope.APPLICATION);
            }
            return true;
        }
        return false;

    }

    //    private boolean handleScaPropsHeader(SOAPHeader soapHeader, SOAPMessageContext context, WorkContext workContext) {
    //        Iterator<SOAPElement> scaPropsHeaders = (Iterator<SOAPElement>) soapHeader.getChildElements(new QName(XMLNS_TEST, SCA_PROPS));
    //        if (scaPropsHeaders.hasNext()) {
    //            SOAPElement scaPropsElement = scaPropsHeaders.next();
    //            String address = scaPropsElement.getValue();
    //            if (address != null) {
    //                System.out.println("test:SCAProps value found: " + address);
    //            }
    //            return true;
    //        }
    //        return false;
    //
    //    }

}
