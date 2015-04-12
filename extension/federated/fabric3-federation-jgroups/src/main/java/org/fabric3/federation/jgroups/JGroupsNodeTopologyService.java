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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;

import org.fabric3.api.annotation.management.Management;
import org.fabric3.api.annotation.management.ManagementOperation;
import org.fabric3.api.annotation.monitor.Monitor;
import org.fabric3.api.host.Fabric3Exception;
import org.fabric3.api.host.runtime.HostInfo;
import org.fabric3.federation.node.command.DomainSnapshotCommand;
import org.fabric3.federation.node.command.DomainSnapshotResponse;
import org.fabric3.federation.node.merge.DomainMergeService;
import org.fabric3.spi.container.command.Command;
import org.fabric3.spi.container.executor.CommandExecutorRegistry;
import org.fabric3.spi.federation.topology.MessageReceiver;
import org.fabric3.spi.federation.topology.NodeTopologyService;
import org.fabric3.spi.federation.topology.TopologyListener;
import org.fabric3.spi.model.instance.LogicalCompositeComponent;
import org.fabric3.spi.runtime.event.EventService;
import org.fabric3.spi.runtime.event.Fabric3EventListener;
import org.fabric3.spi.runtime.event.JoinDomain;
import org.fabric3.spi.runtime.event.RuntimeStop;
import org.jgroups.Address;
import org.jgroups.Channel;
import org.jgroups.JChannel;
import org.jgroups.MembershipListener;
import org.jgroups.Message;
import org.jgroups.View;
import org.jgroups.blocks.MessageDispatcher;
import org.jgroups.blocks.RequestOptions;
import org.jgroups.blocks.ResponseMode;
import org.jgroups.util.UUID;
import org.oasisopen.sca.annotation.EagerInit;
import org.oasisopen.sca.annotation.Init;
import org.oasisopen.sca.annotation.Property;
import org.oasisopen.sca.annotation.Reference;
import org.oasisopen.sca.annotation.Service;
import org.w3c.dom.Element;

/**
 * JGroups implementation of the {@link NodeTopologyService}.
 */
@EagerInit
@Management(name = "NodeTopologyService", path = "/runtime/federation/node/view")
@Service(NodeTopologyService.class)
public class JGroupsNodeTopologyService extends AbstractTopologyService implements NodeTopologyService {
    private JChannel domainChannel;
    private MessageDispatcher dispatcher;
    private JoinEventListener joinListener;
    private RuntimeStopEventListener stopListener;
    private View previousView;
    private List<TopologyListener> topologyListeners = new ArrayList<>();
    private Map<String, Map<String, RuntimeInstance>> runtimes = new ConcurrentHashMap<>();
    private Map<String, Channel> channels = new ConcurrentHashMap<>();

    private Element channelConfig;

    private long timeout = 10000;

    private String zoneName;

    private DomainMergeService mergeService;

    public JGroupsNodeTopologyService(@Reference HostInfo info,
                                      @Reference CommandExecutorRegistry executorRegistry,
                                      @Reference DomainMergeService mergeService,
                                      @Reference EventService eventService,
                                      @Reference Executor executor,
                                      @Reference JGroupsHelper helper,
                                      @Monitor TopologyServiceMonitor monitor) {
        super(info, executorRegistry, eventService, executor, helper, monitor);
        this.mergeService = mergeService;
        this.zoneName = info.getZoneName();
    }

    @Property(required = false)
    @ManagementOperation(description = "Timeout")
    public void setTimeout(long timeout) {
        this.timeout = timeout;
    }

    @Property(required = false)
    public void setChannelConfig(Element config) {
        this.channelConfig = (Element) config.getElementsByTagName("config").item(0);
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

    }

    public void broadcast(Command command) throws Fabric3Exception {
        byte[] payload = helper.serialize(command);
        Message message = new Message(null, domainChannel.getAddress(), payload);
        try {
            domainChannel.send(message);
        } catch (Exception e) {
            throw new Fabric3Exception(e);
        }
    }

    @ManagementOperation(description = "True if the runtime is the zone leader")
    public boolean isZoneLeader() {
        View view = domainChannel.getView();
        Address address = domainChannel.getAddress();
        return view != null && address != null && address.equals(helper.getZoneLeader(zoneName, view));
    }

    public void register(TopologyListener listener) {
        topologyListeners.add(listener);
    }

    public void deregister(TopologyListener listener) {
        topologyListeners.remove(listener);
    }

    public void openChannel(String name, String configuration, MessageReceiver receiver) throws Fabric3Exception {
        if (channels.containsKey(name)) {
            throw new Fabric3Exception("Channel already open:" + name);
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

            DelegatingReceiver delegatingReceiver = new DelegatingReceiver(channel, receiver, helper, monitor);
            channel.setReceiver(delegatingReceiver);
            channel.connect(info.getDomain().getAuthority() + ":" + name);
        } catch (Exception e) {
            throw new Fabric3Exception(e);
        }
    }

    public void closeChannel(String name) throws Fabric3Exception {
        Channel channel = channels.remove(name);
        if (channel == null) {
            throw new Fabric3Exception("Channel not found: " + name);
        }
        channel.close();
    }

