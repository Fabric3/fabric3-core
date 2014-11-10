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
package org.fabric3.spi.federation.addressing;

import java.io.Serializable;

import org.fabric3.spi.host.Port;

/**
 * A physical socket address.
 */
public class SocketAddress implements Serializable {
    private static final long serialVersionUID = -6325896048393741909L;

    private String runtimeName;
    private String zone;
    private String protocol;
    private String address;
    private Port port;

    /**
     * Constructor.
     *
     * @param runtimeName the runtime name where the socket is located
     * @param zone        the zone
     * @param protocol    the protocol used for the socket, e.g. TCP
     * @param address     the IP address the socket should send/listen on
     * @param port        the socket port
     */
    public SocketAddress(String runtimeName, String zone, String protocol, String address, Port port) {
        this.runtimeName = runtimeName;
        this.zone = zone;
        this.protocol = protocol;
        this.address = address;
        this.port = port;
    }

    public String getRuntimeName() {
        return runtimeName;
    }

    public String getZone() {
        return zone;
    }

    public String getProtocol() {
        return protocol;
    }

    public String getAddress() {
        return address;
    }

    public Port getPort() {
        return port;
    }

    public String toProtocolString() {
        return protocol + "://" + address + ":" + port.getNumber();
    }

    public String toString() {
        return protocol + "://" + address + ":" + port.getNumber() + " [" + runtimeName + "]";
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        SocketAddress that = (SocketAddress) o;

        return port.getNumber() == that.port.getNumber() && !(address != null ? !address.equals(that.address) : that.address != null) && !(
                protocol != null ? !protocol.equals(that.protocol) : that.protocol != null) && !(
                runtimeName != null ? !runtimeName.equals(that.runtimeName) : that.runtimeName != null);

    }

    public int hashCode() {
        int result = runtimeName != null ? runtimeName.hashCode() : 0;
        result = 31 * result + (protocol != null ? protocol.hashCode() : 0);
        result = 31 * result + (address != null ? address.hashCode() : 0);
        result = 31 * result + port.getNumber();
        return result;
    }
}
