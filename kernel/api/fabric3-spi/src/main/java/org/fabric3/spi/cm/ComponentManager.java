/*
 * Fabric3
 * Copyright (c) 2009 Metaform Systems
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
package org.fabric3.spi.cm;

import java.net.URI;
import java.util.List;

import org.fabric3.spi.component.Component;

/**
 * Responsible for tracking and managing the component tree for a runtime instance. The tree corresponds to components deployed to the current runtime
 * and hence may be sparse in comparison to the assembly component hierarchy for the SCA domain.
 *
 * @version $Rev$ $Date$
 */
public interface ComponentManager {

    /**
     * Registers a component which will be managed by the runtime
     *
     * @param component the component
     * @throws RegistrationException when an error ocurrs registering the component
     */
    void register(Component component) throws RegistrationException;

    /**
     * Deregisters a component
     *
     * @param component the component to deregister
     * @throws RegistrationException when an error ocurrs registering the component
     */
    void unregister(Component component) throws RegistrationException;

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
     * Returns a list of component URIs in the given hierarchy, e.g a domain or composite within a domain.
     *
     * @param uri a URI representing the hierarchy
     * @return the list of component URIs
     */
    List<URI> getComponentsInHierarchy(URI uri);
}
