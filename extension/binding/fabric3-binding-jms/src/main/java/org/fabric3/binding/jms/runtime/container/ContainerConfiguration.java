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
package org.fabric3.binding.jms.runtime.container;

import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.ExceptionListener;
import javax.jms.MessageListener;
import java.net.URI;

import org.fabric3.api.binding.jms.model.DestinationType;
import org.fabric3.binding.jms.spi.provision.SessionType;

/**
 * Configuration for registering a MessageListener with a {@link MessageContainerManager}.
 */
public class ContainerConfiguration {
    private URI uri;
    private String messageSelector;

    private MessageListener messageListener;
    private ExceptionListener exceptionListener;
    private SessionType type = SessionType.AUTO_ACKNOWLEDGE;
    private Destination destination;
    private ConnectionFactory factory;
    private int cacheLevel;
    private int deliveryMode;

    private int minReceivers = 1;
    private int maxReceivers = 1;
    private int idleLimit = 1;

    private int receiveTimeout = -1;

    private int maxMessagesToProcess = -1;
    private long recoveryInterval = 5000;   // default 5 seconds
    private boolean durable;
    private boolean localDelivery;
    private DestinationType destinationType;
    private String subscriptionId;

    public URI getUri() {
        return uri;
    }

    public void setUri(URI uri) {
        this.uri = uri;
    }

    public String getMessageSelector() {
        return messageSelector;
    }

    public void setMessageSelector(String messageSelector) {
        this.messageSelector = messageSelector;
    }

    public MessageListener getMessageListener() {
        return messageListener;
    }

    public void setMessageListener(MessageListener messageListener) {
        this.messageListener = messageListener;
    }

    public ExceptionListener getExceptionListener() {
        return exceptionListener;
    }

    public void setExceptionListener(ExceptionListener exceptionListener) {
        this.exceptionListener = exceptionListener;
    }

    public SessionType getSessionType() {
        return type;
    }

    public void setSessionType(SessionType type) {
        this.type = type;
    }

    public Destination getDestination() {
        return destination;
    }

    public void setDestinationType(DestinationType type) {
        this.destinationType = type;
    }

    public DestinationType getDestinationType() {
        return destinationType;
    }

    public void setDestination(Destination destination) {
        this.destination = destination;
    }

    public ConnectionFactory getFactory() {
        return factory;
    }

    public void setFactory(ConnectionFactory factory) {
        this.factory = factory;
    }

    public int getCacheLevel() {
        return cacheLevel;
    }

    public void setCacheLevel(int cacheLevel) {
        this.cacheLevel = cacheLevel;
    }

    /**
     * Returns the JMS delivery mode as defined by {@link javax.jms.DeliveryMode}.
     *
     * @return the delivery mode.
     */
    public int getDeliveryMode() {
        return deliveryMode;
    }

    /**
     * Sets the JMS delivery mode as defined by {@link javax.jms.DeliveryMode}.
     *
     * @param deliveryMode the delivery mode.
     */
    public void setDeliveryMode(int deliveryMode) {
        this.deliveryMode = deliveryMode;
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

    public void setSubscriptionId(String subscriptionId) {
        this.subscriptionId = subscriptionId;
    }

    public String getSubscriptionId() {
        return subscriptionId;
    }
}
