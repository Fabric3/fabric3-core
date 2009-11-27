/*
 * Fabric3
 * Copyright (c) 2009 Metaform Systems
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
package org.fabric3.host.monitor;

import java.io.IOException;
import java.net.URI;
import java.net.URL;

/**
 * A MonitorFactory creates implementations of components' monitor interfaces that interface with a its monitoring scheme. For example, a
 * implementation may create versions that emit appropriate logging events or which send notifications to a management API.
 *
 * @version $Rev$ $Date$
 */
public interface MonitorFactory {
    /**
     * Return a monitor for a monitor interface.
     *
     * @param monitorInterface the monitoring interface
     * @return an implementation of the monitoring interface; will not be null
     */
    <T> T getMonitor(Class<T> monitorInterface);

    /**
     * Return a monitor for a component's monitor interface.
     *
     * @param monitorInterface the component's monitoring interface
     * @param componentId      the specific component to monitor
     * @return an implementation of the monitoring interface; will not be null
     */
    <T> T getMonitor(Class<T> monitorInterface, URI componentId);

    /**
     * Reads monitor congfiguration from an external source.
     *
     * @param url the configuration URL
     * @throws IOException if the configuration cannot be read
     */
    void readConfiguration(URL url) throws IOException;

}
