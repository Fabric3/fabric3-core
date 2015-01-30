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
package org.fabric3.federation.jgroups;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.Executor;

import org.fabric3.api.annotation.management.Management;
import org.fabric3.api.annotation.management.ManagementOperation;
import org.fabric3.api.host.runtime.HostInfo;
import org.fabric3.spi.container.ContainerException;
import org.fabric3.spi.container.command.Command;
import org.fabric3.spi.container.command.Response;
import org.fabric3.spi.container.command.ResponseCommand;
import org.fabric3.spi.container.executor.CommandExecutorRegistry;
import org.fabric3.spi.federation.topology.MessageException;
import org.fabric3.spi.runtime.event.EventService;
import org.fabric3.spi.runtime.event.Fabric3EventListener;
import org.fabric3.spi.runtime.event.JoinDomain;
import org.fabric3.spi.runtime.event.RuntimeStop;
import org.jgroups.Channel;
import org.jgroups.JChannel;
import org.jgroups.Message;
import org.jgroups.MessageListener;
import org.jgroups.blocks.RequestHandler;
import org.jgroups.logging.CustomLogFactory;
import org.jgroups.protocols.TP;
import org.jgroups.protocols.pbcast.GMS;
import org.jgroups.stack.Protocol;
import org.oasisopen.sca.annotation.Init;
import org.oasisopen.sca.annotation.Property;
import org.oasisopen.sca.annotation.Reference;

/**
 * Provides base functionality for JGroups-based topology services.
 */
@Management
public abstract class AbstractTopologyService {
    protected String runtimeName;
    protected String domainName;

    protected HostInfo info;
    protected Executor executor;
    protected JGroupsHelper helper;
    protected CommandExecutorRegistry executorRegistry;
    protected EventService eventService;
    protected TopologyServiceMonitor monitor;

    protected String defaultBindAddress;
    protected boolean printLocalAddress;
    protected long defaultTimeout = 10000;
    // TODO add properties from http://community.jboss.org/wiki/SystemProps

    public AbstractTopologyService(HostInfo info,
                                   CommandExecutorRegistry executorRegistry,
                                   EventService eventService,
                                   Executor executor,
                                   JGroupsHelper helper,
                                   TopologyServiceMonitor monitor) {
        this.info = info;
        this.executorRegistry = executorRegistry;
        this.eventService = eventService;
        this.executor = executor;
        this.helper = helper;
        this.monitor = monitor;
        runtimeName = info.getRuntimeName();

    }

    @Property(required = false)
    public void setPrintLocalAddress(boolean printLocalAddress) {
        this.printLocalAddress = printLocalAddress;
    }

    @Property(required = false)
    @ManagementOperation(description = "Default timeout")
    public void setDefaultTimeout(long defaultTimeout) {
        this.defaultTimeout = defaultTimeout;
    }

    @ManagementOperation(description = "Default timeout")
    public long getDefaultTimeout() {
        return defaultTimeout;
    }

    @Property(required = false)
    public void setDefaultBindAddress(String defaultBindAddress) {
        this.defaultBindAddress = defaultBindAddress;
    }

    @ManagementOperation(description = "The runtime name")
    public String getRuntimeName() {
        return runtimeName;
    }

    @ManagementOperation(description = "Default bind address")
    public String getDefaultBindAddressSetting() {
        return defaultBindAddress;
    }

    @ManagementOperation(description = "True if the domain channel is connected")
    public boolean isConnected() {
        return getDomainChannel().isConnected();
    }

    @ManagementOperation(description = "The number of sent messages")
    public long getSentMessages() {
        return getDomainChannel().getSentMessages();
    }

    @ManagementOperation(description = "The number of sent bytes")
    public long getSentBytes() {
        return getDomainChannel().getSentBytes();
    }

    @ManagementOperation(description = "The number of received messages")
    public long getReceivedMessages() {
        return getDomainChannel().getReceivedMessages();
    }

    @ManagementOperation(description = "The number of received bytes")
    public long getReceivedBytes() {
        return getDomainChannel().getReceivedBytes();
    }

    @Reference
    public void setFactory(CustomLogFactory factory) {
        // reference needed to load custom log factory prior to JGroups classes
    }

    @Init
    public void init() throws Exception {
        // set the bind address if it is specified in the system configuration and not specified at JVM startup 
        if (defaultBindAddress != null && System.getProperty("jgroups.bind_addr") == null) {
            System.setProperty("jgroups.bind_addr", defaultBindAddress);
        }
        domainName = info.getDomain().getAuthority();

        // setup runtime notifications
        Fabric3EventListener<JoinDomain> joinListener = getJoinListener();
        eventService.subscribe(JoinDomain.class, joinListener);
        Fabric3EventListener<RuntimeStop> stopListener = getStopListener();
        eventService.subscribe(RuntimeStop.class, stopListener);
    }

    abstract JChannel getDomainChannel();

    abstract Fabric3EventListener<JoinDomain> getJoinListener();

    abstract Fabric3EventListener<RuntimeStop> getStopListener();

    protected void initializeChannel(Channel channel) {
        // turn off local message reception
        channel.setDiscardOwnMessages(true);

        TP transport = channel.getProtocolStack().getTransport();
        // Replace the default thread pool
        transport.setDefaultThreadPool(executor);

        for (Protocol protocol : channel.getProtocolStack().getProtocols()) {
            if (protocol instanceof GMS) {
                ((GMS) protocol).setPrintLocalAddr(printLocalAddress);
            }
        }
    }

    protected class Fabric3MessageListener implements MessageListener {

        public void receive(Message msg) {
            try {
                String runtimeName = org.jgroups.util.UUID.get(msg.getSrc());
                monitor.receiveMessage(runtimeName);
                Command command = (Command) helper.deserialize(msg.getBuffer());
                executorRegistry.execute(command);
            } catch (MessageException | ContainerException e) {
                monitor.error("Error receiving message from: " + runtimeName, e);
            }
        }

        public void getState(OutputStream output) throws Exception {

        }

        public void setState(InputStream input) throws Exception {

        }

    }

    protected class Fabric3RequestHandler implements RequestHandler {

        public Object handle(Message msg) {
            try {
                String runtimeName = org.jgroups.util.UUID.get(msg.getSrc());
                monitor.handleMessage(runtimeName);
                Object deserialized = helper.deserialize(msg.getBuffer());
                assert deserialized instanceof ResponseCommand;
                ResponseCommand command = (ResponseCommand) deserialized;
                executorRegistry.execute(command);
                Response response = command.getResponse();
                response.setRuntimeName(runtimeName);
                return helper.serialize(response);
            } catch (MessageException | ContainerException e) {
                monitor.error("Error handling message from: " + runtimeName, e);
                RemoteSystemException ex = new RemoteSystemException(e);
                ex.setRuntimeName(runtimeName);
                try {
                    return helper.serialize(ex);
                } catch (MessageException e1) {
                    monitor.error("Error handling message from: " + runtimeName, e);
                }
            }
            throw new MessageRuntimeException("Unable to handle request");
        }
    }

}