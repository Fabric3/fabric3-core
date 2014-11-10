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
import org.fabric3.transport.ftp.spi.FtpLetContainer;

/**
 * Handles the <code>CWD</code> command.
 * <p/>
 */
public class CwdRequestHandler implements RequestHandler {
    private FtpLetContainer container;

    @Reference
    public void setContainer(FtpLetContainer container) {
        this.container = container;
    }

    /**
     * Services the <code>CWD</code> request.
     *
     * @param request Object the encapsuates the current FTP command.
     */
    public void service(Request request) {
        FtpSession session = request.getSession();
        if (!session.isAuthenticated()) {
            session.write(new DefaultResponse(530, "Access Denied"));
            return;
        }
        String directory = request.getArgument();
        if (!container.isRegistered(directory)) {
            session.write(new DefaultResponse(550, directory + ": No such file or directory"));
            return;
        }
        session.setCurrentDirectory(directory);
        session.write(new DefaultResponse(250, "CWD command successful"));

    }

}