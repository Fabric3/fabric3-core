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
package org.fabric3.api.model.type.component;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import org.fabric3.api.model.type.ModelObject;

/**
 * A component configuration in a composite file.
 */
public class ComponentDefinition<I extends Implementation<?>> extends ModelObject<Composite> {
    private static final long serialVersionUID = 4909969579651563484L;

    private String name;
    private I implementation;
    private String key;
    private int order = Integer.MIN_VALUE;
    private URI contributionUri;

    private Map<String, ServiceDefinition<ComponentDefinition>> services = new HashMap<>();
    private Map<String, ReferenceDefinition<ComponentDefinition>> references = new HashMap<>();
    private Map<String, ComponentProducer> producers = new HashMap<>();
    private Map<String, ComponentConsumer> consumers = new HashMap<>();
    private Map<String, PropertyValue> propertyValues = new HashMap<>();

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
        implementation.setParent(this);
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
     * Returns the references configured by this component.
     *
     * @return the component references
     */
    public Map<String, ReferenceDefinition<ComponentDefinition>> getReferences() {
        return references;
    }

    /**
     * Add a reference to this component.
     *
     * @param reference the reference to add
     */
    public void add(ReferenceDefinition<ComponentDefinition> reference) {
        reference.setParent(this);
        pushElement(reference);
        references.put(reference.getName(), reference);
    }

    /**
     * Removes a reference.
     *
     * @param reference the reference to remove
     */
    public void remove(ReferenceDefinition<ComponentDefinition> reference) {
        reference.setParent(null);
        removeElement(reference);
        references.remove(reference.getName());
    }

    /**
     * Returns the services configured by this component definition.
     *
     * @return the services configured by this component
     */
    public Map<String, ServiceDefinition<ComponentDefinition>> getServices() {
        return services;
    }

    /**
     * Adds a service to this component.
     *
     * @param service the service to add
     */
    public void add(ServiceDefinition<ComponentDefinition> service) {
        service.setParent(this);
        pushElement(service);
        services.put(service.getName(), service);
    }

    /**
     * Removes a service.
     *
     * @param service the service to remove
     */
    public void remove(ServiceDefinition<ComponentDefinition> service) {
        service.setParent(null);
        removeElement(service);
        services.remove(service.getName());
    }


    /**
     * Adds a producer to this component.
     *
     * @param producer the producer to add
     */
    public void add(ComponentProducer producer) {
        producer.setParent(this);
        pushElement(producer);
        producers.put(producer.getName(), producer);
    }

    /**
     * Removes a producer.
     *
     * @param producer the producer to remove
     */
    public void remove(ComponentProducer producer) {
        producer.setParent(null);
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
        consumer.setParent(this);
        pushElement(consumer);
        consumers.put(consumer.getName(), consumer);
    }

    /**
     * Removes a consumer.
     *
     * @param consumer the consumer to remove
     */
    public void remove(ComponentConsumer consumer) {
        consumer.setParent(null);
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
        value.setParent(this);
        pushElement(value);
        propertyValues.put(value.getName(), value);
    }

    /**
     * Removes a property value.
     *
     * @param value the value to remove
     */
    public void remove(PropertyValue value) {
        value.setValue(null);
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
     * Returns the order for collection- and array-based wires or {@link Integer#MIN_VALUE} if not specified.
     *
     * @return the order for collection- and array-based wires or {@link Integer#MIN_VALUE} if not specified
     */
    public int getOrder() {
        return order;
    }

    /**
     * Sets the order for collection- and array-based wires.
     *
     * @param order the order
     */
    public void setOrder(int order) {
        this.order = order;
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
