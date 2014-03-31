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

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.DeliveryMode;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.transaction.HeuristicMixedException;
import javax.transaction.HeuristicRollbackException;
import javax.transaction.NotSupportedException;
import javax.transaction.RollbackException;
import javax.transaction.Status;
import javax.transaction.SystemException;
import javax.transaction.Transaction;
import javax.transaction.TransactionManager;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.fabric3.binding.jms.runtime.common.JmsHelper;
import org.fabric3.binding.jms.runtime.common.JmsRuntimeConstants;
import org.fabric3.api.binding.jms.model.CorrelationScheme;
import org.fabric3.binding.jms.spi.provision.SessionType;
import org.fabric3.binding.jms.spi.provision.OperationPayloadTypes;
import org.fabric3.spi.container.binding.handler.BindingHandler;
import org.fabric3.spi.container.invocation.CallbackReference;
import org.fabric3.spi.container.invocation.CallbackReferenceSerializer;
import org.fabric3.spi.container.invocation.Message;
import org.fabric3.spi.container.invocation.MessageImpl;
import org.fabric3.spi.container.wire.Interceptor;
import org.oasisopen.sca.ServiceRuntimeException;
import org.oasisopen.sca.ServiceUnavailableException;

/**
 * Dispatches an invocation to a destination.
 */
public class JmsInterceptor implements Interceptor {
    private static final String SCA_CALLBACK_DESTINATION = "scaCallbackDestination";
    private static final Message ONE_WAY_RESPONSE = new MessageImpl();

    private Interceptor next;
    private String methodName;
    private OperationPayloadTypes payloadTypes;
    private Destination destination;
    private Destination callbackDestination;
    private String callbackUri;
    private ConnectionFactory connectionFactory;
    private CorrelationScheme correlationScheme;
    private ResponseListener responseListener;
    private ClassLoader classLoader;
    private boolean oneWay;
    private SessionType sessionType;
    private TransactionManager tm;
    private long responseTimeout;
    private boolean persistent;
    private int deliveryMode;
    private long timeToLive;
    private String jmsType;
    private int priority;
    private Map<String, String> properties;
    private List<BindingHandler<javax.jms.Message>> handlers;

    /**
     * Constructor.
     *
     * @param configuration the configuration template
     * @param handlers      the binding handlers, may be null
     */
    public JmsInterceptor(InterceptorConfiguration configuration, List<BindingHandler<javax.jms.Message>> handlers) {
        WireConfiguration wireConfig = configuration.getWireConfiguration();
        this.destination = wireConfig.getRequestDestination();
        this.callbackDestination = wireConfig.getCallbackDestination();
        this.callbackUri = wireConfig.getCallbackUri();
        this.connectionFactory = wireConfig.getRequestConnectionFactory();
        this.correlationScheme = wireConfig.getCorrelationScheme();
        this.classLoader = wireConfig.getClassloader();
        this.responseListener = wireConfig.getResponseListener();
        this.tm = wireConfig.getTransactionManager();
        this.sessionType = wireConfig.getSessionType();
        this.responseTimeout = wireConfig.getResponseTimeout();
        this.persistent = wireConfig.isPersistent();
        this.oneWay = configuration.isOneWay();
        this.methodName = configuration.getOperationName();
        this.payloadTypes = configuration.getPayloadTypes();
        this.deliveryMode = configuration.getDeliveryMode();
        this.timeToLive = configuration.getTimeToLive();
        this.jmsType = configuration.getJmsType();
        this.priority = configuration.getPriority();
        this.properties = configuration.getProperties();
        this.handlers = handlers;

    }

