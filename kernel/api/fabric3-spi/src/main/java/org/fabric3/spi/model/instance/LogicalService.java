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

import org.fabric3.api.model.type.component.ComponentType;
import org.fabric3.api.model.type.component.Service;

/**
 * An instantiated service.
 */
public class LogicalService extends LogicalBindable {
    private static final long serialVersionUID = -2417797075030173948L;

    private Service<ComponentType> definition;
    private LogicalComponent<?> leafComponent;
    private LogicalService leafService;

    /**
     * Default constructor
     *
     * @param uri        the service uri
     * @param definition the service definition
     * @param parent     the service parent component
     */
    @SuppressWarnings("unchecked")
    public LogicalService(URI uri, Service<ComponentType> definition, LogicalComponent<?> parent) {
        super(uri, definition != null ? definition.getServiceContract() : null, parent);
        this.definition = definition;
        leafComponent = parent;
        leafService = this;
    }

    /**
     * Returns the service definition for the logical service.
     *
     * @return the service definition for the logical service
     */
    public Service<ComponentType> getDefinition() {
        return definition;
    }

    /**
     * Returns the leaf component this service is promoted from. The leaf component is determined by descending down the service promotion hierarchy
     * to the original service provided by a component.
     *
     * @return the leaf component
     */
    public LogicalComponent<?> getLeafComponent() {
        return leafComponent;
    }

    public LogicalService getLeafService() {
        return leafService;
    }

}
