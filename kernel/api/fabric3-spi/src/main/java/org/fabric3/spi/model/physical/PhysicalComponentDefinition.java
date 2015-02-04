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
package org.fabric3.spi.model.physical;

import javax.xml.namespace.QName;
import java.io.Serializable;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

/**
 * Used to provision a component on a runtime.
 */
public abstract class PhysicalComponentDefinition implements Serializable {
    private static final long serialVersionUID = -4354673356182365263L;

    private URI uri;
    private QName deployable;
    private URI contributionUri;
    private ClassLoader classLoader;
    private List<PhysicalPropertyDefinition> propertyDefinitions = new ArrayList<>();

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
     * Gets the contribution URI.
     *
     * @return contribution URI
     */
    public URI getContributionUri() {
        return contributionUri;
    }

    /**
     * Sets the contribution URI.
     *
     * @param contributionUri contribution URI.
     */
    public void setContributionUri(URI contributionUri) {
        this.contributionUri = contributionUri;
    }

    /**
     * Returns the implementation classloader.
     *
     * @return the implementation classloader
     */
    public ClassLoader getClassLoader() {
        return classLoader;
    }

    /**
     * Returns the implementation classloader.
     *
     * @param classLoader the implementation classloader
     */
    public void setClassLoader(ClassLoader classLoader) {
        this.classLoader = classLoader;
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
