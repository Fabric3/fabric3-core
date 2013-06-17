/*
* Fabric3
* Copyright (c) 2009-2013 Metaform Systems
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
package org.fabric3.transport.ftp.server.host;

import java.util.Map;

import org.apache.mina.common.IdleStatus;
import org.apache.mina.common.IoHandler;
import org.apache.mina.common.IoSession;
import org.oasisopen.sca.annotation.Reference;

import org.fabric3.api.annotation.monitor.Monitor;
import org.fabric3.transport.ftp.server.monitor.FtpMonitor;
import org.fabric3.transport.ftp.server.protocol.DefaultRequest;
import org.fabric3.transport.ftp.server.protocol.DefaultResponse;
import org.fabric3.transport.ftp.server.protocol.FtpSession;
import org.fabric3.transport.ftp.server.protocol.Request;
import org.fabric3.transport.ftp.server.protocol.RequestHandler;
import org.fabric3.transport.ftp.server.protocol.Response;
import org.fabric3.transport.ftp.server.security.User;

/**
 * TODO Use monitor instead of System.err.
 */
public class FtpHandler implements IoHandler {

    private Map<String, RequestHandler> requestHandlers;
    private FtpMonitor ftpMonitor;

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
     * Injects the FTP commands.
     *
     * @param ftpCommands FTP commands.
     */
    @Reference
    public void setRequestHandlers(Map<String, RequestHandler> ftpCommands) {
        this.requestHandlers = ftpCommands;
    }

    public void exceptionCaught(IoSession session, Throwable throwable) throws Exception {
        FtpSession ftpSession = new FtpSession(session);
        ftpMonitor.onException(throwable, ftpSession.getUserName());
    }

    public void messageReceived(IoSession session, Object message) throws Exception {
        FtpSession ftpSession = new FtpSession(session);
        ftpMonitor.onCommand(message, ftpSession.getUserName());

        Request request = new DefaultRequest(message.toString(), ftpSession);

        RequestHandler requestHandler = requestHandlers.get(request.getCommand());
        if (requestHandler == null) {
            session.write(new DefaultResponse(502, "Command " + request.getCommand() + " not implemented"));
        } else {
            requestHandler.service(request);
        }
    }

    public void messageSent(IoSession session, Object message) throws Exception {
        FtpSession ftpSession = new FtpSession(session);
        ftpMonitor.onResponse(message, ftpSession.getUserName());
    }

    public void sessionCreated(IoSession session) throws Exception {
        Response response = new DefaultResponse(220, "Service ready for new user.");
        session.write(response);
    }

    public void sessionIdle(IoSession session, IdleStatus idleStatus) throws Exception {
        // The session was idle more than the timeout period which is probably a hung client
        // Report an error and wait to close the session
        User user = (User) session.getAttribute(FtpSession.USER);
        if (user != null) {
            ftpMonitor.connectionTimedOut(user.getName());
        } else {
            ftpMonitor.connectionTimedOut("anonymous");
        }
        session.closeOnFlush().awaitUninterruptibly(10000);
    }

    public void sessionOpened(IoSession session) throws Exception {
    }

    public void sessionClosed(IoSession session) throws Exception {
    }

}
