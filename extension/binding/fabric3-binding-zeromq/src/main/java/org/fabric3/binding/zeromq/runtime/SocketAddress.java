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
package org.fabric3.binding.zeromq.runtime;

import java.io.Serializable;

import org.fabric3.spi.host.Port;

/**
 * A socket address.
 */
public class SocketAddress implements Serializable {
    private static final long serialVersionUID = -6325896048393741909L;

    private String runtimeName;
    private String protocol;
    private String address;
    private Port port;

    /**
     * Constructor.
     *
     * @param runtimeName the runtime name where the socket is located
     * @param protocol    the protocol used for the socket, e.g. TCP
     * @param address     the IP address the socket should send/listen on
     * @param port        the socket port
     */
    public SocketAddress(String runtimeName, String protocol, String address, Port port) {
        this.runtimeName = runtimeName;
        this.protocol = protocol;
        this.address = address;
        this.port = port;
    }

    public String getRuntimeName() {
        return runtimeName;
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

    @Override
    public String toString() {
        return protocol + "://" + address + ":" + port.getNumber() + " [" + runtimeName + "]";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SocketAddress that = (SocketAddress) o;

        return port.getNumber() == that.port.getNumber() && !(address != null ? !address.equals(that.address) : that.address != null)
                && !(protocol != null ? !protocol.equals(that.protocol) : that.protocol != null)
                && !(runtimeName != null ? !runtimeName.equals(that.runtimeName) : that.runtimeName != null);

    }

    @Override
    public int hashCode() {
        int result = runtimeName != null ? runtimeName.hashCode() : 0;
        result = 31 * result + (protocol != null ? protocol.hashCode() : 0);
        result = 31 * result + (address != null ? address.hashCode() : 0);
        result = 31 * result + port.getNumber();
        return result;
    }
}
