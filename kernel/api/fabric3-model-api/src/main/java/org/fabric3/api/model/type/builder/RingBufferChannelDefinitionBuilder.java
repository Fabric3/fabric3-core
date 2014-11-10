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

import org.fabric3.api.model.type.component.ChannelDefinition;
import org.fabric3.api.model.type.component.RingBufferData;
import static org.fabric3.api.model.type.component.RingBufferData.PhasedBlockingType;
import static org.fabric3.api.model.type.component.RingBufferData.WaitStrategyType;

/**
 *
 */
public class RingBufferChannelDefinitionBuilder extends AbstractBuilder {
    private static final QName METADATA = new QName(org.fabric3.api.Namespaces.F3, "metadata");

    private final ChannelDefinition definition;
    private final RingBufferData data;

    /**
     * Creates a ring buffer channel builder.
     *
     * @param name the channel name
     * @return the builder
     */
    public static RingBufferChannelDefinitionBuilder newBuilder(String name) {
        return new RingBufferChannelDefinitionBuilder(name);
    }

    protected RingBufferChannelDefinitionBuilder(String name) {
        definition = new ChannelDefinition(name);
        data = new RingBufferData();
        definition.addMetadata(METADATA, data);
    }

    public RingBufferChannelDefinitionBuilder type(String type) {
        checkState();
        definition.setType(type);
        return this;
    }

    public RingBufferChannelDefinitionBuilder ringSize(int size) {
        checkState();
        data.setRingSize(size);
        return this;
    }

    public RingBufferChannelDefinitionBuilder blockingTimeout(long nanos) {
        checkState();
        data.setBlockingTimeoutNanos(nanos);
        return this;
    }

    public RingBufferChannelDefinitionBuilder phasedBlockingType(PhasedBlockingType type) {
        checkState();
        data.setPhasedBlockingType(type);
        return this;
    }

    public RingBufferChannelDefinitionBuilder spinTimeout(long nanos) {
        checkState();
        data.setSpinTimeoutNanos(nanos);
        return this;
    }

    public RingBufferChannelDefinitionBuilder waitStrategy(WaitStrategyType strategy) {
        checkState();
        data.setWaitStrategy(strategy);
        return this;
    }

    public RingBufferChannelDefinitionBuilder yieldTimeout(long nanos) {
        checkState();
        data.setYieldTimeoutNanos(nanos);
        return this;
    }

    /**
     * Builds the channel definition.
     *
     * @return the definition
     */
    public ChannelDefinition build() {
        checkState();
        freeze();
        return definition;
    }

}
