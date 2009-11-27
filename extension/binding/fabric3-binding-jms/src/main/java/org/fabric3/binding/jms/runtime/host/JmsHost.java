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
*/

package org.fabric3.binding.jms.runtime.host;

import java.net.URI;
import javax.jms.JMSException;

/**
 * Provisions MessageListeners that dispatch to service endpoints with the underlying JMS infrastructure.
 *
 * @version $Rev$ $Date$
 */
public interface JmsHost {

    /**
     * Returns true if a listener for the service URI is registered.
     *
     * @param serviceUri the service URI
     * @return true if a listener is registered
     */
    boolean isRegistered(URI serviceUri);

    /**
     * Register a MessageListener which dispatches inbound JMS messages to a service.
     *
     * @param configuration the configuration for the message listener to register
     * @throws JMSException if an error registering the listener is encountered
     */
    public void register(ListenerConfiguration configuration) throws JMSException;

    /**
     * Unregister the MessageListener for the given service
     *
     * @param serviceUri the service URI
     * @throws JMSException if an error unregistering the listener is encountered
     */
    public void unregister(URI serviceUri) throws JMSException;

}
