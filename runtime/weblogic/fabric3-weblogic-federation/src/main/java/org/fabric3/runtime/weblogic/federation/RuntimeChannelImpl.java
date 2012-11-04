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
package org.fabric3.runtime.weblogic.federation;

import java.io.IOException;
import java.rmi.RemoteException;
import java.util.concurrent.atomic.AtomicBoolean;

import org.fabric3.runtime.weblogic.cluster.ChannelException;
import org.fabric3.runtime.weblogic.cluster.RuntimeChannel;
import org.fabric3.spi.classloader.SerializationService;
import org.fabric3.spi.command.Command;
import org.fabric3.spi.command.Response;
import org.fabric3.spi.command.ResponseCommand;
import org.fabric3.spi.executor.CommandExecutorRegistry;
import org.fabric3.spi.executor.ExecutionException;
import org.fabric3.spi.federation.MessageReceiver;

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
        } catch (IOException e) {
            throw new ChannelException(e);
        } catch (ClassNotFoundException e) {
            throw new ChannelException(e);
        } catch (ExecutionException e) {
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
        } catch (IOException e) {
            throw new ChannelException(e);
        } catch (ClassNotFoundException e) {
            throw new ChannelException(e);
        } catch (ExecutionException e) {
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
        } catch (IOException e) {
            throw new ChannelException(e);
        } catch (ClassNotFoundException e) {
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