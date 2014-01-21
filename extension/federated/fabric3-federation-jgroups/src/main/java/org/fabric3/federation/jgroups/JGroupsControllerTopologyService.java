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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;

import org.fabric3.api.annotation.management.Management;
import org.fabric3.api.annotation.management.ManagementOperation;
import org.fabric3.api.annotation.monitor.Monitor;
import org.fabric3.api.host.runtime.HostInfo;
import org.fabric3.federation.deployment.command.ControllerAvailableCommand;
import org.fabric3.spi.command.Command;
import org.fabric3.spi.command.CommandExecutorRegistry;
import org.fabric3.spi.command.Response;
import org.fabric3.spi.command.ResponseCommand;
import org.fabric3.spi.federation.topology.ControllerTopologyService;
import org.fabric3.spi.federation.topology.ErrorResponse;
import org.fabric3.spi.federation.topology.MessageException;
import org.fabric3.spi.federation.topology.MessageReceiver;
import org.fabric3.spi.federation.topology.RemoteSystemException;
import org.fabric3.spi.federation.topology.RuntimeInstance;
import org.fabric3.spi.federation.topology.TopologyListener;
import org.fabric3.spi.federation.topology.Zone;
import org.fabric3.spi.federation.topology.ZoneChannelException;
import org.fabric3.spi.runtime.event.EventService;
import org.fabric3.spi.runtime.event.Fabric3EventListener;
import org.fabric3.spi.runtime.event.JoinDomain;
import org.fabric3.spi.runtime.event.RuntimeStart;
import org.fabric3.spi.runtime.event.RuntimeStop;
import org.jgroups.Address;
import org.jgroups.Channel;
import org.jgroups.JChannel;
import org.jgroups.MembershipListener;
import org.jgroups.Message;
import org.jgroups.SuspectedException;
import org.jgroups.TimeoutException;
import org.jgroups.View;
import org.jgroups.blocks.MessageDispatcher;
import org.jgroups.blocks.RequestOptions;
import org.jgroups.blocks.ResponseMode;
import org.jgroups.util.UUID;
import org.oasisopen.sca.annotation.EagerInit;
import org.oasisopen.sca.annotation.Init;
import org.oasisopen.sca.annotation.Property;
import org.oasisopen.sca.annotation.Reference;
import org.w3c.dom.Element;

/**
 * JGroups implementation of the {@link ControllerTopologyService}.
 */
@EagerInit
@Management(name = "ControllerTopologyService", path = "/runtime/federation/controller/view")
public class JGroupsControllerTopologyService extends AbstractTopologyService implements ControllerTopologyService {
    private JChannel domainChannel;
    private MessageDispatcher dispatcher;
    private JoinEventListener joinListener;
    private RuntimeStopEventListener stopListener;
    private View previousView;
    private List<TopologyListener> topologyListeners = new ArrayList<TopologyListener>();
    private Map<String, Map<String, RuntimeInstance>> runtimes = new ConcurrentHashMap<String, Map<String, RuntimeInstance>>();
    private Map<String, Channel> channels = new ConcurrentHashMap<String, Channel>();
    private Element channelConfig;

    public JGroupsControllerTopologyService(@Reference HostInfo info,
                                            @Reference CommandExecutorRegistry executorRegistry,
                                            @Reference EventService eventService,
                                            @Reference Executor executor,
                                            @Reference JGroupsHelper helper,
                                            @Monitor TopologyServiceMonitor monitor) {
        super(info, executorRegistry, eventService, executor, helper, monitor);
    }

    @Reference(required = false)
    public void setTopologyListeners(List<TopologyListener> topologyListeners) {
        this.topologyListeners = topologyListeners;
    }

    @Init
    public void init() throws Exception {
        super.init();
        if (channelConfig != null) {
            domainChannel = new JChannel(channelConfig);
        } else {
            domainChannel = new JChannel();
        }

        domainChannel.setName(runtimeName);

        initializeChannel(domainChannel);

        Fabric3MessageListener domainMessageListener = new Fabric3MessageListener();
        Fabric3RequestHandler domainRequestHandler = new Fabric3RequestHandler();
        DomainMembershipListener membershipListener = new DomainMembershipListener();

        dispatcher = new MessageDispatcher(domainChannel, domainMessageListener, membershipListener, domainRequestHandler);

        // setup runtime notifications
        joinListener = new JoinEventListener();
        eventService.subscribe(JoinDomain.class, joinListener);
        eventService.subscribe(RuntimeStart.class, new RuntimeStartEventListener());
        stopListener = new RuntimeStopEventListener();
        eventService.subscribe(RuntimeStop.class, stopListener);
    }

