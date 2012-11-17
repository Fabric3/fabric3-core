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

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.fabric3.model.type.AbstractPolicyAware;
import org.fabric3.model.type.CapabilityAware;

/**
 * A base component type.
 */
public class ComponentType extends AbstractPolicyAware<Implementation> implements CapabilityAware {
    private static final long serialVersionUID = 5302580019263119837L;

    private Map<String, AbstractService> services = new HashMap<String, AbstractService>();
    private Map<String, ConsumerDefinition> consumers = new HashMap<String, ConsumerDefinition>();
    private Map<String, AbstractReference> references = new HashMap<String, AbstractReference>();
    private Map<String, ProducerDefinition> producers = new HashMap<String, ProducerDefinition>();
    private Map<String, Property> properties = new HashMap<String, Property>();
    private Map<String, ResourceReferenceDefinition> resourceReferences = new HashMap<String, ResourceReferenceDefinition>();
    private Set<String> requiredCapabilities = new HashSet<String>();

    /**
     * Returns the services provided by the implementation keyed by name.
     *
     * @return services provided by the implementation
     */
    public Map<String, AbstractService> getServices() {
        return services;
    }

    /**
     * Adds a service provided by the implementation.
     *
     * @param service the service to add
     */
    public void add(AbstractService service) {
        service.setParent(this);
        if (roundTrip) {
            pushElement(service);
        }
        services.put(service.getName(), service);
    }

    /**
     * Returns the consumers provided by the implementation.
     *
     * @return the consumers provided by the implementation
     */
    public Map<String, ConsumerDefinition> getConsumers() {
        return consumers;
    }

    /**
     * Adds a consumer provided by the implementation.
     *
     * @param consumer the consumer to add
     */
    public void add(ConsumerDefinition consumer) {
        consumer.setParent(this);
        if (roundTrip) {
            pushElement(consumer);
        }
        consumers.put(consumer.getName(), consumer);
    }

    /**
     * Returns references defined by the implementation keyed by name.
     *
     * @return references defined by the implementation
     */
    public Map<String, AbstractReference> getReferences() {
        return references;
    }

    /**
     * Adds a reference defined by the implementation.
     *
     * @param reference the reference to add
     */
    public void add(AbstractReference reference) {
        reference.setParent(this);
        if (roundTrip) {
            pushElement(reference);
        }
        references.put(reference.getName(), reference);
    }

    /**
     * Returns producers defined by implementation keyed by name.
     *
     * @return producers defined by implementation
     */
    public Map<String, ProducerDefinition> getProducers() {
        return producers;
    }

    /**
     * Adds a producer to the implementation.
     *
     * @param producer the producer to add
     */
    public void add(ProducerDefinition producer) {
        producer.setParent(this);
        if (roundTrip) {
            pushElement(producer);
        }
        producers.put(producer.getName(), producer);
    }

    /**
     * Returns properties defined by the implementation keyed by name.
     *
     * @return properties defined by the implementation
     */
    public Map<String, Property> getProperties() {
        return properties;
    }

    /**
     * Add a property defined by the implementation.
     *
     * @param property the property to add
     */
    public void add(Property property) {
        property.setParent(this);
        if (roundTrip) {
            pushElement(property);
        }
        properties.put(property.getName(), property);
    }

    /**
     * Returns resource references defined by the implementation keyed by name.
     *
     * @return resource references defined by the implementation
     */
    public Map<String, ResourceReferenceDefinition> getResourceReferences() {
        return resourceReferences;
    }

    /**
     * Adds a resource reference defined by the implementation keyed by name.
     *
     * @param definition the resource reference to add
     */
    public void add(ResourceReferenceDefinition definition) {
        definition.setParent(this);
        if (roundTrip) {
            pushElement(definition);
        }
        resourceReferences.put(definition.getName(), definition);
    }

    public Set<String> getRequiredCapabilities() {
        return requiredCapabilities;
    }

    public void addRequiredCapability(String capability) {
        requiredCapabilities.add(capability);
    }
}
