/*
 * Fabric3
 * Copyright (c) 2009-2013 Metaform Systems
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
package org.fabric3.federation.jgroups;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.Executor;

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

import org.fabric3.api.annotation.management.Management;
import org.fabric3.api.annotation.management.ManagementOperation;
import org.fabric3.api.host.runtime.HostInfo;
import org.fabric3.spi.command.Command;
import org.fabric3.spi.command.Response;
import org.fabric3.spi.command.ResponseCommand;
import org.fabric3.spi.runtime.event.EventService;
import org.fabric3.spi.runtime.event.Fabric3EventListener;
import org.fabric3.spi.runtime.event.JoinDomain;
import org.fabric3.spi.runtime.event.RuntimeStop;
import org.fabric3.spi.command.CommandExecutorRegistry;
import org.fabric3.spi.command.ExecutionException;
import org.fabric3.spi.federation.topology.MessageException;
import org.fabric3.spi.federation.topology.RemoteSystemException;

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
            } catch (MessageException e) {
                monitor.error("Error receiving message from: " + runtimeName, e);
            } catch (ExecutionException e) {
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
            } catch (MessageException e) {
                monitor.error("Error handling message from: " + runtimeName, e);
                RemoteSystemException ex = new RemoteSystemException(e);
                ex.setRuntimeName(runtimeName);
                try {
                    return helper.serialize(ex);
                } catch (MessageException e1) {
                    monitor.error("Error handling message from: " + runtimeName, e);
                }
            } catch (ExecutionException e) {
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