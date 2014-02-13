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
 *
 * ----------------------------------------------------
 *
 * Portions originally based on Apache Tuscany 2007
 * licensed under the Apache 2.0 license.
 *
 */
package org.fabric3.spi.model.physical;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.fabric3.api.ChannelEvent;

/**
 * Metadata for an event stream that transmits events.
 */
public class PhysicalEventStreamDefinition implements Serializable {
    private static final long serialVersionUID = 8684345140369447283L;
    private String name;
    private List<String> eventTypes = new ArrayList<>();
    private List<PhysicalEventFilterDefinition> filters = new ArrayList<>();
    private List<PhysicalHandlerDefinition> handlers = new ArrayList<>();
    private boolean channelEvent;

    public PhysicalEventStreamDefinition(String name) {
        this.name = name;
    }

    /**
     * Gets the name of the stream.
     *
     * @return stream name.
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the name of the stream.
     *
     * @param name stream name.
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Returns the fully qualified name of event types for this stream. The types are returned in order that they are passed to the stream.
     *
     * @return the stream parameter types.
     */
    public List<String> getEventTypes() {
        return eventTypes;
    }

    /**
     * Add the fully qualified name of the source event type to the operation.
     *
     * @param type the source event type to be added.
     */
    public void addEventType(String type) {
        eventTypes.add(type);
    }

    /**
     * Returns {@link PhysicalEventFilterDefinition}s for the stream.
     *
     * @return filter definitions for the stream
     */
    public List<PhysicalEventFilterDefinition> getFilters() {
        return filters;
    }

    /**
     * Adds a {@link PhysicalEventFilterDefinition}.
     *
     * @param definition the definition to add
     */
    public void addFilterDefinition(PhysicalEventFilterDefinition definition) {
        filters.add(definition);
    }

    /**
     * Returns the {@link PhysicalHandlerDefinition}s for the stream.
     *
     * @return handler definitions for the stream
     */
    public List<PhysicalHandlerDefinition> getHandlers() {
        return handlers;
    }

    /**
     * Adds a {@link PhysicalHandlerDefinition} to the stream.
     *
     * @param definition the definition
     */
    public void addHandlerDefinition(PhysicalHandlerDefinition definition) {
        handlers.add(definition);
    }

    /**
     * For consumer streams, sets if the event type should be a {@link ChannelEvent}.
     *
     * @param channelEvent true if the event type should be a {@link ChannelEvent}
     */
    public void setChannelEvent(boolean channelEvent) {
        this.channelEvent = channelEvent;
    }

    /**
     * Returns true if the event type should be a {@link ChannelEvent}.
     */
    public boolean isChannelEvent() {
        return channelEvent;
    }
}