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
package org.fabric3.monitor.spi.destination;

import java.io.IOException;

import org.fabric3.api.annotation.monitor.MonitorLevel;
import org.fabric3.monitor.spi.event.MonitorEventEntry;

/**
 * Manages and dispatches to {@link MonitorDestination}s.
 */
public interface MonitorDestinationRegistry {

    /**
     * Registers a {@link MonitorDestination}.
     *
     * @param destination the destination
     */
    void register(MonitorDestination destination);

    /**
     * Un-registers a monitor destination corresponding to the given name.
     *
     * @param name the destination name
     * @return the un-registered name
     */
    MonitorDestination unregister(String name);

    /**
     * Returns the index for the destination corresponding to the given name.
     *
     * @param name the destination name
     * @return the index
     */
    int getIndex(String name);

    /**
     * Dispatches the entry to a destination.
     *
     * @param entry the entry
     * @throws IOException if there is a dispatch error
     */
    void write(MonitorEventEntry entry) throws IOException;

    /**
     * Dispatches event data to a destination.
     *
     * @param index     the destination index
     * @param level     the monitor level to write
     * @param timestamp the timestamp
     * @param source    the event source
     * @param args      the arguments
     * @throws IOException if there is a dispatch error
     */
    void write(int index, MonitorLevel level, long timestamp, String source, String template, Object... args) throws IOException;

}
