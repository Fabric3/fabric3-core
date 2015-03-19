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
    private List<Class<?>> eventTypes = new ArrayList<>();
    private List<PhysicalEventFilterDefinition> filters = new ArrayList<>();
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
    public List<Class<?>> getEventTypes() {
        return eventTypes;
    }

    /**
     * Add the fully qualified name of the source event type to the operation.
     *
     * @param type the source event type to be added.
     */
    public void addEventType(Class<?> type) {
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