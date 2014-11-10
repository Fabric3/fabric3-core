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
 */
package org.fabric3.api;

/**
 * A holder for events sent through a channel ring buffer.
 * <p/>
 * Sequenced consumers may use this as the event type to modify contents for consumers in a later sequence. For example, a consumer responsible for
 * deserialization may set a parsed value using {@link #setParsed(Object)}.
 */
public interface ChannelEvent {

    /**
     * Returns the raw event.
     *
     * @return the event
     */
    <T> T getEvent(Class<T> type);

    /**
     * Returns the parsed event if applicable; otherwise null.
     *
     * @return the parsed event or null
     */
    <T> T getParsed(Class<T> type);

    /**
     * Sets the parsed event.
     *
     * @param parsed the event
     */
    void setParsed(Object parsed);

    /**
     * Returns true if the event is an end of batch.
     *
     * @return true if the event is an end of batch.
     */
    boolean isEndOfBatch();

    /**
     * Returns the event sequence number or -1 if not defined.
     *
     * @return the sequence number or -1
     */
    long getSequence();

}
