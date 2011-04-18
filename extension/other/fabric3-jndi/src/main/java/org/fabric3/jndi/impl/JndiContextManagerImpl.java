/*
 * Fabric3
 * Copyright (c) 2009-2011 Metaform Systems
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
package org.fabric3.jndi.impl;

import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.Name;
import javax.naming.NameNotFoundException;
import javax.naming.NamingException;

import org.osoa.sca.annotations.Destroy;

import org.fabric3.api.annotation.monitor.Monitor;
import org.fabric3.jndi.spi.JndiContextManager;

/**
 * @version $Rev$ $Date$
 */
public class JndiContextManagerImpl implements JndiContextManager {
    private Map<String, Context> contexts = new ConcurrentHashMap<String, Context>();
    private ContextManagerMonitor monitor;

    public JndiContextManagerImpl(@Monitor ContextManagerMonitor monitor) {
        this.monitor = monitor;
    }

    @Destroy
    public void destroy() {
        for (Context context : contexts.values()) {
            try {
                context.close();
            } catch (NamingException e) {
                monitor.closeError(e);
            }
        }
    }

    public void register(String name, Properties properties) throws NamingException {
        ClassLoader old = Thread.currentThread().getContextClassLoader();
        try {
            Thread.currentThread().setContextClassLoader(getClass().getClassLoader());
            Context context = new InitialContext(properties);
            if (contexts.containsKey(name)) {
                throw new NamingException("Duplicate context: " + name);
            }
            contexts.put(name, context);
        } finally {
            Thread.currentThread().setContextClassLoader(old);
        }
    }

    public void unregister(String name) throws NamingException {
        Context context = contexts.remove(name);
        if (context != null) {
            context.close();
        }
    }

    public Context get(String name) {
        return contexts.get(name);
    }

    public <T> T lookup(Class<T> clazz, String name) throws NamingException {
        ClassLoader old = Thread.currentThread().getContextClassLoader();
        try {
            Thread.currentThread().setContextClassLoader(getClass().getClassLoader());
            for (Context context : contexts.values()) {
                try {
                    Object bound = context.lookup(name);
                    if (bound != null) {
                        return clazz.cast(bound);
                    }
                } catch (NameNotFoundException e) {
                    // ignore and continue
                }
            }
            return null;
        } finally {
            Thread.currentThread().setContextClassLoader(old);
        }
    }

    public <T> T lookup(Class<T> clazz, Name name) throws NamingException {
        ClassLoader old = Thread.currentThread().getContextClassLoader();
        try {
            Thread.currentThread().setContextClassLoader(getClass().getClassLoader());
            for (Context context : contexts.values()) {
                try {
                    Object bound = context.lookup(name);
                    if (bound != null) {
                        return clazz.cast(bound);
                    }
                } catch (NameNotFoundException e) {
                    // ignore and continue
                }
            }
            return null;
        } finally {
            Thread.currentThread().setContextClassLoader(old);
        }
    }

}
