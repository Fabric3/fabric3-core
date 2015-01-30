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
 * Portions originally based on Apache Tuscany 2007
 * licensed under the Apache 2.0 license.
 */
package org.fabric3.spi.model.instance;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.fabric3.api.model.type.component.ComponentDefinition;
import org.fabric3.api.model.type.component.CompositeImplementation;

/**
 * An instantiated composite component in the domain.
 */
public class LogicalCompositeComponent extends LogicalComponent<CompositeImplementation> {
    private static final long serialVersionUID = 6661201121307925462L;

    private Map<LogicalReference, List<LogicalWire>> wires = new HashMap<>();
    private Map<URI, LogicalComponent<?>> components = new HashMap<>();
    private Map<URI, LogicalChannel> channels = new HashMap<>();
    private List<LogicalResource<?>> resources = new ArrayList<>();
    private boolean autowire;

    /**
     * Instantiates a composite component.
     *
     * @param uri        the component URI
     * @param definition the component definition
     * @param parent     the component parent
     */
    public LogicalCompositeComponent(URI uri, ComponentDefinition<CompositeImplementation> definition, LogicalCompositeComponent parent) {
        super(uri, definition, parent);
    }

    /**
     * Instantiates a composite component.
     *
     * @param uri        the component URI
     * @param definition the component definition
     * @param autowire   true if autowire is enabled
     */
    public LogicalCompositeComponent(URI uri, ComponentDefinition<CompositeImplementation> definition, boolean autowire) {
        super(uri, definition, null);
        this.autowire = autowire;
    }

    /**
     * Adds a wire to this composite component.
     *
     * @param logicalReference the wire source
     * @param logicalWire      the wire to be added to this composite component
     */
    public void addWire(LogicalReference logicalReference, LogicalWire logicalWire) {
        List<LogicalWire> logicalWires = wires.get(logicalReference);
        if (logicalWires == null) {
            logicalWires = new ArrayList<>();
            wires.put(logicalReference, logicalWires);
        }
        logicalWires.add(logicalWire);
    }

    /**
     * Adds a set of wires to this composite component.
     *
     * @param logicalReference the source for the wires
     * @param newWires         the wires to add
     */
    public void addWires(LogicalReference logicalReference, List<LogicalWire> newWires) {
        List<LogicalWire> logicalWires = wires.get(logicalReference);
        if (logicalWires == null) {
            logicalWires = new ArrayList<>();
            wires.put(logicalReference, logicalWires);
        }
        logicalWires.addAll(newWires);
    }

    /**
     * Gets the resolved targets sourced by the specified logical reference.
     *
     * @param logicalReference Logical reference that sources the wire.
     * @return Resolved targets for the reference.
     */
    public List<LogicalWire> getWires(LogicalReference logicalReference) {
        List<LogicalWire> logicalWires = wires.get(logicalReference);
        if (logicalWires == null) {
            return Collections.emptyList();
        }
        return logicalWires;
    }

    /**
     * Returns a map of wires keyed by logical reference contained in this composite.
     *
     * @return a map of wires  keyed by logical reference contained in this composite
     */
    public Map<LogicalReference, List<LogicalWire>> getWires() {
        return wires;
    }

    /**
     * Returns the child components of the current component.
     *
     * @return the child components of the current component
     */
    public Collection<LogicalComponent<?>> getComponents() {
        return components.values();
    }

    /**
     * Returns a child component with the given URI.
     *
     * @param uri the child component URI
     * @return a child component with the given URI.
     */
    public LogicalComponent<?> getComponent(URI uri) {
        return components.get(uri);
    }

    /**
     * Removes a child component with the given URI.
     *
     * @param uri the child component URI
     */
    public void removeComponent(URI uri) {
        components.remove(uri);
    }

    /**
     * Adds a child component
     *
     * @param component the child component to add
     */
    public void addComponent(LogicalComponent<?> component) {
        components.put(component.getUri(), component);
    }

    /**
     * Returns the channels contained in the current component.
     *
     * @return the channels contained in the current component
     */
    public Collection<LogicalChannel> getChannels() {
        return channels.values();
    }

    /**
     * Returns a channel with the given URI.
     *
     * @param uri the channel URI
     * @return the channel
     */
    public LogicalChannel getChannel(URI uri) {
        return channels.get(uri);
    }

    /**
     * Removes a channel with the given URI.
     *
     * @param uri the channel URI
     */
    public void removeChannel(URI uri) {
        channels.remove(uri);
    }

    /**
     * Adds a channel.
     *
     * @param channel the channel to add
     */
    public void addChannel(LogicalChannel channel) {
        channels.put(channel.getUri(), channel);
    }

    public Collection<LogicalResource<?>> getResources() {
        return resources;
    }

    public void addResource(LogicalResource<?> resource) {
        resources.add(resource);
    }

    /**
     * Sets the component state.
     *
     * @param state the instance state
     */
    @Override
    public void setState(LogicalState state) {
        super.setState(state);
        for (LogicalComponent<?> component : getComponents()) {
            component.setState(state);
        }
    }

    /**
     * Returns true if autowire is enabled.
     *
     * @return true if autowire is enabled
     */
    public boolean isAutowire() {
        return autowire;
    }
}
