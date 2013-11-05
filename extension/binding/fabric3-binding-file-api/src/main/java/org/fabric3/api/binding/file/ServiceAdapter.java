/*
 * Fabric3 Copyright (c) 2009-2013 Metaform Systems
 * 
 * Fabric3 is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version, with the following exception:
 * 
 * Linking this software statically or dynamically with other modules is making
 * a combined work based on this software. Thus, the terms and conditions of the
 * GNU General Public License cover the whole combination.
 * 
 * As a special exception, the copyright holders of this software give you
 * permission to link this software with independent modules to produce an
 * executable, regardless of the license terms of these independent modules, and
 * to copy and distribute the resulting executable under terms of your choice,
 * provided that you also meet, for each linked independent module, the terms
 * and conditions of the license of that module. An independent module is a
 * module which is not derived from or based on this software. If you modify
 * this software, you may extend this exception to your version of the software,
 * but you are not obligated to do so. If you do not wish to do so, delete this
 * exception statement from your version.
 * 
 * Fabric3 is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * Fabric3. If not, see <http://www.gnu.org/licenses/>.
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
     * <p/>
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
