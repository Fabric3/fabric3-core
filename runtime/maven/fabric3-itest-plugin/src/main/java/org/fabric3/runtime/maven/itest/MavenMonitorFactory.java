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
package org.fabric3.runtime.maven.itest;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.URI;
import java.net.URL;
import java.text.MessageFormat;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.WeakHashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

import org.apache.maven.plugin.logging.Log;

import org.fabric3.api.annotation.logging.LogLevels;
import org.fabric3.host.monitor.MonitorFactory;

/**
 * @version $Rev$ $Date$
 */
public class MavenMonitorFactory implements MonitorFactory {
    private final Log log;
    private String bundleName;
    private Level defaultLevel;
    private final Map<Class<?>, WeakReference<?>> proxies = new WeakHashMap<Class<?>, WeakReference<?>>();

    public MavenMonitorFactory(Log log, String bundleName) {
        this.log = log;
        this.bundleName = bundleName;
        this.defaultLevel = Level.FINEST;
    }

    public void readConfiguration(URL url) throws IOException {
        throw new UnsupportedOperationException();
    }

    public synchronized <T> T getMonitor(Class<T> monitorInterface, URI componentId) {
        return getMonitor(monitorInterface);
    }

    public synchronized <T> T getMonitor(Class<T> monitorInterface) {
        T proxy = getCachedMonitor(monitorInterface);
        if (proxy == null) {
            proxy = createMonitor(monitorInterface);
            proxies.put(monitorInterface, new WeakReference<T>(proxy));
        }
        return proxy;
    }

