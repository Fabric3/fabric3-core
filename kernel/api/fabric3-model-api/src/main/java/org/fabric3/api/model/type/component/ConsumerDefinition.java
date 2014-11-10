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
 *
 * Portions originally based on Apache Tuscany 2007
 * licensed under the Apache 2.0 license.
 */
package org.fabric3.api.model.type.component;

import java.util.List;

import org.fabric3.api.model.type.contract.DataType;

/**
 * A consumer introspected from a component type.
 */
public class ConsumerDefinition extends AbstractConsumer<ComponentType> {
    private static final long serialVersionUID = -4222312633353056234L;
    public static final int NO_SEQUENCE = 0;

    private int sequence = NO_SEQUENCE;

    /**
     * Constructor.
     *
     * @param name the consumer name
     */
    public ConsumerDefinition(String name) {
        this(name, null);
    }

    /**
     * Constructor.
     *
     * @param name  the consumer name
     * @param types the data types required by this consumer
     */
    public ConsumerDefinition(String name, List<DataType> types) {
        super(name, types);
    }

    /**
     * Returns the sequence number the consumer should receive messages from a channel.
     *
     * @return the sequence number
     */
    public int getSequence() {
        return sequence;
    }

    /**
     * Sets the sequence number the consumer should receive messages from a channel.
     *
     * @param sequence the sequence number
     */
    public void setSequence(int sequence) {
        this.sequence = sequence;
    }
}