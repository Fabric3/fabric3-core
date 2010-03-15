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
package org.fabric3.runtime.weblogic.jms.runtime;

import javax.jms.ConnectionFactory;
import javax.naming.InitialContext;
import javax.naming.NameNotFoundException;
import javax.naming.NamingException;

import org.fabric3.binding.jms.spi.common.ConnectionFactoryDefinition;
import org.fabric3.binding.jms.spi.runtime.JmsConstants;
import org.fabric3.binding.jms.spi.runtime.JmsResolutionException;
import org.fabric3.binding.jms.spi.runtime.ProviderConnectionFactoryResolver;

/**
 * Resolves connection factories against the local runtime JNDI tree.
 * <p/>
 * Note this implementation requires connection factories be targeted to all WebLogic instances in the cluster.
 *
 * @version $Rev$ $Date$
 */
public class WebLogicConnectionFactoryResolver implements ProviderConnectionFactoryResolver {

    public ConnectionFactory resolve(ConnectionFactoryDefinition definition) throws JmsResolutionException {
        String location = getConnectionFactoryLocation(definition);
        InitialContext context = null;
        try {
            context = new InitialContext();
            return (ConnectionFactory) context.lookup(location);
        } catch (NameNotFoundException e) {
            return null;
        } catch (NamingException e) {
            throw new JmsResolutionException("Error resolving connection factory: " + location, e);
        } finally {
            if (context != null) {
                try {
                    context.close();
                } catch (NamingException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * Returns the JNDI location for the connection factory definition. If default XA or non-XA connection factories are specified, the WebLogic
     * defaults will be returned.
     *
     * @param definition the connection factory location.
     * @return the JNDI location of the connection factory
     */
    private String getConnectionFactoryLocation(ConnectionFactoryDefinition definition) {
        String name = definition.getName();
        if (JmsConstants.DEFAULT_XA_CONNECTION_FACTORY.equals(name)) {
            name = "weblogic.jms.XAConnectionFactory";
        } else if (JmsConstants.DEFAULT_CONNECTION_FACTORY.equals(name)) {
            name = "weblogic.jms.ConnectionFactory";
        }
        // the name is the connection factory
        return name;
    }

}