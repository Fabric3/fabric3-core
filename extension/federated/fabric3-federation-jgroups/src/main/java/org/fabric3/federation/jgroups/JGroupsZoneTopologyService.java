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
import org.jgroups.Message;
import org.jgroups.SuspectedException;
import org.jgroups.TimeoutException;
import org.jgroups.View;
import org.jgroups.blocks.GroupRequest;
import org.jgroups.blocks.MessageDispatcher;
import org.jgroups.util.Rsp;
import org.jgroups.util.RspList;
import org.osoa.sca.annotations.Init;
import org.osoa.sca.annotations.Property;
import org.osoa.sca.annotations.Reference;

import org.fabric3.federation.command.RuntimeSyncCommand;
import org.fabric3.host.runtime.HostInfo;
import org.fabric3.spi.command.Command;
import org.fabric3.spi.event.EventService;
import org.fabric3.spi.event.Fabric3EventListener;
import org.fabric3.spi.event.JoinDomain;
import org.fabric3.spi.event.RuntimeStop;
import org.fabric3.spi.executor.CommandExecutorRegistry;
import org.fabric3.spi.executor.ExecutionException;
import org.fabric3.spi.topology.ControllerNotFoundException;
import org.fabric3.spi.topology.MessageException;
import org.fabric3.spi.topology.ZoneTopologyService;

/**
 * @version $Rev$ $Date$
 */
public class JGroupsZoneTopologyService extends AbstractTopologyService implements ZoneTopologyService {
    private String zoneName = "default.zone";
    private long defaultTimeout = 3000;
    private Channel domainChannel;
    private Fabric3EventListener<JoinDomain> joinListener;
    private Fabric3EventListener<RuntimeStop> stopListener;
    private MessageDispatcher domainDispatcher;
    private boolean synchronize = true;

    public JGroupsZoneTopologyService(@Reference HostInfo info,
                                      @Reference CommandExecutorRegistry executorRegistry,
                                      @Reference EventService eventService,
                                      @Reference Executor executor,
                                      @Reference JGroupsHelper helper) {
        super(info, executorRegistry, eventService, executor, helper);
    }

    @Property(required = false)
    public void setZoneName(String zoneName) {
        this.zoneName = zoneName;
    }

    @Property(required = false)
    public void setDefaultTimeout(long defaultTimeout) {
        this.defaultTimeout = defaultTimeout;
    }

    @Property(required = false)
    public void setSynchronize(boolean synchronize) {
        this.synchronize = synchronize;
    }

    @Init
    public void init() throws ChannelException {
        super.init();
        domainChannel = new JChannel();
        String runtimeName = domainName + ":participant:" + zoneName + ":" + runtimeId;
        domainChannel.setName(runtimeName);
        initializeChannel(domainChannel);

        Fabric3MessageListener domainMessageListener = new Fabric3MessageListener();
        Fabric3RequestHandler domainRequestHandler = new Fabric3RequestHandler();
        domainDispatcher = new MessageDispatcher(domainChannel, domainMessageListener, null, domainRequestHandler);
    }

    public void broadcastMessage(byte[] payload) throws MessageException {
        // null address will send to everyone
        List<Address> addresses = helper.getRuntimeAddressesInZone(zoneName, domainChannel.getView());
        for (Address address : addresses) {
            sendAsync(address, payload);
        }
    }

    public void sendAsynchronousMessage(String runtimeName, byte[] payload) throws MessageException {
        Address address = helper.getRuntimeAddress(runtimeName, domainChannel.getView());
        sendAsync(address, payload);
    }

    public byte[] sendSynchronousMessage(String runtimeName, byte[] payload, long timeout) throws MessageException {
        Address address = helper.getRuntimeAddress(runtimeName, domainChannel.getView());
        if (address == null) {
            throw new MessageException("Runtme not found: " + runtimeName);
        }
        return send(address, payload, timeout);
    }

    public List<byte[]> sendSynchronousMessage(byte[] payload, long timeout) throws MessageException {
        List<byte[]> values = new ArrayList<byte[]>();
        List<Address> addresses = helper.getRuntimeAddressesInZone(zoneName, domainChannel.getView());
        Vector<Address> dest = new Vector<Address>(addresses);
        Message message = new Message(null, domainChannel.getAddress(), payload);
        RspList responses = domainDispatcher.castMessage(dest, message, GroupRequest.GET_ALL, timeout);
        for (Map.Entry<Address, Rsp> response : responses.entrySet()) {
            Object val = response.getValue().getValue();
            assert val instanceof byte[] : " expected byte[] for response";
            values.add((byte[]) val);
        }
        return values;
    }

    public byte[] sendSynchronousControllerMessage(byte[] payload, long timeout) throws MessageException {
        Address controller = helper.getController(domainChannel.getView());
        if (controller == null) {
            throw new ControllerNotFoundException("Controller could not be located");
        }
        return send(controller, payload, timeout);
    }

    public void sendAsynchronousControllerMessage(byte[] payload) throws MessageException {
        Address controller = helper.getController(domainChannel.getView());
        if (controller == null) {
            throw new ControllerNotFoundException("Controller could not be located");
        }
        sendAsync(controller, payload);
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

    private byte[] send(Address address, byte[] payload, long timeout) throws MessageException {
        try {
            Address sourceAddress = domainChannel.getAddress();
            Message message = new Message(address, sourceAddress, payload);
            Object val = domainDispatcher.sendMessage(message, GroupRequest.GET_ALL, timeout);
            assert val instanceof byte[] : " expected byte[] for response";
            return (byte[]) val;
        } catch (TimeoutException e) {
            throw new MessageException("Error sending message to: " + runtimeName, e);
        } catch (SuspectedException e) {
            throw new MessageException("Error sending message to: " + runtimeName, e);
        }
    }

    private void sendAsync(Address adddress, byte[] payload) throws MessageException {
        try {
            Address sourceAddress = domainChannel.getAddress();
            Message message = new Message(adddress, sourceAddress, payload);
            domainChannel.send(message);
        } catch (ChannelClosedException e) {
            throw new MessageException("Error broadcasting message to zone: " + zoneName, e);
        } catch (ChannelNotConnectedException e) {
            throw new MessageException("Error broadcasting message to zone: " + zoneName, e);
        }
    }

    private void synchronize() throws MessageException, ExecutionException {
        // send the sync request
        View view = domainChannel.getView();
        Address address = helper.getController(view);
        if (address == null) {
            // controller not present
            address = helper.getZoneLeader(zoneName, view);
            if (domainChannel.getAddress().equals(address)) {
                // current runtime is the zone. do nothing
                return;
            }
        }
        RuntimeSyncCommand commmand = new RuntimeSyncCommand(runtimeName, zoneName, null);
        byte[] serialized = helper.serialize(commmand);
        byte[] response = send(address, serialized, defaultTimeout);
        Command command = (Command) helper.deserialize(response);
        executorRegistry.execute(command);
    }

    class JoinEventListener implements Fabric3EventListener<JoinDomain> {

        public void onEvent(JoinDomain event) {
            try {
                domainChannel.connect(domainName);
                domainDispatcher.start();
                if (synchronize) {
                    synchronize();
                }
            } catch (ChannelException e) {
                // TODO log error
            } catch (MessageException e) {
                // TODO log error
            } catch (ExecutionException e) {
                // TODO log error
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


}