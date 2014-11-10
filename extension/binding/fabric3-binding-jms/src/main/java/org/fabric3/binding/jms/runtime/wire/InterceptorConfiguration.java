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

import java.util.HashMap;
import java.util.Map;

import org.fabric3.binding.jms.spi.provision.OperationPayloadTypes;

/**
 * Template for configuring a {@link JmsInterceptor}.
 */
public class InterceptorConfiguration {
    private String operationName;
    private OperationPayloadTypes payloadTypes;
    private boolean oneWay;
    private WireConfiguration wireConfiguration;
    private int deliveryMode = -1;
    private String jmsType;
    private long timeToLive = -1;
    private int priority = -1;
    private Map<String, String> properties = new HashMap<>();

    public InterceptorConfiguration(WireConfiguration wireConfiguration) {
        this.wireConfiguration = wireConfiguration;
    }

    public WireConfiguration getWireConfiguration() {
        return wireConfiguration;
    }

    public String getOperationName() {
        return operationName;
    }

    public void setOperationName(String operationName) {
        this.operationName = operationName;
    }

    public OperationPayloadTypes getPayloadTypes() {
        return payloadTypes;
    }

    public void setPayloadType(OperationPayloadTypes payloadTypes) {
        this.payloadTypes = payloadTypes;
    }

    public boolean isOneWay() {
        return oneWay;
    }

    public void setOneWay(boolean oneWay) {
        this.oneWay = oneWay;
    }

    public void setDeliveryMode(int deliveryMode) {
        this.deliveryMode = deliveryMode;
    }

    public int getDeliveryMode() {
        return deliveryMode;
    }

    public void setJmsType(String type) {
        this.jmsType = type;
    }

    public String getJmsType() {
        return jmsType;
    }

    public void setTimeToLive(long time) {
        this.timeToLive = time;
    }

    public long getTimeToLive() {
        return timeToLive;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public int getPriority() {
        return priority;
    }

    public void addProperty(String key, String value) {
        properties.put(key, value);
    }

    public Map<String, String> getProperties() {
        return properties;
    }
}
