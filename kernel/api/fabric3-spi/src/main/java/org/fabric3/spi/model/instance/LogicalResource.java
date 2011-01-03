/*
 * Fabric3
 * Copyright (c) 2009-2011 Metaform Systems
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

import org.fabric3.model.type.component.ResourceDefinition;

/**
 * An instantiated resource defined in a composite.
 *
 * @version $Rev$ $Date$
 */
public class LogicalResource<R extends ResourceDefinition> extends LogicalScaArtifact<LogicalCompositeComponent> {
    private static final long serialVersionUID = -8094856609591381761L;

    private R definition;
    private LogicalState state = LogicalState.NEW;
    private QName deployable;
    private String zone = "LocalZone";

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

    @Override
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

    @Override
    public int hashCode() {
        return definition.hashCode();
    }


}