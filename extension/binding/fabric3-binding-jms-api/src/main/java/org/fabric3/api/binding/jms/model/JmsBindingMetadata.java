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
package org.fabric3.api.binding.jms.model;

import java.util.HashMap;
import java.util.Map;

import org.fabric3.api.model.type.ModelObject;

/**
 * Encapsulates binding configuration.
 */
public class JmsBindingMetadata extends ModelObject {
    // headers specified in a JMS URI. URI headers have higher priority than plain headers
    private HeadersDefinition uriHeaders = new HeadersDefinition();
    // headers specified in the binding.jms/headers element
    private HeadersDefinition headers = new HeadersDefinition();

    private String jndiUrl;
    private Destination destination;
    private ActivationSpec activationSpec;
    private ConnectionFactoryDefinition connectionFactory = new ConnectionFactoryDefinition();
    private ResponseDefinition response;
    private MessageSelection uriMessageSelection;
    private MessageSelection messageSelection;
    private CorrelationScheme correlationScheme = CorrelationScheme.MESSAGE_ID;
    private Map<String, OperationPropertiesDefinition> operationProperties = new HashMap<>();

    // Fabric3-specific configuration settings
    private CacheLevel cacheLevel = CacheLevel.ADMINISTERED_OBJECTS;
    private int minReceivers = 1;
    private int maxReceivers = 1;
    private int idleLimit = 1;
    private int receiveTimeout = 15000;  // set the timeout in milliseconds
    private int responseTimeout = 600000;  // set the default response wait to 10 minutes
    private int maxMessagesToProcess = -1;
    private long recoveryInterval = 5000;   // default 5 seconds
    private boolean durable = false;
    private boolean localDelivery;
    private String subscriptionId;
    private boolean clientAcknowledge;

    public ConnectionFactoryDefinition getConnectionFactory() {
        return connectionFactory;
    }

    public void setConnectionFactory(ConnectionFactoryDefinition connectionFactory) {
        this.connectionFactory = connectionFactory;
    }

    public CorrelationScheme getCorrelationScheme() {
        return correlationScheme;
    }

    public void setCorrelationScheme(CorrelationScheme correlationScheme) {
        this.correlationScheme = correlationScheme;
    }

    public Destination getDestination() {
        return destination;
    }

    public void setDestination(Destination destination) {
        this.destination = destination;
    }

    public String getJndiUrl() {
        return jndiUrl;
    }

    public void setJndiUrl(String jndiUrl) {
        this.jndiUrl = jndiUrl;
    }

    public ResponseDefinition getResponse() {
        return response;
    }

    public void setResponse(ResponseDefinition response) {
        this.response = response;
    }

    public Destination getResponseDestination() {
        if (response == null) {
            return null;
        }
        return response.getDestination();
    }

    public ConnectionFactoryDefinition getResponseConnectionFactory() {
        if (response == null) {
            return null;
        }
        return response.getConnectionFactory();
    }

    public HeadersDefinition getHeaders() {
        return headers;
    }

    public HeadersDefinition getUriHeaders() {
        return uriHeaders;
    }

    public ActivationSpec getActivationSpec() {
        return activationSpec;
    }

    public void setActivationSpec(ActivationSpec activationSpec) {
        this.activationSpec = activationSpec;
    }

    public MessageSelection getUriMessageSelection() {
        return uriMessageSelection;
    }

    public void setUriMessageSelection(MessageSelection selection) {
        this.uriMessageSelection = selection;
    }

    public MessageSelection getMessageSelection() {
        return messageSelection;
    }

    public void setMessageSelection(MessageSelection selection) {
        this.messageSelection = selection;
    }

    public Map<String, OperationPropertiesDefinition> getOperationProperties() {
        return operationProperties;
    }

    public void addOperationProperties(String name, OperationPropertiesDefinition definition) {
        operationProperties.put(name, definition);
    }

    public boolean isResponse() {
        return response != null;
    }

    public CacheLevel getCacheLevel() {
        return cacheLevel;
    }

    public void setCacheLevel(CacheLevel cacheLevel) {
        this.cacheLevel = cacheLevel;
    }

