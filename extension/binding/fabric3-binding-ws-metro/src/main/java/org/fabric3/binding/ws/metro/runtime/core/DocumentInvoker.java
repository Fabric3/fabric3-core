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
 *
 * Portions originally based on Apache Tuscany 2007
 * licensed under the Apache 2.0 license.
 */
package org.fabric3.binding.ws.metro.runtime.core;

import javax.xml.soap.MessageFactory;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;
import javax.xml.ws.WebServiceContext;
import javax.xml.ws.WebServiceException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.sun.xml.ws.api.SOAPVersion;
import com.sun.xml.ws.api.message.Packet;
import com.sun.xml.ws.api.server.Invoker;
import com.sun.xml.ws.fault.SOAPFaultBuilder;
import org.fabric3.binding.ws.metro.runtime.MetroConstants;
import org.fabric3.spi.container.invocation.Message;
import org.fabric3.spi.container.invocation.MessageCache;
import org.fabric3.spi.container.invocation.WorkContext;
import org.fabric3.spi.container.wire.Interceptor;
import org.fabric3.spi.container.wire.InvocationChain;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

/**
 * Receives a web service invocation from the Metro transport layer and dispatches it through an interceptor chain to a target service that accepts Document
 * parameter types, avoiding JAXB deserialization.
 */
public class DocumentInvoker extends Invoker {
    private Map<String, InvocationChain> chains = new HashMap<>();
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
        if (args.length != 1) {
            throw new UnsupportedOperationException("Illegal number of arguments");
        }
        if (!(args[0] instanceof SOAPMessage)) {
            throw new UnsupportedOperationException("Expected SOAPMessage but was: " + args[0].getClass());
        }

        // the work context is populated by the current tubeline
        WorkContext workContext = (WorkContext) packet.invocationProperties.get(MetroConstants.WORK_CONTEXT);
        if (workContext == null) {
            // programming error
            throw new AssertionError("Work context not set");
        }
        Message input = MessageCache.getAndResetMessage();
        try {
            SOAPMessage soapMessage = (SOAPMessage) args[0];
            Node node = soapMessage.getSOAPBody().extractContentAsDocument();

            input.setWorkContext(workContext);
            input.setBody(node);
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
        } catch (SOAPException e) {
            throw new InvocationTargetException(e);
        } finally {
            input.reset();
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