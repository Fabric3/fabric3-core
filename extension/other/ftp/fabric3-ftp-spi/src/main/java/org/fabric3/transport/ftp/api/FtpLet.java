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
package org.fabric3.transport.ftp.api;

import java.io.InputStream;

/**
 * Interface for receiving FTP callbacks.
 * <p/>
 * Note: The concept is borrowed from Apache MINA FTP Server.
 */
public interface FtpLet {

    /**
     * Callback when data is uploaded by the remote FTP client.
     *
     * @param fileName    Name of the file being uploaded.
     * @param contentType the type of data (e.g. binary or text) being uploaded
     * @param uploadData  Stream of data that is being uploaded.
     * @return true if the operation completed
     * @throws Exception If unable to handle the data.
     */
    boolean onUpload(String fileName, String contentType, InputStream uploadData) throws Exception;

}
