/*
 * Fabric3
 * Copyright (c) 2009-2011 Metaform Systems
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
package org.fabric3.binding.jms.spi.common;

import java.util.HashMap;
import java.util.Map;

import org.fabric3.model.type.ModelObject;

/**
 * Encapsulates binding configuration.
 *
 * @version $Revision$ $Date$
 */
public class JmsBindingMetadata extends ModelObject {
    private static final long serialVersionUID = 4623441503097788831L;

    // headers specified in a JMS URI. URI headers have higher priority than plain headers
    private HeadersDefinition uriHeaders = new HeadersDefinition();
    // headers specified in the binding.jms/headers element
    private HeadersDefinition headers = new HeadersDefinition();

    private String jndiUrl;
    private DestinationDefinition destination;
    private ActivationSpec activationSpec;
    private ConnectionFactoryDefinition connectionFactory = new ConnectionFactoryDefinition();
    private ResponseDefinition response;
    private MessageSelection messageSelection;
    private CorrelationScheme correlationScheme = CorrelationScheme.MESSAGE_ID;
    private Map<String, OperationPropertiesDefinition> operationProperties = new HashMap<String, OperationPropertiesDefinition>();

    // Fabric3-specific configuration settings
    private CacheLevel cacheLevel;
    private int minReceivers = 1;
    private int maxReceivers = 1;
    private int idleLimit = 1;
    private int transactionTimeout = 30; // in seconds
    private int receiveTimeout = (transactionTimeout / 2) * 1000;  // set the timeout in milliseconds to half that of the trx timeout
    private int responseTimeout = 600000;  // set the default response wait to 10 minutes
    private int maxMessagesToProcess = -1;
    private long recoveryInterval = 5000;   // default 5 seconds
    private boolean durable = false;
    private boolean localDelivery;
    private String clientIdSpecifier;

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

    public DestinationDefinition getDestination() {
        return destination;
    }

    public void setDestination(DestinationDefinition destination) {
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

    public DestinationDefinition getResponseDestination() {
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

    public MessageSelection getMessageSelection() {
        return messageSelection;
    }

    public void setMessageSelection(MessageSelection messageSelection) {
        this.messageSelection = messageSelection;
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

    public int getTransactionTimeout() {
        return transactionTimeout;
    }

    public void setTransactionTimeout(int transactionTimeout) {
        this.transactionTimeout = transactionTimeout;
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

    public String getClientIdSpecifier() {
        return clientIdSpecifier;
    }

    public void setClientIdSpecifier(String specifier) {
        this.clientIdSpecifier = specifier;
    }

    public JmsBindingMetadata snapshot() {
        JmsBindingMetadata copy = new JmsBindingMetadata();
        copy.correlationScheme = this.correlationScheme;
        copy.jndiUrl = this.jndiUrl;
        copy.destination = this.destination;
        copy.messageSelection = this.messageSelection;
        copy.connectionFactory.setCreate(this.connectionFactory.getCreate());
        copy.connectionFactory.setName(this.connectionFactory.getName());
        copy.connectionFactory.setTemplateName(this.connectionFactory.getTemplateName());
        copy.connectionFactory.getProperties().putAll(this.connectionFactory.getProperties());
        copy.response = this.response;

        copy.headers.setDeliveryMode(this.headers.getDeliveryMode());
        copy.headers.getProperties().putAll(this.headers.getProperties());
        copy.headers.setPriority(this.headers.getPriority());
        copy.headers.setTimeToLive(this.headers.getTimeToLive());
        copy.headers.setJmsType(this.headers.getJmsType());

        copy.uriHeaders.setDeliveryMode(this.uriHeaders.getDeliveryMode());
        copy.uriHeaders.getProperties().putAll(this.uriHeaders.getProperties());
        copy.uriHeaders.setPriority(this.uriHeaders.getPriority());
        copy.uriHeaders.setTimeToLive(this.uriHeaders.getTimeToLive());
        copy.uriHeaders.setJmsType(this.uriHeaders.getJmsType());

        if (this.operationProperties != null) {
            copy.operationProperties = new HashMap<String, OperationPropertiesDefinition>();
            copy.operationProperties.putAll(this.operationProperties);
        }

        copy.cacheLevel = this.cacheLevel;
        copy.minReceivers = this.minReceivers;
        copy.maxReceivers = this.maxReceivers;
        copy.idleLimit = this.idleLimit;
        copy.transactionTimeout = this.transactionTimeout;
        copy.receiveTimeout = this.receiveTimeout;
        copy.responseTimeout = this.responseTimeout;
        copy.maxMessagesToProcess = this.maxMessagesToProcess;
        copy.recoveryInterval = this.recoveryInterval;
        copy.durable = this.durable;
        copy.localDelivery = this.localDelivery;
        copy.clientIdSpecifier = this.clientIdSpecifier;
        copy.activationSpec = this.activationSpec;
        return copy;
    }

}
