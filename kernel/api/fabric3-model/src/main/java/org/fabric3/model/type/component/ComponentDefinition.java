/*
 * Fabric3
 * Copyright (c) 2009-2012 Metaform Systems
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
 */
public class ComponentDefinition<I extends Implementation<?>> extends AbstractPolicyAware {
    private static final long serialVersionUID = 4909969579651563484L;

    private final String name;
    private Autowire autowire = Autowire.INHERITED;
    private I implementation;
    private Map<String, ComponentService> services = new HashMap<String, ComponentService>();
    private Map<String, ComponentReference> references = new HashMap<String, ComponentReference>();
    private Map<String, ComponentProducer> producers = new HashMap<String, ComponentProducer>();
    private Map<String, ComponentConsumer> consumers = new HashMap<String, ComponentConsumer>();
    private Map<String, PropertyValue> propertyValues = new HashMap<String, PropertyValue>();
    private String key;
    private URI contributionUri;

    /**
     * Constructor.
     *
     * @param name the component name
     */
    public ComponentDefinition(String name) {
        this.name = name;
    }

    /**
     * Constructor.
     *
     * @param name           the component name
     * @param implementation the component implementation
     */
    public ComponentDefinition(String name, I implementation) {
        this.name = name;
        this.implementation = implementation;
    }

    /**
     * Sets the component implementation.
     *
     * @param implementation the component implementation
     */
    public void setImplementation(I implementation) {
        if (roundTrip) {
            pushElement(implementation);
        }
        this.implementation = implementation;
    }

    /**
     * Returns the component implementation.
     *
     * @return the implementation of this component
     */
    public I getImplementation() {
        return implementation;
    }

    /**
     * Gets the component type.
     *
     * @return Component type.
     */
    public ComponentType getComponentType() {
        return getImplementation().getComponentType();
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
     * @return the autowire status for the component
     */
    public Autowire getAutowire() {
        return autowire;
    }

    /**
     * Sets the autowire status for the component.
     *
     * @param autowire the autowire status
     */
    public void setAutowire(Autowire autowire) {
        this.autowire = autowire;
    }

    /**
     * Returns the references configured by this component.
     *
     * @return the component references
     */
    public Map<String, ComponentReference> getReferences() {
        return references;
    }

    /**
     * Add a reference to this component.
     *
     * @param reference the reference to add
     */
    public void add(ComponentReference reference) {
        pushElement(reference);
        references.put(reference.getName(), reference);
    }

    /**
     * Removes a reference.
     *
     * @param reference the reference to remove
     */
    public void remove(ComponentReference reference) {
        removeElement(reference);
        references.remove(reference.getName());
    }

    /**
     * Returns the services configured by this component definition.
     *
     * @return the services configured by this component
     */
    public Map<String, ComponentService> getServices() {
        return services;
    }

    /**
     * Adds a service to this component.
     *
     * @param service the service to add
     */
    public void add(ComponentService service) {
        pushElement(service);
        services.put(service.getName(), service);
    }

    /**
     * Removes a service.
     *
     * @param service the service to remove
     */
    public void remove(ComponentService service) {
        removeElement(service);
        services.remove(service.getName());
    }


    /**
     * Adds a producer to this component.
     *
     * @param producer the producer to add
     */
    public void add(ComponentProducer producer) {
        pushElement(producer);
        producers.put(producer.getName(), producer);
    }

    /**
     * Removes a producer.
     *
     * @param producer the producer to remove
     */
    public void remove(ComponentProducer producer) {
        removeElement(producer);
        producers.remove(producer.getName());
    }


    /**
     * Returns the producers configured by this component definition.
     *
     * @return the producers configured by this component
     */
    public Map<String, ComponentProducer> getProducers() {
        return producers;
    }

    /**
     * Adds a consumer to this component.
     *
     * @param consumer the consumer to add
     */
    public void add(ComponentConsumer consumer) {
        pushElement(consumer);
        consumers.put(consumer.getName(), consumer);
    }

    /**
     * Removes a consumer.
     *
     * @param consumer the consumer to remove
     */
    public void remove(ComponentConsumer consumer) {
        removeElement(consumer);
        consumers.remove(consumer.getName());
    }


    /**
     * Returns the consumers configured by this component definition.
     *
     * @return the consumers configured by this component
     */
    public Map<String, ComponentConsumer> getConsumers() {
        return consumers;
    }

    /**
     * Returns the property values configured by this component definition.
     *
     * @return the configured property values
     */
    public Map<String, PropertyValue> getPropertyValues() {
        return propertyValues;
    }

    /**
     * Adds a configured property value.
     *
     * @param value the property value to add
     */
    public void add(PropertyValue value) {
        pushElement(value);
        propertyValues.put(value.getName(), value);
    }

    /**
     * Removes a property value.
     *
     * @param value the value to remove
     */
    public void remove(PropertyValue value) {
        removeElement(value);
        propertyValues.remove(value.getName());
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
     * @param key The value of the key
     */
    public void setKey(String key) {
        this.key = key;
    }

    /**
     * Returns the URI of the contribution the component definition is contained in.
     *
     * @return the URI of the contribution the component definition is contained in
     */
    public URI getContributionUri() {
        return contributionUri;
    }

    /**
     * Sets the URI of the contribution the component definition is contained in.
     *
     * @param contributionUri the URI of the contribution the component definition is contained in
     */
    public void setContributionUri(URI contributionUri) {
        this.contributionUri = contributionUri;
    }


}
