/*
* Fabric3
* Copyright (c) 2009 Metaform Systems
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
package org.fabric3.transport.ftp.server.handler;

import java.io.IOException;
import java.io.InputStream;

import org.osoa.sca.annotations.Reference;

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
 *
 * @version $Rev$ $Date$
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
