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
package org.fabric3.binding.jms.runtime.resolver;

import java.util.Hashtable;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;

import org.fabric3.binding.jms.spi.common.ConnectionFactoryDefinition;
import org.fabric3.binding.jms.spi.common.DestinationDefinition;
import org.fabric3.binding.jms.spi.runtime.JmsResolutionException;

/**
 * Resolves administered objects, specifically connection factories and destinations. Different strategies may be used for resolution as defined by
 * {@link ConnectionFactoryDefinition} or {@link DestinationDefinition}.
 *
 * @version $Rev$ $Date$
 */
public interface AdministeredObjectResolver {

    /**
     * Resolves a ConnectionFactory.
     *
     * @param definition the connection factory definition
     * @param env        properties for use when resolving the ConnectionFactory.
     * @return the connection factory.
     * @throws JmsResolutionException if there is an error during resolution
     */
    ConnectionFactory resolve(ConnectionFactoryDefinition definition, Hashtable<String, String> env) throws JmsResolutionException;

    /**
     * Resolves a destination.
     *
     * @param definition the destination definition
     * @param factory    the connection factory
     * @param env        environment properties used during resolution
     * @return the destination
     * @throws JmsResolutionException if there is an error during resolution
     */
    Destination resolve(DestinationDefinition definition, ConnectionFactory factory, Hashtable<String, String> env) throws JmsResolutionException;

}
