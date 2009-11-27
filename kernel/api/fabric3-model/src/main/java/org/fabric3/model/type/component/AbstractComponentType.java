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
 * Base component type representation.
 *
 * @version $Rev$ $Date$
 */
public abstract class AbstractComponentType<S extends ServiceDefinition,
        R extends ReferenceDefinition,
        P extends Property,
        RD extends ResourceDefinition>
        extends ModelObject implements CapabilityAware {
    private static final long serialVersionUID = 5302580019263119837L;
    private String scope;
    private int initLevel;
    private long maxAge;
    private long maxIdleTime;
    private final Map<String, S> services = new HashMap<String, S>();
    private final Map<String, R> references = new HashMap<String, R>();
    private final Map<String, P> properties = new HashMap<String, P>();
    private final Map<String, RD> resources = new HashMap<String, RD>();
    private final Set<String> requiredCapabilities = new HashSet<String>();

    protected AbstractComponentType() {
    }

    /**
     * Returns the lifecycle scope for the component.
     *
     * @return the lifecycle scope for the component
     */
    public String getScope() {
        return scope;
    }

    /**
     * Sets the lifecycle scope for the component.
     *
     * @param scope the lifecycle scope for the component
     */
    public void setScope(String scope) {
        this.scope = scope;
    }

    /**
     * Returns the default initialization level for components of this type. A value greater than zero indicates that components should be eagerly
     * initialized.
     *
     * @return the default initialization level
     */
    public int getInitLevel() {
        return initLevel;
    }

    /**
     * Sets the default initialization level for components of this type. A value greater than zero indicates that components should be eagerly
     * initialized.
     *
     * @param initLevel default initialization level for components of this type
     */
    public void setInitLevel(int initLevel) {
        this.initLevel = initLevel;
    }

    /**
     * Returns true if this component should be eagerly initialized.
     *
     * @return true if this component should be eagerly initialized
     */
    public boolean isEagerInit() {
        return initLevel > 0;
    }

    /**
     * Returns the idle time allowed between operations in milliseconds if the implementation is conversational
     *
     * @return the idle time allowed between operations in milliseconds if the implementation is conversational
     */
    public long getMaxIdleTime() {
        return maxIdleTime;
    }

    /**
     * Sets the idle time allowed between operations in milliseconds if the implementation is conversational.
     *
     * @param maxIdleTime the idle time allowed between operations in milliseconds if the implementation is conversational
     */
    public void setMaxIdleTime(long maxIdleTime) {
        this.maxIdleTime = maxIdleTime;
    }

    /**
     * Returns the maximum age a conversation may remain active in milliseconds if the implementation is conversational
     *
     * @return the maximum age a conversation may remain active in milliseconds if the implementation is conversational
     */
    public long getMaxAge() {
        return maxAge;
    }

    /**
     * Sets the maximum age a conversation may remain active in milliseconds if the implementation is conversational.
     *
     * @param maxAge the maximum age a conversation may remain active in milliseconds if the implementation is conversational
     */
    public void setMaxAge(long maxAge) {
        this.maxAge = maxAge;
    }

    /**
     * Returns a live Map of the services provided by the implementation.
     *
     * @return a live Map of the services provided by the implementation
     */
    public Map<String, S> getServices() {
        return services;
    }

    /**
     * Add a service to those provided by the implementation. Any existing service with the same name is replaced.
     *
     * @param service a service provided by the implementation
     */
    public void add(S service) {
        services.put(service.getName(), service);
    }

    /**
     * Checks if this component type has a service with a certain name.
     *
     * @param name the name of the service to check
     * @return true if there is a service defined with that name
     */
    public boolean hasService(String name) {
        return services.containsKey(name);
    }

    /**
     * Returns a live Map of references to services consumed by the implementation.
     *
     * @return a live Map of references to services consumed by the implementation
     */
    public Map<String, R> getReferences() {
        return references;
    }

    /**
     * Add a reference to a service consumed by the implementation. Any existing reference with the same name is replaced.
     *
     * @param reference a reference to a service consumed by the implementation
     */
    public void add(R reference) {
        references.put(reference.getName(), reference);
    }

    /**
     * Checks if this component type has a reference with a certain name.
     *
     * @param name the name of the reference to check
     * @return true if there is a reference defined with that name
     */
    public boolean hasReference(String name) {
        return references.containsKey(name);
    }

    /**
     * Returns a live Map of properties that can be used to configure the implementation.
     *
     * @return a live Map of properties that can be used to configure the implementation
     */
    public Map<String, P> getProperties() {
        return properties;
    }

    /**
     * Add a property that can be used to configure the implementation. Any existing property with the same name is replaced.
     *
     * @param property a property that can be used to configure the implementation
     */
    public void add(P property) {
        properties.put(property.getName(), property);
    }

    /**
     * Checks if this component type has a property with a certain name.
     *
     * @param name the name of the property to check
     * @return true if there is a property defined with that name
     */
    public boolean hasProperty(String name) {
        return properties.containsKey(name);
    }

    /**
     * Returns a live Map of resoures that can be used to configure the implementation.
     *
     * @return a live Map of resources that can be used to configure the implementation
     */
    public Map<String, RD> getResources() {
        return resources;
    }

    /**
     * Add a resource that can be used to configure the implementation. Any existing resource with the same name is replaced.
     *
     * @param resource a resource that can be used to configure the implementation
     */
    public void add(RD resource) {
        resources.put(resource.getName(), resource);
    }

    /**
     * Checks if this component type has a resource with a certain name.
     *
     * @param name the name of the resource to check
     * @return true if there is a resource defined with that name
     */
    public boolean hasResource(String name) {
        return resources.containsKey(name);
    }

    public Set<String> getRequiredCapabilities() {
        return requiredCapabilities;
    }

    public void addRequiredCapability(String capability) {
        requiredCapabilities.add(capability);
    }
}
