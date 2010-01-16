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
import org.jgroups.blocks.GroupRequest;
import org.jgroups.blocks.MessageDispatcher;
import org.jgroups.blocks.RequestHandler;
import org.osoa.sca.annotations.EagerInit;
import org.osoa.sca.annotations.Init;
import org.osoa.sca.annotations.Reference;

import org.fabric3.host.runtime.HostInfo;
import org.fabric3.spi.event.EventService;
import org.fabric3.spi.event.Fabric3EventListener;
import org.fabric3.spi.event.JoinDomain;
import org.fabric3.spi.event.RuntimeStop;
import org.fabric3.spi.executor.CommandExecutorRegistry;
import org.fabric3.spi.topology.DomainTopologyService;
import org.fabric3.spi.topology.MessageException;
import org.fabric3.spi.topology.MessageTimeoutException;
import org.fabric3.spi.topology.RuntimeInstance;
import org.fabric3.spi.topology.Zone;

/**
 * @version $Rev$ $Date$
 */
@EagerInit
public class JGroupsDomainTopologyService extends AbstractTopologyService implements DomainTopologyService {
    private Channel domainChannel;
    private MessageDispatcher dispatcher;
    private JoinEventListener joinListener;
    private RuntimeStopEventListener stopListener;

    public JGroupsDomainTopologyService(@Reference HostInfo info,
                                        @Reference CommandExecutorRegistry executorRegistry,
                                        @Reference EventService eventService,
                                        @Reference Executor executor,
                                        @Reference JGroupsHelper helper) {
        super(info, executorRegistry, eventService, executor, helper);
    }


    @Init
    public void init() throws ChannelException {
        super.init();
        domainChannel = new JChannel();
        domainChannel.setName(runtimeName);

        initializeChannel(domainChannel);

        dispatcher = new MessageDispatcher(domainChannel, null, null, new DomainRequestHandler());

        // setup runtime notifications
        joinListener = new JoinEventListener();
        eventService.subscribe(JoinDomain.class, joinListener);
        stopListener = new RuntimeStopEventListener();
        eventService.subscribe(RuntimeStop.class, stopListener);
    }

    public List<Zone> getZones() {
        Address controller = domainChannel.getAddress();
        List<Zone> zones = new ArrayList<Zone>();
        Vector<Address> members = domainChannel.getView().getMembers();
        for (Address member : members) {
            if (!member.equals(controller)) {
                String zoneName = helper.getZoneName(member);
                Zone zone = new Zone(zoneName);
                if (!zones.contains(zone)) {
                    zones.add(zone);
                }
            }
        }
        return zones;
    }

    public List<RuntimeInstance> getRuntimes() {
        List<RuntimeInstance> runtimes = new ArrayList<RuntimeInstance>();
        for (Address member : domainChannel.getView().getMembers()) {
            String name = org.jgroups.util.UUID.get(member);
            RuntimeInstance runtime = new RuntimeInstance(name);
            runtimes.add(runtime);
        }
        return runtimes;
    }


    public <T> T getTransportMetaData(String zone, Class<T> type, String transport) {
        return null;
    }

    public void broadcastMessage(String zoneName, byte[] payload) throws MessageException {
        try {
            List<Address> addresses = helper.getRuntimeAddressesInZone(zoneName, domainChannel.getView());
            for (Address address : addresses) {
                Message message = new Message(address, domainChannel.getAddress(), payload);
                domainChannel.send(message);
            }
        } catch (ChannelClosedException e) {
            throw new MessageException("Error sending message to zone: " + zoneName, e);
        } catch (ChannelNotConnectedException e) {
            throw new MessageException("Error sending message to zone: " + zoneName, e);
        }
    }

    public void broadcastMessage(byte[] payload) throws MessageException {
        Message message = new Message(null, domainChannel.getAddress(), payload);
        try {
            domainChannel.send(message);
        } catch (ChannelNotConnectedException e) {
            throw new MessageException(e);
        } catch (ChannelClosedException e) {
            throw new MessageException(e);
        }
    }

    public List<byte[]> sendSynchronousMessageToZone(String zoneName, byte[] payload, long timeout) throws MessageException {
        try {
            List<Address> addresses = helper.getRuntimeAddressesInZone(zoneName, domainChannel.getView());
            List<byte[]> responses = new ArrayList<byte[]>(addresses.size());
            for (Address address : addresses) {
                Message message = new Message(address, domainChannel.getAddress(), payload);
                Object o = dispatcher.sendMessage(message, GroupRequest.GET_ALL, timeout);
                assert o instanceof byte[] : "Expected byte[] but was " + o;
                responses.add((byte[]) o);

            }
            return responses;
        } catch (TimeoutException e) {
            throw new MessageTimeoutException("Timeout sending message to zone: " + zoneName, e);
        } catch (SuspectedException e) {
            throw new MessageException("Error sending message to zone: " + zoneName, e);
        }
    }

    public byte[] sendSynchronousMessage(String runtimeName, byte[] payload, long timeout) throws MessageException {
        try {
            Address address = helper.getRuntimeAddress(runtimeName, domainChannel.getView());
            Message message = new Message(address, domainChannel.getAddress(), payload);
            Object o = dispatcher.sendMessage(message, GroupRequest.GET_ALL, timeout);
            assert o instanceof byte[] : "Expected byte[] but was " + o;
            return (byte[]) o;
        } catch (TimeoutException e) {
            throw new MessageTimeoutException("Timeout sending message to runtime: " + runtimeName, e);
        } catch (SuspectedException e) {
            throw new MessageException("Error sending message to runtime: " + runtimeName, e);
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

    protected String getRuntimeName() {
        return domainName + ":controller:" + runtimeId;
    }

    class JoinEventListener implements Fabric3EventListener<JoinDomain> {

        public void onEvent(JoinDomain event) {
            try {
                domainChannel.connect(domainName);
                dispatcher.start();
            } catch (ChannelException e) {
                // TODO log error
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

    private class DomainRequestHandler implements RequestHandler {

        public Object handle(Message msg) {
            System.out.println("--------------------handling: " + runtimeName);
            return msg.getBuffer();
        }
    }


}
