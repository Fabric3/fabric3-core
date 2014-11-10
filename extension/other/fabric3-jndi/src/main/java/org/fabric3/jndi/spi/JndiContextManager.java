/*
 * Fabric3
 * Copyright (c) 2009-2015 Metaform Systems
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
