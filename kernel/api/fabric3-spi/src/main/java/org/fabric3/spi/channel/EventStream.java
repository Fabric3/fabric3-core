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
package org.fabric3.spi.channel;

import org.fabric3.spi.model.physical.PhysicalEventStreamDefinition;

/**
 * Transmits events to a channel. As events are transmitted through the stream, they are processed by a series of handlers.
 */
public interface EventStream {

    /**
     * Returns the physical event stream metadata.
     *
     * @return the physical event stream metadata
     */
    PhysicalEventStreamDefinition getDefinition();

    /**
     * Returns the head handler for the stream.
     *
     * @return the head handler for the stream
     */
    EventStreamHandler getHeadHandler();

    /**
     * Returns the tail handler for the stream.
     *
     * @return the tail handler for the stream
     */
    EventStreamHandler getTailHandler();

    /**
     * Adds a handler to the stream.
     *
     * @param handler the handler to add
     */
    void addHandler(EventStreamHandler handler);

    /**
     * Adds a handler to the stream at the given index.
     *
     * @param index   the location where to add the handler
     * @param handler the handler to add
     */
    void addHandler(int index, EventStreamHandler handler);

}
