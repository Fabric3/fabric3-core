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

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import org.fabric3.model.type.AbstractPolicyAware;

/**
 * A component configuration in a composite file.
 *
 * @version $Rev$ $Date$
 */
public class ComponentDefinition<I extends Implementation<?>> extends AbstractPolicyAware {
    private static final long serialVersionUID = 4909969579651563484L;

    private final String name;
    private Autowire autowire = Autowire.INHERITED;
    private I implementation;
    private final Map<String, ComponentService> services = new HashMap<String, ComponentService>();
    private final Map<String, ComponentReference> references = new HashMap<String, ComponentReference>();
    private final Map<String, PropertyValue> propertyValues = new HashMap<String, PropertyValue>();
    private String key;
    private URI contributionUri;

    /**
     * Constructor specifying the component's name.
     *
     * @param name the name of this component
     */
    public ComponentDefinition(String name) {
        this.name = name;
    }

    /**
     * Constructor specifying the component's name and implementation.
     *
     * @param name           the name of this component
     * @param implementation the implementation of this component
     */
    public ComponentDefinition(String name, I implementation) {
        this.name = name;
        this.implementation = implementation;
    }

    /**
     * Sets the {@link Implementation} of this component.
     *
     * @param implementation the implementation of this component
     */
    public void setImplementation(I implementation) {
        this.implementation = implementation;
    }

    /**
     * Returns the {@link Implementation} of this component.
     *
     * @return the implementation of this component
     */
    public I getImplementation() {
        return implementation;
    }

    /**
     * Returns the name of this component.
     *
     * @return the name of this component
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the autowire status for the component.
     *
     * @return the autowire status for the component.
     */
    public Autowire getAutowire() {
        return autowire;
    }

    /**
     * Sets the autowire status for the component.
     *
     * @param autowire the autowire status.
     */
    public void setAutowire(Autowire autowire) {
        this.autowire = autowire;
    }

    /**
     * Returns a live Map of the {@link ComponentReference targets} configured by this component definition.
     *
     * @return the reference targets configured by this component
     */
    public Map<String, ComponentReference> getReferences() {
        return references;
    }

    /**
     * Add a reference target configuration to this component. Any existing configuration for the reference named in the target is replaced.
     *
     * @param target the target to add
     */
    public void add(ComponentReference target) {
        references.put(target.getName(), target);
    }

    /**
     * Returns a live Map of the {@link ComponentService}s configured by this component definition.
     *
     * @return the services configured by this component
     */
    public Map<String, ComponentService> getServices() {
        return services;
    }

    /**
     * Add a service configuration to this component. Any existing configuration for the service is replaced.
     *
     * @param service the service to add
     */
    public void add(ComponentService service) {
        services.put(service.getName(), service);
    }

    /**
     * Returns a live Map of {@link PropertyValue property values} configured by this component definition.
     *
     * @return the property values configured by this component
     */
    public Map<String, PropertyValue> getPropertyValues() {
        return propertyValues;
    }

    /**
     * Add a property value configuration to this component. Any existing configuration for the property names in the property value is replaced.
     *
     * @param value the property value to add
     */
    public void add(PropertyValue value) {
        propertyValues.put(value.getName(), value);
    }

    /**
     * Returns the key to be used if this component is wired to a map of references.
     *
     * @return The value of the key.
     */
    public String getKey() {
        return key;
    }

    /**
     * Sets the key to be used if this component is wired to a map of references.
     *
     * @param key The value of the key.
     */
    public void setKey(String key) {
        this.key = key;
    }

    /**
     * Gets the component type.
     *
     * @return Component type.
     */
    public AbstractComponentType getComponentType() {
        return getImplementation().getComponentType();
    }

    /**
     * Returns the URI of the contribution the component definition is contained in.
     *
     * @return the URI of the contribution the component definition is contained in.
     */
    public URI getContributionUri() {
        return contributionUri;
    }

    /**
     * Sets the URI of the contribution the component definition is contained in.
     *
     * @param contributionUri the URI of the contribution the component definition is contained in.
     */
    public void setContributionUri(URI contributionUri) {
        this.contributionUri = contributionUri;
    }

}
