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
package org.fabric3.transport.ftp.server.data;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Represents a data connection interface.
 */
public interface DataConnection {

    /**
     * Get an input stream to the data connection.
     *
     * @return Input stream to the data cnnection.
     * @throws IOException If unable to get input stream.
     */
    InputStream getInputStream() throws IOException;

    /**
     * Get an output stream to the data connection.
     *
     * @return Output stream to the data connection.
     * @throws IOException If unable to get output stream.
     */
    OutputStream getOutputStream() throws IOException;

    /**
     * Closes a data connection.
     */
    void close();

    /**
     * Opens a data connection.
     *
     * @throws IOException If unable to open connection.
     */
    void open() throws IOException;

    /**
     * Initializes a data connection.
     *
     * @throws IOException If unable to open connection.
     */
    void initialize() throws IOException;

}
