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
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;

import org.jgroups.Address;
import org.jgroups.Channel;
import org.jgroups.JChannel;
import org.jgroups.MembershipListener;
import org.jgroups.Message;
import org.jgroups.View;
import org.jgroups.blocks.MessageDispatcher;
import org.jgroups.blocks.RequestOptions;
import org.jgroups.blocks.ResponseMode;
import org.jgroups.util.Rsp;
import org.jgroups.util.RspList;
import org.jgroups.util.UUID;
import org.oasisopen.sca.annotation.EagerInit;
import org.oasisopen.sca.annotation.Init;
import org.oasisopen.sca.annotation.Property;
import org.oasisopen.sca.annotation.Reference;
import org.w3c.dom.Element;

import org.fabric3.api.annotation.management.Management;
import org.fabric3.api.annotation.management.ManagementOperation;
import org.fabric3.api.annotation.monitor.Monitor;
import org.fabric3.federation.deployment.command.ControllerAvailableCommand;
import org.fabric3.federation.deployment.command.DeploymentCommand;
import org.fabric3.federation.deployment.command.RuntimeMetadataResponse;
import org.fabric3.federation.deployment.command.RuntimeMetadataUpdateCommand;
import org.fabric3.federation.deployment.command.RuntimeUpdateCommand;
import org.fabric3.federation.deployment.command.RuntimeUpdateResponse;
import org.fabric3.federation.deployment.command.ZoneMetadataResponse;
import org.fabric3.federation.deployment.command.ZoneMetadataUpdateCommand;
import org.fabric3.host.runtime.HostInfo;
import org.fabric3.spi.command.Command;
import org.fabric3.spi.command.Response;
import org.fabric3.spi.command.ResponseCommand;
import org.fabric3.spi.event.EventService;
import org.fabric3.spi.event.Fabric3EventListener;
import org.fabric3.spi.event.JoinDomain;
import org.fabric3.spi.event.RuntimeStop;
import org.fabric3.spi.executor.CommandExecutor;
import org.fabric3.spi.executor.CommandExecutorRegistry;
import org.fabric3.spi.executor.ExecutionException;
import org.fabric3.spi.federation.ControllerNotFoundException;
import org.fabric3.spi.federation.MessageException;
import org.fabric3.spi.federation.MessageReceiver;
import org.fabric3.spi.federation.TopologyListener;
import org.fabric3.spi.federation.ZoneChannelException;
import org.fabric3.spi.federation.ZoneTopologyService;

/**
 *
 */
@EagerInit
@Management(name = "ZoneTopologyService", path = "/runtime/federation/zone/view")
public class JGroupsZoneTopologyService extends AbstractTopologyService implements ZoneTopologyService {
    private static final int NOT_UPDATED = -1;
    private static final int UPDATED = 1;

    private String zoneName = "default.zone";
    private Element channelConfig;
    private JChannel domainChannel;
    private Fabric3EventListener<JoinDomain> joinListener;
    private Fabric3EventListener<RuntimeStop> stopListener;
    private MessageDispatcher domainDispatcher;
    private boolean synchronize = true;
    private final Object viewLock = new Object();
    private View previousView;
    private List<TopologyListener> topologyListeners = new ArrayList<TopologyListener>();
    private Map<String, Serializable> runtimeMetadata = new ConcurrentHashMap<String, Serializable>();
    private Map<String, String> transportMetadata = new ConcurrentHashMap<String, String>();
    private Map<String, Channel> channels = new ConcurrentHashMap<String, Channel>();

    private int state = NOT_UPDATED;

    public JGroupsZoneTopologyService(@Reference HostInfo info,
                                      @Reference CommandExecutorRegistry executorRegistry,
                                      @Reference EventService eventService,
                                      @Reference Executor executor,
                                      @Reference JGroupsHelper helper,
                                      @Monitor TopologyServiceMonitor monitor) {
        super(info, executorRegistry, eventService, executor, helper, monitor);
    }

    @Property(required = false)
    public void setZoneName(String zoneName) {
        this.zoneName = zoneName;
    }

