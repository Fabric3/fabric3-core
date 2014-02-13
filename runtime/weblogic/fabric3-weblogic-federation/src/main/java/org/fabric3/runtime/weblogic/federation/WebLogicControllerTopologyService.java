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
package org.fabric3.runtime.weblogic.federation;

import javax.management.JMException;
import javax.naming.Binding;
import javax.naming.CommunicationException;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NameAlreadyBoundException;
import javax.naming.NameNotFoundException;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.event.EventContext;
import java.io.IOException;
import java.io.Serializable;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.fabric3.api.annotation.monitor.Monitor;
import org.fabric3.runtime.weblogic.cluster.ChannelException;
import org.fabric3.runtime.weblogic.cluster.RuntimeChannel;
import org.fabric3.spi.classloader.SerializationService;
import org.fabric3.spi.command.Command;
import org.fabric3.spi.command.Response;
import org.fabric3.spi.command.ResponseCommand;
import org.fabric3.spi.federation.topology.MessageReceiver;
import org.fabric3.spi.federation.topology.TopologyListener;
import org.fabric3.spi.federation.topology.ZoneChannelException;
import org.fabric3.spi.runtime.event.EventService;
import org.fabric3.spi.runtime.event.Fabric3EventListener;
import org.fabric3.spi.runtime.event.JoinDomain;
import org.fabric3.spi.runtime.event.RuntimeStop;
import org.fabric3.spi.command.CommandExecutorRegistry;
import org.fabric3.spi.federation.topology.ControllerTopologyService;
import org.fabric3.spi.federation.topology.MessageException;
import org.fabric3.spi.federation.topology.RuntimeInstance;
import org.fabric3.spi.federation.topology.Zone;
import org.oasisopen.sca.annotation.EagerInit;
import org.oasisopen.sca.annotation.Init;
import org.oasisopen.sca.annotation.Reference;
import org.oasisopen.sca.annotation.Service;
import static org.fabric3.runtime.weblogic.federation.Constants.CONTROLLER_CHANNEL;
import static org.fabric3.runtime.weblogic.federation.Constants.DYNAMIC_CHANNEL_CONTEXT;
import static org.fabric3.runtime.weblogic.federation.Constants.PARTICIPANT_CONTEXT;

/**
 * Provides domain-wide controller communication using the WebLogic clustered JNDI tree.
 */
@Service(ControllerTopologyService.class)
@EagerInit
public class WebLogicControllerTopologyService implements ControllerTopologyService {
    private static final String RUNTIME_NAME = "controller";

    private CommandExecutorRegistry executorRegistry;
    private EventService eventService;
    private SerializationService serializationService;
    private WebLogicTopologyMonitor monitor;
    private InitialContext rootContext;
    private EventContext participantContext;
    private RuntimeChannelImpl controllerChannel;
    private JmxHelper jmxHelper;
    private String domainName;

    private Set<String> openChannels = new HashSet<>();

    public WebLogicControllerTopologyService(@Reference CommandExecutorRegistry executorRegistry,
                                             @Reference EventService eventService,
                                             @Reference SerializationService serializationService,
                                             @Reference JmxHelper jmxHelper,
                                             @Monitor WebLogicTopologyMonitor monitor) {
        this.executorRegistry = executorRegistry;
        this.eventService = eventService;
        this.serializationService = serializationService;
        this.jmxHelper = jmxHelper;
        this.monitor = monitor;
    }

    @Init
    public void init() throws NamingException, JMException {
        domainName = jmxHelper.getRuntimeJmxAttribute(String.class, "DomainConfiguration/Name");
        eventService.subscribe(JoinDomain.class, new JoinDomainListener());
        eventService.subscribe(RuntimeStop.class, new RuntimeStopListener());
    }

    public Set<Zone> getZones() {
        List<RuntimeInstance> runtimes = getRuntimes();
        if (runtimes.isEmpty()) {
            return Collections.emptySet();
        }
        Zone zone = new Zone(domainName, runtimes);
        return Collections.singleton(zone);
    }

