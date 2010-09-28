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
package org.fabric3.binding.web.runtime.channel;

import java.io.Serializable;

import org.fabric3.spi.channel.EventStreamHandler;
import org.fabric3.spi.channel.EventWrapper;
import org.fabric3.spi.federation.MessageException;
import org.fabric3.spi.federation.MessageReceiver;
import org.fabric3.spi.federation.ZoneTopologyService;

/**
 * Implements POST semantics for the publish/subscribe protocol, where data is sent as events to the channel.
 * <p/>
 * An event is read from the HTTP request body and stored as a string in an {@link EventWrapper}. XML (JAXB) and JSON are supported as content type
 * systems. It is the responsibility of consumers to deserialize the wrapper content into an expected Java type.
 * <p/>
 * This publisher will replicate events to other runtimes in a zone if a {@link ZoneTopologyService} is available. This allows all browser clients to
 * be notified of events emitted by all runtimes in a zone.
 *
 * @version $Rev$ $Date$
 */
public class DefaultChannelPublisher implements ChannelPublisher, EventStreamHandler, MessageReceiver {
    private String channelName;
    private ChannelMonitor monitor;
    private ZoneTopologyService topologyService;

    private EventStreamHandler next;

    /**
     * Constructor.
     *
     * @param channelName     the name of the channel the publisher sends messages to
     * @param topologyService the topology service for broadcasting events to other runtimes in the same zone. May be null, in which case events will
     *                        not be clustered.
     * @param monitor         the monitor for reporting errors
     */
    public DefaultChannelPublisher(String channelName, ZoneTopologyService topologyService, ChannelMonitor monitor) {
        this.channelName = channelName;
        this.topologyService = topologyService;
        this.monitor = monitor;
    }

    public void handle(Object event) {
        if (!(event instanceof EventWrapper) && event instanceof Serializable) {
            try {
                replicate((Serializable) event);
            } catch (MessageException e) {
                monitor.replicationError(e);
            }
        }
        // pass the object to the head stream handler
        next.handle(event);
    }

    public void publish(EventWrapper wrapper) throws PublishException {
        handle(wrapper);
    }

    public void setNext(EventStreamHandler next) {
        this.next = next;
    }

    public EventStreamHandler getNext() {
        return next;
    }

    public void onMessage(Object object) {
        handle(object);
    }

    /**
     * Replicates an event to other runtimes in a zone.
     *
     * @param event the event
     * @throws MessageException if an error replicated the event is encountered
     */
    private void replicate(Serializable event) throws MessageException {
        if (topologyService != null && topologyService.supportsDynamicChannels()) {
            topologyService.sendAsynchronous(channelName, event);
        }
    }

}
