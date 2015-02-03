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
package org.fabric3.api.binding.file;

import java.io.File;
import java.io.IOException;

/**
 * Implementations adapt the service-side binding infrastructure to a particular file system-based transport protocol.
 */
public interface ServiceAdapter {

    /**
     * Called to return expected service parameter types for a detected file as an array. For example, the service may require an input stream If data
     * in error.
     *
     * If an implementation throws an exception, the invocation will be aborted and {@link #afterInvoke(File, Object[])} will <strong>NOT</string> be
     * called. Instead, the detected file will be moved to the error directory. It is therefore important to ensure all resource streams are closed
     * prior to throwing an exception.
     *
     * @param file the detected file
     * @return the expected service parameters
     * @throws InvalidDataException if an unrecoverable data error is encountered.
     */
    Object[] beforeInvoke(File file) throws InvalidDataException;

    /**
     * Called after an invocation has been made. This method will be called for both successful invocations as well as ones that raise
     * errors.Implementations should close any open resource streams.
     *
     * @param file    the detected file
     * @param payload the service parameters used for the invocation
     * @throws IOException if an exception occurs
     */

    void afterInvoke(File file, Object[] payload) throws IOException;

    /**
     * Called when an error processing the file is encountered.
     *
     * @param file           the file to delete
     * @param errorDirectory the error directory
     * @param e              the exception encountered
     * @throws IOException if an exception handling the error occurs
     */
    void error(File file, File errorDirectory, Exception e) throws IOException;

    /**
     * Called if the binding is configured to delete data files.
     *
     * @param file the file to delete
     * @throws IOException if an exception deleting the file occurs
     */
    void delete(File file) throws IOException;

    /**
     * Called if the binding is configured to archive data files.
     *
     * @param file             the file to archive
     * @param archiveDirectory the archive directory
     * @throws IOException if an exception archiving the file occurs
     */
    void archive(File file, File archiveDirectory) throws IOException;

}
