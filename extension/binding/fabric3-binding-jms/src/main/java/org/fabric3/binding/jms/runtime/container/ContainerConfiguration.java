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
package org.fabric3.binding.jms.runtime.container;

import java.net.URI;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.ExceptionListener;
import javax.jms.MessageListener;

import org.fabric3.binding.jms.spi.common.DestinationType;
import org.fabric3.binding.jms.spi.common.TransactionType;

/**
 * Configuration for registering a MessageListener with a {@link MessageContainerManager}.
 *
 * @version $Rev$ $Date$
 */
public class ContainerConfiguration {
    private URI uri;
    private String messageSelector;

    private MessageListener messageListener;
    private ExceptionListener exceptionListener;
    private TransactionType type = TransactionType.NONE;
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
    private String clientId;
    private DestinationType destinationType;

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

    public TransactionType getType() {
        return type;
    }

    public void setType(TransactionType type) {
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

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }
}
