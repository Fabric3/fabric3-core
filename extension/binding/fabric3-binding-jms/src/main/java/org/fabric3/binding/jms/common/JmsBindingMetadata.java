/*
 * Fabric3
 * Copyright (c) 2009 Metaform Systems
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
package org.fabric3.binding.jms.common;

import java.util.Collections;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import javax.naming.Context;

import org.fabric3.model.type.ModelObject;

/**
 * Encapsulates binding configuration.
 * <p/>
 * TODO Support for overriding request connection, response connection and operation properties from an activation spec and resource adaptor.
 * <p/>
 * TODO Support exception listener
 *
 * @version $Revision$ $Date$
 */
public class JmsBindingMetadata extends ModelObject {
    private static final long serialVersionUID = 4623441503097788831L;

    private CorrelationScheme correlationScheme = CorrelationScheme.MESSAGE_ID;
    private String initialContextFactory;
    private String jndiUrl;
    private DestinationDefinition destination;
    private ConnectionFactoryDefinition connectionFactory = new ConnectionFactoryDefinition();
    private ResponseDefinition response;
    private HeadersDefinition headers;
    private Map<String, OperationPropertiesDefinition> operationProperties;

    // Fabric3-specific configuration settings
    private CacheLevel cacheLevel;
    private int minReceivers = 1;
    private int maxReceivers = 1;
    private int idleLimit = 1;
    private int transactionTimeout = 30; // in seconds
    private int receiveTimeout = (transactionTimeout / 2) * 1000;  // set the timeout in milliseconds to half that of the trx timeout
    private int maxMessagesToProcess = -1;
    private long recoveryInterval = 5000;   // default 5 seconds
    private boolean durable = false;
    private boolean localDelivery;
    private String clientId;
    private String durableSubscriptionName;

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

    public String getInitialContextFactory() {
        return initialContextFactory;
    }

    public void setInitialContextFactory(String initialContextFactory) {
        this.initialContextFactory = initialContextFactory;
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
        return response.getDestination();
    }

    public ConnectionFactoryDefinition getResponseConnectionFactory() {
        return response.getConnectionFactory();
    }

    public Hashtable<String, String> getEnv() {
        Hashtable<String, String> props = new Hashtable<String, String>();
        if (jndiUrl != null) {
            props.put(Context.PROVIDER_URL, getJndiUrl());
        }
        if (initialContextFactory != null) {
            props.put(Context.INITIAL_CONTEXT_FACTORY, getInitialContextFactory());
        }
        return props;
    }

    public HeadersDefinition getHeaders() {
        return headers;
    }

    public void setHeaders(HeadersDefinition headers) {
        this.headers = headers;
    }

    public Map<String, OperationPropertiesDefinition> getOperationProperties() {
        if (operationProperties == null) {
            return Collections.emptyMap();
        } else {
            return operationProperties;
        }
    }

    public void addOperationProperties(String name, OperationPropertiesDefinition operationProperties) {
        if (this.operationProperties == null) {
            this.operationProperties = new HashMap<String, OperationPropertiesDefinition>();
        }
        this.operationProperties.put(name, operationProperties);
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

    public String getDurableSubscriptionName() {
        return durableSubscriptionName;
    }

    public void setDurableSubscriptionName(String durableSubscriptionName) {
        this.durableSubscriptionName = durableSubscriptionName;
    }
}
