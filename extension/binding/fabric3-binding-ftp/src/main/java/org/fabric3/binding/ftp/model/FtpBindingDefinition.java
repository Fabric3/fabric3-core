/*
* Fabric3
* Copyright (c) 2009 Metaform Systems
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
package org.fabric3.binding.ftp.model;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import org.fabric3.binding.ftp.common.Constants;
import org.fabric3.model.type.component.BindingDefinition;

/**
 * Binding definition loaded from the SCDL.
 *
 * @version $Rev$ $Date$
 */
public class FtpBindingDefinition extends BindingDefinition {
    private static final long serialVersionUID = -889044951554792780L;

    private final TransferMode transferMode;
    private List<String> commands = new ArrayList<String>();
    private String tmpFileSuffix;

    /**
     * Initializes the binding type.
     *
     * @param uri          Target URI.
     * @param transferMode the FTP transfer mode
     */
    public FtpBindingDefinition(URI uri, TransferMode transferMode) {
        super(uri, Constants.BINDING_QNAME);
        this.transferMode = transferMode;
    }

    /**
     * Gets the transfer mode.
     *
     * @return File transfer mode.
     */
    public TransferMode getTransferMode() {
        return transferMode;
    }

    /**
     * Gets the list of commands to execute before a STOR operation, i.e. a service invocation.
     *
     * @return the list of commands to execute before a STOR
     */
    public List<String> getSTORCommands() {
        return commands;
    }


    /**
     * Adds a command to execute before a put.
     *
     * @param command the command
     */
    public void addSTORCommand(String command) {
        commands.add(command);
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
