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

import javax.xml.namespace.QName;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.fabric3.api.model.type.Namespace;
import org.fabric3.api.model.type.RuntimeMode;

/**
 * A composite component type.
 */
public class Composite extends ComponentType {
    private QName name;
    private URI contributionUri;

    private boolean deployable;
    private List<RuntimeMode> modes = Arrays.asList(RuntimeMode.VM, RuntimeMode.NODE);
    private List<String> environments = Collections.emptyList();

    private Map<String, Component<? extends Implementation<?>>> components = new HashMap<>();
    private Map<QName, Include> includes = new HashMap<>();
    private List<Wire> wires = new ArrayList<>();
    private Map<String, Channel> channels = new HashMap<>();
    private List<Resource> resources = new ArrayList<>();

    // views are caches of all properties, references, wires, or components contained in the composite and its included composites
    private Map<String, Component<? extends Implementation<?>>> componentsView = new HashMap<>();
    private Map<String, Channel> channelsView = new HashMap<>();
    private List<Wire> wiresView = new ArrayList<>();
    private List<Resource> resourcesView = new ArrayList<>();

    private List<Namespace> namespaces;

    // determines if this composite is a pointer. Pointers are references to composites that do not yet exist or have been deleted such
    // as from an include or implementation.composite. Used primarily in tooling environments.
    private boolean pointer;

    /**
     * Constructor.
     *
     * @param name the qualified name of this composite
     */
    public Composite(QName name) {
        this.name = name;
    }

    /**
     * Constructor.
     *
     * @param name    the qualified name of this composite
     * @param pointer true if this composite is a pointer
     * @param uri     the contribution URI
     */
    public Composite(QName name, boolean pointer, URI uri) {
        this.name = name;
        this.pointer = pointer;
        this.contributionUri = uri;
    }

    /**
     * Returns the qualified name of this composite. The namespace portion of this name is the targetNamespace for other qualified names used in the composite.
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
     * @param contributionUri contribution URI
     */
    public void setContributionUri(URI contributionUri) {
        this.contributionUri = contributionUri;
    }

    /**
     * Returns true if this composite is configured as a deployable.
     *
     * @return true if this composite is configured as a deployable
     */
    public boolean isDeployable() {
        return deployable;
    }

    /**
     * Sets if this composite is configured as a deployable.
     *
     * @param deployable if this composite is configured as a deployable
     */
    public void setDeployable(boolean deployable) {
        this.deployable = deployable;
    }

    /**
     * Returns the runtime modes this composite is activated under.
     *
     * @return the runtime modes
     */
    public List<RuntimeMode> getModes() {
        return modes;
    }

    /**
     * Sets the runtime modes this composite is activated under.
     *
     * @param modes the runtime modes this composite is activated under
     */
    public void setModes(List<RuntimeMode> modes) {
        this.modes = modes;
    }

    /**
     * Returns the environments this composite is activated under.
     *
     * @return the environments this composite is activated under
     */
    public List<String> getEnvironments() {
        return environments;
    }

    /**
     * Sets the environments this composite is activated under.
     *
     * @param environments the environments this composite is activated under
     */
    public void setEnvironments(List<String> environments) {
        this.environments = environments;
    }

    /**
     * Returns true if this composite is a pointer.
     *
     * @return true if this composite is a pointer
     */
    public boolean isPointer() {
        return pointer;
    }

    /**
     * Returns all components including ones from included composites
     *
     * @return components
     */
    public Map<String, Component<? extends Implementation<?>>> getComponents() {
        return componentsView;
    }

    /**
     * Adds a component to this composite.
     *
     * @param component the component
     */
    public void add(Component<? extends Implementation<?>> component) {
        component.setParent(this);
        componentsView.put(component.getName(), component);
        components.put(component.getName(), component);
    }

    /**
     * Returns all wires including the ones from included composites.
     *
     * @return wires
     */
    public List<Wire> getWires() {
        return wiresView;
    }

    /**
     * Adds a wire to the composite.
     *
     * @param wire the wire
     */
    public void add(Wire wire) {
        wire.setParent(this);
        wires.add(wire);
        wiresView.add(wire);
    }

    /**
     * Returns all channels including ones from included composites.
     *
     * @return channels
     */
    public Map<String, Channel> getChannels() {
        return channelsView;
    }

    /**
     * Adds a channel to the composite.
     *
     * @param channel the channel
     */
    public void add(Channel channel) {
        channel.setParent(this);
        channelsView.put(channel.getName(), channel);
        channels.put(channel.getName(), channel);
    }

    /**
     * Returns all resources including ones from included composites.
     *
     * @return channels
     */
    public List<Resource> getResources() {
        return resourcesView;
    }

    /**
     * Adds a resource to the composite.
     *
     * @param resource the resource
     */
    public void add(Resource resource) {
        resource.setParent(this);
        resourcesView.add(resource);
        resources.add(resource);
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
        include.setParent(this);
        includes.put(include.getName(), include);
        componentsView.putAll(include.getIncluded().getComponents());
        wiresView.addAll(include.getIncluded().getWires());
        channelsView.putAll(include.getIncluded().getChannels());
        resourcesView.addAll(include.getIncluded().getResources());
    }

    /**
     * Returns components declared in this composite, except components from included composites.
     *
     * @return components
     */
    public Map<String, Component<? extends Implementation<?>>> getDeclaredComponents() {
        return components;
    }

    /**
     * Returns the wires declared in this composite, except wires from included composites.
     *
     * @return wires
     */
    public List<Wire> getDeclaredWires() {
        return wires;
    }

    /**
     * Returns channels declared in this composite, except channels from included composites.
     *
     * @return channels
     */
    public Map<String, Channel> getDeclaredChannels() {
        return channels;
    }

    /**
     * Returns resources declared in this composite, except resources from included composites.
     *
     * @return resources
     */
    public List<Resource> getDeclaredResources() {
        return resources;
    }

    public void addNamespace(String prefix, String uri) {
        if (namespaces == null) {
            namespaces = new ArrayList<>();
        }
        namespaces.add(new Namespace(prefix, uri));
    }

    public List<Namespace> getNamespaces() {
        if (namespaces == null) {
            return Collections.emptyList();
        }
        return namespaces;
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
