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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.xml.namespace.QName;

import org.fabric3.model.type.PolicyAware;

/**
 * A composite component type.
 *
 * @version $Rev$ $Date$
 */
public class Composite extends AbstractComponentType implements PolicyAware {
    private static final long serialVersionUID = -3126069884608566611L;

    private QName name;
    private URI contributionUri;
    private boolean local;
    private Autowire autowire;
    private Set<QName> intents;
    private Set<QName> policySets;

    private Map<String, ComponentDefinition<? extends Implementation<?>>> components =
            new HashMap<String, ComponentDefinition<? extends Implementation<?>>>();
    private Map<QName, Include> includes = new HashMap<QName, Include>();
    private List<WireDefinition> wires = new ArrayList<WireDefinition>();
    private Map<String, ChannelDefinition> channels = new HashMap<String, ChannelDefinition>();

    // views are caches of all properties, references, wires, or components contained in the composite and its included composites
    private Map<String, Property> propertiesView = new HashMap<String, Property>();
    private Map<String, ReferenceDefinition> referencesView = new HashMap<String, ReferenceDefinition>();
    private Map<String, ServiceDefinition> servicesView = new HashMap<String, ServiceDefinition>();
    private Map<String, ComponentDefinition<? extends Implementation<?>>> componentsView =
            new HashMap<String, ComponentDefinition<? extends Implementation<?>>>();
    private Map<String, ChannelDefinition> channelsView = new HashMap<String, ChannelDefinition>();
    private List<WireDefinition> wiresView = new ArrayList<WireDefinition>();

    private Map<QName, Object> metadata = new HashMap<QName, Object>();

    /**
     * Constructor.
     *
     * @param name the qualified name of this composite
     */
    public Composite(QName name) {
        this.name = name;
    }

    /**
     * Returns the qualified name of this composite. The namespace portion of this name is the targetNamespace for other qualified names used in the
     * composite.
     *
     * @return the qualified name of this composite
     */
    public QName getName() {
        return name;
    }

    /**
     * Returns the URI of the contribution this composite is associated with.
     *
     * @return the URI of the contribution this composite is associated with
     */
    public URI getContributionUri() {
        return contributionUri;
    }

    /**
     * Sets the URI of the contribution this composite is associated with.
     *
     * @param contributionUri tcontribution URI
     */
    public void setContributionUri(URI contributionUri) {
        this.contributionUri = contributionUri;
    }

    /**
     * Indicates if components in this composite should be co-located.
     *
     * @return true if components in this composite should be co-located
     */
    public boolean isLocal() {
        return local;
    }

    /**
     * Sets whether components in this composite should be co-located.
     *
     * @param local true if components in this composite should be co-located
     */
    public void setLocal(boolean local) {
        this.local = local;
    }

    /**
     * Returns the autowire status for the composite.
     *
     * @return the autowire status for the composite
     */
    public Autowire getAutowire() {
        return autowire;
    }

    /**
     * Sets the autowire status for the composite.
     *
     * @param autowire the autowire status for the composite
     */
    public void setAutowire(Autowire autowire) {
        this.autowire = autowire;
    }

    @Override
    public Map<String, Property> getProperties() {
        return propertiesView;
    }

    public void add(Property property) {
        super.add(property);
        propertiesView.put(property.getName(), property);
    }

    @Override
    public Map<String, ReferenceDefinition> getReferences() {
        return referencesView;
    }

    /**
     * Returns all references including ones are included composites as a CompositeReference subtype.
     *
     * @return references
     */
    public Map<String, CompositeReference> getCompositeReferences() {
        return cast(referencesView);
    }

    @Override
    public void add(ReferenceDefinition reference) {
        if (!(reference instanceof CompositeReference)) {
            throw new IllegalArgumentException("Reference type must be " + CompositeReference.class.getName());
        }
        super.add(reference);
        referencesView.put(reference.getName(), reference);
    }

    @Override
    public Map<String, ServiceDefinition> getServices() {
        return servicesView;
    }

    /**
     * Returns all services including ones from included composites as a CompositeService subtype.
     *
     * @return services
     */
    public Map<String, CompositeService> getCompositeServices() {
        return cast(servicesView);
    }

