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
import java.net.InetAddress;

import org.osoa.sca.annotations.Property;
import org.osoa.sca.annotations.Reference;

import org.fabric3.transport.ftp.server.data.DataConnection;
import org.fabric3.transport.ftp.server.data.PassiveDataConnection;
import org.fabric3.transport.ftp.server.passive.PassiveConnectionService;
import org.fabric3.transport.ftp.server.protocol.DefaultResponse;
import org.fabric3.transport.ftp.server.protocol.FtpSession;
import org.fabric3.transport.ftp.server.protocol.Request;
import org.fabric3.transport.ftp.server.protocol.RequestHandler;

/**
 * Handles the <code>PASV</code> command.
 *
 * @version $Rev$ $Date$
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

    @Property
    public void setListenAddress(String listenAddress) {
        this.listenAddress = listenAddress;
    }

    /**
     * Sets the optional timeout in milliseconds for sockets that are idle.
     *
     * @param timeout timeout in milliseconds.
     */
    @Property
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