    @Property(required = false)
    public void setChannelConfig(Element config) {
        this.channelConfig = (Element) config.getElementsByTagName("config").item(0);
    }

    @Reference(required = false)
    public void setTopologyListeners(List<TopologyListener> listeners) {
        this.topologyListeners.addAll(listeners);
    }

    /**
     * Property to configure whether the runtime should attempt an update by querying a zone leader or the controller. In some topologies, the runtime
     * may pull deployment information from a persistent store, which eliminates the need to update via a peer or the controller.
     *
     * @param synchronize true if the runtime should attempt an update (the default)
     */
    @Property(required = false)
    public void setSynchronize(boolean synchronize) {
        this.synchronize = synchronize;
    }

    @Property(required = false)
    public void setTransportMetadata(Map<String, String> transportMetadata) {
        this.transportMetadata = transportMetadata;
    }

    @Init
    public void init() throws Exception {
        super.init();
        if (!synchronize) {
            state = UPDATED;
        }
        ZoneMetadataUpdateCommandExecutor zoneMetadataExecutor = new ZoneMetadataUpdateCommandExecutor();
        executorRegistry.register(ZoneMetadataUpdateCommand.class, zoneMetadataExecutor);

        RuntimeMetadataUpdateCommandExecutor runtimeMetadataExecutor = new RuntimeMetadataUpdateCommandExecutor();
        executorRegistry.register(RuntimeMetadataUpdateCommand.class, runtimeMetadataExecutor);

        ControllerAvailableCommandExecutor executor = new ControllerAvailableCommandExecutor();
        executorRegistry.register(ControllerAvailableCommand.class, executor);

        if (channelConfig != null) {
            domainChannel = new JChannel(channelConfig);
        } else {
            domainChannel = new JChannel();
        }
        domainChannel.setName(info.getRuntimeName());
        initializeChannel(domainChannel);

        Fabric3MessageListener messageListener = new Fabric3MessageListener();
        Fabric3RequestHandler requestHandler = new Fabric3RequestHandler();
        ZoneMemberListener memberListener = new ZoneMemberListener();
        domainDispatcher = new MessageDispatcher(domainChannel, messageListener, memberListener, requestHandler);
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
        runtimeMetadata.put(key, metadata);
    }

    @ManagementOperation(description = "True if the runtime is the zone leader")
    public boolean isZoneLeader() {
        View view = domainChannel.getView();
        Address address = domainChannel.getAddress();
        return view != null && address != null && address.equals(helper.getZoneLeader(zoneName, view));
    }

