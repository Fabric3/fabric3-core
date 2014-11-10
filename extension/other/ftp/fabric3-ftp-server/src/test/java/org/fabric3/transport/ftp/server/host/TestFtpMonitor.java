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

import org.fabric3.api.annotation.monitor.Severe;
import org.fabric3.transport.ftp.server.monitor.FtpMonitor;

/**
 *
 */
public class TestFtpMonitor implements FtpMonitor {

    public void onCommand(Object command, String user) {
        System.err.println("Command received from user " + user + ": " + command);
    }

    public void onException(Throwable throwable, String user) {
        System.err.println("Exception " + throwable.getMessage() + " by user " + user);
        throwable.printStackTrace();
    }

    public void uploadError(String user) {
        System.err.println("Upload error: " + user);
    }

    public void noFtpLetRegistered(String resource) {
        System.err.println("No registered FTPLet:" + resource);
    }

    @Severe
    public void connectionTimedOut(String user) {
        System.err.println("Connection timeout: " + user);
    }

    public void onResponse(Object response, String user) {
        System.err.println("Response sent to user " + user + ": " + response);
    }

}
