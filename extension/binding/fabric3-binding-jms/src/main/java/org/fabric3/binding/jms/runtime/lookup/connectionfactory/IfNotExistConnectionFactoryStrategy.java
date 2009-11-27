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
package org.fabric3.binding.jms.runtime.lookup.connectionfactory;

import java.util.Collections;
import java.util.Hashtable;
import javax.jms.ConnectionFactory;
import javax.naming.Context;
import javax.naming.NameNotFoundException;

import org.osoa.sca.annotations.Reference;

import org.fabric3.binding.jms.common.ConnectionFactoryDefinition;
import org.fabric3.binding.jms.runtime.lookup.ConnectionFactoryStrategy;
import org.fabric3.binding.jms.runtime.lookup.JmsLookupException;
import org.fabric3.binding.jms.runtime.lookup.JndiHelper;
import org.fabric3.binding.jms.spi.runtime.factory.ConnectionFactoryManager;
import org.fabric3.binding.jms.spi.runtime.factory.FactoryRegistrationException;

/**
 * Implementation that attempts to resolve a connection by searching the ConnectionFactoryRegistry, then JNDI and then, if not found, creating it.
 */
public class IfNotExistConnectionFactoryStrategy implements ConnectionFactoryStrategy {
    private ConnectionFactoryStrategy always;
    private ConnectionFactoryManager manager;

    public IfNotExistConnectionFactoryStrategy(@Reference ConnectionFactoryManager manager) {
        this.always = new AlwaysConnectionFactoryStrategy(manager);
        this.manager = manager;
    }

    public ConnectionFactory getConnectionFactory(ConnectionFactoryDefinition definition, Hashtable<String, String> env) throws JmsLookupException {
        String name = definition.getName();
        try {
            ConnectionFactory factory = manager.get(name);
            if (factory != null) {
                return factory;
            }
            if (!env.contains(Context.INITIAL_CONTEXT_FACTORY)) {
                // java.naming.factory.initial is not defined, resort to creating
                factory = always.getConnectionFactory(definition, env);
                return manager.register(name, factory, Collections.<String, String>emptyMap());
            }
            factory = (ConnectionFactory) JndiHelper.lookup(name, env);
            return manager.register(name, factory, Collections.<String, String>emptyMap());
        } catch (NameNotFoundException ex) {
            return always.getConnectionFactory(definition, env);
        } catch (FactoryRegistrationException e) {
            throw new JmsLookupException("Unable to lookup: " + name, e);
        }

    }

}
