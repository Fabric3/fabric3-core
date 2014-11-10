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
package org.fabric3.api.binding.zeromq.model;

import java.io.Serializable;
import java.util.List;

/**
 * Holds ZeroMQ binding metadata.
 */
public class ZeroMQMetadata implements Serializable {
    private static final long serialVersionUID = 6236084212498002778L;

    private List<SocketAddressDefinition> socketAddresses;

    private String channelName;
    private long highWater = -1;
    private long multicastRate = -1;
    private long multicastRecovery = -1;
    private long sendBuffer = -1;
    private long receiveBuffer = -1;
    private String wireFormat;
    private long timeout = 10;  // in milliseconds; default to 10

    /**
     * Returns the list of hosts to connect or bind to or null if not explicitly set.
     *
     * @return the list of hosts or null
     */
    public List<SocketAddressDefinition> getSocketAddresses() {
        return socketAddresses;
    }

    /**
     * Sets the list of hosts to connect or bind to.
     *
     * @param socketAddresses the list of hosts
     */
    public void setSocketAddresses(List<SocketAddressDefinition> socketAddresses) {
        this.socketAddresses = socketAddresses;
    }

    /**
     * Returns the channel name
     *
     * @return the channel name
     */
    public String getChannelName() {
        return channelName;
    }

    /**
     * Sets the channel name
     *
     * @param channelName the channel name to set
     */
    public void setChannelName(String channelName) {
        this.channelName = channelName;
    }

    /**
     * Returns the socket high water mark.
     *
     * @return the socket high water mark or -1 if not specified
     */
    public long getHighWater() {
        return highWater;
    }

    /**
     * Sets the socket high water mark.
     *
     * @param highWater the socket high water mark
     */
    public void setHighWater(long highWater) {
        this.highWater = highWater;
    }

    /**
     * Returns the socket multicast rate.
     *
     * @return the socket multicast rate or -1 if not specified
     */
    public long getMulticastRate() {
        return multicastRate;
    }

    /**
     * Sets the socket multicast rate.
     *
     * @param rate socket multicast rate
     */
    public void setMulticastRate(long rate) {
        this.multicastRate = rate;
    }

    /**
     * Returns the socket multicast recovery interval.
     *
     * @return the socket multicast recovery interval or -1 if not specified
     */
    public long getMulticastRecovery() {
        return multicastRecovery;
    }

    /**
     * Sets the socket multicast recovery interval.
     *
     * @param multicastRecovery socket multicast recovery interval
     */
    public void setMulticastRecovery(long multicastRecovery) {
        this.multicastRecovery = multicastRecovery;
    }

    /**
     * Gets the socket send buffer size.
     *
     * @return the socket send buffer size or -1 if not specified
     */
    public long getSendBuffer() {
        return sendBuffer;
    }

    /**
     * Sets the socket send buffer size.
     *
     * @param sendBuffer socket send buffer size
     */
    public void setSendBuffer(long sendBuffer) {
        this.sendBuffer = sendBuffer;
    }

    /**
     * Gets the socket receive buffer size.
     *
     * @return the socket receive buffer size or -1 if not specified
     */
    public long getReceiveBuffer() {
        return receiveBuffer;
    }

    /**
     * Sets the socket receive buffer size.
     *
     * @param receiveBuffer socket receive buffer size
     */
    public void setReceiveBuffer(long receiveBuffer) {
        this.receiveBuffer = receiveBuffer;
    }

    /**
     * Returns the wire format for serializing messages.
     *
     * @return the wire format for serializing messages
     */
    public String getWireFormat() {
        return wireFormat;
    }

    /**
     * Sets the wire format for serializing messages.
     *
     * @param wireFormat the wire format for serializing messages
     */
    public void setWireFormat(String wireFormat) {
        this.wireFormat = wireFormat;
    }

    /**
     * Returns the timeout in milliseconds
     *
     * @return the timeout in milliseconds
     */
    public long getTimeout() {
        return timeout;
    }

    /**
     * Sets the timeout in milliseconds
     *
     * @param timeout the timeout in milliseconds
     */
    public void setTimeout(long timeout) {
        this.timeout = timeout;
    }
}
