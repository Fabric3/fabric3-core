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
package org.fabric3.federation.executor;

import org.osoa.sca.annotations.EagerInit;
import org.osoa.sca.annotations.Init;
import org.osoa.sca.annotations.Reference;

import org.fabric3.api.annotation.Monitor;
import org.fabric3.federation.command.RuntimeSyncCommand;
import org.fabric3.spi.executor.CommandExecutor;
import org.fabric3.spi.executor.CommandExecutorRegistry;
import org.fabric3.spi.executor.ExecutionException;
import org.fabric3.spi.topology.MessageException;
import org.fabric3.spi.topology.ZoneTopologyService;

/**
 * Processes a {@link RuntimeSyncCommand} on a zone leader by returning the cached deployment command for the current state of the zone.
 *
 * @version $Rev: 7826 $ $Date: 2009-11-14 13:32:05 +0100 (Sat, 14 Nov 2009) $
 */
@EagerInit
public class RuntimeSyncCommandExecutor implements CommandExecutor<RuntimeSyncCommand> {
    private ZoneTopologyService topologyService;
    private CommandExecutorRegistry executorRegistry;
    private RuntimeSyncMonitor monitor;

    public RuntimeSyncCommandExecutor(@Reference ZoneTopologyService topologyService,
                                      @Reference CommandExecutorRegistry executorRegistry,
                                      @Monitor RuntimeSyncMonitor monitor) {
        this.topologyService = topologyService;
        this.executorRegistry = executorRegistry;
        this.monitor = monitor;
    }

    @Init
    public void init() {
        executorRegistry.register(RuntimeSyncCommand.class, this);
    }

    public void execute(RuntimeSyncCommand command) throws ExecutionException {
        String runtime = command.getRuntimeName();
        monitor.receivedSyncRequest(runtime);
        // TODO pull from cache
        byte[] response = null;
        try {
            topologyService.sendAsynchronousMessage(runtime, response);
        } catch (MessageException e) {
            throw new ExecutionException("Error sending deployment command to " + runtime, e);
        }
    }
}