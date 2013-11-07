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
 *
 * ----------------------------------------------------
 *
 * Portions originally based on Apache Tuscany 2007
 * licensed under the Apache 2.0 license.
 *
 */
package org.fabric3.binding.jms.runtime.wire;

import javax.jms.BytesMessage;
import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.fabric3.binding.jms.runtime.common.JmsRuntimeConstants;
import org.fabric3.binding.jms.runtime.common.ListenerMonitor;
import org.fabric3.api.binding.jms.model.CorrelationScheme;
import org.fabric3.api.binding.jms.model.TransactionType;
import org.fabric3.binding.jms.spi.provision.OperationPayloadTypes;
import org.fabric3.binding.jms.spi.provision.PayloadType;
import org.fabric3.spi.container.binding.handler.BindingHandler;
import org.fabric3.spi.container.invocation.CallbackReference;
import org.fabric3.spi.container.invocation.CallbackReferenceSerializer;
import org.fabric3.spi.container.invocation.MessageCache;
import org.fabric3.spi.container.invocation.WorkContext;
import org.fabric3.spi.container.invocation.WorkContextCache;
import org.fabric3.spi.container.wire.Interceptor;
import org.fabric3.spi.xml.XMLFactory;

/**
 * Listens for requests sent to a destination and dispatches them to a service, returning a response to the response destination.
 */
public class ServiceListener implements MessageListener {
    private WireHolder wireHolder;
    private Map<String, InvocationChainHolder> invocationChainMap;
    private InvocationChainHolder onMessageHolder;
    private Destination defaultResponseDestination;
    private ConnectionFactory responseFactory;
    private TransactionType transactionType;
    private ClassLoader classLoader;
    private ListenerMonitor monitor;
    private XMLFactory xmlFactory;
    private XMLInputFactory xmlInputFactory;
    private List<BindingHandler<Message>> handlers;

    public ServiceListener(WireHolder wireHolder,
                           Destination defaultResponseDestination,
                           ConnectionFactory responseFactory,
                           TransactionType transactionType,
                           ClassLoader classLoader,
                           XMLFactory xmlFactory,
                           List<BindingHandler<Message>> handlers,
                           ListenerMonitor monitor) {
        this.wireHolder = wireHolder;
        this.defaultResponseDestination = defaultResponseDestination;
        this.responseFactory = responseFactory;
        this.transactionType = transactionType;
        this.classLoader = classLoader;
        this.xmlFactory = xmlFactory;
        this.handlers = handlers;
        this.monitor = monitor;
        invocationChainMap = new HashMap<String, InvocationChainHolder>();
        for (InvocationChainHolder chainHolder : wireHolder.getInvocationChains()) {
            String name = chainHolder.getChain().getPhysicalOperation().getName();
            if ("onMessage".equals(name)) {
                onMessageHolder = chainHolder;
            }
            invocationChainMap.put(name, chainHolder);
        }
    }

    public void onMessage(Message request) {
        ClassLoader oldCl = Thread.currentThread().getContextClassLoader();
        try {
            // set the TCCL to the target service classloader
            Thread.currentThread().setContextClassLoader(classLoader);
            InvocationChainHolder holder = getHolder(request);
            Interceptor interceptor = holder.getChain().getHeadInterceptor();
            boolean oneWay = holder.getChain().getPhysicalOperation().isOneWay();
            OperationPayloadTypes payloadTypes = holder.getPayloadTypes();
            PayloadType inputType = payloadTypes.getInputType();
            Object payload = MessageHelper.getPayload(request, inputType);

            switch (inputType) {

                case OBJECT:
                    if (payload != null && !payload.getClass().isArray()) {
                        payload = new Object[]{payload};
                    }
                    invoke(request, interceptor, payload, payloadTypes, oneWay, transactionType);
                    break;
                case XML:
                    invoke(request, interceptor, payload, payloadTypes, oneWay, transactionType);
                    break;
                case TEXT:
                    // non-encoded text
                    payload = new Object[]{payload};
                    invoke(request, interceptor, payload, payloadTypes, oneWay, transactionType);
                    break;
                case STREAM:
                    throw new UnsupportedOperationException();
                default:
                    payload = new Object[]{payload};
                    invoke(request, interceptor, payload, payloadTypes, oneWay, transactionType);
                    break;
            }
        } catch (JMSException e) {
            // TODO This could be a temporary error and should be sent to a dead letter queue. For now, just log the error.
            monitor.redeliveryError(e);
        } catch (JmsBadMessageException e) {
            // The message is invalid and cannot be processed. Log the error.
            monitor.redeliveryError(e);
        } finally {
            Thread.currentThread().setContextClassLoader(oldCl);
        }
    }

    private void invoke(Message request,
                        Interceptor interceptor,
                        Object payload,
                        OperationPayloadTypes payloadTypes,
                        boolean oneWay,
                        TransactionType transactionType) throws JMSException, JmsBadMessageException {
        WorkContext workContext = setWorkContext(request);
        if (PayloadType.XML == payloadTypes.getInputType()) {
            payload = new Object[]{payload};
        }
        org.fabric3.spi.container.invocation.Message inMessage = MessageCache.getAndResetMessage();
        inMessage.setWorkContext(workContext);
        inMessage.setBody(payload);

        applyHandlers(request, inMessage);

        org.fabric3.spi.container.invocation.Message outMessage = interceptor.invoke(inMessage);

        if (oneWay) {
            // one-way message, return without waiting for a response
            inMessage.reset();
            return;
        }
        Connection connection = null;
        Session responseSession = null;
        try {
            connection = responseFactory.createConnection();
            if (TransactionType.GLOBAL == transactionType) {
                responseSession = connection.createSession(true, Session.SESSION_TRANSACTED);
            } else {
                responseSession = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
            }
            Object responsePayload = outMessage.getBody();
            PayloadType returnType;
            if (outMessage.isFault()) {
                returnType = payloadTypes.getFaultType();
            } else {
                returnType = payloadTypes.getOutputType();
            }
            Message response = createMessage(responsePayload, responseSession, returnType);
            sendResponse(request, responseSession, outMessage, response);
        } finally {
            inMessage.reset();
            workContext.reset();
            if (responseSession != null) {
                responseSession.close();
            }
            if (connection != null) {
                connection.close();
            }
        }
    }

