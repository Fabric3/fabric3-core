/*
 * Fabric3 Copyright (c) 2009-2012 Metaform Systems
 * 
 * Fabric3 is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version, with the following exception:
 * 
 * Linking this software statically or dynamically with other modules is making
 * a combined work based on this software. Thus, the terms and conditions of the
 * GNU General Public License cover the whole combination.
 * 
 * As a special exception, the copyright holders of this software give you
 * permission to link this software with independent modules to produce an
 * executable, regardless of the license terms of these independent modules, and
 * to copy and distribute the resulting executable under terms of your choice,
 * provided that you also meet, for each linked independent module, the terms
 * and conditions of the license of that module. An independent module is a
 * module which is not derived from or based on this software. If you modify
 * this software, you may extend this exception to your version of the software,
 * but you are not obligated to do so. If you do not wish to do so, delete this
 * exception statement from your version.
 * 
 * Fabric3 is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * Fabric3. If not, see <http://www.gnu.org/licenses/>.
 */
package org.fabric3.binding.zeromq.runtime.management;

import java.net.URI;
import java.util.HashSet;
import java.util.Set;

import org.oasisopen.sca.annotation.EagerInit;
import org.oasisopen.sca.annotation.Reference;

import org.fabric3.api.annotation.management.Management;
import org.fabric3.api.annotation.management.ManagementOperation;
import org.fabric3.api.annotation.monitor.Monitor;
import org.fabric3.binding.zeromq.runtime.message.Publisher;
import org.fabric3.binding.zeromq.runtime.message.Receiver;
import org.fabric3.binding.zeromq.runtime.message.Sender;
import org.fabric3.binding.zeromq.runtime.message.Subscriber;
import org.fabric3.spi.management.ManagementException;
import org.fabric3.spi.management.ManagementService;

/**
 * @version $Revision: 10212 $ $Date: 2011-03-15 18:20:58 +0100 (Tue, 15 Mar 2011) $
 */
@EagerInit
@Management(path = "/runtime/transports/zeromq", description = "Manages ZeroMQ infrastructure")
public class ZeroMQManagementServiceImpl implements ZeroMQManagementService {
    private static final String SUBSCRIBERS_PATH = "transports/zeromq/subscribers/";
    private static final String PUBLISHERS_PATH = "transports/zeromq/publishers/";
    private static final String SENDERS_PATH = "transports/zeromq/senders/";
    private static final String RECEIVERS_PATH = "transports/zeromq/receivers/";

    private Set<String> subscribers = new HashSet<String>();
    private Set<String> publishers = new HashSet<String>();
    private Set<String> senders = new HashSet<String>();
    private Set<String> receivers = new HashSet<String>();

    private ManagementService managementService;
    private ManagementMonitor monitor;

    public ZeroMQManagementServiceImpl(@Reference ManagementService managementService, @Monitor ManagementMonitor monitor) {
        this.managementService = managementService;
        this.monitor = monitor;
    }

    @ManagementOperation
    public Set<String> getSubscribers() {
        return subscribers;
    }

    @ManagementOperation
    public Set<String> getPublishers() {
        return publishers;
    }

    @ManagementOperation
    public Set<String> getSenders() {
        return senders;
    }

    @ManagementOperation
    public Set<String> getReceivers() {
        return receivers;
    }

    public void register(String channelName, URI subscriberId, Subscriber subscriber) {
        try {
            subscribers.add(channelName);
            managementService.export(SUBSCRIBERS_PATH + channelName, "", "", subscriber);
        } catch (ManagementException e) {
            monitor.error("Error registering subscriber for channel " + channelName, e);
        }
    }

    public void unregister(String channelName, URI subscriberId) {
        try {
            subscribers.remove(channelName);
            managementService.remove(SUBSCRIBERS_PATH + channelName, "");
        } catch (ManagementException e) {
            monitor.error("Error unregistering subscriber for channel " + channelName, e);
        }
    }

    public void register(String channelName, Publisher publisher) {
        publishers.add(channelName);
        try {
            managementService.export(PUBLISHERS_PATH + channelName, "", "", publisher);
        } catch (ManagementException e) {
            monitor.error("Error registering publisher for channel " + channelName, e);
        }
    }

    public void unregister(String channelName) {
        try {
            publishers.remove(channelName);
            managementService.remove(PUBLISHERS_PATH + channelName, "");
        } catch (ManagementException e) {
            monitor.error("Error unregistering publisher for channel " + channelName, e);
        }
    }

    public void registerSender(String id, Sender sender) {
        senders.add(id);
        try {
            managementService.export(SENDERS_PATH + id, "", "", sender);
        } catch (ManagementException e) {
            monitor.error("Error registering publisher for channel " + id, e);
        }
    }

    public void unregisterSender(String id) {
        senders.remove(id);
        try {
            managementService.remove(SENDERS_PATH + id, "");
        } catch (ManagementException e) {
            monitor.error("Error unregistering sender: " + id, e);
        }
    }

    public void registerReceiver(String id, Receiver receiver) {
        receivers.add(id);
        try {
            managementService.export(RECEIVERS_PATH + id, "", "", receiver);
        } catch (ManagementException e) {
            monitor.error("Error registering receiver: " + id, e);
        }
    }

    public void unregisterReceiver(String id) {
        receivers.remove(id);
        try {
            managementService.remove(RECEIVERS_PATH + id, "");
        } catch (ManagementException e) {
            monitor.error("Error unregistering receiver: " + id, e);
        }
    }
}
