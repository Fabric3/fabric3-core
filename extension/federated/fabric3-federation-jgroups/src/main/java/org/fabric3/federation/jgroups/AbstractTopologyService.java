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
package org.fabric3.federation.jgroups;

import java.util.UUID;
import java.util.concurrent.Executor;

import org.jgroups.Channel;
import org.jgroups.ChannelException;
import org.jgroups.JChannel;
import org.jgroups.Message;
import org.jgroups.MessageListener;
import org.jgroups.blocks.RequestHandler;
import org.jgroups.logging.LogFactory;
import org.jgroups.protocols.TP;
import org.jgroups.protocols.pbcast.GMS;
import org.jgroups.stack.Protocol;
import org.osoa.sca.annotations.Init;
import org.osoa.sca.annotations.Property;

import org.fabric3.host.runtime.HostInfo;
import org.fabric3.spi.command.Command;
import org.fabric3.spi.command.ResponseCommand;
import org.fabric3.spi.event.EventService;
import org.fabric3.spi.event.Fabric3EventListener;
import org.fabric3.spi.event.JoinDomain;
import org.fabric3.spi.event.RuntimeStop;
import org.fabric3.spi.executor.CommandExecutorRegistry;
import org.fabric3.spi.executor.ExecutionException;
import org.fabric3.spi.topology.MessageException;
import org.fabric3.spi.topology.RemoteSystemException;
import org.fabric3.spi.topology.Response;

/**
 * Provides base functionality for JGroups-based topology services.
 *
 * @version $Rev$ $Date$
 */
public abstract class AbstractTopologyService {
    protected String runtimeName;
    protected String runtimeId;
    protected String domainName;

    protected HostInfo info;
    protected Executor executor;
    protected JGroupsHelper helper;
    protected CommandExecutorRegistry executorRegistry;
    protected EventService eventService;
    protected TopologyServiceMonitor monitor;

    protected boolean printlocalAddress;
    protected String logLevel = "error";
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
    }

    @Property(required = false)
    public void setLogLevel(String logLevel) {
        this.logLevel = logLevel;
    }

    // TODO migration issue: prviously, set runtime name in system config
    @Property(required = false)
    public void setRuntimeId(String runtimeId) {
        this.runtimeId = runtimeId;
    }

    @Property(required = false)
    public void setPrintlocalAddress(boolean printlocalAddress) {
        this.printlocalAddress = printlocalAddress;
    }

    @Init
    public void init() throws ChannelException {
        LogFactory.getLog(JChannel.class).setLevel(logLevel);
        domainName = info.getDomain().getAuthority();
        if (runtimeId == null) {
            runtimeId = UUID.randomUUID().toString();
        }
        runtimeName = getRuntimeName();

        // setup runtime notifications
        Fabric3EventListener<JoinDomain> joinListener = getJoinListener();
        eventService.subscribe(JoinDomain.class, joinListener);
        Fabric3EventListener<RuntimeStop> stopListener = getStopListener();
        eventService.subscribe(RuntimeStop.class, stopListener);
    }

    abstract Fabric3EventListener<JoinDomain> getJoinListener();

    abstract Fabric3EventListener<RuntimeStop> getStopListener();

    abstract String getRuntimeName();

    protected void initializeChannel(Channel channel) {
        // turn off local mesage reception
        channel.setOpt(Channel.LOCAL, false);

        TP transport = channel.getProtocolStack().getTransport();
        // Replace the default thread pool
        transport.setDefaultThreadPool(executor);

        // set the log level
        transport.setLevel(logLevel);

        for (Protocol protocol : channel.getProtocolStack().getProtocols()) {
            protocol.setLevel(logLevel);
            if (protocol instanceof GMS) {
                ((GMS) protocol).setPrintLocalAddr(printlocalAddress);
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
                monitor.error("Error receiving message from: "+ runtimeName, e);
            } catch (ExecutionException e) {
                monitor.error("Error receiving message from: "+ runtimeName, e);
            }
        }

        public byte[] getState() {
            return new byte[0];
        }

        public void setState(byte[] state) {
            // no-op
        }
    }

    protected class Fabric3RequestHandler implements RequestHandler {

        public Object handle(Message msg) {
            try {
                String runtimeName = org.jgroups.util.UUID.get(msg.getSrc());
                monitor.handleMessage(runtimeName);
                Object deserialized =  helper.deserialize(msg.getBuffer());
                assert deserialized instanceof ResponseCommand;
                ResponseCommand command = (ResponseCommand) deserialized;
                executorRegistry.execute(command);
                Response response = command.getResponse();
                response.setRuntimeName(getRuntimeName());
                return helper.serialize(response);
            } catch (MessageException e) {
                monitor.error("Error handling message from: "+ runtimeName, e);
                RemoteSystemException ex = new RemoteSystemException(e);
                ex.setRuntimeName(getRuntimeName());
                try {
                    return helper.serialize(ex);
                } catch (MessageException e1) {
                    monitor.error("Error handling message from: "+ runtimeName, e);
                }
            } catch (ExecutionException e) {
                monitor.error("Error handling message from: "+ runtimeName, e);
                RemoteSystemException ex = new RemoteSystemException(e);
                ex.setRuntimeName(getRuntimeName());
                try {
                    return helper.serialize(ex);
                } catch (MessageException e1) {
                    monitor.error("Error handling message from: "+ runtimeName, e);
                }
            }
            throw new MessageRuntimeException("Unable to handle request");
        }
    }


}