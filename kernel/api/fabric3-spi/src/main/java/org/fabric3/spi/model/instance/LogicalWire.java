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

/**
 * A wire from a reference to a service in the domain. A wire always targets a service in the domain (as opposed to a service hosted externally) and hence is
 * expressed using the SCA URI of the target service. A wire is expressed by using the <code>target</code> attribute of a <code>reference</code> element or
 * using the <code>wire</code> element. Furthermore, a wire may be unbound or explicitly configured with a binding. If the wire is unbound and crosses process
 * boundaries, it will be bound by the runtime using the SCA binding. <p/> During deployment, wires are created and resolved incrementally. A wire is created
 * for When a wire is instantiated, its source reference and target service URI are resolved against the domain.
 */
public class LogicalWire extends LogicalScaArtifact<LogicalComponent<?>> {
    private static final long serialVersionUID = -643283191171197255L;

    private LogicalReference source;
    private LogicalService target;
    private LogicalBinding sourceBinding;
    private LogicalBinding targetBinding;

    private QName deployable;
    private LogicalState state = LogicalState.NEW;

    /**
     * Instantiates a logical wire.
     *
     * @param parent     component within which the wire is defined.
     * @param source     the source reference of the wire
     * @param target     the target service
     * @param deployable the target service deployable
     */
    public LogicalWire(LogicalComponent<?> parent, LogicalReference source, LogicalService target, QName deployable) {
        super(parent);
        this.source = source;
        this.target = target;
        this.deployable = deployable;
    }

    /**
     * Gets the source of the wire.
     *
     * @return source of the wire.
     */
    public LogicalReference getSource() {
        return source;
    }

    /**
     * Gets the target service of the wire.
     *
     * @return target service of the wire.
     */
    public LogicalService getTarget() {
        return target;
    }

    /**
     * Returns the wire state.
     *
     * @return the wire state
     */
    public LogicalState getState() {
        return state;
    }

    /**
     * Sets the wire state.
     *
     * @param state the wire state
     */
    public void setState(LogicalState state) {
        this.state = state;
    }

    /**
     * Returns the deployable of the target for this wire. A source of a wire may be deployed via a different deployable thant its target. This value is used to
     * track the target deployable so the wire may be undeployed along wih the target even if the source is not.
     *
     * @return the deployable that provisioned the wire.
     */
    public QName getTargetDeployable() {
        return deployable;
    }

    public LogicalBinding getSourceBinding() {
        return sourceBinding;
    }

    public void setSourceBinding(LogicalBinding sourceBinding) {
        this.sourceBinding = sourceBinding;
    }

    public LogicalBinding getTargetBinding() {
        return targetBinding;
    }

    public void setTargetBinding(LogicalBinding targetBinding) {
        this.targetBinding = targetBinding;
    }

    public QName getDeployable() {
        return deployable;
    }

    public void setDeployable(QName deployable) {
        this.deployable = deployable;
    }

    /**
     * Tests for quality whether the source and target URIs are the same.
     *
     * @param obj Object to be tested against.
     */
    @Override
    public boolean equals(Object obj) {

        if (this == obj) {
            return true;
        }

        if ((obj == null) || (obj.getClass() != this.getClass())) {
            return false;
        }

        LogicalWire test = (LogicalWire) obj;
        return target.equals(test.target) && source.equals(test.source);

    }

    /**
     * Hash code based on the source and target URIs.
     *
     * @return Hash code based on the source and target URIs.
     */
    public int hashCode() {

        int hash = 7;
        hash = 31 * hash + source.hashCode();
        hash = 31 * hash + target.hashCode();
        return hash;

    }

}
