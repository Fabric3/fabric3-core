/*
* Fabric3
* Copyright (c) 2009-2012 Metaform Systems
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
package org.fabric3.binding.ftp.provision;

import java.net.URI;
import java.util.List;

import org.fabric3.binding.ftp.common.Constants;
import org.fabric3.spi.model.physical.PhysicalTargetDefinition;

/**
 * @version $Rev$ $Date$
 */
public class FtpTargetDefinition extends PhysicalTargetDefinition {
    private static final long serialVersionUID = -494501322715192283L;
    private final URI classLoaderId;
    private final boolean active;
    private final FtpSecurity security;
    private int connectTimeout;
    private int socketTimeout;
    private List<String> commands;
    private String tmpFileSuffix;

    /**
     * Initializes the classloader id, transfer mode, and timeout.
     *
     * @param classLoaderId  the classloader id to deserialize parameters in
     * @param active         FTP transfer mode
     * @param security       Security parameters
     * @param connectTimeout the timeout to use for opening socket connections
     * @param socketTimeout  the timeout to use for blocking connection operations
     */
    public FtpTargetDefinition(URI classLoaderId, boolean active, FtpSecurity security, int connectTimeout, int socketTimeout) {
        this.classLoaderId = classLoaderId;
        this.active = active;
        this.security = security;
        this.connectTimeout = connectTimeout;
        this.socketTimeout = socketTimeout;
    }

    /**
     * Returns the classloader id to deserialize parameters in.
     *
     * @return the classloader id to deserialize parameters in
     */
    public URI getClassLoaderId() {
        return classLoaderId;
    }

    /**
     * Gets the FTP transfer mode.
     *
     * @return True if user wants active transfer mode.
     */
    public boolean isActive() {
        return active;
    }

    /**
     * Get the security parameters.
     *
     * @return Get the security parameters.
     */
    public FtpSecurity getSecurity() {
        return security;
    }

    /**
     * Returns the timeout value to use for opening connections or {@link Constants#NO_TIMEOUT} if none is set.
     *
     * @return the timeout value to use for opening connections or {@link Constants#NO_TIMEOUT} if none is set
     */
    public int getConectTimeout() {
        return connectTimeout;
    }

    /**
     * Sets the timeout value to use for opening connections.
     *
     * @param socketTimeout the timeout value
     */
    public void setConnectTimeout(int socketTimeout) {
        this.socketTimeout = socketTimeout;
    }

    /**
     * Returns the timeout value to use for blocking operations or {@link Constants#NO_TIMEOUT} if none is set.
     *
     * @return the timeout value to use for blocking operations or {@link Constants#NO_TIMEOUT} if none is set
     */
    public int getSocketTimeout() {
        return socketTimeout;
    }

    /**
     * Sets the timeout value to use for blocking operations.
     *
     * @param socketTimeout the timeout value
     */
    public void setSocketTimeout(int socketTimeout) {
        this.socketTimeout = socketTimeout;
    }

    /**
     * The commands to execute before a STOR operation (i.e. a service invocation)
     *
     * @return the commands
     */
    public List<String> getSTORCommands() {
        return commands;
    }

    /**
     * Sets the list of commands to execute before a STOR operation.
     *
     * @param commands the commands
     */
    public void setSTORCommands(List<String> commands) {
        this.commands = commands;
    }

    /**
     * Gets the temporary file suffix to be used while file in transmission (i.e. during STOR operation).
     *
     * @return temporary file suffix
     */
    public String getTmpFileSuffix() {
        return tmpFileSuffix;
    }

    /**
     * Sets the temporary file suffix to be used while file in transmission (i.e. during STOR operation).
     *
     * @param tmpFileSuffix temporary file suffix
     */
    public void setTmpFileSuffix(String tmpFileSuffix) {
        this.tmpFileSuffix = tmpFileSuffix;
    }

}