    public Message invoke(Message message) {
        Connection connection = null;
        Session session = null;
        ClassLoader oldCl = Thread.currentThread().getContextClassLoader();
        try {
            // set the context classloader to the one that loaded the connection factory implementation.
            // this is required by some JMS providers
            Thread.currentThread().setContextClassLoader(connectionFactory.getClass().getClassLoader());
            connection = connectionFactory.createConnection();
            connection.start();
            int status = tm.getStatus();
            Transaction suspended = null;
            boolean begun = false;
            if (Status.STATUS_NO_TRANSACTION == status && SessionType.GLOBAL_TRANSACTED == sessionType) {
                tm.begin();
                begun = true;
            } else if ((Status.STATUS_ACTIVE == status && SessionType.AUTO_ACKNOWLEDGE == sessionType)) {
                suspended = tm.suspend();
            }

            if (SessionType.GLOBAL_TRANSACTED == sessionType) {
                session = connection.createSession(true, Session.SESSION_TRANSACTED);
            } else {
                session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
            }

            MessageProducer producer = session.createProducer(destination);

            if (!persistent || DeliveryMode.NON_PERSISTENT == deliveryMode) {
                producer.setDeliveryMode(DeliveryMode.NON_PERSISTENT);
            }

            if (timeToLive >= 0) {
                producer.setTimeToLive(timeToLive);
            }

            javax.jms.Message jmsMessage = createMessage(message, session);
            setHeaders(jmsMessage);
            String correlationId = null;
            if (correlationScheme == CorrelationScheme.CORRELATION_ID) {
                correlationId = UUID.randomUUID().toString();
                jmsMessage.setJMSCorrelationID(correlationId);
            }

            // apply any handlers
            applyHandlers(message, jmsMessage);

            // enqueue the message            
            producer.send(jmsMessage);

            // if the correlation scheme is configured to use the message id, the correlation id must set after the message is sent since the
            // JMS provider may not have set it
            if (correlationScheme == CorrelationScheme.MESSAGE_ID) {
                correlationId = jmsMessage.getJMSMessageID();
            }

            if (!oneWay) {
                // request-response, block on response
                Message resp = receive(correlationId, session, message);
                if (begun) {
                    tm.commit();
                }
                if (suspended != null) {
                    tm.resume(suspended);
                }
                return resp;
            } else {
                if (begun) {
                    tm.commit();
                }
                if (suspended != null) {
                    tm.resume(suspended);
                }
                // one-way invocation, return an empty message
                return ONE_WAY_RESPONSE;
            }

        } catch (JMSException | JmsBadMessageException e) {
            throw new ServiceRuntimeException("Unable to receive response", e);
        } catch (IOException e) {
            throw new ServiceRuntimeException("Error serializing callback references", e);
        } catch (SystemException | RollbackException | HeuristicRollbackException | HeuristicMixedException | NotSupportedException e) {
            throw new ServiceRuntimeException(e);
        } finally {
            JmsHelper.closeQuietly(session);
            JmsHelper.closeQuietly(connection);
            Thread.currentThread().setContextClassLoader(oldCl);
        }
    }

    public Interceptor getNext() {
        return next;
    }

    public void setNext(Interceptor next) {
        this.next = next;
    }

    /**
     * Blocks waiting for a response message from the service provider.
     *
     * @param correlationId the id for correlating the response message
     * @param session       the session to perform the receive in
     * @param message       the current message
     * @return the response message
     * @throws JMSException           if an error occurs in the JMS provider waiting for or processing the response
     * @throws JmsBadMessageException if an unrecoverable error such as a bad message type occurs waiting for or processing the response
     */
    private Message receive(String correlationId, Session session, Message message) throws JMSException, JmsBadMessageException {
        javax.jms.Message resultMessage = responseListener.receive(correlationId, session, responseTimeout);
        if (resultMessage == null) {
            throw new ServiceUnavailableException("Timeout waiting for response to message: " + correlationId);
        }

        ClassLoader old = Thread.currentThread().getContextClassLoader();
        try {
            // set the context classloader to the application classloader so message types can be deserialized properly
            // (message types defined in an application contribution will not be visible to the JMS extension classloader
            Thread.currentThread().setContextClassLoader(classLoader);
            if (resultMessage.getBooleanProperty(JmsRuntimeConstants.FAULT_HEADER)) {
                Object payload = MessageHelper.getPayload(resultMessage, payloadTypes.getFaultType());
                message.setBodyWithFault(payload);
            } else {
                Object payload = MessageHelper.getPayload(resultMessage, payloadTypes.getOutputType());
                message.setBody(payload);
            }
        } finally {
            Thread.currentThread().setContextClassLoader(old);
        }
        return message;
    }

