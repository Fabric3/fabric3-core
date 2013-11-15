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
