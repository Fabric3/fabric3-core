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
package org.fabric3.federation.command;

import org.fabric3.spi.command.Command;

/**
 * Aggregates a set of commands for deploying components to a zone.
 *
 * @version $Rev$ $Date$
 */
public class ZoneDeploymentCommand implements Command {
    private static final long serialVersionUID = 8673100303949676875L;

    private String id;
    private byte[] extensionCommands;
    private byte[] commands;
    private String correlationId;
    private boolean synchronization;

    /**
     * Constructor.
     *
     * @param id                the unique command id
     * @param extensionCommands the serialized set of commands used to deploy extensions required to run the components being deployed
     * @param commands          the serialized set of commands used to deploy components
     * @param correlationId     the correlation id used to associate the deployment command with an originating request
     * @param synchronization   true if this command was in response to a runtime request to synchronize with the domain
     */
    public ZoneDeploymentCommand(String id, byte[] extensionCommands, byte[] commands, String correlationId, boolean synchronization) {
        this.id = id;
        this.extensionCommands = extensionCommands;
        this.commands = commands;
        this.correlationId = correlationId;
        this.synchronization = synchronization;
    }

    /**
     * The unique command id.
     *
     * @return the unique command id
     */
    public String getId() {
        return id;
    }

    /**
     * The correlation id used to associate the deployment command with an originating request.
     *
     * @return the id or null if the command is not correlated with a request
     */
    public String getCorrelationId() {
        return correlationId;
    }

    /**
     * Returns true if this command was in response to a runtime request to synchronize with the domain.
     *
     * @return true if this command was in response to a runtime request to synchronize with the domain
     */
    public boolean isSynchronization() {
        return synchronization;
    }

    /**
     * Returns the serialized list of extension commands used to deploy components.
     *
     * @return the serialized list of extension commands used to deploy components
     */
    public byte[] getExtensionCommands() {
        return extensionCommands;
    }

    /**
     * Returns the serialized list of commands used to deploy components.
     *
     * @return the serialized list of commands used to deploy components
     */
    public byte[] getCommands() {
        return commands;
    }

}
