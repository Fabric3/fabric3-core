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

import javax.xml.namespace.QName;

import org.fabric3.api.model.type.component.BindingDefinition;

/**
 * An instantiated binding.
 */
public class LogicalBinding<BD extends BindingDefinition> extends LogicalScaArtifact<LogicalBindable> {
    private static final long serialVersionUID = 8153501808553226042L;

    private BD definition;
    private LogicalState state = LogicalState.NEW;
    private QName deployable;
    private boolean assigned;
    private boolean callback;

    public LogicalBinding(BD definition, LogicalBindable parent) {
        super(parent);
        this.definition = definition;
    }

    public LogicalBinding(BD definition, LogicalBindable parent, QName deployable) {
        super(parent);
        this.definition = definition;
        this.deployable = deployable;
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
