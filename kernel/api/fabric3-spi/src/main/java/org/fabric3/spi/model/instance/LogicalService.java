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
 *
 * ----------------------------------------------------
 *
 * Portions originally based on Apache Tuscany 2007
 * licensed under the Apache 2.0 license.
 *
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