    public void sendAsynchronous(String channelName, Serializable message) throws Fabric3Exception {
        Channel channel = channels.get(channelName);
        if (channel == null) {
            throw new Fabric3Exception("Channel not found: " + channelName);
        }
        try {
            byte[] payload = helper.serialize(message);
            Message jMessage = new Message(null, null, payload);
            channel.send(jMessage);
        } catch (Exception e) {
            throw new Fabric3Exception(e);
        }
    }

    public void sendAsynchronous(String runtimeName, String name, Serializable message) throws Fabric3Exception {
        Channel channel = channels.get(name);
        if (channel == null) {
            throw new Fabric3Exception("Channel not found: " + name);
        }
        try {
            View view = channel.getView();
            if (view == null) {
                throw new Fabric3Exception("Federation channel closed or not connected when sending message to: " + runtimeName);
            }
            Address address = helper.getRuntimeAddress(runtimeName, view);
            byte[] payload = helper.serialize(message);
            Message jMessage = new Message(address, null, payload);
            channel.send(jMessage);
        } catch (Exception e) {
            throw new Fabric3Exception(e);
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

    JChannel getDomainChannel() {
        return domainChannel;
    }

    class JoinEventListener implements Fabric3EventListener<JoinDomain> {

        public void onEvent(JoinDomain event) {
            try {
                domainChannel.connect(domainName);
                dispatcher.start();
                monitor.joinedDomain(runtimeName);
                List<Address> members = domainChannel.getView().getMembers();
                if (!members.isEmpty()) {
                    // Obtain a snapshot of the logical domain from the oldest runtime
                    Address oldest = members.get(0);
                    // check if oldest member is the current
                    String oldestName = UUID.get(oldest);
                    if (runtimeName.equals(oldestName)) {
                        // this runtime is the oldest, skip
                        monitor.noRuntimes();
                        return;
                    }

                    try {
                        DomainSnapshotCommand command = new DomainSnapshotCommand();
                        byte[] payload = helper.serialize(command);
                        View view = domainChannel.getView();
                        if (view == null) {
                            throw new Fabric3Exception("Federation channel closed or not connected when sending message to: " + runtimeName);
                        }
                        Message message = new Message(oldest, domainChannel.getAddress(), payload);
                        RequestOptions options = new RequestOptions(ResponseMode.GET_FIRST, timeout);
                        Object o = dispatcher.sendMessage(message, options);
                        DomainSnapshotResponse response = (DomainSnapshotResponse) helper.deserialize((byte[]) o);
                        monitor.receivedSnapshot(UUID.get(oldest));
                        LogicalCompositeComponent snapshot = response.getSnapshot();
                        // merge the snapshot with the live logical domain
                        mergeService.merge(snapshot);
                    } catch (Exception e) {
                        throw new Fabric3Exception("Error sending message to runtime: " + runtimeName, e);
                    }
                }
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
            monitor.disconnect();
            for (Channel channel : channels.values()) {
                if (channel.isConnected()) {
                    channel.disconnect();
                    channel.close();
                }
            }
        }
    }

    /**
     * Listener that tracks changes to domain membership.
     */
    private class DomainMembershipListener implements MembershipListener {

        public void viewAccepted(View newView) {
            Set<Address> newZoneLeaders = helper.getNewZoneLeaders(previousView, newView);
            Set<Address> newRuntimes = helper.getNewRuntimes(previousView, newView);
            Set<Address> removedRuntimes = helper.getRemovedRuntimes(previousView, newView);
            previousView = newView;
            if (newZoneLeaders.isEmpty() && newRuntimes.isEmpty() && removedRuntimes.isEmpty()) {
                return;
            }
            for (Address address : removedRuntimes) {
                remove(address);
            }
            for (Address address : newRuntimes) {
                String newRuntime = UUID.get(address);
                String zoneName = helper.getZoneName(address);
                if (zoneName == null) {
                    continue;
                }
                Map<String, RuntimeInstance> zones = runtimes.get(zoneName);
                if (zones == null) {
                    zones = new HashMap<>();
                    runtimes.put(zoneName, zones);
                }
                if (!newRuntime.equals(runtimeName)) {
                    monitor.runtimeJoined(newRuntime);
                }
                for (TopologyListener listener : topologyListeners) {
                    listener.onJoin(newRuntime);
                }
            }
            for (Address address : newZoneLeaders) {
                String newZoneLeader = UUID.get(address);
                for (TopologyListener listener : topologyListeners) {
                    listener.onLeaderElected(newZoneLeader);
                }
            }
        }

        public void suspect(Address suspected) {
            remove(suspected);
        }

        private void remove(Address suspected) {
            String suspectedRuntime = UUID.get(suspected);
            String suspectedZone = helper.getZoneName(suspected);
            if (suspectedZone == null) {
                return;
            }

            Map<String, RuntimeInstance> instances = runtimes.get(suspectedZone);
            if (instances != null) {
                instances.remove(suspectedRuntime);
                if (instances.isEmpty()) {
                    runtimes.remove(suspectedZone);
                    mergeService.drop(suspectedZone);
                }
            }
            monitor.runtimeRemoved(suspectedRuntime);
            for (TopologyListener listener : topologyListeners) {
                listener.onLeave(suspectedRuntime);
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
