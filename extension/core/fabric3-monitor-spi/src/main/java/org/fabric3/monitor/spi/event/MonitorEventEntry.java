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
package org.fabric3.monitor.spi.event;

import java.nio.ByteBuffer;

import org.fabric3.api.annotation.monitor.MonitorLevel;
import org.fabric3.monitor.spi.buffer.ResizableByteBuffer;
import org.fabric3.monitor.spi.buffer.ResizableByteBufferMonitor;

/**
 * An entry for writing a monitor event to a ring buffer.
 */
public class MonitorEventEntry {
    private static final int DEFAULT_PARAM_SIZE = 10;

    private String template;
    private boolean parse = true;
    private ParameterEntry[] entries;
    private int parameterLimit = 0;

    private long sequence;
    private ResizableByteBuffer buffer;
    private long timestamp;
    private MonitorLevel level;
    private int destinationIndex;
    private long entryTimestamp;
    private boolean endOfBatch;

    /**
     * Constructor.
     *
     * @param capacity the fixed event size in bytes
     */
    public MonitorEventEntry(int capacity, ResizableByteBufferMonitor monitor) {
        buffer = new ResizableByteBuffer(ByteBuffer.allocateDirect(capacity), monitor);
        entries = new ParameterEntry[DEFAULT_PARAM_SIZE];
        for (int i = 0; i < entries.length; i++) {
            entries[i] = new ParameterEntry();
        }
    }

    /**
     * Returns the {@link ParameterEntry}s for this event.
     *
     * @return the {@link ParameterEntry}s
     */
    public ParameterEntry[] getEntries() {
        return entries;
    }

    /**
     * Sets the number of set parameters.
     *
     * @param limit the number of set parameters
     */
    public void setLimit(int limit) {
        parameterLimit = limit;
    }

    /**
     * Returns the number of set parameters, e.g. 0 is no parameters, 1 is single parameter.
     *
     * @return the number of set parameters
     */
    public int getLimit() {
        return parameterLimit;
    }

    /**
     * Returns the monitor event template.
     *
     * @return the monitor event template
     */
    public String getTemplate() {
        return template;
    }

    /**
     * Sets the monitor event template.
     *
     * @param template the monitor event template
     */
    public void setTemplate(String template) {
        this.template = template;
    }

    /**
     * Returns if the template should be parsed.
     *
     * @return true if the template should be parsed
     */
    public boolean isParse() {
        return parse;
    }

    /**
     * Sets if the template should be parsed.
     *
     * @param parse if the template should be parsed
     */
    public void setParse(boolean parse) {
        this.parse = parse;
    }

    /**
     * Returns the event entry timestamp in milliseconds.
     *
     * @return the event entry timestamp in milliseconds
     */
    public long getEntryTimestamp() {
        return entryTimestamp;
    }

    /**
     * Sets the event entry timestamp in milliseconds.
     *
     * @param entryTimestamp the event entry timestamp in milliseconds
     */
    public void setEntryTimestamp(long entryTimestamp) {
        this.entryTimestamp = entryTimestamp;
    }

    /**
     * Returns the ring buffer sequence number for the current event contained in this entry.
     *
     * @return the ring buffer sequence number
     */
    public long getSequence() {
        return sequence;
    }

    /**
     * Sets the ring buffer sequence number for the current event contained in this entry.
     *
     * @param sequence the ring buffer sequence number
     */
    public void setSequence(long sequence) {
        this.sequence = sequence;
    }

    /**
     * Returns true if this event is an end-of-batch message.
     *
     * @return true if this event is an end-of-batch message
     */
    public boolean isEndOfBatch() {
        return endOfBatch;
    }

    /**
     * Sets if this event is an end-of-batch message.
     *
     * @param endOfBatch true if this event is an end-of-batch message
     */
    public void setEndOfBatch(boolean endOfBatch) {
        this.endOfBatch = endOfBatch;
    }

    /**
     * Returns the buffer containing the event.
     *
     * @return the event buffer
     */
    public ResizableByteBuffer getBuffer() {
        return buffer;
    }

    /**
     * Sets the buffer containing the event.
     *
     * @param buffer the event buffer
     */
    public void setBuffer(ResizableByteBuffer buffer) {
        this.buffer = buffer;
    }

    /**
     * Returns the event level.
     *
     * @return the event level
     */
    public MonitorLevel getLevel() {
        return level;
    }

    /**
     * Sets the event level.
     *
     * @param level the event level
     */
    public void setLevel(MonitorLevel level) {
        this.level = level;
    }

    /**
     * Returns the current timestamp in nanoseconds for the event contained in this entry.
     *
     * @return the current timestamp in nanoseconds
     */
    public long getTimestampNanos() {
        return timestamp;
    }

    /**
     * Sets the current timestamp in nanoseconds for the event contained in this entry.
     *
     * @param timestamp the current timestamp in nanoseconds
     */
    public void setTimestampNanos(long timestamp) {
        this.timestamp = timestamp;
    }

    /**
     * Returns the index of the destination the event must be sent to.
     *
     * @return the destination index
     */
    public int getDestinationIndex() {
        return destinationIndex;
    }

    /**
     * Sets the index of the destination the event must be sent to.
     *
     * @param index the destination index
     */
    public void setDestinationIndex(int index) {
        this.destinationIndex = index;
    }

}
