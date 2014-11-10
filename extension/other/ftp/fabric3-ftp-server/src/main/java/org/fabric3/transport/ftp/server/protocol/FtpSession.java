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
package org.fabric3.transport.ftp.server.protocol;

import org.apache.mina.common.IoSession;
import org.apache.mina.common.WriteFuture;

import org.fabric3.transport.ftp.api.FtpConstants;
import org.fabric3.transport.ftp.server.data.DataConnection;
import org.fabric3.transport.ftp.server.security.User;


/**
 * Represents an FTP session between a client and a server.
 */
public class FtpSession {

    public static final String USER = "org.fabric3.ftp.server.user";
    public static final String PASSIVE_PORT = "org.fabric3.ftp.server.passive.port";
    public static final String DATA_CONNECTION = "org.fabric3.ftp.server.data.connection";
    public static final String CURRENT_DIRECTORY = "org.fabric3.ftp.server.directory";
    public static final String CONTENT_TYPE = "org.fabric3.ftp.server.content.type";

    private IoSession ioSession;

    /**
     * Initializes the wrapped IO session.
     *
     * @param ioSession Wrapped IO session.
     */
    public FtpSession(IoSession ioSession) {
        this.ioSession = ioSession;
    }

    /**
     * Sets the current user.
     *
     * @param user Current user.
     */
    public void setUser(User user) {
        ioSession.setAttribute(USER, user);
    }

    /**
     * Gets the name of the user associated with the session.
     *
     * @return Name of the user associated with the session.
     */
    public String getUserName() {
        User user = getUser();
        return user != null ? user.getName() : "";
    }

    /**
     * Gets the current user.
     *
     * @return Current user.
     */
    public User getUser() {
        return (User) ioSession.getAttribute(USER);
    }

    /**
     * Sets the session as authenticate.
     */
    public void setAuthenticated() {
        getUser().setAuthenticated();
    }

    /**
     * Checks the whether the user is authenticated.
     *
     * @return True if the user is authenticated.
     */
    public boolean isAuthenticated() {
        User user = getUser();
        return user != null && user.isAuthenticated();
    }

    /**
     * Gets the passive port.
     *
     * @return Passiv port.
     */
    public int getPassivePort() {
        return (Integer) ioSession.getAttribute(PASSIVE_PORT);
    }

    /**
     * Set the passive port.
     *
     * @param passivePort Passive port.
     */
    public void setPassivePort(int passivePort) {
        ioSession.setAttribute(PASSIVE_PORT, passivePort);
    }

    /**
     * Returs the current working directory.
     *
     * @return the working directory.
     */
    public String getCurrentDirectory() {
        String dir = (String) ioSession.getAttribute(CURRENT_DIRECTORY);
        if (dir == null) {
            return "/";
        }
        return dir;
    }

    /**
     * Sets the current working directory.
     *
     * @param dir the working directory.
     */
    public void setCurrentDirectory(String dir) {
        ioSession.setAttribute(CURRENT_DIRECTORY, dir);
    }

    /**
     * Writes a message out.
     *
     * @param object Message to be written.
     * @return Write future for the async operation.
     */
    public WriteFuture write(Object object) {
        return ioSession.write(object);
    }

    /**
     * Sets the initialized data connection.
     *
     * @param dataConnection Initialized data connection.
     */
    public void setDataConnection(DataConnection dataConnection) {
        ioSession.setAttribute(DATA_CONNECTION, dataConnection);
    }

    /**
     * Gets the initialized data connection.
     *
     * @return Initialized data connection.
     */
    public DataConnection getDataConnection() {
        return (DataConnection) ioSession.getAttribute(DATA_CONNECTION);
    }

    /**
     * Closes the data connection.
     */
    public void closeDataConnection() {

        DataConnection dataConnection = getDataConnection();
        if (null != dataConnection) {
            dataConnection.close();
        }

        setDataConnection(null);
        setPassivePort(0);

    }

    /**
     * Sets the data content type
     *
     * @param type the file transfer type
     */
    public void setContentType(String type) {
        if ("I".equals(type)) {
            ioSession.setAttribute(CONTENT_TYPE, FtpConstants.BINARY_TYPE);
        } else {
            ioSession.setAttribute(CONTENT_TYPE, FtpConstants.TEXT_TYPE);
        }
    }

    /**
     * Returns the data content type
     *
     * @return the file transfer type
     */
    public String getContentType() {
        String type = (String) ioSession.getAttribute(CONTENT_TYPE);
        if (type == null) {
            // default to ASCII/text
            return FtpConstants.TEXT_TYPE;
        }
        return type;
    }

    /**
     * Closes the FTP session.
     */
    public void close() {
        ioSession.close();
    }

}
