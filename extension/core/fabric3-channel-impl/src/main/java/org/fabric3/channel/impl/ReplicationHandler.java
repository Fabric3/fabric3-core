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
package org.fabric3.channel.impl;

import java.io.Serializable;

import org.fabric3.spi.container.channel.EventStreamHandler;
import org.fabric3.spi.container.channel.EventWrapper;
import org.fabric3.spi.federation.topology.MessageException;
import org.fabric3.spi.federation.topology.MessageReceiver;
import org.fabric3.spi.federation.topology.ParticipantTopologyService;

/**
 * Responsible for handling event replication in a zone. Specifically, replicates events to other channel instances and passes replicated events through to
 * downstream handlers.
 */
public class ReplicationHandler implements EventStreamHandler, MessageReceiver {
    private String channelName;
    private ParticipantTopologyService topologyService;
    private EventStreamHandler next;
    private ReplicationMonitor monitor;

    public ReplicationHandler(String channelName, ParticipantTopologyService topologyService, ReplicationMonitor monitor) {
        this.topologyService = topologyService;
        this.channelName = channelName;
        this.monitor = monitor;
    }

    public void setNext(EventStreamHandler next) {
        this.next = next;
    }

    public EventStreamHandler getNext() {
        return next;
    }

    public void handle(Object event, boolean endOfBatch) {
        if (!(event instanceof EventWrapper) && event instanceof Serializable) {
            // check for EventWrapper to avoid re-replicating an event that was just replicated
            try {
                topologyService.sendAsynchronous(channelName, (Serializable) event);
            } catch (MessageException e) {
                monitor.error(e);
            }
        }
        // pass the object to the head stream handler
        next.handle(event, endOfBatch);
    }

    public void onMessage(Object event) {
        next.handle(event, true);
    }
}
