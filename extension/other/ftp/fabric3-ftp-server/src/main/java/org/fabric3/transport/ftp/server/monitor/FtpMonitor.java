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
package org.fabric3.transport.ftp.server.monitor;

import org.fabric3.api.annotation.monitor.Info;
import org.fabric3.api.annotation.monitor.Severe;

/**
 * Monitor interface for logging significant events.
 */
public interface FtpMonitor {

    /**
     * Logged when a command is received by the FTP server.
     *
     * @param command Command that was received.
     * @param user    User that sent the command.
     */
    @Info("Command received from user {1}: {0}")
    void onCommand(Object command, String user);

    /**
     * Logged when a response is sent by the FTP server.
     *
     * @param response Response that was sent.
     * @param user     User that sent the command.
     */
    @Info("Response sent to user {1}: {0}")
    void onResponse(Object response, String user);

    /**
     * Logged when an exception occurs.
     *
     * @param throwable Exception that occurred.
     * @param user      User whose command caused the exception.
     */
    @Severe("Exception caught for user {1}: {0}")
    void onException(Throwable throwable, String user);

    /**
     * Logged when an upload error occurs.
     *
     * @param user User whose command caused the exception.
     */
    @Severe("FTPLet aborted upload for user: {0}")
    void uploadError(String user);

    /**
     * Logged when an FtpLet not found for a resource.
     *
     * @param resource the resource address.
     */
    @Severe("No registered FtpLet for resource: {0}")
    void noFtpLetRegistered(String resource);

    /**
     * Logged when a connection times out.
     *
     * @param user the user.
     */
    @Severe("FTP Connection timed out for user: {0}")
    void connectionTimedOut(String user);

}
