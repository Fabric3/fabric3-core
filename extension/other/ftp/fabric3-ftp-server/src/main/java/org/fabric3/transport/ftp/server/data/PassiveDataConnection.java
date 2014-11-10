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
package org.fabric3.transport.ftp.server.data;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;

/**
 * Represents a passive data connection.
 */
public class PassiveDataConnection implements DataConnection {

    private ServerSocket serverSocket;
    private InetAddress bindAddress;
    private int passivePort;
    private Socket socket;
    private int idleTimeout;

    /**
     * Initializes the passive data connection.
     *
     * @param bindAddress the address to bind the socket to.
     * @param passivePort Passive port.
     * @param idleTimeout the time to wait in milliseconds for an accept() operation on the passive socket.
     */
    public PassiveDataConnection(InetAddress bindAddress, int passivePort, int idleTimeout) {
        this.bindAddress = bindAddress;
        this.passivePort = passivePort;
        this.idleTimeout = idleTimeout;
    }

    /**
     * Closes the data connection.
     */
    public void close() {
        try {
            if (socket != null) {
                socket.close();
            }
        } catch (IOException ignore1) {
        } finally {
            try {
                if (serverSocket != null) {
                    serverSocket.close();
                }
            } catch (IOException ignore2) {
            }
        }
    }

    /**
     * Get an input stream to the data connection.
     *
     * @return Input stream to the data cnnection.
     * @throws IOException If unable to get input stream.
     */
    public InputStream getInputStream() throws IOException {
        return socket.getInputStream();
    }

    /**
     * Get an output stream to the data connection.
     *
     * @return Output stream to the data connection.
     * @throws IOException If unable to get output stream.
     */
    public OutputStream getOutputStream() throws IOException {
        return socket.getOutputStream();
    }

    /**
     * Opens the data connection.
     *
     * @throws IOException If unable to open connection.
     */
    public void open() throws IOException {
        socket = serverSocket.accept();
    }

    /**
     * Initializes a data connection.
     *
     * @throws IOException If unable to open connection.
     */
    public void initialize() throws IOException {
        serverSocket = new ServerSocket();
        // set the timeout to wait for the client to respond
        serverSocket.setSoTimeout(idleTimeout);
        SocketAddress address = new InetSocketAddress(bindAddress, passivePort);
        serverSocket.bind(address);
    }

}
