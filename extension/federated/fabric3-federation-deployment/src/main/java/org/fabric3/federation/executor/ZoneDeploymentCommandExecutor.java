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
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.osoa.sca.annotations.EagerInit;
import org.osoa.sca.annotations.Init;
import org.osoa.sca.annotations.Reference;

import org.fabric3.api.annotation.Monitor;
import org.fabric3.federation.command.RuntimeDeploymentCommand;
import org.fabric3.federation.command.ZoneDeploymentCommand;
import org.fabric3.federation.event.RuntimeSynchronized;
import org.fabric3.model.type.component.Scope;
import org.fabric3.spi.classloader.ClassLoaderRegistry;
import org.fabric3.spi.classloader.MultiClassLoaderObjectInputStream;
import org.fabric3.spi.classloader.MultiClassLoaderObjectOutputStream;
import org.fabric3.spi.command.Command;
import org.fabric3.spi.component.InstanceLifecycleException;
import org.fabric3.spi.component.ScopeRegistry;
import org.fabric3.spi.event.EventService;
import org.fabric3.spi.executor.CommandExecutor;
import org.fabric3.spi.executor.CommandExecutorRegistry;
import org.fabric3.spi.executor.ExecutionException;
import org.fabric3.spi.topology.MessageException;
import org.fabric3.spi.topology.RuntimeInstance;
import org.fabric3.spi.topology.RuntimeService;
import org.fabric3.spi.topology.ZoneManager;

/**
 * Processes a ZoneDeploymentCommand. This may result in routing the command locally, to an individual runtime, or to  all runtimes in a zone
 * depending on the correlation semantics.
 *
 * @version $Rev$ $Date$
 */
@EagerInit
public class ZoneDeploymentCommandExecutor implements CommandExecutor<ZoneDeploymentCommand> {
    private ZoneManager zoneManager;
    private CommandExecutorRegistry executorRegistry;
    private RuntimeService runtimeService;
    private ScopeRegistry scopeRegistry;
    private EventService eventService;
    private ZoneDeploymentCommandExecutorMonitor monitor;
    private boolean domainSynchronized;
    private ClassLoaderRegistry classLoaderRegistry;

    public ZoneDeploymentCommandExecutor(@Reference ZoneManager zoneManager,
                                         @Reference CommandExecutorRegistry executorRegistry,
                                         @Reference RuntimeService runtimeService,
                                         @Reference ScopeRegistry scopeRegistry,
                                         @Reference EventService eventService,
                                         @Reference ClassLoaderRegistry classLoaderRegistry,
                                         @Monitor ZoneDeploymentCommandExecutorMonitor monitor) {
        this.zoneManager = zoneManager;
        this.executorRegistry = executorRegistry;
        this.runtimeService = runtimeService;
        this.scopeRegistry = scopeRegistry;
        this.eventService = eventService;
        this.monitor = monitor;
        this.classLoaderRegistry = classLoaderRegistry;
    }

    @Init
    public void init() {
        executorRegistry.register(ZoneDeploymentCommand.class, this);
    }

    public void execute(ZoneDeploymentCommand command) throws ExecutionException {
        String correlationId = command.getCorrelationId();
        if (correlationId != null) {
            // the command is destined to a specific runtime
            routeToRuntime(command);
        } else {
            // route the command to all runtimes in the zone
            routeToZone(command);
        }
        domainSynchronized = true;
    }

    private void routeToRuntime(ZoneDeploymentCommand command) throws ExecutionException {
        String correlationId = command.getCorrelationId();
        String runtimeName = runtimeService.getRuntimeName();
        // route the command to a specific runtime
        if (correlationId.equals(runtimeName)) {
            if ((domainSynchronized && command.isSynchronization())) {
                // the zone is already synchronized, ignore as this may be a duplicate
                eventService.publish(new RuntimeSynchronized());
                return;
            }
            routeLocally(command);
        } else {
            String id = command.getId();
            boolean routed = false;
            for (RuntimeInstance runtime : zoneManager.getRuntimes()) {
                String target = runtime.getName();
                if (target.equals(correlationId)) {
                    // deploy to the runtime
                    try {
                        byte[] serialized = serializeRuntimeCommand(command);
                        zoneManager.sendMessage(target, serialized);
                    } catch (IOException e) {
                        throw new ExecutionException(e);
                    } catch (MessageException e) {
                        throw new ExecutionException(e);
                    }
                    monitor.routed(target, id);
                    routed = true;
                }
            }
            if (!routed) {
                throw new NoTargetRuntimeException("Runtime " + correlationId + " not found for deployment command: " + id);
            }
        }

    }

    private void routeToZone(ZoneDeploymentCommand command) throws ExecutionException {
        String runtimeName = runtimeService.getRuntimeName();
        // route the command to all runtimes in the zone
        for (RuntimeInstance runtime : zoneManager.getRuntimes()) {
            String target = runtime.getName();
            if (runtimeName.equals(target)) {
                routeLocally(command);
            } else {
                try {
                    // deploy to the runtime
                    byte[] serialized = serializeRuntimeCommand(command);
                    zoneManager.sendMessage(target, serialized);
                } catch (IOException e) {
                    throw new ExecutionException(e);
                } catch (MessageException e) {
                    throw new ExecutionException(e);
                }
                String id = command.getId();
                monitor.routed(target, id);
            }
        }

    }

    private synchronized void routeLocally(ZoneDeploymentCommand command) throws ExecutionException {
        String runtimeName = runtimeService.getRuntimeName();
        String id = command.getId();
        monitor.routed(runtimeName, id);
        eventService.publish(new RuntimeSynchronized());

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
    }

    private byte[] serializeRuntimeCommand(ZoneDeploymentCommand command) throws IOException {
        ByteArrayOutputStream bas = new ByteArrayOutputStream();
        MultiClassLoaderObjectOutputStream stream = new MultiClassLoaderObjectOutputStream(bas);
        String id = command.getId();
        byte[] extensionCommands = command.getExtensionCommands();
        byte[] commands = command.getCommands();
        boolean synchronization = command.isSynchronization();
        RuntimeDeploymentCommand runtimeCommand = new RuntimeDeploymentCommand(id, extensionCommands, commands, synchronization);
        stream.writeObject(runtimeCommand);
        stream.close();
        return bas.toByteArray();
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
