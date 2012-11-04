/*
 * Fabric3
 * Copyright (c) 2009-2012 Metaform Systems
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
package org.fabric3.jndi.spi;

import java.util.Properties;
import javax.naming.Context;
import javax.naming.Name;
import javax.naming.NamingException;

/**
 * Manages JNDI context resources and provides aggregated resolution operations across those managed contexts.
 */
public interface JndiContextManager {

    /**
     * Registers and instantiates a JNDI context.
     *
     * @param name       the unique name identifying the context
     * @param properties the properties used to instantiate the context
     * @throws NamingException if the context cannot be instantiated
     */
    void register(String name, Properties properties) throws NamingException;

    /**
     * Closes and and removes a JNDI context.
     *
     * @param name the unique name identifying the context
     * @throws NamingException if the context cannot be removed
     */
    void unregister(String name) throws NamingException;

    /**
     * Returns the registered context for the given name or null.
     *
     * @param name the context name
     * @return the registered context for the given name or null
     */
    Context get(String name);

    /**
     * Performs a JNDI lookup across all registered contexts.
     *
     * @param clazz the type of the bound object
     * @param name  the bound object name
     * @param <T>   the generic representing the bound object type
     * @return the bound object or null if not found
     * @throws NamingException if there is a resolution error
     */
    <T> T lookup(Class<T> clazz, String name) throws NamingException;

    /**
     * Performs a JNDI lookup across all registered contexts for the given JNDI name.
     *
     * @param clazz the type of the bound object
     * @param name  the bound object name
     * @param <T>   the generic representing the bound object type
     * @return the bound object or null if not found
     * @throws NamingException if there is a resolution error
     */
    <T> T lookup(Class<T> clazz, Name name) throws NamingException;

}
