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
     * Returns the sequence number or -1 if not defined.
     *
     * @return the sequence number or -1
     */
    long getSequence();

}
