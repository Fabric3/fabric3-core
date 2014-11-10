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

/**
 * An abstract consumer type.
 */
public abstract class AbstractConsumer<P extends ModelObject> extends BindableDefinition<P> {
    private static final long serialVersionUID = -2867241238552796639L;

    private String name;
    private List<DataType> types;

    protected List<URI> sources = new ArrayList<>();

    /**
     * Constructor.
     *
     * @param name the consumer name
     */
    public AbstractConsumer(String name) {
        this.name = name;
    }

    /**
     * Constructor.
     *
     * @param name  the consumer name
     * @param types the data types
     */
    public AbstractConsumer(String name, List<DataType> types) {
        this.name = name;
        this.types = types;
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
    public List<DataType> getTypes() {
        return types;
    }

    /**
     * Sets the data types required by this consumer.
     *
     * @param types the data types required by this consumer
     */
    public void setTypes(List<DataType> types) {
        this.types = types;
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

}