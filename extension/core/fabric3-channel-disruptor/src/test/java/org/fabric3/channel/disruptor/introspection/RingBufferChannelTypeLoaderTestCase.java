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
package org.fabric3.channel.disruptor.introspection;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import java.io.ByteArrayInputStream;

import junit.framework.TestCase;
import org.fabric3.api.model.type.component.RingBufferData;
import org.fabric3.api.model.type.component.Channel;
import org.fabric3.spi.model.physical.ChannelConstants;
import org.fabric3.spi.introspection.DefaultIntrospectionContext;
import org.fabric3.spi.introspection.IntrospectionContext;

/**
 *
 */
public class RingBufferChannelTypeLoaderTestCase extends TestCase {
    private static final byte[] RING_SIZE = "<channel name = 'channel' ring.size = '10'/>".getBytes();
    private static final byte[] BLOCKING = "<channel name = 'channel' wait.strategy = 'blocking'/>".getBytes();
    private static final byte[] BACKOFF = "<channel name = 'channel' wait.strategy = 'backoff'/>".getBytes();
    private static final byte[] YIELDING = "<channel name = 'channel' wait.strategy = 'yielding'/>".getBytes();
    private static final byte[] SLEEPING = "<channel name = 'channel' wait.strategy = 'sleeping'/>".getBytes();
    private static final byte[] SPIN = "<channel name = 'channel' wait.strategy = 'spin'/>".getBytes();
    private static final byte[] TIMEOUT = "<channel name = 'channel' wait.strategy = 'timeout'/>".getBytes();
    private static final byte[] PHASED_LOCK = "<channel name = 'channel' phased.blocking.type = 'lock'/>".getBytes();
    private static final byte[] PHASED_SLEEP = "<channel name = 'channel' phased.blocking.type = 'sleep'/>".getBytes();
    private static final byte[] BLOCKING_TIMEOUT = "<channel name = 'channel' blocking.timeout = '10'/>".getBytes();
    private static final byte[] SPIN_TIMEOUT = "<channel name = 'channel' spin.timeout = '10'/>".getBytes();
    private static final byte[] YIELD_TIMEOUT = "<channel name = 'channel' yield.timeout = '10'/>".getBytes();

    private RingBufferChannelTypeLoader loader;
    private IntrospectionContext context;
    private Channel definition;

    public void testRingSize() throws Exception {
        XMLStreamReader reader = getReader(RING_SIZE);

        loader.load(definition, reader, context);

        assertFalse(context.hasErrors());
        RingBufferData data = definition.getMetadata(ChannelConstants.METADATA, RingBufferData.class);
        assertEquals(10, data.getRingSize());
    }

    public void testBlockWaitStrategy() throws Exception {
        XMLStreamReader reader = getReader(BLOCKING);

        loader.load(definition, reader, context);

        assertFalse(context.hasErrors());
        RingBufferData data = definition.getMetadata(ChannelConstants.METADATA, RingBufferData.class);
        assertEquals(RingBufferData.WaitStrategyType.BLOCKING, data.getWaitStrategy());
    }

    public void testBackOffWaitStrategy() throws Exception {
        XMLStreamReader reader = getReader(BACKOFF);

        loader.load(definition, reader, context);

        assertFalse(context.hasErrors());
        RingBufferData data = definition.getMetadata(ChannelConstants.METADATA, RingBufferData.class);
        assertEquals(RingBufferData.WaitStrategyType.BACKOFF, data.getWaitStrategy());
    }

    public void testYieldWaitStrategy() throws Exception {
        XMLStreamReader reader = getReader(YIELDING);

        loader.load(definition, reader, context);

        assertFalse(context.hasErrors());
        RingBufferData data = definition.getMetadata(ChannelConstants.METADATA, RingBufferData.class);
        assertEquals(RingBufferData.WaitStrategyType.YIELDING, data.getWaitStrategy());
    }

    public void testSleepingWaitStrategy() throws Exception {
        XMLStreamReader reader = getReader(SLEEPING);

        loader.load(definition, reader, context);

        assertFalse(context.hasErrors());
        RingBufferData data = definition.getMetadata(ChannelConstants.METADATA, RingBufferData.class);
        assertEquals(RingBufferData.WaitStrategyType.SLEEPING, data.getWaitStrategy());
    }

    public void testSpinWaitStrategy() throws Exception {
        XMLStreamReader reader = getReader(SPIN);

        loader.load(definition, reader, context);

        assertFalse(context.hasErrors());
        RingBufferData data = definition.getMetadata(ChannelConstants.METADATA, RingBufferData.class);
        assertEquals(RingBufferData.WaitStrategyType.SPIN, data.getWaitStrategy());
    }

    public void testTimeoutWaitStrategy() throws Exception {
        XMLStreamReader reader = getReader(TIMEOUT);

        loader.load(definition, reader, context);

        assertFalse(context.hasErrors());
        RingBufferData data = definition.getMetadata(ChannelConstants.METADATA, RingBufferData.class);
        assertEquals(RingBufferData.WaitStrategyType.TIMEOUT, data.getWaitStrategy());
    }

    public void testBlockingLock() throws Exception {
        XMLStreamReader reader = getReader(PHASED_LOCK);

        loader.load(definition, reader, context);

        assertFalse(context.hasErrors());
        RingBufferData data = definition.getMetadata(ChannelConstants.METADATA, RingBufferData.class);
        assertEquals(RingBufferData.PhasedBlockingType.LOCK, data.getPhasedBlockingType());
    }

    public void testSleepLock() throws Exception {
        XMLStreamReader reader = getReader(PHASED_SLEEP);

        loader.load(definition, reader, context);

        assertFalse(context.hasErrors());
        RingBufferData data = definition.getMetadata(ChannelConstants.METADATA, RingBufferData.class);
        assertEquals(RingBufferData.PhasedBlockingType.SLEEP, data.getPhasedBlockingType());
    }

    public void testBlockingTimeout() throws Exception {
        XMLStreamReader reader = getReader(BLOCKING_TIMEOUT);

        loader.load(definition, reader, context);

        assertFalse(context.hasErrors());
        RingBufferData data = definition.getMetadata(ChannelConstants.METADATA, RingBufferData.class);
        assertEquals(10, data.getBlockingTimeoutNanos());
    }

    public void testSpinTimeout() throws Exception {
        XMLStreamReader reader = getReader(SPIN_TIMEOUT);

        loader.load(definition, reader, context);

        assertFalse(context.hasErrors());
        RingBufferData data = definition.getMetadata(ChannelConstants.METADATA, RingBufferData.class);
        assertEquals(10, data.getSpinTimeoutNanos());
    }

    public void testYieldTimeout() throws Exception {
        XMLStreamReader reader = getReader(YIELD_TIMEOUT);

        loader.load(definition, reader, context);

        assertFalse(context.hasErrors());
        RingBufferData data = definition.getMetadata(ChannelConstants.METADATA, RingBufferData.class);
        assertEquals(10, data.getYieldTimeoutNanos());
    }

    private XMLStreamReader getReader(byte[] xml) throws XMLStreamException {
        XMLStreamReader reader = XMLInputFactory.newFactory().createXMLStreamReader(new ByteArrayInputStream(xml));
        reader.nextTag();
        return reader;
    }

    public void setUp() throws Exception {
        super.setUp();
        loader = new RingBufferChannelTypeLoader();
        context = new DefaultIntrospectionContext();
        definition = new Channel("channel");
    }
}
