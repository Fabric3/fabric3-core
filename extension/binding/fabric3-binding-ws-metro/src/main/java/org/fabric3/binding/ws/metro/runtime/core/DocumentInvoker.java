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
 *
 * ----------------------------------------------------
 *
 * Portions originally based on Apache Tuscany 2007
 * licensed under the Apache 2.0 license.
 *
 */
package org.fabric3.binding.ws.metro.runtime.core;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.xml.soap.MessageFactory;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;
import javax.xml.ws.WebServiceContext;
import javax.xml.ws.WebServiceException;

import com.sun.xml.ws.api.SOAPVersion;
import com.sun.xml.ws.api.message.Packet;
import com.sun.xml.ws.api.server.Invoker;
import com.sun.xml.ws.fault.SOAPFaultBuilder;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import org.fabric3.binding.ws.metro.runtime.MetroConstants;
import org.fabric3.spi.invocation.Message;
import org.fabric3.spi.invocation.MessageImpl;
import org.fabric3.spi.invocation.WorkContext;
import org.fabric3.spi.wire.Interceptor;
import org.fabric3.spi.wire.InvocationChain;


/**
 * Receives a web service invocation from the Metro transport layer and dispatches it through an interceptor chain to a target service that accepts
 * Document parameter types, avoiding JAXB deserialization.
 */
public class DocumentInvoker extends Invoker {
    private Map<String, InvocationChain> chains = new HashMap<String, InvocationChain>();
    private MessageFactory factory;

    /**
     * Constructor.
     *
     * @param chains the invocation chains for the wire.
     */
    public DocumentInvoker(List<InvocationChain> chains) {
        for (InvocationChain chain : chains) {
            this.chains.put(chain.getPhysicalOperation().getName(), chain);
        }
        ClassLoader old = Thread.currentThread().getContextClassLoader();
        try {
            // set classloader to pick up correct SAAJ implementation
            Thread.currentThread().setContextClassLoader(getClass().getClassLoader());
            factory = MessageFactory.newInstance();
        } catch (SOAPException e) {
            // programming error
            throw new AssertionError(e);
        } finally {
            Thread.currentThread().setContextClassLoader(old);
        }

    }

    public Object invoke(Packet packet, Method method, Object... args) throws InvocationTargetException {
        // the work context is populated by the current tubeline
        WorkContext workContext = (WorkContext) packet.invocationProperties.get(MetroConstants.WORK_CONTEXT);
        if (workContext == null) {
            // programming error
            throw new AssertionError("Work context not set");
        }
        Message input;
        try {
            if (args.length != 1) {
                throw new UnsupportedOperationException("Illegal number of arguments");
            }
            if (!(args[0] instanceof SOAPMessage)) {
                throw new UnsupportedOperationException("Expected SOAPMessage but was: " + args[0].getClass());
            }
            SOAPMessage soapMessage = (SOAPMessage) args[0];
            Node node = soapMessage.getSOAPBody().extractContentAsDocument();
            input = new MessageImpl(node, false, workContext);
        } catch (SOAPException e) {
            throw new InvocationTargetException(e);
        }
        String operationName = packet.getWSDLOperation().getLocalPart();
        if (operationName == null) {
            throw new AssertionError("No invocation chain found for WSDL operation: " + operationName);
        }
        Interceptor head = chains.get(operationName).getHeadInterceptor();
        Message ret = head.invoke(input);

        Object body = ret.getBody();
        if (!ret.isFault()) {
            return createResponse(body);
        } else {
            return createFault((Throwable) body);
        }
    }

    /**
     * Overridden as the superclass method throws <code>UnsupportedOperationException</code>
     */
    @Override
    public void start(WebServiceContext wsc) {
    }

    private SOAPMessage createResponse(Object body) throws InvocationTargetException {
        try {
            SOAPMessage soapMessage = factory.createMessage();
            soapMessage.getSOAPBody().addDocument((Document) body);
            soapMessage.saveChanges();
            return soapMessage;
        } catch (SOAPException e) {
            throw new InvocationTargetException(e);
        }
    }

    private SOAPMessage createFault(Throwable e) {
        try {
            // FIXME SOAP 1.1
            // FIXME If SOAPFault, get details and use SAAJ to create SOAPMessage otherwise system exception and do below
            // The following only works for Java exceptions
            com.sun.xml.ws.api.message.Message fault = SOAPFaultBuilder.createSOAPFaultMessage(SOAPVersion.SOAP_11, null, e);
            return fault.readAsSOAPMessage();
        } catch (SOAPException e2) {
            throw new WebServiceException(e2);
        }
    }


}