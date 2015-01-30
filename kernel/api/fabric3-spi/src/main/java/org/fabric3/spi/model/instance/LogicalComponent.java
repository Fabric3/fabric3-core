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
package org.fabric3.spi.model.instance;

import javax.xml.namespace.QName;
import java.net.URI;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.fabric3.api.host.Names;
import org.fabric3.api.model.type.component.Component;
import org.fabric3.api.model.type.component.Implementation;

/**
 * An instantiated component in the domain.
 */
public class LogicalComponent<I extends Implementation<?>> extends LogicalScaArtifact<LogicalCompositeComponent> {
    private static final long serialVersionUID = -3520150701040845117L;

    private URI uri;
    private Component<I> definition;
    private Map<String, LogicalProperty> properties = new HashMap<>();
    private Map<String, LogicalService> services = new HashMap<>();
    private Map<String, LogicalReference> references = new HashMap<>();
    private Map<String, LogicalProducer> producers = new HashMap<>();
    private Map<String, LogicalConsumer> consumers = new HashMap<>();
    private Map<String, LogicalResourceReference<?>> resourceReferences = new HashMap<>();
    private String zone = Names.LOCAL_ZONE;
    private QName deployable;
    private LogicalState state = LogicalState.NEW;

    /**
     * @param uri        URI of the component.
     * @param definition Definition of the component.
     * @param parent     Parent of the component.
     */
    public LogicalComponent(URI uri, Component<I> definition, LogicalCompositeComponent parent) {
        super(parent);
        this.uri = uri;
        this.definition = definition;
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
     * Returns the resource references required by the current component.
     *
     * @return the resources references required by the current component
     */
    public Collection<LogicalResourceReference<?>> getResourceReferences() {
        return resourceReferences.values();
    }

    /**
     * Returns a resource reference with the given URI.
     *
     * @param name the resource name
     * @return the resource.
     */
    public LogicalResourceReference<?> getResourceReference(String name) {
        return resourceReferences.get(name);
    }

    /**
     * Adds a the resolved resource
     *
     * @param resourceReference the resource to add
     */
    public void addResource(LogicalResourceReference<?> resourceReference) {
        resourceReferences.put(resourceReference.getUri().getFragment(), resourceReference);
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
     * Returns the component producers.
     *
     * @return the producers
     */
    public Collection<LogicalProducer> getProducers() {
        return producers.values();
    }

    /**
     * Returns a producer with the given URI.
     *
     * @param name the producer name
     * @return the producer.
     */
    public LogicalProducer getProducer(String name) {
        return producers.get(name);
    }

    /**
     * Adds a producer.
     *
     * @param producer the producer to add
     */
    public void addProducer(LogicalProducer producer) {
        producers.put(producer.getUri().getFragment(), producer);
    }

    /**
     * Returns the component consumer.
     *
     * @return the producers
     */
    public Collection<LogicalConsumer> getConsumers() {
        return consumers.values();
    }

    /**
     * Returns a consumer with the given URI.
     *
     * @param name the producer name
     * @return the producer.
     */
    public LogicalConsumer getConsumer(String name) {
        return consumers.get(name);
    }

    /**
     * Adds a the consumer.
     *
     * @param consumer the consumer to add
     */
    public void addConsumer(LogicalConsumer consumer) {
        consumers.put(consumer.getUri().getFragment(), consumer);
    }

    /**
     * Returns the resolved properties for the component.
     *
     * @return the resolved properties for the component
     */
    public Map<String, LogicalProperty> getAllProperties() {
        return properties;
    }

    /**
     * Gets a property.
     *
     * @param name the name of the property.
     * @return the property or null if not found
     */
    public LogicalProperty getProperties(String name) {
        return properties.get(name);
    }

    /**
     * Sets a collection of resolved property values
     *
     * @param property the parsed property
     */
    public void setProperties(LogicalProperty property) {
        properties.put(property.getName(), property);
    }

    /**
     * Returns the component implementation type.
     *
     * @return the component implementation type
     */
    public Component<I> getDefinition() {
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
