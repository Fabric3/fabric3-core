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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.osoa.sca.annotations.EagerInit;
import org.osoa.sca.annotations.Init;
import org.osoa.sca.annotations.Reference;

import org.fabric3.api.annotation.Monitor;
import org.fabric3.federation.command.RuntimeDeploymentCommand;
import org.fabric3.federation.event.RuntimeSynchronized;
import org.fabric3.model.type.component.Scope;
import org.fabric3.spi.classloader.ClassLoaderRegistry;
import org.fabric3.spi.classloader.MultiClassLoaderObjectInputStream;
import org.fabric3.spi.command.Command;
import org.fabric3.spi.component.InstanceLifecycleException;
import org.fabric3.spi.component.ScopeRegistry;
import org.fabric3.spi.event.EventService;
import org.fabric3.spi.executor.CommandExecutor;
import org.fabric3.spi.executor.CommandExecutorRegistry;
import org.fabric3.spi.executor.ExecutionException;

/**
 * A CommandExecutor that processes deployment commands on a participant node.
 *
 * @version $Rev$ $Date$
 */
@EagerInit
public class RuntimeDeploymentCommandExecutor implements CommandExecutor<RuntimeDeploymentCommand> {
    private CommandExecutorRegistry executorRegistry;
    private EventService eventService;
    private ScopeRegistry scopeRegistry;
    private RuntimeDeploymentCommandExecutorMonitor monitor;
    // indicates whether the runtime has been synchronized with the domain
    private boolean domainSynchronized;
    private ClassLoaderRegistry classLoaderRegistry;

    public RuntimeDeploymentCommandExecutor(@Reference CommandExecutorRegistry executorRegistry,
                                            @Reference EventService eventService,
                                            @Reference ScopeRegistry scopeRegistry,
                                            @Reference ClassLoaderRegistry classLoaderRegistry,
                                            @Monitor RuntimeDeploymentCommandExecutorMonitor monitor) {
        this.executorRegistry = executorRegistry;
        this.eventService = eventService;
        this.scopeRegistry = scopeRegistry;
        this.monitor = monitor;
        this.classLoaderRegistry = classLoaderRegistry;
    }

    @Init
    public void init() {
        executorRegistry.register(RuntimeDeploymentCommand.class, this);
    }

    public void execute(RuntimeDeploymentCommand command) throws ExecutionException {
        if (domainSynchronized && command.isSynchronization()) {
            // When a participant boots, it periodiclly issues synchronization requests to the zone manager until the first deployment command is
            // received. Since communications are asynchronous, it is possible multiple requests may be issued if a response is not received during
            // the elapsed time period. If this happens, only the first deployment command should be processed.
            eventService.publish(new RuntimeSynchronized());
            return;
        }
        String id = command.getId();
        monitor.receivedDeploymentCommand(id);

        // execute the extension commands first before deserializing the other commands as they may contain extension-specific metadata classes
        byte[] serializedExtensionCommands = command.getExtensionCommands();
        List<Command> extensionCommands = deserialize(serializedExtensionCommands);
        for (Command cmd : extensionCommands) {
            executorRegistry.execute(cmd);
        }
        try {
            scopeRegistry.getScopeContainer(Scope.COMPOSITE).reinject();
        } catch (InstanceLifecycleException e) {
            throw new ExecutionException(e);
        }

        byte[] serializedCommands = command.getCommands();
        List<Command> commands = deserialize(serializedCommands);
        for (Command cmd : commands) {
            executorRegistry.execute(cmd);
        }
        try {
            scopeRegistry.getScopeContainer(Scope.COMPOSITE).reinject();
        } catch (InstanceLifecycleException e) {
            throw new ExecutionException(e);
        }

        eventService.publish(new RuntimeSynchronized());
        domainSynchronized = true;
    }

    @SuppressWarnings({"unchecked"})
    private List<Command> deserialize(byte[] commands) throws ExecutionException {
        MultiClassLoaderObjectInputStream ois = null;
        try {
            InputStream stream = new ByteArrayInputStream(commands);
            // Deserialize the command set. As command set classes may be loaded in an extension classloader, use a MultiClassLoaderObjectInputStream
            // to deserialize classes in the appropriate classloader.
            ois = new MultiClassLoaderObjectInputStream(stream, classLoaderRegistry);
            return (List<Command>) ois.readObject();
        } catch (IOException e) {
            throw new ExecutionException(e);
        } catch (ClassNotFoundException e) {
            throw new ExecutionException(e);
        } finally {
            try {
                if (ois != null) {
                    ois.close();
                }
            } catch (IOException e) {
                // ignore;
            }
        }
    }

}