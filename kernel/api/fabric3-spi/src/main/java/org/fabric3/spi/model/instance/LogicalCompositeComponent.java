/*
* Fabric3
* Copyright (c) 2009-2013 Metaform Systems
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
*/
package org.fabric3.spi.model.instance;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.fabric3.model.type.component.ComponentDefinition;
import org.fabric3.model.type.component.CompositeImplementation;

/**
 * An instantiated composite component in the domain.
 */
public class LogicalCompositeComponent extends LogicalComponent<CompositeImplementation> {
    private static final long serialVersionUID = 6661201121307925462L;

    private Map<LogicalReference, List<LogicalWire>> wires = new HashMap<LogicalReference, List<LogicalWire>>();
    private Map<URI, LogicalComponent<?>> components = new HashMap<URI, LogicalComponent<?>>();
    private Map<URI, LogicalChannel> channels = new HashMap<URI, LogicalChannel>();
    private List<LogicalResource<?>> resources = new ArrayList<LogicalResource<?>>();

    /**
     * Instantiates a composite component.
     *
     * @param uri        URI of the component.
     * @param definition Definition of the component.
     * @param parent     Parent of the component.
     */
    public LogicalCompositeComponent(URI uri, ComponentDefinition<CompositeImplementation> definition, LogicalCompositeComponent parent) {
        super(uri, definition, parent);
    }

    /**
     * Adds a wire to this composite component.
     *
     * @param logicalReference the wire source
     * @param logicalWire      Wire to be added to this composite component.
     */
    public void addWire(LogicalReference logicalReference, LogicalWire logicalWire) {
        List<LogicalWire> logicalWires = wires.get(logicalReference);
        if (logicalWires == null) {
            logicalWires = new ArrayList<LogicalWire>();
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
            logicalWires = new ArrayList<LogicalWire>();
            wires.put(logicalReference, logicalWires);
        }
        logicalWires.addAll(newWires);
    }

    /**
     * Adds a set of wires to this composite component, overriding any existing ones.
     *
     * @param logicalReference the source for the wires
     * @param logicalWires     the list of wires
     */
    public void overrideWires(LogicalReference logicalReference, List<LogicalWire> logicalWires) {
        wires.put(logicalReference, logicalWires);
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

}
