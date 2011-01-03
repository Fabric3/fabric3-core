/*
 * Fabric3
 * Copyright (c) 2009-2011 Metaform Systems
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
package org.fabric3.monitor.model;

import org.fabric3.model.type.component.ResourceReferenceDefinition;
import org.fabric3.model.type.contract.ServiceContract;

/**
 * @version $Rev$ $Date$
 */
public class MonitorResourceReference extends ResourceReferenceDefinition {
    private static final long serialVersionUID = -6723752212878850748L;
    private String channelName;

    /**
     * Constructor that uses the default channel.
     *
     * @param name     the resource name
     * @param contract the service contract required of the resource
     */
    public MonitorResourceReference(String name, ServiceContract contract) {
        super(name, contract, false);
    }

    /**
     * Constructor.
     *
     * @param name        the resource name
     * @param contract    the service contract required of the resource
     * @param channelName the target channel to send monitor events
     */
    public MonitorResourceReference(String name, ServiceContract contract, String channelName) {
        super(name, contract, false);
        this.channelName = channelName;
    }


    /**
     * Returns the target channel to send monitor events or null if the channel is not specified and a default should be used.
     *
     * @return the target channel to send monitor events or null
     */
    public String getChannelName() {
        return channelName;
    }
}
