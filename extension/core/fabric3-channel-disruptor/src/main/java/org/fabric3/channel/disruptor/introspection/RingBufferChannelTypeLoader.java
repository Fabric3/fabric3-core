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
package org.fabric3.channel.disruptor.introspection;

import javax.xml.stream.Location;
import javax.xml.stream.XMLStreamReader;

import org.fabric3.api.model.type.component.RingBufferData;
import org.fabric3.api.model.type.component.ChannelDefinition;
import org.fabric3.spi.model.physical.ChannelConstants;
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

    public void load(ChannelDefinition definition, XMLStreamReader reader, IntrospectionContext context) {
        Location startLocation = reader.getLocation();

        RingBufferData data = new RingBufferData();

        int ringSize = parseRingSize(definition, reader, startLocation, context, data);
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

        definition.addMetadata(ChannelConstants.METADATA, data);
    }

    private int parseRingSize(ChannelDefinition definition, XMLStreamReader reader, Location startLocation, IntrospectionContext context, RingBufferData data) {
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

    private long parseLong(ChannelDefinition definition,
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