    @ManagementOperation(description = "The zones in the domain")
    public Set<Zone> getZones() {
        return helper.getZones(runtimes);
    }

    @ManagementOperation(description = "The runtimes in the domain")
    public List<String> getRuntimeNames() {
        List<String> runtimes = new ArrayList<String>();
        for (Address member : domainChannel.getView().getMembers()) {
            String name = org.jgroups.util.UUID.get(member);
            runtimes.add(name);
        }
        return runtimes;
    }

    @Property(required = false)
    public void setChannelConfig(Element config) {
        this.channelConfig = (Element) config.getElementsByTagName("config").item(0);
    }

    public List<RuntimeInstance> getRuntimes() {
        List<RuntimeInstance> list = new ArrayList<RuntimeInstance>();
        for (Map<String, RuntimeInstance> map : runtimes.values()) {
            for (RuntimeInstance runtime : map.values()) {
                list.add(runtime);
            }
        }
        return list;
    }

    public void broadcast(String zoneName, Command command) throws MessageException {
        try {
            byte[] payload = helper.serialize(command);
            List<Address> addresses = helper.getRuntimeAddressesInZone(zoneName, domainChannel.getView());
            for (Address address : addresses) {
                Message message = new Message(address, domainChannel.getAddress(), payload);
                domainChannel.send(message);
            }
        } catch (Exception e) {
            throw new MessageException("Error sending message to zone: " + zoneName, e);
        }
    }

    public void broadcast(Command command) throws MessageException {
        byte[] payload = helper.serialize(command);
        Message message = new Message(null, domainChannel.getAddress(), payload);
        try {
            domainChannel.send(message);
        } catch (Exception e) {
            throw new MessageException(e);
        }
    }

    public List<Response> sendSynchronousToZone(String zoneName, ResponseCommand command, boolean failFast, long timeout) throws MessageException {
        byte[] payload = helper.serialize(command);
        List<Address> addresses = helper.getRuntimeAddressesInZone(zoneName, domainChannel.getView());
        List<Response> responses = new ArrayList<Response>(addresses.size());
        for (Address address : addresses) {
            Message message = new Message(address, domainChannel.getAddress(), payload);
            try {
                RequestOptions options = new RequestOptions(ResponseMode.GET_ALL, timeout);
                Object o = dispatcher.sendMessage(message, options);
                if (o instanceof Exception) {
                    // an error was returned by the other end
                    RemoteSystemException response = new RemoteSystemException((Exception) o);
                    response.setRuntimeName(UUID.get(address));
                    responses.add(response);
                    if (failFast) {
                        // abort sending to remaining runtimes as an error was encountered and fail-fast behavior is enforced
                        break;
                    }
                } else if (o instanceof byte[]) {
                    Object deserialized = helper.deserialize((byte[]) o);
                    if (deserialized instanceof Response) {
                        Response response = (Response) deserialized;
                        responses.add(response);
                        if (failFast && response instanceof ErrorResponse) {
                            // abort sending to remaining runtimes as an error was encountered and fail-fast behavior is enforced
                            break;
                        }
                    } else {
                        throw new AssertionError("Unknown response type: " + deserialized);
                    }
                } else {
                    throw new AssertionError("Unknown response type: " + o);
                }
            } catch (TimeoutException e) {
                RemoteSystemException response = new RemoteSystemException(e);
                response.setRuntimeName(UUID.get(address));
                responses.add(response);
                break;
            } catch (SuspectedException e) {
                RemoteSystemException response = new RemoteSystemException(e);
                response.setRuntimeName(UUID.get(address));
                responses.add(response);
                break;
            } catch (MessageException e) {
                RemoteSystemException response = new RemoteSystemException(e);
                response.setRuntimeName(UUID.get(address));
                responses.add(response);
                break;
            } catch (Exception e) {
                RemoteSystemException response = new RemoteSystemException(e);
                response.setRuntimeName(UUID.get(address));
                responses.add(response);
                break;
            }
        }
        return responses;
    }

    public boolean isChannelOpen(String name) {
        return channels.containsKey(name);
    }

