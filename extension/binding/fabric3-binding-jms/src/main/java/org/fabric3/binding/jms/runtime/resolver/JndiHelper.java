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
 *
 * ----------------------------------------------------
 *
 * Portions originally based on Apache Tuscany 2007
 * licensed under the Apache 2.0 license.
 *
 */
package org.fabric3.binding.jms.runtime.resolver;

import java.util.Hashtable;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

/**
 * Performs JNDI resolution.
 *
 * @version $Revision$ $Date$
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
