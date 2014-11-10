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
package org.fabric3.spi.model.instance;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import org.fabric3.api.model.type.component.ConsumerDefinition;

/**
 * A consumer on an instantiated component in the domain.
 */
public class LogicalConsumer extends LogicalInvocable {
    private static final long serialVersionUID = -8094856609591381761L;
    private ConsumerDefinition definition;
    private List<URI> sources;

    /**
     * Constructor.
     *
     * @param uri        the consumer URI
     * @param definition the consumer type definition
     * @param parent     the parent component
     */
    public LogicalConsumer(URI uri, ConsumerDefinition definition, LogicalComponent<?> parent) {
        super(uri, null, parent);
        this.definition = definition;
        sources = new ArrayList<>();
        if (definition != null) {
            // null check for testing so full model does not need to be instantiated
            addIntents(definition.getIntents());
            addPolicySets(definition.getPolicySets());
        }
    }

    /**
     * Returns the producer type definition.
     *
     * @return the producer type definition
     */
    public ConsumerDefinition getDefinition() {
        return definition;
    }

    /**
     * Returns the configured source channel URIs.
     *
     * @return the configured source channel URIs
     */
    public List<URI> getSources() {
        return sources;
    }

    /**
     * Adds a configured source channel URIs.
     *
     * @param sources the source channel URIs
     */
    public void addSources(List<URI> sources) {
        this.sources.addAll(sources);
    }

    /**
     * Adds a configured source channel URI.
     *
     * @param uri the source channel URI
     */
    public void addSource(URI uri) {
        sources.add(uri);
    }

    @Override
    public boolean equals(Object obj) {

        if (this == obj) {
            return true;
        }

        if ((obj == null) || (obj.getClass() != this.getClass())) {
            return false;
        }

        LogicalConsumer test = (LogicalConsumer) obj;
        return getUri().equals(test.getUri());

    }

    @Override
    public int hashCode() {
        return getUri().hashCode();
    }


}