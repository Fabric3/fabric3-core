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
package org.fabric3.api.node;

import java.net.URL;

/**
 * The main API for accessing a service fabric domain.
 */
public interface Domain {

    /**
     * Returns a proxy to the service with the given interface. The service may be local or remote depending on the deployment topology.
     *
     * @param interfaze the service interface.
     * @return the service
     */
    <T> T getService(Class<T> interfaze);

    /**
     * Returns a proxy to a channel.
     *
     * @param interfaze the channel interface
     * @param name      the channel name
     * @return the channel
     */
    <T> T getChannel(Class<T> interfaze, String name);

    /**
     * Subscribes to receive events from a channel.
     *
     * @param interfaze the channel interface
     * @param name      the channel name
     * @param consumer  the consumer
     */
    void subscribe(Class<?> interfaze, String name, Object consumer);

    /**
     * Deploys a service endpoint.
     *
     * @param interfaze the service endpoint
     * @param instance  the service instance
     */
    <T> void deploy(Class<T> interfaze, T instance);

    /**
     * Deploys a composite.
     *
     * @param composite the composite file
     */
    void deploy(URL composite);

}
