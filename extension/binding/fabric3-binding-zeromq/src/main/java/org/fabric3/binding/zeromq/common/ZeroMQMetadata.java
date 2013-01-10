/*
 * Fabric3 Copyright (c) 2009-2012 Metaform Systems
 * 
 * Fabric3 is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version, with the following exception:
 * 
 * Linking this software statically or dynamically with other modules is making
 * a combined work based on this software. Thus, the terms and conditions of the
 * GNU General Public License cover the whole combination.
 * 
 * As a special exception, the copyright holders of this software give you
 * permission to link this software with independent modules to produce an
 * executable, regardless of the license terms of these independent modules, and
 * to copy and distribute the resulting executable under terms of your choice,
 * provided that you also meet, for each linked independent module, the terms
 * and conditions of the license of that module. An independent module is a
 * module which is not derived from or based on this software. If you modify
 * this software, you may extend this exception to your version of the software,
 * but you are not obligated to do so. If you do not wish to do so, delete this
 * exception statement from your version.
 * 
 * Fabric3 is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * Fabric3. If not, see <http://www.gnu.org/licenses/>.
 */
package org.fabric3.binding.zeromq.common;

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
}