    private void sendResponse(Message request, Session responseSession, org.fabric3.spi.container.invocation.Message outMessage, Message response)
            throws JMSException, JmsBadMessageException {
        CorrelationScheme correlationScheme = wireHolder.getCorrelationScheme();
        switch (correlationScheme) {
            case CORRELATION_ID: {
                response.setJMSCorrelationID(request.getJMSCorrelationID());
                break;
            }
            case MESSAGE_ID: {
                response.setJMSCorrelationID(request.getJMSMessageID());
                break;
            }
        }
        if (outMessage.isFault()) {
            response.setBooleanProperty(JmsRuntimeConstants.FAULT_HEADER, true);
        }
        MessageProducer producer;
        if (request.getJMSReplyTo() != null) {
            // if a reply to destination is set, use it
            producer = responseSession.createProducer(request.getJMSReplyTo());
        } else {
            if (defaultResponseDestination == null) {
                throw new JmsBadMessageException("JMSReplyTo must be set as no response destination was configured on the service");
            }
            producer = responseSession.createProducer(defaultResponseDestination);
        }
        producer.send(response);
    }

    private Message createMessage(Object payload, Session session, PayloadType payloadType) throws JMSException {
        switch (payloadType) {
            case STREAM:
                throw new UnsupportedOperationException("Stream message not yet supported");
            case XML:
            case TEXT:
                if (payload != null && !(payload instanceof String)) {
                    // this should not happen
                    throw new IllegalArgumentException("Response payload is not a string: " + payload);
                }
                return session.createTextMessage((String) payload);
            case OBJECT:
                if (payload != null && !(payload instanceof Serializable)) {
                    // this should not happen
                    throw new IllegalArgumentException("Response payload is not serializable: " + payload);
                }
                return session.createObjectMessage((Serializable) payload);
            default:
                return MessageHelper.createBytesMessage(session, payload, payloadType);
        }
    }

    private InvocationChainHolder getHolder(Message message) throws JmsBadMessageException, JMSException {
        List<InvocationChainHolder> chainHolders = wireHolder.getInvocationChains();
        if (chainHolders.size() == 1) {
            return chainHolders.get(0);
        } else if (onMessageHolder != null) {
            return onMessageHolder;
        }

        String opName = message.getStringProperty(JmsRuntimeConstants.OPERATION_HEADER);
        if (opName != null) {
            InvocationChainHolder chainHolder = invocationChainMap.get(opName);
            if (chainHolder == null) {
                throw new JmsBadMessageException("Unable to match operation on the service contract: " + opName);
            }
            return chainHolder;
        } else {
            if (message instanceof TextMessage) {
                TextMessage textMessage = (TextMessage) message;
                String payload = textMessage.getText();
                return getHolderBasedOnElementName(payload.getBytes());

            }
            if (message instanceof BytesMessage) {
                BytesMessage bytesMessage = (BytesMessage) message;
                byte[] payload = new byte[(int) bytesMessage.getBodyLength()];
                bytesMessage.readBytes(payload);
                return getHolderBasedOnElementName(payload);

            }
            throw new JmsBadMessageException("Unable to match operation on the service contract");
        }
    }

    private InvocationChainHolder getHolderBasedOnElementName(byte[] payload) throws JmsBadMessageException {
        if (xmlInputFactory == null) {
            xmlInputFactory = xmlFactory.newInputFactoryInstance();
        }
        try {
            XMLStreamReader reader = xmlInputFactory.createXMLStreamReader(new ByteArrayInputStream(payload));
            reader.nextTag();
            String name = reader.getName().getLocalPart();
            InvocationChainHolder chainHolder = invocationChainMap.get(name);
            if (chainHolder == null) {
                throw new JmsBadMessageException("Unable to match operation on for name: " + name);
            }
            return chainHolder;
        } catch (XMLStreamException e) {
            throw new JmsBadMessageException("Unable to process message", e);
        }
    }

    /**
     * Sets the WorkContext for the request.
     *
     *
     * @param request     the message received from the JMS transport
     * @return the work context
     * @throws JmsBadMessageException if an error is encountered setting the work context
     */
    @SuppressWarnings({"unchecked"})
    private WorkContext setWorkContext(Message request) throws JmsBadMessageException {
        try {
            WorkContext workContext = WorkContextCache.getAndResetThreadWorkContext();
            String encoded = request.getStringProperty(JmsRuntimeConstants.CONTEXT_HEADER);
            if (encoded == null) {
                return workContext;
            }
            List<CallbackReference> stack = CallbackReferenceSerializer.deserialize(encoded);
            workContext.addCallbackReferences(stack);
            return workContext;
        } catch (JMSException ex) {
            throw new JmsBadMessageException("Error deserializing callback references", ex);
        } catch (IOException ex) {
            throw new JmsBadMessageException("Error deserializing callback references", ex);
        }
    }

    private void applyHandlers(Message request, org.fabric3.spi.container.invocation.Message inMessage) {
        if (handlers != null) {
            for (BindingHandler<Message> handler : handlers) {
                handler.handleInbound(request, inMessage);
            }
        }
    }

}