/*
 * Fabric3
 * Copyright (c) 2009-2013 Metaform Systems
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
 * -----------------------------------------------------------------------
 *
 * Copyright 2001-2004 The Apache Software Foundation.
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
package org.apache.commons.logging;

import java.util.Hashtable;

import org.fabric3.monitor.log.clogging.CommonsLogFactory;

/**
 * This class exists to allow Fabric3 to replace Commons Logging with a direct bridge to its monitor subsystem.
 */
public abstract class LogFactory {

    static LogFactory logFactory = new CommonsLogFactory();

    public static final String PRIORITY_KEY = "priority";

    public static final String TCCL_KEY = "use_tccl";

    public static final String FACTORY_PROPERTY = "org.apache.commons.logging.LogFactory";

    public static final String FACTORY_DEFAULT = "org.apache.commons.logging.impl.SLF4JLogFactory";

    public static final String FACTORY_PROPERTIES = "commons-logging.properties";

    protected static final String SERVICE_ID = "META-INF/services/org.apache.commons.logging.LogFactory";

    public static final String DIAGNOSTICS_DEST_PROPERTY = "org.apache.commons.logging.diagnostics.dest";

    public static final String HASHTABLE_IMPLEMENTATION_PROPERTY = "org.apache.commons.logging.LogFactory.HashtableImpl";

    protected static Hashtable factories = null;

    protected static LogFactory nullClassLoaderFactory = null;

    protected LogFactory() {
    }

    public abstract Object getAttribute(String name);

    public abstract String[] getAttributeNames();

    public abstract Log getInstance(Class clazz) throws LogConfigurationException;

    public abstract Log getInstance(String name) throws LogConfigurationException;

    public abstract void release();

    public abstract void removeAttribute(String name);

    public abstract void setAttribute(String name, Object value);

    public static LogFactory getFactory() throws LogConfigurationException {
        return logFactory;
    }

    public static Log getLog(Class clazz) throws LogConfigurationException {
        return (getFactory().getInstance(clazz));
    }

    public static Log getLog(String name) throws LogConfigurationException {
        return (getFactory().getInstance(name));
    }

    public static void release(ClassLoader classLoader) {
    }

    public static void releaseAll() {
    }

    public static String objectId(Object o) {
        if (o == null) {
            return "null";
        } else {
            return o.getClass().getName() + "@" + System.identityHashCode(o);
        }
    }

    protected static Object createFactory(String factoryClass, ClassLoader classLoader) {
        throw new UnsupportedOperationException();
    }

    protected static ClassLoader directGetContextClassLoader() {
        throw new UnsupportedOperationException();
    }

    protected static ClassLoader getContextClassLoader() throws LogConfigurationException {
        throw new UnsupportedOperationException();
    }

    protected static ClassLoader getClassLoader(Class clazz) {
        throw new UnsupportedOperationException();
    }

    protected static boolean isDiagnosticsEnabled() {
        throw new UnsupportedOperationException();
    }

    protected static void logRawDiagnostic(String msg) {
        throw new UnsupportedOperationException();
    }

    protected static LogFactory newFactory(final String factoryClass, final ClassLoader classLoader, final ClassLoader contextClassLoader) {
        throw new UnsupportedOperationException();
    }

    protected static LogFactory newFactory(final String factoryClass, final ClassLoader classLoader) {
        throw new UnsupportedOperationException();
    }
}