    @SuppressWarnings({"unchecked"})
    @Override
    public void add(ServiceDefinition service) {
        if (!(service instanceof CompositeService)) {
            throw new IllegalArgumentException("Service type must be " + CompositeService.class.getName());
        }
        super.add(service);
        servicesView.put(service.getName(), service);
    }

    /**
     * Returns all components including ones from included composites
     *
     * @return components
     */
    public Map<String, ComponentDefinition<? extends Implementation<?>>> getComponents() {
        return componentsView;
    }

    /**
     * Adds a component to this composite.
     *
     * @param component the component
     */
    public void add(ComponentDefinition<? extends Implementation<?>> component) {
        componentsView.put(component.getName(), component);
        components.put(component.getName(), component);
    }

    /**
     * Returns all wires including the ones from included composites.
     *
     * @return wires
     */
    public List<WireDefinition> getWires() {
        return wiresView;
    }

    public Map<String, ChannelDefinition> getChannels() {
        return channelsView;
    }

    /**
     * Returns properties declared in this composite, except properties from included composites.
     *
     * @return properties
     */
    public Map<String, Property> getDeclaredProperties() {
        return super.getProperties();
    }

    /**
     * Returns references declared in this composite, except references from included composites.
     *
     * @return references
     */
    public Map<String, ReferenceDefinition> getDeclaredReferences() {
        return super.getReferences();
    }

    /**
     * Returns services declared in this composite, except services from included composites.
     *
     * @return services
     */
    public Map<String, ServiceDefinition> getDeclaredServices() {
        return super.getServices();
    }

    /**
     * Returns components declared in this composite, except components from included composites.
     *
     * @return components
     */
    public Map<String, ComponentDefinition<? extends Implementation<?>>> getDeclaredComponents() {
        return components;
    }

    /**
     * Returns the wires declared in this composite, except wires from included composites.
     *
     * @return wires
     */
    public List<WireDefinition> getDeclaredWires() {
        return wires;
    }

    /**
     * Adds a wire to the composite.
     *
     * @param wire the wire
     */
    public void add(WireDefinition wire) {
        wires.add(wire);
        wiresView.add(wire);
    }

    /**
     * Returns channels declared in this composite, except channels from included composites.
     *
     * @return channels
     */
    public Map<String, ChannelDefinition> getDeclaredChannels() {
        return channels;
    }

    /**
     * Adds a channel to the composite.
     *
     * @param channel the channel
     */
    public void add(ChannelDefinition channel) {
        channelsView.put(channel.getName(), channel);
        channels.put(channel.getName(), channel);
    }

    /**
     * Returns included composites.
     *
     * @return included composites
     */
    public Map<QName, Include> getIncludes() {
        return includes;
    }

    /**
     * Adds an included composite.
     *
     * @param include the composite to include
     */
    public void add(Include include) {
        includes.put(include.getName(), include);
        componentsView.putAll(include.getIncluded().getComponents());
        referencesView.putAll(include.getIncluded().getReferences());
        propertiesView.putAll(include.getIncluded().getProperties());
        servicesView.putAll(include.getIncluded().getServices());
        wiresView.addAll(include.getIncluded().getWires());
        channelsView.putAll(include.getIncluded().getChannels());
    }

    public void addIntent(QName intent) {
        intents.add(intent);
    }

    public Set<QName> getIntents() {
        return intents;
    }

    public void addPolicySet(QName policySet) {
        policySets.add(policySet);
    }

    public void setIntents(Set<QName> intents) {
        this.intents = intents;
    }

    public Set<QName> getPolicySets() {
        return policySets;
    }

    public void setPolicySets(Set<QName> policySets) {
        this.policySets = policySets;
    }

    public void addMetadata(QName name, Object data) {
        metadata.put(name, data);
    }

    public <T> T getMetadata(QName name, Class<T> type) {
        return type.cast(metadata.get(name));
    }

    public Map<QName, Object> getMetadata() {
        return metadata;
    }

    public int hashCode() {
        return name.hashCode();
    }


    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        Composite that = (Composite) o;
        return name.equals(that.name);
    }

    @SuppressWarnings({"unchecked"})
    private <T> T cast(Object o) {
        return (T) o;
    }


}
