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
package org.fabric3.monitor.impl.router;

import java.nio.ByteBuffer;

import org.fabric3.api.annotation.monitor.MonitorLevel;

/**
 * An entry for writing a monitor event to a ring buffer.
 */
public class MonitorEventEntry {
    private long sequence;
    private ByteBuffer buffer;
    private long timestamp;
    private MonitorLevel level;
    private int destinationIndex;

    /**
     * Constructor.
     *
     * @param capacity the fixed event size in bytes
     */
    public MonitorEventEntry(int capacity) {
        buffer = ByteBuffer.allocateDirect(capacity);
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
     * Returns the buffer containing the event.
     *
     * @return the event buffer
     */
    public ByteBuffer getBuffer() {
        return buffer;
    }

    /**
     * Sets the buffer containing the event.
     *
     * @param buffer the event buffer
     */
    public void setBuffer(ByteBuffer buffer) {
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
