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
