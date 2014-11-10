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

import org.fabric3.api.model.type.component.AbstractService;

/**
 * An instantiated service in the domain.
 */
public class LogicalService extends Bindable {
    private static final long serialVersionUID = -2417797075030173948L;

    private AbstractService definition;
    private URI promote;
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
    public LogicalService(URI uri, AbstractService definition, LogicalComponent<?> parent) {
        super(uri, definition != null ? definition.getServiceContract() : null, parent);
        this.definition = definition;
        if (definition != null) {
            // null check for testing so full model does not need to be instantiated
            addIntents(definition.getIntents());
            addPolicySets(definition.getPolicySets());
        }
        leafComponent = parent;
        leafService = this;
    }

    /**
     * Returns the service definition for the logical service.
     *
     * @return the service definition for the logical service
     */
    public AbstractService getDefinition() {
        return definition;
    }

    /**
     * Returns the component service uri promoted by this service.
     *
     * @return the component service uri promoted by this service
     */
    public URI getPromotedUri() {
        return promote;
    }

    /**
     * Sets the component service uri promoted by this service
     *
     * @param uri the component service uri promoted by this service
     */
    public void setPromotedUri(URI uri) {
        this.promote = uri;
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

    /**
     * Sets the leaf component
     *
     * @param component the leaf component
     */
    public void setLeafComponent(LogicalComponent<?> component) {
        this.leafComponent = component;
    }

    public LogicalService getLeafService() {
        return leafService;
    }

    public void setLeafService(LogicalService leafService) {
        this.leafService = leafService;
    }
}
