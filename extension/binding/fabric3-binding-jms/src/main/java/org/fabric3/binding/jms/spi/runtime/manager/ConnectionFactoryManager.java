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
package org.fabric3.binding.jms.spi.runtime.manager;

import java.util.Map;
import javax.jms.ConnectionFactory;

/**
 * Manages JMS connection factories. Implementations are responsible for registering connection factories provided by a JMS provider with the runtime
 * JTA transaction manager in a way specific to the latter. For example, a ConnectionFactoryManager may implement JMS connection and session pooling
 * specific to the transaction manager.
 */
public interface ConnectionFactoryManager {

    /**
     * Registers a connection factory.
     *
     * @param name    the connection factory name
     * @param factory the connection factory
     * @return the registered connection factory, which may be a wrapper
     * @throws FactoryRegistrationException if there is an error registering
     */
    ConnectionFactory register(String name, ConnectionFactory factory) throws FactoryRegistrationException;

    /**
     * Registers a connection factory.
     *
     * @param name       the connection factory name
     * @param factory    the connection factory
     * @param properties properties such as pooling configuration
     * @return the registered connection factory, which may be a wrapper
     * @throws FactoryRegistrationException if there is an error registering
     */
    ConnectionFactory register(String name, ConnectionFactory factory, Map<String, String> properties) throws FactoryRegistrationException;

    /**
     * Removes a registered connection factory.
     *
     * @param name the connection factory name
     * @return the unregistered connection factory
     * @throws FactoryRegistrationException if there is an error un-registering
     */
    ConnectionFactory unregister(String name) throws FactoryRegistrationException;

    /**
     * Returns the registered connection factory for the given name.
     *
     * @param name the name the connection factory was registered with
     * @return the connection factory or null if no factory for the name was registered
     */
    ConnectionFactory get(String name);

}