    public List<RuntimeInstance> getRuntimes() {
        List<RuntimeInstance> instances = new ArrayList<>();
        try {
            NamingEnumeration<Binding> list = rootContext.listBindings(PARTICIPANT_CONTEXT);
            while (list.hasMore()) {
                Binding binding = list.next();
                RuntimeChannel channel = (RuntimeChannel) binding.getObject();
                String RUNTIME_NAME = channel.getRuntimeName();
                RuntimeInstance runtimeInstance = new RuntimeInstance(RUNTIME_NAME);
                instances.add(runtimeInstance);
            }
        } catch (NamingException | RemoteException e) {
            monitor.error(e);
        }
        return instances;
    }

    public void broadcast(Command command) throws MessageException {
        try {
            byte[] payload = serializationService.serialize(command);
            NamingEnumeration<Binding> list = rootContext.listBindings(PARTICIPANT_CONTEXT);
            while (list.hasMore()) {
                Binding binding = list.next();
                RuntimeChannel channel = (RuntimeChannel) binding.getObject();
                if (channel.isActive()) {
                    channel.send(payload);
                }
            }
        } catch (NamingException | ChannelException | IOException e) {
            throw new MessageException(e);
        }
    }

    public void broadcast(String zoneName, Command command) throws MessageException {
        List<RuntimeChannel> channels = getChannels();
        try {
            byte[] payload = serializationService.serialize(command);
            for (RuntimeChannel channel : channels) {
                if (channel.isActive()) {
                    channel.send(payload);
                }
            }
        } catch (IOException | ChannelException e) {
            throw new MessageException(e);
        }
    }

    public List<Response> sendSynchronousToZone(String zoneName, ResponseCommand command, boolean failFast, long timeout) throws MessageException {
        List<RuntimeChannel> channels = getChannels();
        if (channels.isEmpty()) {
            throw new MessageException("No managed servers found to deploy to");
        }
        List<Response> responses = new ArrayList<>();
        byte[] payload;
        try {
            payload = serializationService.serialize(command);
        } catch (IOException e) {
            throw new MessageException(e);
        }
        for (RuntimeChannel channel : channels) {
            try {
                if (!channel.isActive()) {
                    continue;
                }
                byte[] responsePayload = channel.sendSynchronous(payload);
                Response response = serializationService.deserialize(Response.class, responsePayload);
                responses.add(response);
                // TODO handle exceptions and rollback
            } catch (ChannelException | ClassNotFoundException | IOException e) {
                throw new MessageException(e);
            }
        }
        return responses;
    }

    public boolean isChannelOpen(String name) {
        return openChannels.contains(name);
    }

    public void openChannel(String name, String configuration, MessageReceiver receiver, TopologyListener listener) throws ZoneChannelException {
        if (isChannelOpen(name)) {
            throw new ZoneChannelException("Channel already open: " + name);
        }

        Context rootContext = null;
        Context dynamicChannelContext = null;
        RuntimeChannelImpl channel = new RuntimeChannelImpl(RUNTIME_NAME, executorRegistry, serializationService, receiver, monitor);
        try {
            dynamicChannelContext = JndiHelper.getContext(DYNAMIC_CHANNEL_CONTEXT, rootContext);
            dynamicChannelContext.bind(name + ":" + RUNTIME_NAME, channel);
            openChannels.add(name);
        } catch (NameAlreadyBoundException e) {
            try {
                dynamicChannelContext.rebind(name + ":" + RUNTIME_NAME, channel);
            } catch (NamingException ex) {
                monitor.errorMessage("Error binding channel: " + name);
                monitor.errorDetail(e);
            }
        } catch (NamingException e) {
            // controller may not be available
            monitor.errorMessage("Error binding channel: " + name);
            monitor.errorDetail(e);
        } finally {
            JndiHelper.close(rootContext, dynamicChannelContext);
        }
    }

