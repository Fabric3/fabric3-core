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

import java.io.InputStream;
import java.io.OutputStream;

import org.fabric3.api.host.Fabric3Exception;
import org.fabric3.spi.federation.topology.MessageReceiver;
import org.jgroups.Address;
import org.jgroups.Channel;
import org.jgroups.Message;
import org.jgroups.Receiver;
import org.jgroups.View;

/**
 *
 */
public class DelegatingReceiver implements Receiver {
    private Channel channel;
    private MessageReceiver receiver;
    private JGroupsHelper helper;
    private TopologyServiceMonitor monitor;

    public DelegatingReceiver(Channel channel, MessageReceiver receiver, JGroupsHelper helper, TopologyServiceMonitor monitor) {
        this.channel = channel;
        this.receiver = receiver;
        this.helper = helper;
        this.monitor = monitor;
    }

    public void receive(Message message) {
        if (message.getSrc() != channel.getAddress()) {
            try {
                Object payload = helper.deserialize(message.getBuffer());
                receiver.onMessage(payload);
            } catch (Fabric3Exception e) {
                monitor.error("Error deserializing message payload", e);
            }
        }
    }

    public void getState(OutputStream output) throws Exception {

    }

    public void setState(InputStream input) throws Exception {

    }

    public void viewAccepted(View newView) {
    }

    public void suspect(Address suspected) {
    }

    public void block() {
    }

    public void unblock() {
    }

}
