/*
 * Fabric3
 * Copyright (c) 2009-2012 Metaform Systems
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

import javax.xml.namespace.QName;
import java.net.URI;

import org.fabric3.channel.handler.FanOutHandler;
import org.fabric3.spi.channel.Channel;
import org.fabric3.spi.channel.ChannelConnection;
import org.fabric3.spi.channel.EventStream;
import org.fabric3.spi.channel.EventStreamHandler;
import org.fabric3.spi.channel.PassThroughHandler;

/**
 * The default Channel implementation.
 */
public class DefaultChannelImpl implements Channel {
    private URI uri;
    private QName deployable;
    private EventStreamHandler headHandler;
    private EventStreamHandler tailHandler;
    private EventStreamHandler inHandler;
    private FanOutHandler fanOutHandler;

    public DefaultChannelImpl(URI uri, QName deployable, FanOutHandler fanOutHandler) {
        this.uri = uri;
        this.deployable = deployable;
        inHandler = new PassThroughHandler();
        this.fanOutHandler = fanOutHandler;
        inHandler.setNext(this.fanOutHandler);
    }

    public DefaultChannelImpl(URI uri, QName deployable, EventStreamHandler inHandler, FanOutHandler fanOutHandler) {
        this.uri = uri;
        this.deployable = deployable;
        this.inHandler = inHandler;
        this.fanOutHandler = fanOutHandler;
        this.inHandler.setNext(fanOutHandler);
    }

    public URI getUri() {
        return uri;
    }

    public QName getDeployable() {
        return deployable;
    }

    public void start() {
        // no-op
    }

    public void stop() {
        // no-op
    }

    public void addHandler(EventStreamHandler handler) {
        if (headHandler == null) {
            headHandler = handler;
            inHandler.setNext(handler);
        } else {
            tailHandler.setNext(handler);
        }
        tailHandler = handler;
        tailHandler.setNext(fanOutHandler);
    }

    public void removeHandler(EventStreamHandler handler) {
        EventStreamHandler current = headHandler;
        EventStreamHandler previous = null;
        while (current != null) {
            if (current == handler) {
                if (headHandler == current) {
                    headHandler = current.getNext();
                }
                if (tailHandler == current) {
                    tailHandler = previous == null ? headHandler : previous;
                }
                if (previous != null) {
                    previous.setNext(current.getNext());
                }
                inHandler.setNext(headHandler);
                return;
            }
            previous = current;
            current = current.getNext();
        }
    }

    public void attach(EventStreamHandler handler) {
        handler.setNext(inHandler);
    }

    public void attach(ChannelConnection connection) {
        EventStream stream = connection.getEventStream();
        stream.getTailHandler().setNext(inHandler);
    }

    public void subscribe(URI uri, ChannelConnection connection) {
        fanOutHandler.addConnection(uri, connection);
    }

    public ChannelConnection unsubscribe(URI uri) {
        return fanOutHandler.removeConnection(uri);
    }
}