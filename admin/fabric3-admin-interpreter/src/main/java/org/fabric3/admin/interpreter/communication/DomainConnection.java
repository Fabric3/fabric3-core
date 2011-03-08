/*
* Fabric3
* Copyright (c) 2009-2011 Metaform Systems
*
* Fabric3 is free software: you can redistribute it and/or modify
* it under the terms of the GNU General Public License as
* published by the Free Software Foundation, either version 3 of
* the License, or (at your option) any later version, with the
* following exception:
*
* Linking this software statically or dynamically with other
* modules is making a combined work based on this software.
* Thus, the terms and conditions of the GNU General Public
* License cover the whole combination.
*
* As a special exception, the copyright holders of this software
* give you permission to link this software with independent
* modules to produce an executable, regardless of the license
* terms of these independent modules, and to copy and distribute
* the resulting executable under terms of your choice, provided
* that you also meet, for each linked independent module, the
* terms and conditions of the license of that module. An
* independent module is a module which is not derived from or
* based on this software. If you modify this software, you may
* extend this exception to your version of the software, but
* you are not obligated to do so. If you do not wish to do so,
* delete this exception statement from your version.
*
* Fabric3 is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty
* of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
* See the GNU General Public License for more details.
*
* You should have received a copy of the
* GNU General Public License along with Fabric3.
* If not, see <http://www.gnu.org/licenses/>.
*/
package org.fabric3.admin.interpreter.communication;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * The interface for performing domain administrative operations.
 *
 * @version $Rev$ $Date$
 */
public interface DomainConnection {

    /**
     * Sets the base admin address, clearing previous addresses on the stack.
     *
     * @param alias   the alias for the address
     * @param address the domain admin address
     */
    void setAddress(String alias, String address);

    /**
     * Push the address on the admin address stack and use it for management operations.
     *
     * @param alias   the alias for the address
     * @param address the address
     */
    void pushAddress(String alias, String address);

    /**
     * Remove that current admin address from the stack.
     *
     * @return the new current domain address
     */
    String popAddress();

    /**
     * Returns the current name (alias) of the runtime this connection is associated with.
     *
     * @return the current runtime name
     */
    String getAlias();

    /**
     * Returns the current address of the runtime this connection is associated with.
     *
     * @return the current runtime address
     */
    String getAddress();

    /**
     * Sets the username to authenticate with.
     *
     * @param username a valid domain admin username
     */
    void setUsername(String username);

    /**
     * Sets the password to authenticate with.
     *
     * @param password a valid domain admin password
     */
    void setPassword(String password);

    /**
     * Creates an HTTP(S) connection to a domain resource.
     *
     * @param path the relative resource path
     * @param verb the HTTP verb
     * @return the connection
     * @throws CommunicationException if there is a non-recoverable connection error
     */
    HttpURLConnection createControllerConnection(String path, String verb) throws CommunicationException;

    /**
     * Creates an HTTP(S) connection to a domain resource.
     *
     * @param path the relative resource path
     * @param verb the HTTP verb
     * @return the connection
     * @throws CommunicationException if there is a non-recoverable connection error
     */
    HttpURLConnection createConnection(String path, String verb) throws CommunicationException;

    /**
     * PUTs a resource.
     *
     * @param path     the relative resource path
     * @param resource the resource to PUT
     * @return the connection
     * @throws CommunicationException if there is a non-recoverable connection error
     */
    HttpURLConnection put(String path, URL resource) throws CommunicationException;

    /**
     * Parses a response stream.
     *
     * @param type   the class of the expected type
     * @param stream the response stream
     * @param <T>    the type
     * @return the parsed instance
     * @throws IOException if there is a parsing error
     */
    <T> T parse(Class<?> type, InputStream stream) throws IOException;


}
