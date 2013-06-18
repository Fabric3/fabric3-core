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
package org.fabric3.channel.disruptor.impl;

import javax.xml.namespace.QName;
import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;

import com.lmax.disruptor.BatchEventProcessor;
import com.lmax.disruptor.EventHandler;
import com.lmax.disruptor.RingBuffer;
import com.lmax.disruptor.Sequence;
import com.lmax.disruptor.SequenceBarrier;
import com.lmax.disruptor.SequenceGroup;
import com.lmax.disruptor.WaitStrategy;
import com.lmax.disruptor.dsl.Disruptor;
import com.lmax.disruptor.dsl.EventHandlerGroup;
import com.lmax.disruptor.dsl.ProducerType;
import org.fabric3.spi.channel.Channel;
import org.fabric3.spi.channel.ChannelConnection;
import org.fabric3.spi.channel.EventStream;
import org.fabric3.spi.channel.EventStreamHandler;
import org.fabric3.spi.model.physical.ChannelSide;

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
    private ChannelSide channelSide;
    private ExecutorService executorService;

    private RingBuffer<RingBufferEvent> ringBuffer;
    private Disruptor<RingBufferEvent> disruptor;

    private Map<URI, ChannelConnection> subscribers;
    private Map<URI, Sequence> sequences;

    private int numberProducers;
    private SequenceGroup sequenceGroup;

    public RingBufferChannel(URI uri, QName deployable, int size, WaitStrategy waitStrategy, ChannelSide channelSide, ExecutorService executorService) {
        this.uri = uri;
        this.deployable = deployable;
        this.size = size;
        this.waitStrategy = waitStrategy;
        this.channelSide = channelSide;
        this.executorService = executorService;
        subscribers = new HashMap<URI, ChannelConnection>();
        sequences = new HashMap<URI, Sequence>();
    }

    @SuppressWarnings("unchecked")
    public void start() {
        ProducerType producerType = numberProducers > 1 ? ProducerType.MULTI : ProducerType.SINGLE;
        disruptor = new Disruptor<RingBufferEvent>(EVENT_FACTORY, size, executorService, producerType, waitStrategy);

        Map<Integer, List<EventHandler<RingBufferEvent>>> sorted = EventHandlerHelper.createAndSort(subscribers.values());

        EventHandlerGroup group = null;
        for (List<EventHandler<RingBufferEvent>> handlers : sorted.values()) {
            if (group == null) {
                group = disruptor.handleEventsWith(handlers.toArray(new EventHandler[handlers.size()]));
            } else {
                group = group.then(handlers.toArray(new EventHandler[handlers.size()]));
            }
        }

        sequenceGroup = new SequenceGroup();
        disruptor.getRingBuffer().addGatingSequences(sequenceGroup);
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

    public ChannelSide getChannelSide() {
        return channelSide;
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
        EventStream stream = connection.getEventStream();
        stream.getTailHandler().setNext(this);
    }

    public void subscribe(URI uri, ChannelConnection connection) {
        if (ringBuffer == null) {
            subscribers.put(uri, connection);
        } else {
            // ring buffer already started, add dynamically
            ChannelEventHandler handler = new ChannelEventHandler(connection);
            SequenceBarrier barrier = ringBuffer.newBarrier();
            BatchEventProcessor<RingBufferEvent> processor = new BatchEventProcessor<RingBufferEvent>(ringBuffer, barrier, handler);
            Sequence sequence = processor.getSequence();
            sequenceGroup.addWhileRunning(ringBuffer, sequence);
            executorService.execute(processor);

            sequences.put(uri, sequence);
            subscribers.put(uri, connection);
        }
    }

    public ChannelConnection unsubscribe(URI uri) {
        ChannelConnection connection = subscribers.remove(uri);
        Sequence sequence = sequences.get(uri);
        if (sequence != null) {
            // may be null if registered prior to channel start
            sequenceGroup.remove(sequence);
        }
        return connection;
    }

    public void handle(Object event, boolean endOfBatch) {
        ringBuffer.publishEvent(TRANSLATOR, event);
    }

    public void setNext(EventStreamHandler next) {
        throw new IllegalStateException();
    }

    public EventStreamHandler getNext() {
        return null;
    }

}
