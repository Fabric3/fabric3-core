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
package org.fabric3.spi.container.component;

import javax.xml.namespace.QName;
import java.net.URI;
import java.util.List;

import org.fabric3.api.host.ContainerException;

/**
 * Responsible for tracking and managing the component tree for a runtime instance. The tree corresponds to components deployed to the current runtime
 * and hence may be sparse in comparison to the assembly component hierarchy for the SCA domain.
 */
public interface ComponentManager {

    /**
     * Registers a component which will be managed by the runtime
     *
     * @param component the component
     * @throws ContainerException when an error occurs registering the component
     */
    void register(Component component) throws ContainerException;

    /**
     * Un-registers a component
     *
     * @param uri the component URI to un-register
     * @throws ContainerException when an error occurs registering the component
     * @return the the component
     */
    Component unregister(URI uri) throws ContainerException;

    /**
     * Returns the component with the given URI
     *
     * @param uri the component URI
     * @return the component or null if not found
     */
    Component getComponent(URI uri);

    /**
     * Returns a list of all registered components.
     *
     * @return a list of all registered components
     */
    List<Component> getComponents();

    /**
     * Returns a list of components in the given structural URI.
     *
     * @param uri a URI representing the hierarchy
     * @return the components
     */
    List<Component> getComponentsInHierarchy(URI uri);

    /**
     * Returns a list of components provisioned by the given deployable composite. The list is transitive and includes components in contained in
     * child composites.
     *
     * @param deployable the composite
     * @return the components.
     */
    List<Component> getDeployedComponents(QName deployable);
}
