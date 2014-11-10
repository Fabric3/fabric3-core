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

import org.oasisopen.sca.annotation.Reference;

import org.fabric3.transport.ftp.server.protocol.DefaultResponse;
import org.fabric3.transport.ftp.server.protocol.FtpSession;
import org.fabric3.transport.ftp.server.protocol.Request;
import org.fabric3.transport.ftp.server.protocol.RequestHandler;
import org.fabric3.transport.ftp.server.security.User;
import org.fabric3.transport.ftp.server.security.UserManager;

/**
 * Handles the <code>PASS</code> command.
 */
public class PassRequestHandler implements RequestHandler {

    private UserManager userManager;

    /**
     * Uses the registered user manager to authenticate the <code>PASS</code> command.
     *
     * @param request Object the encapsuates the current FTP command.
     */
    public void service(Request request) {

        FtpSession session = request.getSession();
        User user = session.getUser();

        if (user == null) {
            session.write(new DefaultResponse(503, "Login with USER first"));
            return;
        }

        String userName = user.getName();
        String password = request.getArgument();

        if (password == null) {
            session.write(new DefaultResponse(501, "Syntax error in parameters or arguments"));
        }

        if (userManager.login(userName, password)) {
            session.setAuthenticated();
            session.write(new DefaultResponse(230, "User logged in, proceed"));
        } else {
            session.write(new DefaultResponse(530, "Authentication failed"));
        }

    }

    /**
     * Injects the user manager.
     *
     * @param userManager Injects the user manager.
     */
    @Reference
    public void setUserManager(UserManager userManager) {
        this.userManager = userManager;
    }

}
