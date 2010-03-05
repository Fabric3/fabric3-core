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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.concurrent.Executor;

import org.jgroups.Address;
import org.jgroups.Channel;
import org.jgroups.ChannelClosedException;
import org.jgroups.ChannelException;
import org.jgroups.ChannelNotConnectedException;
import org.jgroups.JChannel;
import org.jgroups.MembershipListener;
import org.jgroups.Message;
import org.jgroups.SuspectedException;
import org.jgroups.TimeoutException;
import org.jgroups.View;
import org.jgroups.blocks.GroupRequest;
import org.jgroups.blocks.MessageDispatcher;
import org.jgroups.util.Rsp;
import org.jgroups.util.RspList;
import org.jgroups.util.UUID;
import org.osoa.sca.annotations.EagerInit;
import org.osoa.sca.annotations.Init;
import org.osoa.sca.annotations.Property;
import org.osoa.sca.annotations.Reference;

import org.fabric3.api.annotation.Monitor;
import org.fabric3.federation.deployment.command.ControllerAvailableCommand;
import org.fabric3.federation.deployment.command.DeploymentCommand;
import org.fabric3.federation.deployment.command.RuntimeUpdateCommand;
import org.fabric3.federation.deployment.command.RuntimeUpdateResponse;
import org.fabric3.federation.deployment.command.ZoneMetadataResponse;
import org.fabric3.federation.deployment.command.ZoneMetadataUpdateCommand;
import org.fabric3.host.runtime.HostInfo;
import org.fabric3.spi.command.Command;
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
import org.fabric3.spi.federation.Response;
import org.fabric3.spi.federation.ZoneTopologyService;

/**
 * @version $Rev$ $Date$
 */
@EagerInit
public class JGroupsZoneTopologyService extends AbstractTopologyService implements ZoneTopologyService {
    private static final int NOT_UPDATED = -1;
    private static final int UPDATED = 1;

    private String zoneName = "default.zone";
    private Map<String, String> transportMetadata = new HashMap<String, String>();
    private Channel domainChannel;
    private Fabric3EventListener<JoinDomain> joinListener;
    private Fabric3EventListener<RuntimeStop> stopListener;
    private MessageDispatcher domainDispatcher;
    private boolean synchronize = true;
    private final Object viewLock = new Object();

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
    public void init() throws ChannelException {
        super.init();
        if (!synchronize) {
            state = UPDATED;
        }
        ZoneMetadataUpdateCommandExecutor metadataExecutor = new ZoneMetadataUpdateCommandExecutor();
        executorRegistry.register(ZoneMetadataUpdateCommand.class, metadataExecutor);
        ControllerAvailableCommandExecutor executor = new ControllerAvailableCommandExecutor();
        executorRegistry.register(ControllerAvailableCommand.class, executor);
        domainChannel = new JChannel();
        domainChannel.setName(getRuntimeName());
        initializeChannel(domainChannel);

        Fabric3MessageListener messageListener = new Fabric3MessageListener();
        Fabric3RequestHandler requestHandler = new Fabric3RequestHandler();
        ZoneMemberListener memberListener = new ZoneMemberListener();
        domainDispatcher = new MessageDispatcher(domainChannel, messageListener, memberListener, requestHandler);
    }

    public boolean isZoneLeader() {
        View view = domainChannel.getView();
        Address address = domainChannel.getAddress();
        return view != null && address != null && address.equals(helper.getZoneLeader(zoneName, view));
    }

    public boolean isControllerAvailable() {
        View view = domainChannel.getView();
        return view != null && helper.getController(view) != null;
    }

    public String getZoneLeader() {
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
        Address address = helper.getRuntimeAddress(runtimeName, domainChannel.getView());
        sendAsync(address, command);
    }

    public Response sendSynchronous(String runtimeName, ResponseCommand command, long timeout) throws MessageException {
        Address address = helper.getRuntimeAddress(runtimeName, domainChannel.getView());
        if (address == null) {
            throw new MessageException("Runtme not found: " + runtimeName);
        }
        return send(address, command, timeout);
    }

    public List<Response> sendSynchronous(ResponseCommand command, long timeout) throws MessageException {
        List<Response> values = new ArrayList<Response>();
        List<Address> addresses = helper.getRuntimeAddressesInZone(zoneName, domainChannel.getView());
        Vector<Address> dest = new Vector<Address>(addresses);
        byte[] payload = helper.serialize(command);
        Message message = new Message(null, domainChannel.getAddress(), payload);
        RspList responses = domainDispatcher.castMessage(dest, message, GroupRequest.GET_ALL, timeout);
        for (Map.Entry<Address, Rsp> entry : responses.entrySet()) {
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

    protected String getRuntimeName() {
        return domainName + ":participant:" + zoneName + ":" + runtimeId;
    }

    private Response send(Address address, Command command, long timeout) throws MessageException {
        try {
            Address sourceAddress = domainChannel.getAddress();
            byte[] payload = helper.serialize(command);
            Message message = new Message(address, sourceAddress, payload);
            Object val = domainDispatcher.sendMessage(message, GroupRequest.GET_ALL, timeout);
            assert val instanceof byte[] : " expected byte[] for response";
            return (Response) helper.deserialize(((byte[]) val));
        } catch (TimeoutException e) {
            throw new MessageException("Error sending message to: " + runtimeName, e);
        } catch (SuspectedException e) {
            throw new MessageException("Error sending message to: " + runtimeName, e);
        }
    }

    private void sendAsync(Address adddress, Command command) throws MessageException {
        try {
            Address sourceAddress = domainChannel.getAddress();
            byte[] payload = helper.serialize(command);
            Message message = new Message(adddress, sourceAddress, payload);
            domainChannel.send(message);
        } catch (ChannelClosedException e) {
            throw new MessageException("Error broadcasting message to zone: " + zoneName, e);
        } catch (ChannelNotConnectedException e) {
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
            } catch (ChannelException e) {
                monitor.error("Error joining the domain", e);
            } catch (MessageException e) {
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

    private class ZoneMemberListener implements MembershipListener {
        public void viewAccepted(View new_view) {
            synchronized (viewLock) {
                viewLock.notifyAll();
            }
        }

        public void suspect(Address suspected_mbr) {
            // no-op
        }

        public void block() {
            // no-op
        }
    }
}