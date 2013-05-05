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
package org.fabric3.channel.disruptor.impl;

import javax.xml.namespace.QName;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;

import com.lmax.disruptor.EventHandler;
import com.lmax.disruptor.RingBuffer;
import com.lmax.disruptor.WaitStrategy;
import com.lmax.disruptor.dsl.Disruptor;
import com.lmax.disruptor.dsl.ProducerType;
import org.fabric3.spi.channel.Channel;
import org.fabric3.spi.channel.ChannelConnection;
import org.fabric3.spi.channel.EventStream;
import org.fabric3.spi.channel.EventStreamHandler;

/**
 * A channel implementation that uses a Disruptor ring buffer to dispatch to consumers.
 */
public class RingBufferChannel implements Channel, EventStreamHandler {
    private static final RingBufferEventTranslator TRANSLATOR = new RingBufferEventTranslator();
    private static final RingBufferEventFactory EVENT_FACTORY = new RingBufferEventFactory();

    private URI uri;
    private QName deployable;
    private int size;
    private WaitStrategy waitStrategy;
    private ExecutorService executorService;
    private RingBuffer<RingBufferEvent> ringBuffer;
    private Disruptor<RingBufferEvent> disruptor;

    private Map<URI, ChannelConnection> subscribers;

    private int numberProducers;

    public RingBufferChannel(URI uri, QName deployable, int size, WaitStrategy waitStrategy, ExecutorService executorService) {
        this.uri = uri;
        this.deployable = deployable;
        this.size = size;
        this.waitStrategy = waitStrategy;
        this.executorService = executorService;
        subscribers = new HashMap<URI, ChannelConnection>();
    }

    public void start() {
        ProducerType producerType = numberProducers > 1 ? ProducerType.MULTI : ProducerType.SINGLE;
        disruptor = new Disruptor<RingBufferEvent>(EVENT_FACTORY, size, executorService, producerType, waitStrategy);
        EventHandler[] handlers = new EventHandler[subscribers.size()];
        int i = 0;
        for (ChannelConnection connection : subscribers.values()) {
            handlers[i] = new ChannelEventHandler(connection);
        }
        disruptor.handleEventsWith(handlers);

        ringBuffer = disruptor.start();
    }

    public void stop() {
        disruptor.shutdown();
    }

    public URI getUri() {
        return uri;
    }

    public QName getDeployable() {
        return deployable;
    }

    public void addHandler(EventStreamHandler handler) {
        throw new UnsupportedOperationException();
    }

    public void removeHandler(EventStreamHandler handler) {
        throw new UnsupportedOperationException();
    }

    public void attach(EventStreamHandler handler) {
        numberProducers++;
        handler.setNext(this);
    }

    public void attach(ChannelConnection connection) {
        numberProducers++;
        for (EventStream stream : connection.getEventStreams()) {
            stream.getTailHandler().setNext(this);
        }
    }

    public void subscribe(URI uri, ChannelConnection connection) {
        subscribers.put(uri, connection);

    }

    public ChannelConnection unsubscribe(URI uri) {
        return subscribers.remove(uri);
    }

    public void handle(Object event) {
        ringBuffer.publishEvent(TRANSLATOR, event);
    }

    public void setNext(EventStreamHandler next) {
        throw new IllegalStateException();
    }

    public EventStreamHandler getNext() {
        return null;
    }

}
