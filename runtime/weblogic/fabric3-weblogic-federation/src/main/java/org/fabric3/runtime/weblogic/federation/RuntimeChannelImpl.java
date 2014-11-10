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
 */
package org.fabric3.runtime.weblogic.federation;

import java.io.IOException;
import java.rmi.RemoteException;
import java.util.concurrent.atomic.AtomicBoolean;

import org.fabric3.runtime.weblogic.cluster.ChannelException;
import org.fabric3.runtime.weblogic.cluster.RuntimeChannel;
import org.fabric3.spi.classloader.SerializationService;
import org.fabric3.spi.container.ContainerException;
import org.fabric3.spi.container.executor.CommandExecutorRegistry;
import org.fabric3.spi.container.command.Command;
import org.fabric3.spi.container.command.Response;
import org.fabric3.spi.container.command.ResponseCommand;
import org.fabric3.spi.federation.topology.MessageReceiver;

/**
 * Standard implementation of a RuntimeChannel.
 */
public class RuntimeChannelImpl implements RuntimeChannel {
    private String runtimeName;
    private CommandExecutorRegistry executorRegistry;
    private SerializationService serializationService;
    private WebLogicTopologyMonitor monitor;
    private MessageReceiver messageReceiver;
    private AtomicBoolean active;

    public RuntimeChannelImpl(String runtimeName,
                              CommandExecutorRegistry executorRegistry,
                              SerializationService serializationService,
                              WebLogicTopologyMonitor monitor) {
        this(runtimeName, executorRegistry, serializationService, null, monitor);
    }

    public RuntimeChannelImpl(String runtimeName,
                              CommandExecutorRegistry executorRegistry,
                              SerializationService serializationService,
                              MessageReceiver messageReceiver,
                              WebLogicTopologyMonitor monitor) {
        this.runtimeName = runtimeName;
        this.executorRegistry = executorRegistry;
        this.serializationService = serializationService;
        this.messageReceiver = messageReceiver;
        this.monitor = monitor;
        this.active = new AtomicBoolean(true);
    }

    public String getRuntimeName() {
        return runtimeName;
    }

    public byte[] sendSynchronous(byte[] payload) throws RemoteException, ChannelException {
        if (!active.get()) {
            throw new ChannelException("Channel inactive");
        }
        try {
            ResponseCommand command = serializationService.deserialize(ResponseCommand.class, payload);
            executorRegistry.execute(command);
            Response response = command.getResponse();
            return serializationService.serialize(response);
        } catch (IOException | ClassNotFoundException e) {
            throw new ChannelException(e);
        } catch (ContainerException e) {
            // execution exception is not on the server classpath; record message only and log it locally
            monitor.error(e);
            throw new ChannelException(e.getMessage());
        }
    }

    public void send(byte[] payload) throws RemoteException, ChannelException {
        if (!active.get()) {
            throw new ChannelException("Channel inactive");
        }
        try {
            Command command = serializationService.deserialize(Command.class, payload);
            executorRegistry.execute(command);
        } catch (IOException | ClassNotFoundException e) {
            throw new ChannelException(e);
        } catch (ContainerException e) {
            // execution exception is not on the server classpath; record message only and log it locally
            monitor.error(e);
            throw new ChannelException(e.getMessage());
        }
    }

    public void publish(byte[] payload) throws RemoteException, ChannelException {
        if (!active.get()) {
            // ignore as pub/sub does not need to deliver to every runtime
            return;
        }
        if (messageReceiver == null) {
            throw new ChannelException("Channel not configured with a message receiver");
        }
        try {
            Object message = serializationService.deserialize(Object.class, payload);
            messageReceiver.onMessage(message);
        } catch (IOException | ClassNotFoundException e) {
            throw new ChannelException(e);
        }
    }

    public void shutdown() {
        active.set(false);
    }

    public boolean isActive() {
        return active.get();
    }
}