    public void closeChannel(String name) throws ZoneChannelException {
        Context rootContext = null;
        Context dynamicChannelContext = null;
        try {
            dynamicChannelContext = JndiHelper.getContext(DYNAMIC_CHANNEL_CONTEXT, rootContext);
            dynamicChannelContext.unbind(name + ":" + RUNTIME_NAME);
            openChannels.remove(name);
        } catch (CommunicationException e) {
            // Controller was not available. Ignore since the controller could have been shutdown before the participant
        } catch (NamingException e) {
            throw new ZoneChannelException(e);
        } finally {
            JndiHelper.close(rootContext, dynamicChannelContext);
        }
    }

    public void sendAsynchronous(String name, Serializable message) throws MessageException {
        Context rootContext = null;
        try {
            byte[] payload = serializationService.serialize(message);
            NamingEnumeration<Binding> enumeration = rootContext.listBindings(DYNAMIC_CHANNEL_CONTEXT);
            while (enumeration.hasMoreElements()) {
                Binding binding = enumeration.next();
                if (RuntimeChannel.class.getName().equals(binding.getClassName())) {
                    RuntimeChannel channel = (RuntimeChannel) binding.getObject();
                    if (binding.getName().startsWith(name + ":")) {
                        if (channel.getRuntimeName().equals(RUNTIME_NAME)) {
                            // don't send to self
                            continue;
                        }
                        channel.publish(payload);
                    }
                }
            }
        } catch (NamingException | ChannelException | IOException e) {
            throw new MessageException(e);
        } finally {
            JndiHelper.close(rootContext);
        }
    }

    private List<RuntimeChannel> getChannels() throws MessageException {
        try {
            List<RuntimeChannel> channels = new ArrayList<>();
            NamingEnumeration<Binding> list = rootContext.listBindings(PARTICIPANT_CONTEXT);
            while (list.hasMore()) {
                Binding binding = list.next();
                RuntimeChannel channel = (RuntimeChannel) binding.getObject();
                channels.add(channel);
            }
            return channels;
        } catch (NameNotFoundException e) {
            // managed servers may not have initialized
            monitor.noManagedServers();
            return Collections.emptyList();
        } catch (NamingException e) {
            throw new MessageException(e);
        }
    }

    private void bindController() {
        try {
            controllerChannel = new RuntimeChannelImpl(RUNTIME_NAME, executorRegistry, serializationService, monitor);
            rootContext = new InitialContext();
            Context controllerContext = JndiHelper.getContext(Constants.CONTROLLER_CONTEXT, rootContext);
            try {
                controllerContext.bind(CONTROLLER_CHANNEL, controllerChannel);
            } catch (NameAlreadyBoundException e) {
                controllerContext.rebind(CONTROLLER_CHANNEL, controllerChannel);
            }
            Context ctx = JndiHelper.getContext(PARTICIPANT_CONTEXT, rootContext);
            assert ctx instanceof EventContext;
            participantContext = (EventContext) ctx;
        } catch (NamingException e) {
            monitor.errorMessage("Error initializing domain topology service", e);
        } catch (RuntimeException e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * Joins the domain by binding controller federation channels into the JNDI tree.
     */
    private class JoinDomainListener implements Fabric3EventListener<JoinDomain> {

        public void onEvent(JoinDomain event) {
            bindController();
        }
    }

    /**
     * Unbinds controller federation channels from the JNDI tree.
     */
    private class RuntimeStopListener implements Fabric3EventListener<RuntimeStop> {

        public void onEvent(RuntimeStop event) {
            if (controllerChannel != null) {
                // shutdown the controller channel as it may take a while to remove it from the distributed JNDI tree
                controllerChannel.shutdown();
            }
            if (rootContext != null) {
                try {
                    rootContext.unbind(CONTROLLER_CHANNEL);
                    rootContext.close();
                } catch (NamingException e) {
                    monitor.error(e);
                }
            }
            if (participantContext != null) {
                try {
                    participantContext.close();
                } catch (NamingException e) {
                    monitor.error(e);
                }
            }
        }
    }

}
