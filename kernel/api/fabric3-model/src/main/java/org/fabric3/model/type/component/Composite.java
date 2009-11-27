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
 * Represents a composite.
 *
 * @version $Rev$ $Date$
 */
public class Composite extends AbstractComponentType<CompositeService, CompositeReference, Property, ResourceDefinition> implements PolicyAware {
    private static final long serialVersionUID = -3126069884608566611L;

    private final QName name;
    private URI contributionUri;
    private boolean local;
    private Autowire autowire;
    private final Map<String, ComponentDefinition<? extends Implementation<?>>> components =
            new HashMap<String, ComponentDefinition<? extends Implementation<?>>>();
    private final Map<QName, Include> includes = new HashMap<QName, Include>();
    private final List<WireDefinition> wires = new ArrayList<WireDefinition>();

    // views are caches of all properties, references, wires, or components contained in the composite and its included composites
    private final Map<String, Property> propertiesView = new HashMap<String, Property>();
    private final Map<String, CompositeReference> referencesView = new HashMap<String, CompositeReference>();
    private final Map<String, CompositeService> servicesView = new HashMap<String, CompositeService>();
    private final Map<String, ComponentDefinition<? extends Implementation<?>>> componentsView =
            new HashMap<String, ComponentDefinition<? extends Implementation<?>>>();
    private final List<WireDefinition> wiresView = new ArrayList<WireDefinition>();

    private QName constrainingType;
    private Set<QName> intents;
    private Set<QName> policySets;
    private Map<QName, Object> metadata = new HashMap<QName, Object>();

    /**
     * Constructor defining the composite name.
     *
     * @param name the qualified name of this composite
     */
    public Composite(QName name) {
        this.name = name;
        setScope("COMPOSITE");
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
     * Returns the URI of the contribution this componentType is associated with.
     *
     * @return the URI of the contribution this componentType is associated with
     */
    public URI getContributionUri() {
        return contributionUri;
    }

    /**
     * Sets the URI of the contribution this componentType is associated with.
     *
     * @param contributionUri tcontribution URI
     */
    public void setContributionUri(URI contributionUri) {
        this.contributionUri = contributionUri;
    }

    /**
     * Indicates that components in this composite should be co-located.
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
     * Returns if the autowire status for composite
     *
     * @return the autowire status for the composite
     */
    public Autowire getAutowire() {
        return autowire;
    }

    /**
     * Sets the autowire status for the composite
     *
     * @param autowire the autowire status for the composite
     */
    public void setAutowire(Autowire autowire) {
        this.autowire = autowire;
    }

    /**
     * Returns the name of the constraining type for this composite.
     *
     * @return the name of the constraining type for this composite
     */
    public QName getConstrainingType() {
        return constrainingType;
    }

    /**
     * Sets the name of the constraining type for this composite.
     *
     * @param constrainingType the name of the constraining type for this composite
     */
    public void setConstrainingType(QName constrainingType) {
        this.constrainingType = constrainingType;
    }

    @Override
    @SuppressWarnings("unchecked")
    /**
     * Get all properties including the ones are from included composites
     * @return
     */
    public Map<String, Property> getProperties() {
        return propertiesView;
    }

    public void add(Property property) {
        super.add(property);
        propertiesView.put(property.getName(), property);
    }

    @Override
    @SuppressWarnings("unchecked")
    /**
     * Get all references including the ones are from included composites
     * @return
     */
    public Map<String, CompositeReference> getReferences() {
        return referencesView;
    }

    public void add(CompositeReference reference) {
        super.add(reference);
        referencesView.put(reference.getName(), reference);
    }

    @SuppressWarnings("unchecked")
    @Override
    /**
     * Get all services including the ones are from included composites
     * @return
     */
    public Map<String, CompositeService> getServices() {
        return servicesView;
    }

    public void add(CompositeService service) {
        super.add(service);
        servicesView.put(service.getName(), service);
    }

    /**
     * Get all components including the ones are from included composites
     */
    @SuppressWarnings("unchecked")
    public Map<String, ComponentDefinition<? extends Implementation<?>>> getComponents() {
        return componentsView;
    }

    public void add(ComponentDefinition<? extends Implementation<?>> componentDefinition) {
        componentsView.put(componentDefinition.getName(), componentDefinition);
        components.put(componentDefinition.getName(), componentDefinition);
    }

    /**
     * Get all wires including the ones are from included composites
     */
    @SuppressWarnings("unchecked")
    public List<WireDefinition> getWires() {
        return wiresView;
    }

    /**
     * Get declared properties in this composite type, included doesn't count
     */
    public Map<String, Property> getDeclaredProperties() {
        return super.getProperties();
    }

    /**
     * Get declared references in this composite type, included doesn't count
     */
    public Map<String, CompositeReference> getDeclaredReferences() {
        return super.getReferences();
    }

    /**
     * Get declared services in this composite type, included doesn't count
     */
    public Map<String, CompositeService> getDeclaredServices() {
        return super.getServices();
    }

    /**
     * Get declared components in this composite type, included doesn't count
     */
    public Map<String, ComponentDefinition<? extends Implementation<?>>> getDeclaredComponents() {
        return components;
    }

    /**
     * Get declared wires in this composite type, included doesn't count
     */
    public List<WireDefinition> getDeclaredWires() {
        return wires;
    }

    public void add(WireDefinition wireDefn) {
        wires.add(wireDefn);
        wiresView.add(wireDefn);
    }


    public Map<QName, Include> getIncludes() {
        return includes;
    }

    public void add(Include include) {
        includes.put(include.getName(), include);
        componentsView.putAll(include.getIncluded().getComponents());
        referencesView.putAll(include.getIncluded().getReferences());
        propertiesView.putAll(include.getIncluded().getProperties());
        servicesView.putAll(include.getIncluded().getServices());
        wiresView.addAll(include.getIncluded().getWires());
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

}
