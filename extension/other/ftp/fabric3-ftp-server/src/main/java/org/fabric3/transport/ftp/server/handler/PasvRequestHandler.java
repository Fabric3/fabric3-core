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
package org.fabric3.transport.ftp.server.handler;

import java.io.IOException;
import java.net.InetAddress;

import org.oasisopen.sca.annotation.Property;
import org.oasisopen.sca.annotation.Reference;

import org.fabric3.transport.ftp.server.data.DataConnection;
import org.fabric3.transport.ftp.server.data.PassiveDataConnection;
import org.fabric3.transport.ftp.server.passive.PassiveConnectionService;
import org.fabric3.transport.ftp.server.protocol.DefaultResponse;
import org.fabric3.transport.ftp.server.protocol.FtpSession;
import org.fabric3.transport.ftp.server.protocol.Request;
import org.fabric3.transport.ftp.server.protocol.RequestHandler;

/**
 * Handles the <code>PASV</code> command.
 */
public class PasvRequestHandler implements RequestHandler {

    private PassiveConnectionService passiveConnectionService;
    private String listenAddress;
    private int idleTimeout = 60000;  // 60 seconds default

    /**
     * Initializes the passive data connection on request of <code>PASV</code> command from an authenticated user.
     *
     * @param request Object the encapsuates the current FTP command.
     */
    public void service(Request request) {

        FtpSession session = request.getSession();

        if (!session.isAuthenticated()) {
            session.write(new DefaultResponse(530, "Access denied"));
            return;
        }

        int passivePort = 0;

        try {

            passivePort = passiveConnectionService.acquire();

            InetAddress localAddress;
            if (listenAddress == null) {
                localAddress = InetAddress.getLocalHost();
            } else {
                localAddress = InetAddress.getByName(listenAddress);
            }

            String socketAddress = localAddress.getHostAddress().replace('.', ',') + ',' + (passivePort >> 8) + ',' + (passivePort & 0xFF);
            session.setPassivePort(passivePort);

            DataConnection dataConnection = new PassiveDataConnection(localAddress, passivePort, idleTimeout);
            dataConnection.initialize();
            session.setDataConnection(dataConnection);

            session.write(new DefaultResponse(227, "Entering Passive Mode (" + socketAddress + ")"));

        } catch (InterruptedException e) {
            session.write(new DefaultResponse(427, "Can't open passive connection"));
        } catch (IOException e) {
            passiveConnectionService.release(passivePort);
            session.write(new DefaultResponse(427, "Can't open passive connection"));
        }

    }

    @Property(required = false)
    public void setListenAddress(String listenAddress) {
        this.listenAddress = listenAddress;
    }

    /**
     * Sets the optional timeout in milliseconds for sockets that are idle.
     *
     * @param timeout timeout in milliseconds.
     */
    @Property(required = false)
    public void setIdleTimeout(int timeout) {
        this.idleTimeout = timeout / 1000;   // convert to seconds used by Mina
    }


    /**
     * Injects the passive connection service.
     *
     * @param passiveConnectionService Passive connection service.
     */
    @Reference
    public void setPassivePortService(PassiveConnectionService passiveConnectionService) {
        this.passiveConnectionService = passiveConnectionService;
    }

}
