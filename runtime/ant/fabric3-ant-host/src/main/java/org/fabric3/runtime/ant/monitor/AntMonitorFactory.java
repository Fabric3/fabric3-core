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
package org.fabric3.runtime.ant.monitor;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.URI;
import java.net.URL;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.WeakHashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Formatter;
import java.util.logging.Level;
import java.util.logging.LogRecord;

import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;

import org.fabric3.api.annotation.logging.LogLevels;
import org.fabric3.host.monitor.MonitorFactory;

/**
 * A MonitorFactory that forwards events to the Ant logger.
 *
 * @version $Rev$ $Date$
 */
public class AntMonitorFactory implements MonitorFactory {
    private Task task;
    private Level defaultLevel = Level.FINEST;
    private String bundleName = "f3";
    private final Map<Class<?>, WeakReference<?>> proxies = new WeakHashMap<Class<?>, WeakReference<?>>();
    private Formatter formatter = new AntFormatter();

    public AntMonitorFactory(Task task) {
        this.task = task;
    }

    public <T> T getMonitor(Class<T> monitorInterface, URI componentId) {
        return getMonitor(monitorInterface);
    }

    @SuppressWarnings({"unchecked"})
    public void readConfiguration(URL url) throws IOException {
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
        String className = monitorInterface.getName();
        ResourceBundle bundle = locateBundle(monitorInterface, bundleName);

        Method[] methods = monitorInterface.getMethods();
        Map<Method, MethodInfo> methodInfo = new ConcurrentHashMap<Method, MethodInfo>(methods.length);
        for (Method method : methods) {
            String methodName = method.getName();

            LogLevels level = LogLevels.getAnnotatedLogLevel(method);
            Level methodLevel = translateLogLevel(level);
            int throwable = getExceptionParameterIndex(method);

            MethodInfo info = new MethodInfo(task, methodLevel, className, methodName, bundle, throwable, formatter);
            methodInfo.put(method, info);
        }

        InvocationHandler handler = new LoggingHandler(methodInfo);
        Object proxy = Proxy.newProxyInstance(monitorInterface.getClassLoader(), new Class<?>[]{monitorInterface}, handler);
        return monitorInterface.cast(proxy);
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
        //is used when creating the LogRecord in the MethodInfo
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

    private static class MethodInfo {
        private final Task task;
        private final Level level;
        private String className;
        private final String methodName;
        private final ResourceBundle bundle;
        private final int throwable;
        private Formatter formatter;

        private MethodInfo(Task task,
                           Level level,
                           String className,
                           String methodName,
                           ResourceBundle bundle,
                           int throwable,
                           Formatter formatter) {
            this.task = task;
            this.level = level;
            this.className = className;
            this.methodName = methodName;
            this.bundle = bundle;
            this.throwable = throwable;
            this.formatter = formatter;
        }

        private void invoke(Object[] args) {

            // construct the key for the resource bundle
            String key = className + '#' + methodName;

            LogRecord logRecord = new LogRecord(level, key);
            logRecord.setLoggerName(className);
            logRecord.setParameters(args);
            if (args != null && throwable >= 0) {
                logRecord.setThrown((Throwable) args[throwable]);
            }
            logRecord.setResourceBundle(bundle);
            String message = formatter.format(logRecord);
            int antLevel = Project.MSG_DEBUG;

            if (Level.SEVERE == level) {
                antLevel = Project.MSG_ERR;
            } else if (Level.WARNING == level) {
                antLevel = Project.MSG_WARN;
            } else if (Level.INFO == level) {
                antLevel = Project.MSG_INFO;
            } else if (Level.FINE == level) {
                antLevel = Project.MSG_DEBUG;
            } else if (Level.FINER == level) {
                antLevel = Project.MSG_VERBOSE;
            } else if (Level.FINEST == level) {
                antLevel = Project.MSG_VERBOSE;
            }
            task.getProject().log(task, message, antLevel);
        }
    }

    private static class LoggingHandler implements InvocationHandler {
        private final Map<Method, MethodInfo> info;

        public LoggingHandler(Map<Method, MethodInfo> methodInfo) {
            this.info = methodInfo;
        }

        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            MethodInfo methodInfo = info.get(method);
            if (methodInfo != null) {
                methodInfo.invoke(args);
            }
            return null;
        }
    }

}