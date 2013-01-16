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
    private Map<String, String> properties = new HashMap<String, String>();

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
