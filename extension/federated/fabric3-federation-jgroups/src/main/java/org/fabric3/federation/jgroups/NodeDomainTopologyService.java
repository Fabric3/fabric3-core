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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;

import org.fabric3.api.annotation.management.Management;
import org.fabric3.api.annotation.management.ManagementOperation;
import org.fabric3.api.annotation.monitor.Monitor;
import org.fabric3.federation.node.command.DomainSnapshotCommand;
import org.fabric3.federation.node.command.DomainSnapshotResponse;
import org.fabric3.federation.node.merge.DomainMergeService;
import org.fabric3.host.runtime.HostInfo;
import org.fabric3.spi.command.Command;
import org.fabric3.spi.command.Response;
import org.fabric3.spi.command.ResponseCommand;
import org.fabric3.spi.event.EventService;
import org.fabric3.spi.event.Fabric3EventListener;
import org.fabric3.spi.event.JoinDomain;
import org.fabric3.spi.event.RuntimeStop;
import org.fabric3.spi.executor.CommandExecutorRegistry;
import org.fabric3.spi.federation.DomainTopologyService;
import org.fabric3.spi.federation.MessageException;
import org.fabric3.spi.federation.MessageReceiver;
import org.fabric3.spi.federation.RuntimeInstance;
import org.fabric3.spi.federation.TopologyListener;
import org.fabric3.spi.federation.Zone;
import org.fabric3.spi.federation.ZoneChannelException;
import org.fabric3.spi.federation.ZoneTopologyService;
import org.fabric3.spi.model.instance.LogicalCompositeComponent;
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
 * JGroups implementation of the {@link DomainTopologyService} and the {@link ZoneTopologyService} for node runtimes.
 */
@EagerInit
@Management(name = "DomainTopologyService", path = "/runtime/federation/node/view")
@Service(names = {DomainTopologyService.class, ZoneTopologyService.class})
public class NodeDomainTopologyService extends AbstractTopologyService implements DomainTopologyService, ZoneTopologyService {
    private JChannel domainChannel;
    private MessageDispatcher dispatcher;
    private JoinEventListener joinListener;
    private RuntimeStopEventListener stopListener;
    private View previousView;
    private List<TopologyListener> topologyListeners = new ArrayList<TopologyListener>();
    private Map<String, Map<String, String>> transportMetadata = new ConcurrentHashMap<String, Map<String, String>>();
    private Map<String, Map<String, RuntimeInstance>> runtimes = new ConcurrentHashMap<String, Map<String, RuntimeInstance>>();
    private Map<String, Channel> channels = new ConcurrentHashMap<String, Channel>();

    private Element channelConfig;

    private long timeout = 10000;

    private String zoneName;

    private DomainMergeService mergeService;

