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
package org.fabric3.federation.deployment.executor;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import org.fabric3.api.annotation.monitor.Monitor;
import org.fabric3.api.host.Fabric3Exception;
import org.fabric3.api.model.type.component.Scope;
import org.fabric3.federation.deployment.command.DeploymentCommand;
import org.fabric3.federation.deployment.command.DeploymentErrorResponse;
import org.fabric3.federation.deployment.command.DeploymentResponse;
import org.fabric3.federation.deployment.command.SerializedDeploymentUnit;
import org.fabric3.federation.deployment.coordinator.DeploymentCache;
import org.fabric3.federation.deployment.coordinator.RollbackException;
import org.fabric3.federation.deployment.coordinator.RollbackService;
import org.fabric3.spi.classloader.SerializationService;
import org.fabric3.spi.container.ContainerException;
import org.fabric3.spi.container.executor.CommandExecutor;
import org.fabric3.spi.container.executor.CommandExecutorRegistry;
import org.fabric3.spi.container.component.InstanceLifecycleException;
import org.fabric3.spi.container.component.ScopeRegistry;
import org.fabric3.spi.container.command.Command;
import org.fabric3.spi.container.command.CompensatableCommand;
import org.oasisopen.sca.annotation.Destroy;
import org.oasisopen.sca.annotation.EagerInit;
import org.oasisopen.sca.annotation.Init;
import org.oasisopen.sca.annotation.Reference;

/**
 * Processes {@link DeploymentCommand}s on a participant. If there is an error processing a command, a rollback to the previous runtime state will be performed.
 * <p/> This implementation queues deployment commands in the order they are received. A single, asynchronously executing message pump dequeues and processes
 * the deployment commands. This guarantees deployment commands are executed in the order they were received by the runtime.
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
                                     @Reference(name = "executorService") ExecutorService executorService,
                                     @Monitor DeploymentCommandExecutorMonitor monitor) {
        this.executorRegistry = executorRegistry;
        this.cache = cache;
        this.serializationService = serializationService;
        this.rollbackService = rollbackService;
        this.executorService = executorService;
        this.monitor = monitor;
        this.scopeRegistry = scopeRegistry;
        this.deploymentQueue = new LinkedBlockingDeque<>();
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

    public synchronized void execute(DeploymentCommand command) throws ContainerException {
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
                monitor.completed();
            } catch (RuntimeException e) {
                monitor.errorMessage("Error performing deployment", e);
                throw e;
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
            } catch (ContainerException e) {
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
