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
 *
 * Portions originally based on Apache Tuscany 2007
 * licensed under the Apache 2.0 license.
 */
package org.fabric3.binding.jms.runtime.resolver;

import java.util.Hashtable;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

/**
 * Performs JNDI resolution.
 */
public class JndiHelper {

    private JndiHelper() {
    }

    /**
     * Looks up the administered object in JNDI.
     *
     * @param name the object name
     * @param env  environment properties
     * @return the object
     * @throws NamingException if there was an error looking up the object. NameNotFoundException will be thrown if the object is not found in the
     *                         JNDI tree.
     */
    public static Object lookup(String name, Hashtable<String, String> env) throws NamingException {
        ClassLoader oldCl = Thread.currentThread().getContextClassLoader();
        Context ctx = null;
        try {
            Thread.currentThread().setContextClassLoader(JndiHelper.class.getClassLoader());
            ctx = new InitialContext(env);
            return ctx.lookup(name);
        } finally {
            Thread.currentThread().setContextClassLoader(oldCl);
            try {
                if (ctx != null) {
                    ctx.close();
                }
            } catch (NamingException e) {
                e.printStackTrace();
            }
        }
    }
}
