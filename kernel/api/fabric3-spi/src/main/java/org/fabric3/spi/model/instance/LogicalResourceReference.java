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
 * Portions originally based on Apache Tuscany 2007
 * licensed under the Apache 2.0 license.
 */
package org.fabric3.spi.model.instance;

import java.net.URI;

import org.fabric3.api.model.type.component.ResourceReference;

/**
 * A reference to a resource on an instantiated component in the domain.
 */
public class LogicalResourceReference<RD extends ResourceReference> extends LogicalInvocable {
    private static final long serialVersionUID = -6298167441706672513L;

    private RD definition;
    private URI target;

    /**
     * Constructor.
     *
     * @param uri                URI of the resource.
     * @param definition the resource reference definition.
     * @param parent             the parent component
     */
    public LogicalResourceReference(URI uri, RD definition, LogicalComponent<?> parent) {
        super(uri, definition != null ? definition.getServiceContract() : null, parent);
        this.definition = definition;
    }

    /**
     * Gets the definition for this resource.
     *
     * @return Definition for this resource.
     */
    public final RD getDefinition() {
        return definition;
    }

    /**
     * Gets the target for the resource.
     *
     * @return Resource target.
     */
    public URI getTarget() {
        return target;
    }

    /**
     * Sets the target for the resource.
     *
     * @param target Resource target.
     */
    public void setTarget(URI target) {
        this.target = target;
    }

}
