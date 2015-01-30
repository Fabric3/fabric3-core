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

import org.fabric3.api.host.Names;
import org.fabric3.api.model.type.component.Resource;

/**
 * An instantiated resource defined in a composite.
 */
public class LogicalResource<R extends Resource> extends LogicalScaArtifact<LogicalCompositeComponent> {
    private static final long serialVersionUID = -8094856609591381761L;

    private R definition;
    private LogicalState state = LogicalState.NEW;
    private QName deployable;
    private String zone = Names.LOCAL_ZONE;

    /**
     * Constructor.
     *
     * @param definition the resource definition
     * @param parent     the parent component
     */
    public LogicalResource(R definition, LogicalCompositeComponent parent) {
        super(parent);
        this.definition = definition;
    }

    public R getDefinition() {
        return definition;
    }

    /**
     * Returns the instance state.
     *
     * @return the instance state
     */
    public LogicalState getState() {
        return state;
    }

    /**
     * Sets the instance state.
     *
     * @param state the instance state
     */
    public void setState(LogicalState state) {
        this.state = state;
    }

    /**
     * Returns the deployable composite name this logical resource was instantiated from.
     *
     * @return the deployable name
     */
    public QName getDeployable() {
        return deployable;
    }

    /**
     * Sets the name of the deployable composite this resource was instantiated from.
     *
     * @param deployable the deployable name
     */
    public void setDeployable(QName deployable) {
        this.deployable = deployable;
    }

    /**
     * Returns the zone name where the resource is provisioned.
     *
     * @return the zone name where the resource is provisioned
     */
    public String getZone() {
        return zone;
    }

    /**
     * Sets the zone name where the channel is provisioned.
     *
     * @param zone the zone name where the channel is provisioned
     */
    public void setZone(String zone) {
        this.zone = zone;
    }

    public boolean equals(Object obj) {

        if (this == obj) {
            return true;
        }

        if ((obj == null) || (obj.getClass() != this.getClass())) {
            return false;
        }

        LogicalResource<?> test = (LogicalResource) obj;
        return definition.equals(test.definition);

    }

    public int hashCode() {
        return definition.hashCode();
    }

}