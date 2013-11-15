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
package org.fabric3.monitor.spi.writer;

import org.fabric3.api.annotation.monitor.MonitorLevel;
import org.fabric3.monitor.spi.buffer.ResizableByteBuffer;
import org.fabric3.monitor.spi.event.MonitorEventEntry;

/**
 * Writes monitor events to a buffer.
 */
public interface EventWriter {

    /**
     * Writes the event in character form into the buffer.
     *
     * @param level     the monitor level
     * @param timestamp the timestamp
     * @param template  the template
     * @param buffer    the buffer to write into
     * @param args      the arguments
     */
    void write(MonitorLevel level, long timestamp, String template, ResizableByteBuffer buffer, Object[] args);

    /**
     * Writes the event in character form into the entry buffer.
     *
     * @param entry the entry
     * @return the number of bytes written
     */
    int writeTemplate(MonitorEventEntry entry);

    /**
     * Writes the monitor even prefix characters.
     *
     * @param level          the monitor level to write
     * @param entryTimestamp the timestamp
     * @param buffer         the buffer to write into
     * @return the number of bytes written
     */
    int writePrefix(MonitorLevel level, long entryTimestamp, ResizableByteBuffer buffer);
}
