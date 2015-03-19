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
package org.fabric3.api.model.type.builder;

import javax.xml.namespace.QName;

import org.fabric3.api.model.type.component.Channel;
import org.fabric3.api.model.type.component.RingBufferData;
import static org.fabric3.api.model.type.component.RingBufferData.PhasedBlockingType;
import static org.fabric3.api.model.type.component.RingBufferData.WaitStrategyType;

/**
 *
 */
public class RingBufferChannelBuilder extends AbstractBuilder {
    private static final QName METADATA = new QName(org.fabric3.api.Namespaces.F3, "metadata");

    private final Channel definition;
    private final RingBufferData data;

    /**
     * Creates a ring buffer channel builder.
     *
     * @param name the channel name
     * @return the builder
     */
    public static RingBufferChannelBuilder newBuilder(String name) {
        return new RingBufferChannelBuilder(name);
    }

    protected RingBufferChannelBuilder(String name) {
        definition = new Channel(name);
        data = new RingBufferData();
        definition.setMetadata(data);
    }

    public RingBufferChannelBuilder type(String type) {
        checkState();
        definition.setType(type);
        return this;
    }

    public RingBufferChannelBuilder ringSize(int size) {
        checkState();
        data.setRingSize(size);
        return this;
    }

    public RingBufferChannelBuilder blockingTimeout(long nanos) {
        checkState();
        data.setBlockingTimeoutNanos(nanos);
        return this;
    }

    public RingBufferChannelBuilder phasedBlockingType(PhasedBlockingType type) {
        checkState();
        data.setPhasedBlockingType(type);
        return this;
    }

    public RingBufferChannelBuilder spinTimeout(long nanos) {
        checkState();
        data.setSpinTimeoutNanos(nanos);
        return this;
    }

    public RingBufferChannelBuilder waitStrategy(WaitStrategyType strategy) {
        checkState();
        data.setWaitStrategy(strategy);
        return this;
    }

    public RingBufferChannelBuilder yieldTimeout(long nanos) {
        checkState();
        data.setYieldTimeoutNanos(nanos);
        return this;
    }

    /**
     * Builds the channel definition.
     *
     * @return the definition
     */
    public Channel build() {
        checkState();
        freeze();
        return definition;
    }

}
