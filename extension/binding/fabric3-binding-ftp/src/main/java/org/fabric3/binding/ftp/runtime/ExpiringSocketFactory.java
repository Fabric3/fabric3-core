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
package org.fabric3.binding.ftp.runtime;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;

import org.apache.commons.net.DefaultSocketFactory;

/**
 * Overrides the DefaultSocketFactory behavior provided by Apache Commons Net by setting a timeout for opening a socket connection.
 */
public class ExpiringSocketFactory extends DefaultSocketFactory {
    private final int connectTimeout;

    /**
     * Constructor.
     *
     * @param connectTimeout the timeout to wait in milliseconds to open a connection.
     */
    public ExpiringSocketFactory(int connectTimeout) {
        this.connectTimeout = connectTimeout;
    }

    public Socket createSocket(String host, int port) throws IOException {
        InetSocketAddress address = new InetSocketAddress(host, port);
        return createSocket(address, null);
    }

    public Socket createSocket(InetAddress address, int port) throws IOException {
        InetSocketAddress socketAddress = new InetSocketAddress(address, port);
        return createSocket(socketAddress, null);
    }

    public Socket createSocket(String host, int port, InetAddress localAddr, int localPort) throws IOException {
        if (host != null) {
            return createSocket(new InetSocketAddress(host, port), new InetSocketAddress(localAddr, localPort));
        } else {
            return createSocket(new InetSocketAddress(InetAddress.getByName(null), port), new InetSocketAddress(localAddr, localPort));
        }
    }

    public Socket createSocket(InetAddress address, int port, InetAddress localAddr, int localPort) throws IOException {
        if (address != null) {
            return createSocket(new InetSocketAddress(address, port), new InetSocketAddress(localAddr, localPort));
        } else {
            return createSocket(null, new InetSocketAddress(localAddr, localPort));
        }
    }


    private Socket createSocket(InetSocketAddress socketAddress, InetSocketAddress localSocketAddress) throws IOException {
        Socket socket = new Socket();
        if (localSocketAddress != null) {
            socket.bind(localSocketAddress);
        }
        socket.connect(socketAddress, connectTimeout);
        return socket;
    }

}
