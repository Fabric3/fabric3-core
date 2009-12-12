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
package org.fabric3.host.monitor;

import java.lang.ref.WeakReference;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.URI;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.WeakHashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

import org.fabric3.api.annotation.logging.LogLevels;

/**
 * Abstract factory for monitors based on JDK proxies.
 *
 * @version $Rev$ $Date$
 * @see java.util.logging
 */
public abstract class AbstractProxyMonitorFactory implements MonitorFactory {
    private Level defaultLevel = Level.FINEST;
    private String bundleName = "f3";
    private final Map<Class<?>, WeakReference<?>> proxies = new WeakHashMap<Class<?>, WeakReference<?>>();


    public <T> T getMonitor(Class<T> monitorInterface, URI componentId) {
        return getMonitor(monitorInterface);
    }

    public synchronized <T> T getMonitor(Class<T> monitorInterface) {
        T monitor = getCachedMonitor(monitorInterface);
        if (monitor == null) {
            monitor = createMonitor(monitorInterface);
            proxies.put(monitorInterface, new WeakReference<T>(monitor));
        }
        return monitor;
    }

    private <T> T getCachedMonitor(Class<T> monitorInterface) {
        WeakReference<?> ref = proxies.get(monitorInterface);
        return (ref != null) ? monitorInterface.cast(ref.get()) : null;
    }

    private <T> T createMonitor(Class<T> monitorInterface) {
        ResourceBundle bundle = locateBundle(monitorInterface, bundleName);
        Method[] methods = monitorInterface.getMethods();

        Map<Method, MonitorDispatcher> dispatchers = new ConcurrentHashMap<Method, MonitorDispatcher>(methods.length);

        for (Method method : methods) {
            String methodName = method.getName();
            LogLevels level = LogLevels.getAnnotatedLogLevel(method);
            int throwable = getExceptionParameterIndex(method);
            Level jdkLevel = translateLogLevel(level);
            MonitorDispatcher dispatcher = createDispatcher(monitorInterface, methodName, jdkLevel, bundle, throwable);
            dispatchers.put(method, dispatcher);
        }

        InvocationHandler handler = new LoggingHandler(dispatchers);
        Object proxy = Proxy.newProxyInstance(monitorInterface.getClassLoader(), new Class<?>[]{monitorInterface}, handler);
        return monitorInterface.cast(proxy);
    }

    protected abstract MonitorDispatcher createDispatcher(Class<?> monitorInterface,
                                                          String methodName,
                                                          Level level,
                                                          ResourceBundle bundle,
                                                          int throwable);

    private Level translateLogLevel(LogLevels level) {
        Level result;
        if (level == null) {
            result = defaultLevel;
        } else {
            try {
                //Because the LogLevels' values are based on the Level's logging levels,
                //no translation is required, just a pass-through mapping
                result = Level.parse(level.toString());
            } catch (IllegalArgumentException e) {
                //TODO: Add error reporting for unsupported log level
                result = defaultLevel;
            }
        }
        return result;
    }


    private int getExceptionParameterIndex(Method method) {
        int result = -1;
        for (int i = 0; i < method.getParameterTypes().length; i++) {
            Class<?> paramType = method.getParameterTypes()[i];
            if (Throwable.class.isAssignableFrom(paramType)) {
                result = i;
                break;
            }
        }

        //The position in the monitor interface's parameter list of the first throwable
        //is used when creating the LogRecord in the MonitorDispatcher
        return result;
    }

    private <T> ResourceBundle locateBundle(Class<T> monitorInterface, String bundleName) {
        Locale locale = Locale.getDefault();
        ClassLoader cl = monitorInterface.getClassLoader();
        String packageName = monitorInterface.getPackage().getName();
        while (true) {
            try {
                return ResourceBundle.getBundle(packageName + '.' + bundleName, locale, cl);
            } catch (MissingResourceException e) {
                //ok
            }
            int index = packageName.lastIndexOf('.');
            if (index == -1) {
                break;
            }
            packageName = packageName.substring(0, index);
        }
        try {
            return ResourceBundle.getBundle(bundleName, locale, cl);
        } catch (Exception e) {
            return null;
        }
    }

    private static class LoggingHandler implements InvocationHandler {
        private final Map<Method, MonitorDispatcher> dispatchers;

        public LoggingHandler(Map<Method, MonitorDispatcher> dispatchers) {
            this.dispatchers = dispatchers;
        }

        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            MonitorDispatcher dispatcher = dispatchers.get(method);
            if (dispatcher != null) {
                dispatcher.invoke(args);
            }
            return null;
        }
    }

}