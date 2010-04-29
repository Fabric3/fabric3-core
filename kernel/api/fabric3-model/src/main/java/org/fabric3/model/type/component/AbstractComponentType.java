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
package org.fabric3.model.type.component;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.fabric3.model.type.CapabilityAware;
import org.fabric3.model.type.ModelObject;

/**
 * A base component type.
 *
 * @version $Rev$ $Date$
 */
public class AbstractComponentType extends ModelObject implements CapabilityAware {
    private static final long serialVersionUID = 5302580019263119837L;

    private Map<String, ServiceDefinition> services = new HashMap<String, ServiceDefinition>();
    private Map<String, ReferenceDefinition> references = new HashMap<String, ReferenceDefinition>();
    private Map<String, Property> properties = new HashMap<String, Property>();
    private Map<String, ResourceDefinition> resources = new HashMap<String, ResourceDefinition>();
    private Set<String> requiredCapabilities = new HashSet<String>();

    public AbstractComponentType() {
    }

    /**
     * Returns a live Map of the services provided by the implementation.
     *
     * @return a live Map of the services provided by the implementation
     */
    public Map<String, ServiceDefinition> getServices() {
        return services;
    }

    /**
     * Add a service to those provided by the implementation. Any existing service with the same name is replaced.
     *
     * @param service a service provided by the implementation
     */
    public void add(ServiceDefinition service) {
        services.put(service.getName(), service);
    }

    /**
     * Returns a live Map of references to services consumed by the implementation.
     *
     * @return a live Map of references to services consumed by the implementation
     */
    public Map<String, ReferenceDefinition> getReferences() {
        return references;
    }

    /**
     * Add a reference to a service consumed by the implementation. Any existing reference with the same name is replaced.
     *
     * @param reference a reference to a service consumed by the implementation
     */
    public void add(ReferenceDefinition reference) {
        references.put(reference.getName(), reference);
    }

    /**
     * Returns a live Map of properties that can be used to configure the implementation.
     *
     * @return a live Map of properties that can be used to configure the implementation
     */
    public Map<String, Property> getProperties() {
        return properties;
    }

    /**
     * Add a property that can be used to configure the implementation. Any existing property with the same name is replaced.
     *
     * @param property a property that can be used to configure the implementation
     */
    public void add(Property property) {
        properties.put(property.getName(), property);
    }

    /**
     * Returns a live Map of resoures that can be used to configure the implementation.
     *
     * @return a live Map of resources that can be used to configure the implementation
     */
    public Map<String, ResourceDefinition> getResources() {
        return resources;
    }

    /**
     * Add a resource that can be used to configure the implementation. Any existing resource with the same name is replaced.
     *
     * @param resource a resource that can be used to configure the implementation
     */
    public void add(ResourceDefinition resource) {
        resources.put(resource.getName(), resource);
    }

    /**
     * Returns the capabilities required by this component implementation.
     *
     * @return the capabilities required by this component implementation
     */
    public Set<String> getRequiredCapabilities() {
        return requiredCapabilities;
    }

    /**
     * Adds a capability required by this implementation.
     *
     * @param capability the capability
     */
    public void addRequiredCapability(String capability) {
        requiredCapabilities.add(capability);
    }
}