    /**
     * Creates a JMS message to be dispatched to the service endpoint from the invocation message.
     *
     * @param message the invocation message
     * @param session the session that will be used for dispatching the message
     * @return the JMS message
     * @throws JMSException if an error occurs creating the JMS message
     * @throws IOException  if an error occurs writing information to the JMS message
     */
    private javax.jms.Message createMessage(Message message, Session session) throws JMSException, IOException {
        Object[] payload = (Object[]) message.getBody();
        javax.jms.Message jmsMessage;
        switch (payloadTypes.getInputType()) {
            case OBJECT:
                jmsMessage = session.createObjectMessage(payload);
                setRoutingHeaders(message, jmsMessage);
                return jmsMessage;
            case STREAM:
                throw new UnsupportedOperationException("Not yet implemented");
            case TEXT:
                if (payload.length != 1) {
                    throw new UnsupportedOperationException("Only single parameter operations are supported");
                }
                jmsMessage = session.createTextMessage((String) payload[0]);
                setRoutingHeaders(message, jmsMessage);
                return jmsMessage;
            default:
                if (payload.length != 1) {
                    throw new AssertionError("Bytes messages must have a single parameter");
                }
                jmsMessage = MessageHelper.createBytesMessage(session, payload[0], payloadTypes.getInputType());
                setRoutingHeaders(message, jmsMessage);
                return jmsMessage;
        }
    }

    /**
     * Sets any configured JMS headers on the given message.
     *
     * @param message the message
     * @throws JMSException if there is an error setting a header
     */
    private void setHeaders(javax.jms.Message message) throws JMSException {
        // add the operation name being invoked
        message.setStringProperty(JmsRuntimeConstants.OPERATION_HEADER, methodName);
        if (!oneWay) {
            message.setJMSReplyTo(responseListener.getDestination());
        }
        if (priority >= 0) {
            message.setJMSPriority(priority);
        }
        if (jmsType != null) {
            message.setJMSType(jmsType);
        }
        if (!properties.isEmpty()) {
            for (Map.Entry<String, String> entry : properties.entrySet()) {
                message.setStringProperty(entry.getKey(), entry.getValue());
            }
        }
        if (callbackDestination != null) {
            if (oneWay) {
                message.setJMSReplyTo(callbackDestination);
            } else {
                message.setStringProperty(SCA_CALLBACK_DESTINATION, callbackUri);
            }
        }
    }

    /**
     * Adds F3-specific routing headers to a message.
     *
     * @param message    the invocation message
     * @param jmsMessage the JMS message to be dispatched
     * @throws JMSException if an error occurs setting the headers
     * @throws IOException  if an error occurs serializing the routing information
     */
    private void setRoutingHeaders(Message message, javax.jms.Message jmsMessage) throws JMSException, IOException {
        List<CallbackReference> stack = message.getWorkContext().getCallbackReferences();
        if (stack == null || stack.isEmpty()) {
            return;
        }
        jmsMessage.setObjectProperty(JmsRuntimeConstants.CONTEXT_HEADER, CallbackReferenceSerializer.serializeToString(stack));
    }

    private void applyHandlers(Message message, javax.jms.Message jmsMessage) {
        if (handlers != null) {
            for (BindingHandler<javax.jms.Message> handler : handlers) {
                handler.handleOutbound(message, jmsMessage);
            }
        }
    }

}