    public void openChannel(String name, String configuration, MessageReceiver receiver, TopologyListener listener) throws ZoneChannelException {
        if (channels.containsKey(name)) {
            throw new ZoneChannelException("Channel already open:" + name);
        }
        try {

            Channel channel;
            if (configuration != null) {
                channel = new JChannel(configuration);
            } else if (channelConfig != null) {
                channel = new JChannel(channelConfig);
            } else {
                channel = new JChannel();
            }
            channel.setName(runtimeName);
            initializeChannel(channel);
            channels.put(name, channel);

            Object viewLock = new Object();
            List<TopologyListener> listeners = Collections.singletonList(listener);
            TopologyListenerMultiplexer multiplexer = (listener != null) ? new TopologyListenerMultiplexer(helper, viewLock, listeners) : null;
            DelegatingReceiver delegatingReceiver = new DelegatingReceiver(channel, receiver, helper, multiplexer, monitor);
            channel.setReceiver(delegatingReceiver);
            channel.connect(info.getDomain().getAuthority() + ":" + name);
        } catch (Exception e) {
            throw new ZoneChannelException(e);
        }
    }

    public void closeChannel(String name) throws ZoneChannelException {
        Channel channel = channels.remove(name);
        if (channel == null) {
            throw new ZoneChannelException("Channel not found: " + name);
        }
        channel.close();
    }

    public void sendAsynchronous(String name, Serializable message) throws MessageException {
        Channel channel = channels.get(name);
        if (channel == null) {
            throw new MessageException("Channel not found: " + name);
        }
        try {
            byte[] payload = helper.serialize(message);
            Message jMessage = new Message(null, null, payload);
            channel.send(jMessage);
        } catch (Exception e) {
            throw new MessageException(e);
        }
    }

    Fabric3EventListener<JoinDomain> getJoinListener() {
        if (joinListener == null) {
            joinListener = new JoinEventListener();
        }
        return joinListener;
    }

    Fabric3EventListener<RuntimeStop> getStopListener() {
        if (stopListener == null) {
            stopListener = new RuntimeStopEventListener();
        }
        return stopListener;
    }

    @Override
    JChannel getDomainChannel() {
        return domainChannel;
    }

    class JoinEventListener implements Fabric3EventListener<JoinDomain> {

        public void onEvent(JoinDomain event) {
            try {
                domainChannel.connect(domainName);
                dispatcher.start();
            } catch (Exception e) {
                monitor.error("Error joining the domain", e);
            }
        }
    }

    class RuntimeStopEventListener implements Fabric3EventListener<RuntimeStop> {

        public void onEvent(RuntimeStop event) {
            dispatcher.stop();
            domainChannel.disconnect();
            domainChannel.close();
        }
    }

    class RuntimeStartEventListener implements Fabric3EventListener<RuntimeStart> {

        public void onEvent(RuntimeStart event) {
            try {
                // broadcast availability
                ControllerAvailableCommand command = new ControllerAvailableCommand(runtimeName);
                monitor.broadcastAvailability();
                broadcast(command);
            } catch (MessageException e) {
                monitor.error("Error broadcasting availability", e);
            }
        }
    }

    /**
     * Membership listener that tracks cached data in the domain.
     */
    private class DomainMembershipListener implements MembershipListener {

        public void viewAccepted(View newView) {
            final Set<Address> newZoneLeaders = helper.getNewZoneLeaders(previousView, newView);
            final Set<Address> newRuntimes = helper.getNewRuntimes(previousView, newView);
            previousView = newView;
            if (newZoneLeaders.isEmpty() && newRuntimes.isEmpty()) {
                return;
            }
            for (Address address : newZoneLeaders) {
                String name = UUID.get(address);

                for (TopologyListener listener : topologyListeners) {
                    listener.onLeaderElected(name);
                }
            }
            for (Address address : newRuntimes) {
                String name = UUID.get(address);
                for (TopologyListener listener : topologyListeners) {
                    listener.onJoin(name);
                }
                String zoneName = helper.getZoneName(address);
                if (zoneName == null) {
                    continue;
                }
                Map<String, RuntimeInstance> zones = runtimes.get(zoneName);
                if (zones == null) {
                    zones = new HashMap<String, RuntimeInstance>();
                    runtimes.put(zoneName, zones);
                }
                RuntimeInstance instance = new RuntimeInstance(name);
                zones.put(name, instance);
            }

        }

        public void suspect(Address suspected) {
            String name = helper.getZoneName(suspected);
            if (name == null) {
                return;
            }
            String runtimeName = UUID.get(suspected);
            monitor.runtimeRemoved(runtimeName);
            // Member is suspected. If it is a zone leader, remove the runtime instance from the cache
            View view = domainChannel.getView();
            if (view == null) {
                return;
            }
            Map<String, RuntimeInstance> instances = runtimes.get(name);
            if (instances != null) {
                instances.remove(runtimeName);
                if (instances.isEmpty()) {
                    runtimes.remove(name);
                }
            }
            for (TopologyListener listener : topologyListeners) {
                listener.onLeave(runtimeName);
            }

        }

        public void block() {
            // no-op
        }

        public void unblock() {
            // no-op
        }
    }

}
