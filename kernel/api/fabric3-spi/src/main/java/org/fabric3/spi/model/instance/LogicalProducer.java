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

import org.fabric3.api.host.Fabric3Exception;
import org.fabric3.api.model.type.component.Producer;

/**
 * A producer on an instantiated component in the domain.
 */
public class LogicalProducer extends LogicalInvocable {
    private static final long serialVersionUID = 5403855901902189810L;
    private Producer definition;
    private List<URI> targets;

    /**
     * Constructor.
     *
     * @param uri        the producer URI
     * @param definition the producer type definition
     * @param parent     the parent component
     */
    public LogicalProducer(URI uri, Producer definition, LogicalComponent<?> parent) {
        super(uri, definition != null ? definition.getServiceContract() : null, parent);
        this.definition = definition;
        targets = new ArrayList<>();
    }

    /**
     * Returns the producer type definition.
     *
     * @return the producer type definition
     */
    public Producer getDefinition() {
        return definition;
    }

    /**
     * Returns the configured target channel URIs.
     *
     * @return the configured target channel URIs
     */
    public List<URI> getTargets() {
        return targets;
    }

    /**
     * Adds a configured target channel URI.
     *
     * @param uri the target channel URI
     */
    public void addTarget(URI uri) {
        targets.add(uri);
    }

    /**
     * Adds a configured target channel URIs.
     *
     * @param targets the target channel URIs
     */
    public void addTargets(List<URI> targets) {
        this.targets.addAll(targets);
    }

    public LogicalOperation getStreamOperation() {
        if (operations.size() != 1) {
            throw new Fabric3Exception("Invalid number of operations on producer: " + getUri() + ". Producers that are not direct connections to channels may "
                                       + "only have one operation.");
        }
        return operations.get(0);
    }

    public boolean equals(Object obj) {

        if (this == obj) {
            return true;
        }

        if ((obj == null) || (obj.getClass() != this.getClass())) {
            return false;
        }

        LogicalProducer test = (LogicalProducer) obj;
        return getUri().equals(test.getUri());

    }

    public int hashCode() {
        return getUri().hashCode();
    }

}