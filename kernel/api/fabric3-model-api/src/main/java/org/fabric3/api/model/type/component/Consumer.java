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

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import org.fabric3.api.model.type.ModelObject;
import org.fabric3.api.model.type.contract.DataType;
import org.fabric3.api.model.type.contract.ServiceContract;

/**
 * A consumer introspected from a component type.
 */
public class Consumer<P extends ModelObject> extends Bindable<P> {
    public static final int NO_SEQUENCE = 0;
    private boolean direct;

    private String name;
    private int sequence = NO_SEQUENCE;

    private DataType type;
    private List<URI> sources = new ArrayList<>();
    private ServiceContract serviceContract;

    /**
     * Constructor.
     *
     * @param name the consumer name
     */
    public Consumer(String name) {
        this.name = name;
    }

    /**
     * Constructor.
     *
     * @param name  the consumer name
     * @param type the data type required by this consumer
     */
    public Consumer(String name, DataType type) {
        this.name = name;
        this.type = type;
    }

    /**
     * Constructor.
     *
     * @param name   the consumer name
     * @param type  the data type required by this consumer
     * @param direct true if the consumer is a direct connection to the channel
     */
    public Consumer(String name, DataType type, boolean direct) {
        this(name, type);
        this.direct = direct;
    }

    /**
     * Constructor.
     *
     * @param name     the consumer name
     * @param type     the data type required by this consumer
     * @param contract the service contract of this consumer if it is a direct connection to the channel
     */
    public Consumer(String name, DataType type, ServiceContract contract) {
        this(name, type);
        this.serviceContract = contract;
    }

    /**
     * Returns the consumer name.
     *
     * @return the reference name
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the data types required by this consumer.
     *
     * @return the data types required by this consumer
     */
    public DataType getType() {
        return type;
    }

    /**
     * Sets the data type required by this consumer.
     *
     * @param type the data type required by this consumer
     */
    public void setType(DataType type) {
        this.type = type;
    }

    /**
     * Returns the URIs of channels this consumer receives events from.
     *
     * @return the URIs of channels this consumer receives events from
     */
    public List<URI> getSources() {
        return sources;
    }

    /**
     * Sets the source channel uris.
     *
     * @param sources the source channel uris
     */
    public void setSources(List<URI> sources) {
        this.sources = sources;
    }

    /**
     * Adds a channel source for this consumer
     *
     * @param source the source URI
     */
    public void addSource(URI source) {
        sources.add(source);
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

    /**
     * Returns true if the consumer is a direct connection to the channel.
     *
     * @return true if the consumer is a direct connection to the channel
     */
    public boolean isDirect() {
        return direct;
    }

    public ServiceContract getServiceContract() {
        return serviceContract;
    }
}