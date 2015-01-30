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
package org.fabric3.jndi.impl;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.Name;
import javax.naming.NameNotFoundException;
import javax.naming.NamingException;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

import org.fabric3.api.annotation.monitor.Monitor;
import org.fabric3.jndi.spi.JndiContextManager;
import org.oasisopen.sca.annotation.Destroy;

/**
 *
 */
public class JndiContextManagerImpl implements JndiContextManager {
    private Map<String, Context> contexts = new ConcurrentHashMap<>();
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
