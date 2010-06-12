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
package org.fabric3.binding.jms.runtime.resolver.connectionfactory;

import java.util.Hashtable;
import java.util.List;
import javax.jms.ConnectionFactory;
import javax.naming.NameNotFoundException;
import javax.naming.NamingException;
import javax.naming.NoInitialContextException;

import org.osoa.sca.annotations.Reference;

import org.fabric3.binding.jms.runtime.resolver.ConnectionFactoryStrategy;
import org.fabric3.binding.jms.runtime.resolver.JndiHelper;
import org.fabric3.binding.jms.spi.common.ConnectionFactoryDefinition;
import org.fabric3.binding.jms.spi.runtime.ConnectionFactoryManager;
import org.fabric3.binding.jms.spi.runtime.FactoryRegistrationException;
import org.fabric3.binding.jms.spi.runtime.JmsResolutionException;
import org.fabric3.binding.jms.spi.runtime.ProviderConnectionFactoryResolver;

/**
 * Implementation that attempts to resolve a connection by searching the ConnectionFactoryManager, provider resolvers, JNDI and then, if not found,
 * creating it.
 */
public class IfNotExistConnectionFactoryStrategy implements ConnectionFactoryStrategy {
    private ConnectionFactoryStrategy always;
    private ConnectionFactoryManager manager;
    private List<ProviderConnectionFactoryResolver> resolvers;

    public IfNotExistConnectionFactoryStrategy(@Reference ConnectionFactoryManager manager) {
        this.always = new AlwaysConnectionFactoryStrategy(manager);
        this.manager = manager;
    }

    @Reference(required = false)
    public void setResolvers(List<ProviderConnectionFactoryResolver> resolvers) {
        this.resolvers = resolvers;
    }

    public ConnectionFactory getConnectionFactory(ConnectionFactoryDefinition definition, Hashtable<String, String> env)
            throws JmsResolutionException {
        String name = definition.getName();
        try {
            ConnectionFactory factory = manager.get(name);
            if (factory != null) {
                return factory;
            }
            for (ProviderConnectionFactoryResolver resolver : resolvers) {
                factory = resolver.resolve(definition);
                if (factory != null) {
                    manager.register(name, factory);
                    return factory;
                }
            }
            try {
                factory = (ConnectionFactory) JndiHelper.lookup(name, env);
            } catch (NoInitialContextException e) {
                factory = always.getConnectionFactory(definition, env);
            } catch (NameNotFoundException ex) {
                factory = always.getConnectionFactory(definition, env);
            }
            return manager.register(name, factory);
        } catch (FactoryRegistrationException e) {
            throw new JmsResolutionException("Error resolving connection factory: " + name, e);
        } catch (NamingException e) {
            throw new JmsResolutionException("Error resolving connection factory: " + name, e);
        }
    }

}
