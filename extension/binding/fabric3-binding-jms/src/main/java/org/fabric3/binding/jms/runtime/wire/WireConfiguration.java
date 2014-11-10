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
 * Portions originally based on Apache Tuscany 2007
 * licensed under the Apache 2.0 license.
 */
package org.fabric3.binding.jms.runtime.wire;

import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.transaction.TransactionManager;

import org.fabric3.api.binding.jms.model.CorrelationScheme;
import org.fabric3.binding.jms.spi.provision.SessionType;

/**
 * Template for configuring non-operation specific objects for a JmsInterceptor.
 */
public class WireConfiguration {
    private CorrelationScheme correlationScheme;
    private ConnectionFactory requestConnectionFactory;
    private Destination requestDestination;
    private Destination callbackDestination;
    private ClassLoader classloader;
    private ResponseListener responseListener;
    private SessionType sessionType;
    private TransactionManager tm;
    private long responseTimeout;
    boolean persistent = true;
    private String callbackUri;

    public CorrelationScheme getCorrelationScheme() {
        return correlationScheme;
    }

    public void setCorrelationScheme(CorrelationScheme correlationScheme) {
        this.correlationScheme = correlationScheme;
    }

    public Destination getRequestDestination() {
        return requestDestination;
    }

    public void setRequestDestination(Destination requestDestination) {
        this.requestDestination = requestDestination;
    }

    public Destination getCallbackDestination() {
        return callbackDestination;
    }

    public void setCallbackDestination(Destination callbackDestination) {
        this.callbackDestination = callbackDestination;
    }

    public String getCallbackUri() {
        return callbackUri;
    }

    public void setCallbackUri(String callbackUri) {
        this.callbackUri = callbackUri;
    }

    public ConnectionFactory getRequestConnectionFactory() {
        return requestConnectionFactory;
    }

    public void setRequestConnectionFactory(ConnectionFactory requestConnectionFactory) {
        this.requestConnectionFactory = requestConnectionFactory;
    }

    public ClassLoader getClassloader() {
        return classloader;
    }

    public void setClassloader(ClassLoader classloader) {
        this.classloader = classloader;
    }

    public ResponseListener getResponseListener() {
        return responseListener;
    }

    public void setResponseListener(ResponseListener responseListener) {
        this.responseListener = responseListener;
    }

    public SessionType getSessionType() {
        return sessionType;
    }

    public void setSessionType(SessionType sessionType) {
        this.sessionType = sessionType;
    }

    public TransactionManager getTransactionManager() {
        return tm;
    }

    public void setTransactionManager(TransactionManager tm) {
        this.tm = tm;
    }

    public long getResponseTimeout() {
        return responseTimeout;
    }

    public void setResponseTimeout(long responseTimeout) {
        this.responseTimeout = responseTimeout;
    }

    public boolean isPersistent() {
        return persistent;
    }

    public void setPersistent(boolean persistent) {
        this.persistent = persistent;
    }

}