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
 * Portions originally based on Apache Tuscany 2007
 * licensed under the Apache 2.0 license.
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
 *
 */
@EagerInit
@Management(path = "/runtime/transports/zeromq", description = "Manages ZeroMQ infrastructure")
public class ZeroMQManagementServiceImpl implements ZeroMQManagementService {
    private static final String SUBSCRIBERS_PATH = "transports/zeromq/subscribers/";
    private static final String PUBLISHERS_PATH = "transports/zeromq/publishers/";
    private static final String SENDERS_PATH = "transports/zeromq/senders/";
    private static final String RECEIVERS_PATH = "transports/zeromq/receivers/";

    private Set<String> subscribers = new HashSet<>();
    private Set<String> publishers = new HashSet<>();
    private Set<String> senders = new HashSet<>();
    private Set<String> receivers = new HashSet<>();

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