    public int getMinReceivers() {
        return minReceivers;
    }

    public void setMinReceivers(int minReceivers) {
        this.minReceivers = minReceivers;
    }

    public int getMaxReceivers() {
        return maxReceivers;
    }

    public void setMaxReceivers(int maxReceivers) {
        this.maxReceivers = maxReceivers;
    }

    public int getIdleLimit() {
        return idleLimit;
    }

    public void setIdleLimit(int idleLimit) {
        this.idleLimit = idleLimit;
    }

    public int getReceiveTimeout() {
        return receiveTimeout;
    }

    public void setReceiveTimeout(int receiveTimeout) {
        this.receiveTimeout = receiveTimeout;
    }

    public int getResponseTimeout() {
        return responseTimeout;
    }

    public void setResponseTimeout(int responseTimeout) {
        this.responseTimeout = responseTimeout;
    }

    public int getMaxMessagesToProcess() {
        return maxMessagesToProcess;
    }

    public void setMaxMessagesToProcess(int maxMessagesToProcess) {
        this.maxMessagesToProcess = maxMessagesToProcess;
    }

    public long getRecoveryInterval() {
        return recoveryInterval;
    }

    public void setRecoveryInterval(long recoveryInterval) {
        this.recoveryInterval = recoveryInterval;
    }

    public boolean isDurable() {
        return durable;
    }

    public void setDurable(boolean durable) {
        this.durable = durable;
    }

    public boolean isLocalDelivery() {
        return localDelivery;
    }

    public void setLocalDelivery(boolean localDelivery) {
        this.localDelivery = localDelivery;
    }

    public String getSubscriptionId() {
        return subscriptionId;
    }

    public void setSubscriptionId(String id) {
        this.subscriptionId = id;
    }

    public boolean isClientAcknowledge() {
        return clientAcknowledge;
    }

    public void setClientAcknowledge(boolean clientAcknowledge) {
        this.clientAcknowledge = clientAcknowledge;
    }

    public JmsBindingMetadata snapshot() {
        JmsBindingMetadata copy = new JmsBindingMetadata();
        copy.correlationScheme = this.correlationScheme;
        copy.jndiUrl = this.jndiUrl;
        copy.destination = this.destination;
        copy.messageSelection = this.messageSelection;
        copy.connectionFactory.setCreate(this.connectionFactory.getCreate());
        copy.connectionFactory.setName(this.connectionFactory.getName());
        copy.connectionFactory.addProperties(this.connectionFactory.getProperties());
        copy.response = this.response;

        copy.headers.setDeliveryMode(this.headers.getDeliveryMode());
        copy.headers.addProperties(this.headers.getProperties());
        copy.headers.setPriority(this.headers.getPriority());
        copy.headers.setTimeToLive(this.headers.getTimeToLive());
        copy.headers.setJmsType(this.headers.getJmsType());

        copy.uriHeaders.setDeliveryMode(this.uriHeaders.getDeliveryMode());
        copy.uriHeaders.addProperties(this.uriHeaders.getProperties());
        copy.uriHeaders.setPriority(this.uriHeaders.getPriority());
        copy.uriHeaders.setTimeToLive(this.uriHeaders.getTimeToLive());
        copy.uriHeaders.setJmsType(this.uriHeaders.getJmsType());

        if (this.operationProperties != null) {
            copy.operationProperties = new HashMap<>();
            copy.operationProperties.putAll(this.operationProperties);
        }

        copy.cacheLevel = this.cacheLevel;
        copy.minReceivers = this.minReceivers;
        copy.maxReceivers = this.maxReceivers;
        copy.idleLimit = this.idleLimit;
        copy.receiveTimeout = this.receiveTimeout;
        copy.responseTimeout = this.responseTimeout;
        copy.maxMessagesToProcess = this.maxMessagesToProcess;
        copy.recoveryInterval = this.recoveryInterval;
        copy.durable = this.durable;
        copy.localDelivery = this.localDelivery;
        copy.subscriptionId = this.subscriptionId;
        copy.activationSpec = this.activationSpec;
        copy.clientAcknowledge = this.clientAcknowledge;
        return copy;
    }

}
