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

import org.fabric3.spi.federation.topology.MessageException;
import org.fabric3.spi.federation.topology.MessageReceiver;
import org.jgroups.Address;
import org.jgroups.Channel;
import org.jgroups.MembershipListener;
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
    private MembershipListener listener;
    private TopologyServiceMonitor monitor;

    public DelegatingReceiver(Channel channel, MessageReceiver receiver, JGroupsHelper helper, MembershipListener listener, TopologyServiceMonitor monitor) {
        this.channel = channel;
        this.receiver = receiver;
        this.helper = helper;
        this.listener = listener;
        this.monitor = monitor;
    }

    public void receive(Message message) {
        if (message.getSrc() != channel.getAddress()) {
            try {
                Object payload = helper.deserialize(message.getBuffer());
                receiver.onMessage(payload);
            } catch (MessageException e) {
                monitor.error("Error deserializing message payload", e);
            }
        }
    }

    public void getState(OutputStream output) throws Exception {

    }

    public void setState(InputStream input) throws Exception {

    }

    public void viewAccepted(View newView) {
        if (listener != null) {
            listener.viewAccepted(newView);
        }
    }

    public void suspect(Address suspected) {
        if (listener != null) {
            listener.suspect(suspected);
        }
    }

    public void block() {
        if (listener != null) {
            listener.block();
        }
    }

    public void unblock() {
        if (listener != null) {
            listener.unblock();
        }
    }

}