    public NodeDomainTopologyService(@Reference HostInfo info,
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

    @ManagementOperation(description = "The zones in the domain")
    public Set<Zone> getZones() {
        View view = domainChannel.getView();
        return getZones(view);
    }

    @ManagementOperation(description = "The runtimes in the domain")
    public List<String> getRuntimeNames() {
        List<String> runtimes = new ArrayList<String>();
        for (Address member : domainChannel.getView().getMembers()) {
            String name = UUID.get(member);
            runtimes.add(name);
        }
        return runtimes;
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

    public String getTransportMetaData(String zone, String transport) {
        Map<String, String> zoneCache = transportMetadata.get(zone);
        if (zoneCache != null) {
            return zoneCache.get(transport);
        }
        return null;
    }

    public void broadcast(String zoneName, Command command) throws MessageException {
        throw new UnsupportedOperationException();
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
        throw new UnsupportedOperationException();
    }

    public Response sendSynchronous(String runtimeName, ResponseCommand command, long timeout) throws MessageException {
        throw new UnsupportedOperationException();
    }

    // zone methods

    @ManagementOperation(description = "True if the runtime is the zone leader")
    public boolean isZoneLeader() {
        View view = domainChannel.getView();
        Address address = domainChannel.getAddress();
        return view != null && address != null && address.equals(helper.getZoneLeader(zoneName, view));
    }

    public boolean supportsDynamicChannels() {
        return true;
    }

    public void register(TopologyListener listener) {
        topologyListeners.add(listener);
    }

    public void deregister(TopologyListener listener) {
        topologyListeners.remove(listener);
    }

    public void registerMetadata(String key, Serializable metadata) {
    }

    public boolean isControllerAvailable() {
        return false;
    }

    @ManagementOperation(description = "The name of the zone leader")
    public String getZoneLeaderName() {
        View view = domainChannel.getView();
        if (view == null) {
            return null;
        }
        Address address = helper.getZoneLeader(zoneName, view);
        if (address == null) {
            return null;
        }
        return UUID.get(address);
    }

    public void sendAsynchronous(String runtimeName, Command command) throws MessageException {
        throw new UnsupportedOperationException();
    }

    public List<Response> sendSynchronous(ResponseCommand command, long timeout) throws MessageException {
        throw new UnsupportedOperationException();
    }

    public Response sendSynchronousToController(ResponseCommand command, long timeout) throws MessageException {
        throw new UnsupportedOperationException();
    }

    public void sendAsynchronousToController(Command command) throws MessageException {
        throw new UnsupportedOperationException();
    }

    public boolean isChannelOpen(String name) {
        return channels.containsKey(name);
    }

    public void openChannel(String name, String configuration, MessageReceiver receiver) throws ZoneChannelException {
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
            initializeChannel(channel);
            channels.put(name, channel);
            DelegatingReceiver delegatingReceiver = new DelegatingReceiver(channel, receiver, helper, monitor);
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

    public void sendAsynchronous(String runtimeName, String name, Serializable message) throws MessageException {
        Channel channel = channels.get(name);
        if (channel == null) {
            throw new MessageException("Channel not found: " + name);
        }
        try {
            View view = domainChannel.getView();
            if (view == null) {
                throw new MessageException("Federation channel closed or not connected when sending message to: " + runtimeName);
            }
            Address address = helper.getRuntimeAddress(runtimeName, view);
            byte[] payload = helper.serialize(message);
            Message jMessage = new Message(address, null, payload);
            channel.send(jMessage);
        } catch (Exception e) {
            throw new MessageException(e);
        }
    }

    // zone methods
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

    /**
     * Returns a list of zones in the given view
     *
     * @param view the view
     * @return the list of zones
     */
    private Set<Zone> getZones(View view) {
        Address controller = domainChannel.getAddress();
        Set<Zone> zones = new HashSet<Zone>();
        List<Address> members = view.getMembers();
        for (Address member : members) {
            if (!member.equals(controller)) {
                String zoneName = helper.getZoneName(member);
                Map<String, RuntimeInstance> instances = runtimes.get(zoneName);
                List<RuntimeInstance> list = new ArrayList<RuntimeInstance>(instances.values());
                if (zoneName != null) {
                    Zone zone = new Zone(zoneName, list);
                    zones.add(zone);
                }
            }
        }
        return zones;
    }

    class JoinEventListener implements Fabric3EventListener<JoinDomain> {

        public void onEvent(JoinDomain event) {
            try {
                domainChannel.connect(domainName);
                dispatcher.start();
                List<Address> members = domainChannel.getView().getMembers();
                if (!members.isEmpty()) {
                    // Obtain a snapshot of the logical domain from the oldest runtime
                    Address oldest = members.get(0);
                    // check if oldest member is the current
                    String oldestName = UUID.get(oldest);
                    if (runtimeName.equals(oldestName)) {
                        // this runtime is the oldest, skip
                        return;
                    }

                    try {
                        DomainSnapshotCommand command = new DomainSnapshotCommand();
                        byte[] payload = helper.serialize(command);
                        View view = domainChannel.getView();
                        if (view == null) {
                            throw new MessageException("Federation channel closed or not connected when sending message to: " + runtimeName);
                        }
                        Message message = new Message(oldest, domainChannel.getAddress(), payload);
                        RequestOptions options = new RequestOptions(ResponseMode.GET_FIRST, timeout);
                        Object o = dispatcher.sendMessage(message, options);
                        DomainSnapshotResponse response = (DomainSnapshotResponse) helper.deserialize((byte[]) o);
                        LogicalCompositeComponent snapshot = response.getSnapshot();
                        // merge the snapshot with the live logical domain
                        mergeService.merge(snapshot);
                    } catch (Exception e) {
                        throw new MessageException("Error sending message to runtime: " + runtimeName, e);
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
            for (Channel channel : channels.values()) {
                if (channel.isConnected()) {
                    channel.disconnect();
                    channel.close();
                }
            }
        }
    }

    /**
     * Membership listener that tracks transport metadata for zones in the domain.
     */
    private class DomainMembershipListener implements MembershipListener {

        public void viewAccepted(View newView) {
            Set<Address> newZoneLeaders = helper.getNewZoneLeaders(previousView, newView);
            Set<Address> newRuntimes = helper.getNewRuntimes(previousView, newView);
            previousView = newView;
        }

        public void suspect(Address suspected) {
            String zoneName = helper.getZoneName(suspected);
            if (zoneName == null) {
                return;
            }
            String runtimeName = UUID.get(suspected);
            monitor.runtimeRemoved(runtimeName);
            // Member is suspected. If it is a zone leader, remove it from the logical model and the metadata from the cache
            View view = domainChannel.getView();
            if (view == null) {
                return;
            }
            if (suspected.equals(helper.getZoneLeader(zoneName, view))) {
                transportMetadata.remove(zoneName);
                mergeService.drop(zoneName);
            }
            Map<String, RuntimeInstance> instances = runtimes.get(zoneName);
            if (instances != null) {
                instances.remove(runtimeName);
                if (instances.isEmpty()) {
                    runtimes.remove(zoneName);
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
