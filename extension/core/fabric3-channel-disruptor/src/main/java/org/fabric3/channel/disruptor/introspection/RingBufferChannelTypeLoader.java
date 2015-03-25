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

import javax.xml.stream.Location;
import javax.xml.stream.XMLStreamReader;

import com.lmax.disruptor.RingBuffer;
import org.fabric3.api.model.type.component.Channel;
import org.fabric3.api.model.type.component.RingBufferData;
import org.fabric3.spi.introspection.IntrospectionContext;
import org.fabric3.spi.introspection.xml.ChannelTypeLoader;
import org.fabric3.spi.introspection.xml.InvalidValue;
import org.oasisopen.sca.annotation.EagerInit;

/**
 *
 */
@EagerInit
public class RingBufferChannelTypeLoader implements ChannelTypeLoader {
    private static final String RING_SIZE = "ring.size";
    private static final String WAIT_STRATEGY = "wait.strategy";
    private static final String BLOCKING_TIMEOUT = "blocking.timeout";
    private static final String SPIN_TIMEOUT = "spin.timeout";
    private static final String YIELD_TIMEOUT = "yield.timeout";
    private static final String PHASED_BLOCKING_TYPE = "phased.blocking.type";

    private static final String[] ATTRIBUTES = new String[]{RING_SIZE, WAIT_STRATEGY, BLOCKING_TIMEOUT, SPIN_TIMEOUT, YIELD_TIMEOUT, PHASED_BLOCKING_TYPE};

    private static final int DEFAULT_RING_SIZE = 65536;
    private static final long DEFAULT_BLOCKING_TIMEOUT = 1000;
    private static final long DEFAULT_SPIN_TIMEOUT = 1000;
    private static final long DEFAULT_YIELD_TIMEOUT = 1000;

    public String[] getAttributes() {
        return ATTRIBUTES;
    }

    public void load(Channel definition, XMLStreamReader reader, IntrospectionContext context) {
        Location startLocation = reader.getLocation();

        RingBufferData data = new RingBufferData();

        int ringSize = parseRingSize(definition, reader, startLocation, context);
        data.setRingSize(ringSize);

        String waitStrategy = reader.getAttributeValue(null, WAIT_STRATEGY);
        if (waitStrategy != null) {
            try {
                RingBufferData.WaitStrategyType type = RingBufferData.WaitStrategyType.valueOf(waitStrategy.toUpperCase());
                data.setWaitStrategy(type);
            } catch (IllegalArgumentException e) {
                context.addError(new InvalidValue("Invalid wait strategy", startLocation, definition));
            }
        }

        long blockingTimeout = parseLong(definition, reader, startLocation, BLOCKING_TIMEOUT, "Invalid blocking timeout: ", DEFAULT_BLOCKING_TIMEOUT, context);
        data.setBlockingTimeoutNanos(blockingTimeout);

        long spinTimeout = parseLong(definition, reader, startLocation, SPIN_TIMEOUT, "Invalid spin timeout: ", DEFAULT_SPIN_TIMEOUT, context);
        data.setSpinTimeoutNanos(spinTimeout);

        long yieldTimeout = parseLong(definition, reader, startLocation, YIELD_TIMEOUT, "Invalid yield timeout: ", DEFAULT_YIELD_TIMEOUT, context);
        data.setYieldTimeoutNanos(yieldTimeout);

        String phasedBlockingStrategy = reader.getAttributeValue(null, PHASED_BLOCKING_TYPE);
        if (phasedBlockingStrategy != null) {
            try {
                RingBufferData.PhasedBlockingType type = RingBufferData.PhasedBlockingType.valueOf(phasedBlockingStrategy.toUpperCase());
                data.setPhasedBlockingType(type);
            } catch (IllegalArgumentException e) {
                context.addError(new InvalidValue("Invalid phased blocking type", startLocation, definition));
            }

        }
        definition.setConnectionType(RingBuffer.class);
        definition.setMetadata(data);
    }

    private int parseRingSize(Channel definition, XMLStreamReader reader, Location startLocation, IntrospectionContext context) {
        String sizeStr = reader.getAttributeValue(null, RING_SIZE);
        int ringSize = DEFAULT_RING_SIZE;
        if (sizeStr != null) {
            try {
                ringSize = Integer.parseInt(sizeStr);
            } catch (NumberFormatException e) {
                context.addError(new InvalidValue("Invalid ring buffer size" + sizeStr, startLocation, definition));
            }
        }
        return ringSize;
    }

    private long parseLong(Channel definition,
                           XMLStreamReader reader,
                           Location startLocation,
                           String attributeName,
                           String errorText,
                           long defaultValue,
                           IntrospectionContext context) {
        String valueStr = reader.getAttributeValue(null, attributeName);
        long value = defaultValue;
        if (valueStr != null) {
            try {
                value = Long.parseLong(valueStr);
            } catch (NumberFormatException e) {
                context.addError(new InvalidValue(errorText + valueStr, startLocation, definition));
            }
        }
        return value;
    }
}
