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
import org.fabric3.spi.container.channel.Channel;
import org.fabric3.spi.container.channel.ChannelConnection;
import org.fabric3.spi.container.channel.EventStream;
import org.fabric3.spi.container.channel.EventStreamHandler;
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
        subscribers = new HashMap<>();
        sequences = new HashMap<>();
    }

    @SuppressWarnings("unchecked")
    public void start() {
        ProducerType producerType = numberProducers > 1 ? ProducerType.MULTI : ProducerType.SINGLE;
        disruptor = new Disruptor<>(EVENT_FACTORY, size, executorService, producerType, waitStrategy);

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
            boolean channelEvent = EventHandlerHelper.isChannelEvent(connection);
            ChannelEventHandler handler = new ChannelEventHandler(connection, channelEvent);
            SequenceBarrier barrier = ringBuffer.newBarrier();
            BatchEventProcessor<RingBufferEvent> processor = new BatchEventProcessor<>(ringBuffer, barrier, handler);
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

    public Object getDirectConnection() {
        return ringBuffer;
    }
}