    @ManagementOperation(description = "True if the controller is reachable")
    public boolean isControllerAvailable() {
        View view = domainChannel.getView();
        return view != null && helper.getController(view) != null;
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

    public void broadcast(Command command) throws MessageException {
        // null address will send to everyone
        List<Address> addresses = helper.getRuntimeAddressesInZone(zoneName, domainChannel.getView());
        for (Address address : addresses) {
            sendAsync(address, command);
        }
    }

    public void sendAsynchronous(String runtimeName, Command command) throws MessageException {
        View view = domainChannel.getView();
        if (view == null) {
            throw new MessageException("Federation channel closed or not connected when sending message to: " + runtimeName);
        }
        Address address = helper.getRuntimeAddress(runtimeName, view);
        sendAsync(address, command);
    }

    public Response sendSynchronous(String runtimeName, ResponseCommand command, long timeout) throws MessageException {
        View view = domainChannel.getView();
        if (view == null) {
            throw new MessageException("Federation channel closed or not connected when sending message to: " + runtimeName);
        }
        Address address = helper.getRuntimeAddress(runtimeName, view);
        if (address == null) {
            throw new MessageException("Runtime not found: " + runtimeName);
        }
        return send(address, command, timeout);
    }

    @SuppressWarnings({"unchecked"})
    public List<Response> sendSynchronous(ResponseCommand command, long timeout) throws MessageException {
        List<Response> values = new ArrayList<Response>();
        List<Address> addresses = helper.getRuntimeAddressesInZone(zoneName, domainChannel.getView());
        Vector<Address> dest = new Vector<Address>(addresses);
        byte[] payload = helper.serialize(command);
        Message message = new Message(null, domainChannel.getAddress(), payload);
        RequestOptions options = new RequestOptions(ResponseMode.GET_ALL, timeout);
        RspList responses;
        try {
            responses = domainDispatcher.castMessage(dest, message, options);
        } catch (Exception e) {
            throw new MessageException("Error sending message", e);
        }
        Set<Map.Entry<Address, Rsp<?>>> set = responses.entrySet();
        for (Map.Entry<Address, Rsp<?>> entry : set) {
            Object val = entry.getValue().getValue();
            assert val instanceof byte[] : " expected byte[] for response";
            Response response = (Response) helper.deserialize((byte[]) val);
            values.add(response);
        }
        return values;
    }

    public Response sendSynchronousToController(ResponseCommand command, long timeout) throws MessageException {
        Address controller = helper.getController(domainChannel.getView());
        if (controller == null) {
            throw new ControllerNotFoundException("Controller could not be located");
        }
        return send(controller, command, timeout);
    }

    public void sendAsynchronousToController(Command command) throws MessageException {
        Address controller = helper.getController(domainChannel.getView());
        if (controller == null) {
            throw new ControllerNotFoundException("Controller could not be located");
        }
        sendAsync(controller, command);
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

    private Response send(Address address, Command command, long timeout) throws MessageException {
        try {
            Address sourceAddress = domainChannel.getAddress();
            byte[] payload = helper.serialize(command);
            Message message = new Message(address, sourceAddress, payload);
            RequestOptions options = new RequestOptions(ResponseMode.GET_ALL, timeout);
            Object val = domainDispatcher.sendMessage(message, options);
            assert val instanceof byte[] : " expected byte[] for response";
            return (Response) helper.deserialize(((byte[]) val));
        } catch (Exception e) {
            throw new MessageException("Error sending message to: " + runtimeName, e);
        }
    }

    private void sendAsync(Address address, Command command) throws MessageException {
        try {
            Address sourceAddress = domainChannel.getAddress();
            byte[] payload = helper.serialize(command);
            Message message = new Message(address, sourceAddress, payload);
            domainChannel.send(message);
        } catch (Exception e) {
            throw new MessageException("Error broadcasting message to zone: " + zoneName, e);
        }
    }

    /**
     * Attempts to update the runtime with the current set of deployments for the zone. The zone leader (i.e. oldest runtime in the zone) is queried
     * for the deployment commands. If the zone leader is unavailable or has not been updated, the controller is queried.
     *
     * @throws MessageException if an error is encountered during update
     */
    private void update() throws MessageException {
        // send the sync request
        View view = domainChannel.getView();
        Address address = helper.getZoneLeader(zoneName, view);
        if (address != null && !domainChannel.getAddress().equals(address)) {
            // check if current runtime is the zone leader - if not, attempt to retrieve cached deployment from it
            try {
                if (update(address)) {
                    return;
                }
            } catch (MessageException e) {
                monitor.error("Error retrieving deployment from zone leader: " + zoneName, e);
            }
        }
        // check the controller
        address = helper.getController(view);
        if (address == null) {
            // controller is not present
            monitor.updateDeferred();
            return;
        }
        update(address);
    }

    /**
     * Performs the actual runtime update by querying the given runtime address.
     *
     * @param address the runtime address
     * @return true if the runtime was updated
     * @throws MessageException if an error is encountered during update
     */
    private boolean update(Address address) throws MessageException {
        String name = UUID.get(address);
        monitor.updating(name);
        RuntimeUpdateCommand command = new RuntimeUpdateCommand(runtimeName, zoneName, null);
        Response response = send(address, command, defaultTimeout);
        assert response instanceof RuntimeUpdateResponse;
        RuntimeUpdateResponse updateResponse = (RuntimeUpdateResponse) response;
        if (!updateResponse.isUpdated()) {
            // not updated, wait until a controller becomes available
            return false;
        }
        // mark synchronized here to avoid multiple retries in case a deployment error is encountered
        state = UPDATED;
        try {
            DeploymentCommand deploymentCommand = updateResponse.getDeploymentCommand();
            executorRegistry.execute(deploymentCommand);
        } catch (ExecutionException e) {
            throw new MessageException(e);
        }
        monitor.updated();
        return true;
    }

    class JoinEventListener implements Fabric3EventListener<JoinDomain> {

        public void onEvent(JoinDomain event) {
            try {
                domainChannel.connect(domainName);
                domainDispatcher.start();
                monitor.joiningDomain(runtimeName);
                if (synchronize) {
                    while (domainChannel.getView() == null) {
                        try {
                            // Wait until the first view is available. Notification will happen when the ZoneMemberListener is called on a
                            // different thread.
                            viewLock.wait(defaultTimeout);
                        } catch (InterruptedException e) {
                            monitor.error("Timeout attempting to join the domain", e);
                            Thread.currentThread().interrupt();
                            return;
                        }
                    }
                    update();
                }
            } catch (Exception e) {
                monitor.error("Error joining the domain", e);
            }
        }
    }

    class RuntimeStopEventListener implements Fabric3EventListener<RuntimeStop> {

        public void onEvent(RuntimeStop event) {
            if (domainDispatcher != null) {
                domainDispatcher.stop();
            }
            if (domainChannel != null && domainChannel.isConnected()) {
                domainChannel.disconnect();
                domainChannel.close();
            }
            for (Channel channel : channels.values()) {
                if (channel.isConnected()) {
                    channel.disconnect();
                    channel.close();
                }
            }
        }
    }

    private class ControllerAvailableCommandExecutor implements CommandExecutor<ControllerAvailableCommand> {

        public void execute(ControllerAvailableCommand command) throws ExecutionException {
            if (UPDATED == state) {
                return;
            }
            try {
                // A controller is now available and this runtime has not been synchronized. This can happen when the first member in a zone becomes
                // available before a controller.
                View view = domainChannel.getView();
                Address controller = helper.getController(view);
                update(controller);
            } catch (MessageException e) {
                monitor.error("Error updating the runtime", e);
            }
        }
    }

    private class ZoneMetadataUpdateCommandExecutor implements CommandExecutor<ZoneMetadataUpdateCommand> {

        public void execute(ZoneMetadataUpdateCommand command) throws ExecutionException {
            ZoneMetadataResponse response = new ZoneMetadataResponse(zoneName, transportMetadata);
            command.setResponse(response);
        }
    }

    private class RuntimeMetadataUpdateCommandExecutor implements CommandExecutor<RuntimeMetadataUpdateCommand> {

        public void execute(RuntimeMetadataUpdateCommand command) throws ExecutionException {
            RuntimeMetadataResponse response = new RuntimeMetadataResponse(runtimeMetadata);
            command.setResponse(response);
        }
    }

    private class ZoneMemberListener implements MembershipListener {
        public void viewAccepted(View newView) {
            synchronized (viewLock) {
                try {
                    Set<Address> newZoneLeaders = helper.getNewZoneLeaders(previousView, newView);
                    Set<Address> newRuntimes = helper.getNewRuntimes(previousView, newView);
                    previousView = newView;
                    if (newZoneLeaders.isEmpty() && newRuntimes.isEmpty()) {
                        return;
                    }
                    for (Address address : newRuntimes) {
                        String name = UUID.get(address);
                        for (TopologyListener listener : topologyListeners) {
                            listener.onJoin(name);
                        }
                    }
                    for (Address address : newZoneLeaders) {
                        String name = UUID.get(address);
                        for (TopologyListener listener : topologyListeners) {
                            listener.onLeaderElected(name);
                        }
                    }
                } finally {
                    viewLock.notifyAll();
                }
            }
        }

        public void suspect(Address suspected) {
            String runtimeName = UUID.get(suspected);
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