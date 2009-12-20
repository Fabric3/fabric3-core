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
package org.fabric3.spi.model.instance;

import java.net.URI;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import javax.xml.namespace.QName;

import org.w3c.dom.Document;

import org.fabric3.model.type.component.Autowire;
import org.fabric3.model.type.component.ComponentDefinition;
import org.fabric3.model.type.component.Implementation;

/**
 * Represents an instantiated component in the domain.
 *
 * @version $Rev$ $Date$
 */
public class LogicalComponent<I extends Implementation<?>> extends LogicalScaArtifact<LogicalCompositeComponent> {
    private static final long serialVersionUID = -3520150701040845117L;

    private URI uri;
    private final ComponentDefinition<I> definition;
    private final Map<String, Document> propertyValues = new HashMap<String, Document>();
    private final Map<String, LogicalService> services = new HashMap<String, LogicalService>();
    private final Map<String, LogicalReference> references = new HashMap<String, LogicalReference>();
    private final Map<String, LogicalResource<?>> resources = new HashMap<String, LogicalResource<?>>();
    private String zone;
    private QName deployable;
    private Autowire autowire;
    private LogicalState state = LogicalState.NEW;

    /**
     * @param uri        URI of the component.
     * @param definition Definition of the component.
     * @param parent     Parent of the component.
     */
    public LogicalComponent(URI uri, ComponentDefinition<I> definition, LogicalCompositeComponent parent) {
        super(parent);
        this.uri = uri;
        this.definition = definition;
        if (definition != null) {
            // null check for testing so full model does not need to be instantiated
            addIntents(definition.getIntents());
            addPolicySets(definition.getPolicySets());
        }
    }

    /**
     * Returns the component uri.
     *
     * @return the uri
     */
    public URI getUri() {
        return uri;
    }

    /**
     * Returns the zone name where the component is provisioned.
     *
     * @return the zone name where the component is provisioned
     */
    public String getZone() {
        return zone;
    }

    /**
     * Sets the zone name where the component is provisioned.
     *
     * @param zone the zone name where the component is provisioned
     */
    public void setZone(String zone) {
        this.zone = zone;
    }

    /**
     * Returns the deployable composite name this logical component was instantiated from.
     *
     * @return the deployable name
     */
    public QName getDeployable() {
        return deployable;
    }

    /**
     * Sets the name of the deployable composite this component was instantiated from.
     *
     * @param deployable the deployable name
     */
    public void setDeployable(QName deployable) {
        this.deployable = deployable;
    }

    /**
     * Returns the overriden autowire value or null if not overriden
     *
     * @return the overriden autowire value or null if not overriden
     */
    public Autowire getAutowireOverride() {
        return autowire;
    }

    /**
     * Sets the overriden autowire value
     *
     * @param autowire the autowire value
     */
    public void setAutowireOverride(Autowire autowire) {
        this.autowire = autowire;
    }

    /**
     * Returns the services offered by the current component.
     *
     * @return the services offered by the current component
     */
    public Collection<LogicalService> getServices() {
        return services.values();
    }

    /**
     * Returns a service with the given URI.
     *
     * @param name the service name
     * @return the service.
     */
    public LogicalService getService(String name) {
        return services.get(name);
    }

    /**
     * Adds a the resolved service
     *
     * @param service the service to add
     */
    public void addService(LogicalService service) {
        services.put(service.getUri().getFragment(), service);
    }

    /**
     * Returns the resources required by the current component.
     *
     * @return the resources required by the current component
     */
    public Collection<LogicalResource<?>> getResources() {
        return resources.values();
    }

    /**
     * Returns a resource with the given URI.
     *
     * @param name the resource name
     * @return the resource.
     */
    public LogicalResource<?> getResource(String name) {
        return resources.get(name);
    }

    /**
     * Adds a the resolved resource
     *
     * @param resource the resource to add
     */
    public void addResource(LogicalResource<?> resource) {
        resources.put(resource.getUri().getFragment(), resource);
    }

    /**
     * Returns the resolved component references.
     *
     * @return the component references
     */
    public Collection<LogicalReference> getReferences() {
        return references.values();
    }

    /**
     * Returns a the resolved reference with the given URI.
     *
     * @param name the reference name
     * @return the reference.
     */
    public LogicalReference getReference(String name) {
        return references.get(name);
    }

    /**
     * Adds a resolved reference
     *
     * @param reference the reference to add
     */
    public void addReference(LogicalReference reference) {
        references.put(reference.getUri().getFragment(), reference);
    }

    /**
     * Returns the resolved property values for the component.
     *
     * @return the resolved property values for the component
     */
    public Map<String, Document> getPropertyValues() {
        return propertyValues;
    }

    /**
     * Gets the value of a property.
     *
     * @param name Name of the property.
     * @return Property value for the specified property.
     */
    public Document getPropertyValue(String name) {
        return propertyValues.get(name);
    }

    /**
     * Sets a resolved property value
     *
     * @param name  the property name
     * @param value the property value
     */
    public void setPropertyValue(String name, Document value) {
        propertyValues.put(name, value);
    }

    /**
     * Returns the component implementation type.
     *
     * @return the component implementation type
     */
    public ComponentDefinition<I> getDefinition() {
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

}
