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
import java.io.InputStream;

import org.oasisopen.sca.annotation.Reference;

import org.fabric3.api.annotation.monitor.Monitor;
import org.fabric3.transport.ftp.api.FtpLet;
import org.fabric3.transport.ftp.server.data.DataConnection;
import org.fabric3.transport.ftp.server.monitor.FtpMonitor;
import org.fabric3.transport.ftp.server.passive.PassiveConnectionService;
import org.fabric3.transport.ftp.server.protocol.DefaultResponse;
import org.fabric3.transport.ftp.server.protocol.FtpSession;
import org.fabric3.transport.ftp.server.protocol.Request;
import org.fabric3.transport.ftp.server.protocol.RequestHandler;
import org.fabric3.transport.ftp.spi.FtpLetContainer;

/**
 * Handles the <code>STOR</code> command.
 * <p/>
 * TODO Add mechanism to register the FTPlet.
 */
public class StorRequestHandler implements RequestHandler {

    private PassiveConnectionService passiveConnectionService;
    private FtpLetContainer ftpLetContainer;
    private FtpMonitor ftpMonitor;

    /**
     * Services the <code>STOR</code> request. Currently only supports passive connections. This means <code>STOR</STOR> command should be preceded by
     * a <code>PASV</code> command.
     *
     * @param request Object the encapsuates the current FTP command.
     */
    public void service(Request request) {

        FtpSession session = request.getSession();
        if (!session.isAuthenticated()) {
            session.write(new DefaultResponse(530, "Access Denied"));
            return;
        }
        int passivePort = session.getPassivePort();

        if (0 == passivePort) {
            session.write(new DefaultResponse(503, "PASV must be issued first"));
            return;
        }

        String fileName = request.getArgument();
        if (null == fileName) {
            closeDataConnection(session, passivePort);
            session.write(new DefaultResponse(501, "Syntax error in parameters or arguments"));
            return;
        }

        session.write(new DefaultResponse(150, "File status okay; about to open data connection"));

        DataConnection dataConnection = session.getDataConnection();

        try {
            dataConnection.open();
        } catch (IOException ex) {
            closeDataConnection(session, passivePort);
            session.write(new DefaultResponse(425, "Can't open data connection"));
            return;
        }

        transfer(session, passivePort, dataConnection, fileName);

    }

    /**
     * Sets the monitor for logging significant events.
     *
     * @param ftpMonitor Monitor for logging significant events.
     */
    @Monitor
    public void setFtpMonitor(FtpMonitor ftpMonitor) {
        this.ftpMonitor = ftpMonitor;
    }

    /**
     * Injects the FtpLet container.
     *
     * @param ftpLetContainer Ftplet container.
     */
    @Reference
    public void setFtpLetContainer(FtpLetContainer ftpLetContainer) {
        this.ftpLetContainer = ftpLetContainer;
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

    /*
     * Transfers the file by calling the mapped FtpLet.
     */
    private void transfer(FtpSession session, int passivePort, DataConnection dataConnection, String fileName) {

        try {

            InputStream uploadData = dataConnection.getInputStream();

            FtpLet ftpLet = ftpLetContainer.getFtpLet(session.getCurrentDirectory());
            if (ftpLet == null) {
                ftpMonitor.noFtpLetRegistered(fileName);
                session.write(new DefaultResponse(426, "Data connection error"));
                return;
            }
            String type = session.getContentType();
            if (!ftpLet.onUpload(fileName, type, uploadData)) {
                ftpMonitor.uploadError(session.getUserName());
                session.write(new DefaultResponse(426, "Data connection error"));
                return;
            }
            session.write(new DefaultResponse(226, "Transfer complete"));

        } catch (Exception ex) {
            ftpMonitor.onException(ex, session.getUserName());
            session.write(new DefaultResponse(426, "Data connection error"));
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
