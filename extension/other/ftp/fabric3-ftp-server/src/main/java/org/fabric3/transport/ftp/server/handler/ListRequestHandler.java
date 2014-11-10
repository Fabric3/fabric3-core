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
import java.io.OutputStream;

import org.oasisopen.sca.annotation.Reference;

import org.fabric3.transport.ftp.server.data.DataConnection;
import org.fabric3.transport.ftp.server.passive.PassiveConnectionService;
import org.fabric3.transport.ftp.server.protocol.DefaultResponse;
import org.fabric3.transport.ftp.server.protocol.FtpSession;
import org.fabric3.transport.ftp.server.protocol.Request;
import org.fabric3.transport.ftp.server.protocol.RequestHandler;

/**
 * Handles the <code>LIST</code> command.
 * <p/>
 */
public class ListRequestHandler implements RequestHandler {
    private static final byte[] BYTES = "".getBytes();
    private PassiveConnectionService passiveConnectionService;

    /**
     * Injects the passive connection service.
     *
     * @param passiveConnectionService Passive connection service.
     */
    @Reference
    public void setPassivePortService(PassiveConnectionService passiveConnectionService) {
        this.passiveConnectionService = passiveConnectionService;
    }

    public void service(Request request) {

        FtpSession session = request.getSession();
        int passivePort = session.getPassivePort();

        if (0 == passivePort) {
            session.write(new DefaultResponse(503, "PASV must be issued first"));
            return;
        }

        session.write(new DefaultResponse(150, "File status okay; about to open data connection"));


        try {
            DataConnection dataConnection = session.getDataConnection();
            dataConnection.open();
            OutputStream stream = dataConnection.getOutputStream();
            stream.write(BYTES);
            stream.close();
            session.write(new DefaultResponse(226, "Transfer complete"));
        } catch (IOException ex) {
            session.write(new DefaultResponse(425, "Can't open data connection"));
        } finally {
            closeDataConnection(session, passivePort);
        }

    }


    /*
     * Closes the data connection.
     */
    private void closeDataConnection(FtpSession session, int passivePort) {
        session.closeDataConnection();
        passiveConnectionService.release(passivePort);
    }

}