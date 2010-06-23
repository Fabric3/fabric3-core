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
package org.fabric3.federation.deployment.executor;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import org.oasisopen.sca.annotation.Destroy;
import org.osoa.sca.annotations.EagerInit;
import org.osoa.sca.annotations.Init;
import org.osoa.sca.annotations.Reference;

import org.fabric3.api.annotation.monitor.Monitor;
import org.fabric3.federation.deployment.command.DeploymentCommand;
import org.fabric3.federation.deployment.command.DeploymentErrorResponse;
import org.fabric3.federation.deployment.command.DeploymentResponse;
import org.fabric3.federation.deployment.command.SerializedDeploymentUnit;
import org.fabric3.federation.deployment.coordinator.DeploymentCache;
import org.fabric3.federation.deployment.coordinator.RollbackException;
import org.fabric3.federation.deployment.coordinator.RollbackService;
import org.fabric3.host.Fabric3Exception;
import org.fabric3.model.type.component.Scope;
import org.fabric3.spi.classloader.SerializationService;
import org.fabric3.spi.command.Command;
import org.fabric3.spi.command.CompensatableCommand;
import org.fabric3.spi.component.InstanceLifecycleException;
import org.fabric3.spi.component.ScopeRegistry;
import org.fabric3.spi.executor.CommandExecutor;
import org.fabric3.spi.executor.CommandExecutorRegistry;
import org.fabric3.spi.executor.ExecutionException;

/**
 * Processes {@link DeploymentCommand}s on a participant. If there is an error processing a command, a rollback to the previous runtime state will be
 * performed.
 * <p/>
 * This implementation queues deployment commands in the order they are received. A single, asynchronously executing message pump dequeues and
 * processes the deployment commands. This guarantees deployment commands are executed in the order they were received by the runtime.
 *
 * @version $Rev$ $Date$
 */
@EagerInit
public class DeploymentCommandExecutor implements CommandExecutor<DeploymentCommand> {
    private CommandExecutorRegistry executorRegistry;
    private DeploymentCache cache;
    private SerializationService serializationService;
    private RollbackService rollbackService;
    private ExecutorService executorService;
    private DeploymentCommandExecutorMonitor monitor;
    private ScopeRegistry scopeRegistry;
    private BlockingQueue<DeploymentCommand> deploymentQueue;
    private MessagePump messagePump;

    public DeploymentCommandExecutor(@Reference CommandExecutorRegistry executorRegistry,
                                     @Reference ScopeRegistry scopeRegistry,
                                     @Reference DeploymentCache cache,
                                     @Reference SerializationService serializationService,
                                     @Reference RollbackService rollbackService,
                                     @Reference ExecutorService executorService,
                                     @Monitor DeploymentCommandExecutorMonitor monitor) {
        this.executorRegistry = executorRegistry;
        this.cache = cache;
        this.serializationService = serializationService;
        this.rollbackService = rollbackService;
        this.executorService = executorService;
        this.monitor = monitor;
        this.scopeRegistry = scopeRegistry;
        this.deploymentQueue = new LinkedBlockingDeque<DeploymentCommand>();
    }

    @Init
    public void init() {
        messagePump = new MessagePump();
        executorService.execute(messagePump);
        executorRegistry.register(DeploymentCommand.class, this);
    }

    @Destroy
    public void destroy() {
        messagePump.stop();
    }

    public synchronized void execute(DeploymentCommand command) throws ExecutionException {
        try {
            monitor.received();
            deploymentQueue.put(command);
            DeploymentResponse response = new DeploymentResponse();
            command.setResponse(response);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Asynchronously de-queues and processes deployment commands in the order they were received by the runtime.
     */
    private class MessagePump implements Runnable {
        private AtomicBoolean active = new AtomicBoolean(true);

        public void run() {
            doRun();
            if (active.get()) {
                // reschedule
                executorService.execute(this);
            }
        }

        public void doRun() {
            try {
                DeploymentCommand command = deploymentQueue.poll(1000, TimeUnit.MILLISECONDS);
                if (command == null) {
                    // no deployment
                    return;
                }

                monitor.processing();

                // Execute the provision extension commands first before deserializing the others deployment commands.
                // Provision and extension commands resolve required artifacts, setup classloaders and initialize extensions.
                SerializedDeploymentUnit currentDeploymentUnit = command.getCurrentDeploymentUnit();
                byte[] serializedProvisionCommands = currentDeploymentUnit.getProvisionCommands();
                boolean result = execute(serializedProvisionCommands, command);
                if (!result) {
                    // failed and rolled back
                    return;
                }
                byte[] serializedExtensionCommands = currentDeploymentUnit.getExtensionCommands();
                result = execute(serializedExtensionCommands, command);
                if (!result) {
                    // failed and rolled back
                    return;
                }
                byte[] serializedCommands = currentDeploymentUnit.getCommands();
                result = execute(serializedCommands, command);
                if (!result) {
                    // failed and rolled back
                    return;
                }
                cacheDeployment(command);
            } catch (InterruptedException e) {
                active.set(false);
                Thread.currentThread().interrupt();
            }
        }

        private void stop() {
            active.set(false);
        }

        @SuppressWarnings({"unchecked"})
        private boolean execute(byte[] serializedCommands, DeploymentCommand command) {
            int marker = 0;
            List<CompensatableCommand> commands = null;
            try {
                commands = serializationService.deserialize(List.class, serializedCommands);
                for (Command cmd : commands) {
                    executorRegistry.execute(cmd);
                    marker++;
                }
                if (!commands.isEmpty()) {
                    scopeRegistry.getScopeContainer(Scope.COMPOSITE).reinject();
                }
                return true;
            } catch (IOException e) {
                // no rollback because the commands were never deserialized
                monitor.error(e);
                DeploymentErrorResponse response = new DeploymentErrorResponse(e);
                command.setResponse(response);
                return false;
            } catch (ClassNotFoundException e) {
                // no rollback because the commands were never deserialized
                monitor.error(e);
                DeploymentErrorResponse response = new DeploymentErrorResponse(e);
                command.setResponse(response);
                return false;
            } catch (InstanceLifecycleException e) {
                return handleRollback(command, marker, commands, e);
            } catch (ExecutionException e) {
                return handleRollback(command, marker, commands, e);
            }
        }

        private void cacheDeployment(DeploymentCommand command) {
            String zone = command.getZone();
            SerializedDeploymentUnit fullDeploymentUnit = command.getFullDeploymentUnit();
            DeploymentCommand deploymentCommand = new DeploymentCommand(zone, fullDeploymentUnit, fullDeploymentUnit);
            cache.cache(deploymentCommand);
        }

        private boolean handleRollback(DeploymentCommand command, int marker, List<CompensatableCommand> commands, Fabric3Exception e) {
            monitor.error(e);
            try {
                rollbackService.rollback(commands, marker);
            } catch (RollbackException e1) {
                monitor.error(e);
            }
            DeploymentErrorResponse response = new DeploymentErrorResponse(e);
            command.setResponse(response);
            return false;
        }


    }


}
