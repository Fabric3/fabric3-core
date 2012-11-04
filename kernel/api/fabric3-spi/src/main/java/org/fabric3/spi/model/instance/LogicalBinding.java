/*
 * Fabric3
 * Copyright (c) 2009-2012 Metaform Systems
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

import javax.xml.namespace.QName;

import org.fabric3.model.type.component.BindingDefinition;

/**
 * An instantiated binding.
 */
public class LogicalBinding<BD extends BindingDefinition> extends LogicalScaArtifact<Bindable> {
    private static final long serialVersionUID = 8153501808553226042L;

    private BD definition;
    private LogicalState state = LogicalState.NEW;
    private QName deployable;
    private boolean assigned;
    private boolean callback;

    public LogicalBinding(BD definition, Bindable parent) {
        super(parent);
        this.definition = definition;
        if (definition != null) {
            // null check for testing so full model does not need to be instantiated
            addIntents(definition.getIntents());
            addPolicySets(definition.getPolicySets());
        }
    }

    public LogicalBinding(BD definition, Bindable parent, QName deployable) {
        super(parent);
        this.definition = definition;
        this.deployable = deployable;
        if (definition != null) {
            // null check for testing so full model does not need to be instantiated
            addIntents(definition.getIntents());
            addPolicySets(definition.getPolicySets());
        }
    }

    /**
     * Returns the binding definition.
     *
     * @return the binding definition
     */
    public BD getDefinition() {
        return definition;
    }

    /**
     * Returns the binding state.
     *
     * @return the binding state
     */
    public LogicalState getState() {
        return state;
    }

    /**
     * Sets the binding state.
     *
     * @param state the binding state
     */
    public void setState(LogicalState state) {
        this.state = state;
    }

    /**
     * If this is a service binding, returns the deployable the binding was provisioned with if it was dynamically added to connect a source reference
     * to a target service. Bindings are dynamically added in two instances: to provide a physical transport for binding.sca; and when a reference
     * specifies a binding and the service it is wired to is not configured with a binding of that type.
     *
     * @return the deployable that dynamically provisioned the binding or null of the binding was not dynamically provisioned
     */
    public QName getDeployable() {
        return deployable;
    }

    /**
     * Returns true if the binding was assigned - e.g. binding.sca - by the controller as opposed to being explicitly declared in a composite.
     *
     * @return true f the binding is assigned
     */
    public boolean isAssigned() {
        return assigned;
    }

    /**
     * Sets if the binding is assigned.
     *
     * @param assigned true if the binding is assigned
     */
    public void setAssigned(boolean assigned) {
        this.assigned = assigned;
    }

    /**
     * True if this binding is a callback.
     *
     * @return true if this binding is a callback
     */
    public boolean isCallback() {
        return callback;
    }

    /**
     * Sets if this binding is a callback
     *
     * @param callback true if this binding is a callback
     */
    public void setCallback(boolean callback) {
        this.callback = callback;
    }

}
