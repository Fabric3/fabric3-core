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