    protected <T> ResourceBundle locateBundle(Class<T> monitorInterface, String bundleName) {
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

    protected <T> T getCachedMonitor(Class<T> monitorInterface) {
        WeakReference<?> ref = proxies.get(monitorInterface);
        return (ref != null) ? monitorInterface.cast(ref.get()) : null;
    }

    protected <T> T createMonitor(Class<T> monitorInterface) {
        Method[] methods = monitorInterface.getMethods();
        Map<Method, MethodHandler> handlers = new ConcurrentHashMap<Method, MethodHandler>(methods.length);
        for (Method method : methods) {

            LogLevels level = LogLevels.getAnnotatedLogLevel(method);
            int value = translateLogLevel(level).intValue();
            int throwable = getExceptionParameterIndex(method);

            String message = getMessage(monitorInterface, method);

            MethodHandler handler = null;
            if (throwable == -1) {
                if (value >= Level.SEVERE.intValue()) {
                    handler = new ErrorMessageHandler(log, message);
                } else if (value >= Level.WARNING.intValue()) {
                    handler = new WarnMessageHandler(log, message);
                } else if (value >= Level.INFO.intValue()) {
                    handler = new InfoMessageHandler(log, message);
                } else if (value >= Level.FINEST.intValue()) {
                    handler = new DebugMessageHandler(log, message);
                }
            } else {
                if (value >= Level.SEVERE.intValue()) {
                    handler = new ErrorExceptionHandler(log, message, throwable);
                } else if (value >= Level.WARNING.intValue()) {
                    handler = new WarnExceptionHandler(log, message, throwable);
                } else if (value >= Level.INFO.intValue()) {
                    handler = new InfoExceptionHandler(log, message, throwable);
                } else if (value >= Level.FINEST.intValue()) {
                    handler = new DebugExceptionHandler(log, message, throwable);
                }
            }
            handlers.put(method, handler);
        }

        InvocationHandler handler = new Handler(handlers);
        Object proxy = Proxy.newProxyInstance(monitorInterface.getClassLoader(),
                                              new Class<?>[]{monitorInterface},
                                              handler);
        return monitorInterface.cast(proxy);
    }

    protected String getMessage(Class<?> monitorInterface, Method method) {
        ResourceBundle bundle = locateBundle(monitorInterface, bundleName);
        String key = monitorInterface.getName() + '#' + method.getName();
        String message = null;
        if (bundle != null) {
            try {
                message = bundle.getString(key);
            } catch (MissingResourceException e) {
                // drop through
            }
        }
        if (message == null) {
            StringBuilder builder = new StringBuilder();
            builder.append(key);
            int argCount = method.getParameterTypes().length;
            if (argCount > 0) {
                builder.append(": {0}");
                for (int i = 1; i < argCount; i++) {
                    builder.append(' ');
                    builder.append('{');
                    builder.append(Integer.toString(i));
                    builder.append('}');
                }
            }
            message = builder.toString();
        }
        return message;
    }

    private Level translateLogLevel(LogLevels level) {
        Level result = null;
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

        return result;
    }

    private static class Handler implements InvocationHandler {
        private final Map<Method, MethodHandler> handlers;

        private Handler(Map<Method, MethodHandler> handlers) {
            this.handlers = handlers;
        }

        public Object invoke(Object o, Method method, Object[] objects) throws Throwable {
            MethodHandler handler = handlers.get(method);
            if (handler != null) {
                handler.invoke(objects);
            }
            return null;
        }
    }

    private abstract static class MethodHandler {
        protected final Log log;
        protected final String message;

        protected MethodHandler(Log log, String message) {
            this.log = log;
            this.message = message;
        }

        protected String getMessage(Object[] objects) {
            return MessageFormat.format(message, objects);
        }

        protected abstract void invoke(Object[] objects);

        protected abstract boolean isEnabled();
    }

    private abstract static class MessageHandler extends MethodHandler {
        protected MessageHandler(Log log, String message) {
            super(log, message);
        }

        protected void invoke(Object[] objects) {
            if (isEnabled()) {
                log(getMessage(objects));
            }
        }

        protected abstract void log(String message);
    }

    private abstract static class ExceptionHandler extends MethodHandler {
        private final int throwable;

        protected ExceptionHandler(Log log, String message, int throwable) {
            super(log, message);
            this.throwable = throwable;
        }

        protected void invoke(Object[] objects) {
            if (isEnabled()) {
                Throwable cause = (Throwable) objects[throwable];
                log(getMessage(objects), cause);
            }
        }

        protected abstract void log(String message, Throwable cause);
    }

    private static class ErrorMessageHandler extends MessageHandler {
        private ErrorMessageHandler(Log log, String message) {
            super(log, message);
        }

        protected boolean isEnabled() {
            return log.isErrorEnabled();
        }

        protected void log(String message) {
            log.error(message);
        }
    }

    private static class WarnMessageHandler extends MessageHandler {
        private WarnMessageHandler(Log log, String message) {
            super(log, message);
        }

        protected boolean isEnabled() {
            return log.isWarnEnabled();
        }

        protected void log(String message) {
            log.warn(message);
        }
    }

    private static class InfoMessageHandler extends MessageHandler {
        private InfoMessageHandler(Log log, String message) {
            super(log, message);
        }

        protected boolean isEnabled() {
            return log.isInfoEnabled();
        }

        protected void log(String message) {
            log.info(message);
        }
    }

    private static class DebugMessageHandler extends MessageHandler {
        private DebugMessageHandler(Log log, String message) {
            super(log, message);
        }

        protected boolean isEnabled() {
            return log.isDebugEnabled();
        }

        protected void log(String message) {
            log.debug(message);
        }
    }

    private static class ErrorExceptionHandler extends ExceptionHandler {
        private ErrorExceptionHandler(Log log, String message, int throwable) {
            super(log, message, throwable);
        }

        protected void log(String message, Throwable cause) {
            log.error(message, cause);
        }

        protected boolean isEnabled() {
            return log.isErrorEnabled();
        }
    }

    private static class WarnExceptionHandler extends ExceptionHandler {
        private WarnExceptionHandler(Log log, String message, int throwable) {
            super(log, message, throwable);
        }

        protected void log(String message, Throwable cause) {
            log.warn(message, cause);
        }

        protected boolean isEnabled() {
            return log.isWarnEnabled();
        }
    }

    private static class InfoExceptionHandler extends ExceptionHandler {
        private InfoExceptionHandler(Log log, String message, int throwable) {
            super(log, message, throwable);
        }

        protected void log(String message, Throwable cause) {
            log.info(message, cause);
        }

        protected boolean isEnabled() {
            return log.isInfoEnabled();
        }
    }

    private static class DebugExceptionHandler extends ExceptionHandler {
        private DebugExceptionHandler(Log log, String message, int throwable) {
            super(log, message, throwable);
        }

        protected void log(String message, Throwable cause) {
            log.debug(message, cause);
        }

        protected boolean isEnabled() {
            return log.isDebugEnabled();
        }
    }

}
