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
package org.fabric3.spi.model.physical;

import java.io.Serializable;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import javax.xml.namespace.QName;

/**
 * Used to provision a component on a runtime.
 */
public abstract class PhysicalComponentDefinition implements Serializable {
    private static final long serialVersionUID = -4354673356182365263L;

    private URI uri;
    private QName deployable;
    private URI classLoaderId;
    private List<PhysicalPropertyDefinition> propertyDefinitions = new ArrayList<PhysicalPropertyDefinition>();

    /**
     * Gets the component URI.
     *
     * @return Component id.
     */
    public URI getComponentUri() {
        return uri;
    }

    /**
     * Sets the component URI.
     *
     * @param uri the component id
     */
    public void setComponentUri(URI uri) {
        this.uri = uri;
    }

    /**
     * Returns the QName of the deployable composite this component is deployed as part of.
     *
     * @return the QName of the deployable composite this component is deployed as part of
     */
    public QName getDeployable() {
        return deployable;
    }

    /**
     * Sets the QName of the deployable composite this component is deployed as part of.
     *
     * @param deployable the QName of the deployable composite this component is deployed as part of
     */
    public void setDeployable(QName deployable) {
        this.deployable = deployable;
    }

    /**
     * Gets the classloader id.
     *
     * @return Classloader id.
     */
    public URI getClassLoaderId() {
        return classLoaderId;
    }

    /**
     * Set the classloader id.
     *
     * @param classLoaderId Classloader id.
     */
    public void setClassLoaderId(URI classLoaderId) {
        this.classLoaderId = classLoaderId;
    }

    /**
     * Sets the property definition.
     *
     * @param definition the property definition
     */
    public void setPropertyDefinition(PhysicalPropertyDefinition definition) {
        propertyDefinitions.add(definition);
    }

    /**
     * Returns the property definitions for the component.
     *
     * @return the property definitions for the component
     */
    public List<PhysicalPropertyDefinition> getPropertyDefinitions() {
        return propertyDefinitions;
    }

    @Override
    public boolean equals(Object obj) {

        if (obj == null || obj.getClass() != getClass()) {
            return false;
        }

        PhysicalComponentDefinition other = (PhysicalComponentDefinition) obj;
        return uri.equals(other.getComponentUri());

    }

    @Override
    public int hashCode() {
        return uri.hashCode();
    }

    @Override
    public String toString() {
        return uri.toString();
    }
}
