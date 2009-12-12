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
package org.fabric3.monitor.impl;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import org.osoa.sca.annotations.Reference;

import org.fabric3.host.monitor.AbstractProxyMonitorFactory;
import org.fabric3.host.monitor.MonitorDispatcher;

/**
 * A factory for monitors that forwards events to a {@link Logger Java Logging (JSR47) Logger}.
 *
 * @version $Rev$ $Date$
 * @see java.util.logging
 */
public class JavaLoggingMonitorFactory extends AbstractProxyMonitorFactory {
    private Formatter formatter = new Fabric3LogFormatter();

    public <T> T getMonitor(Class<T> monitorInterface, URI componentId) {
        return getMonitor(monitorInterface);
    }


    @Reference(required = false)
    public void setFormatter(Formatter formatter) {
        this.formatter = formatter;
    }

    @SuppressWarnings({"unchecked"})
    public void readConfiguration(URL url) throws IOException {
        LogManager manager = LogManager.getLogManager();
        InputStream stream = url.openStream();
        manager.readConfiguration(stream);
        String formatterClass = manager.getProperty("fabric3.jdkLogFormatter");
        if (formatterClass != null) {
            try {
                Class<Formatter> clazz = (Class<Formatter>) Class.forName(formatterClass);
                formatter = clazz.newInstance();
            } catch (ClassNotFoundException e) {
                throw new IllegalArgumentException("Invalid formatter class", e);
            } catch (IllegalAccessException e) {
                throw new IllegalArgumentException("Invalid formatter class", e);
            } catch (InstantiationException e) {
                throw new IllegalArgumentException("Invalid formatter class", e);
            }
        }
    }

    protected MonitorDispatcher createDispatcher(Class<?> monitorInterface,
                                                 String methodName,
                                                 Level level,
                                                 ResourceBundle bundle,
                                                 int throwable) {
        String className = monitorInterface.getName();
        Logger logger = Logger.getLogger(className);
        setFormatter(logger, formatter);

        return new JavaMonitorDispatcher(logger, level, methodName, bundle, throwable);
    }


    private void setFormatter(Logger logger, Formatter formatter) {
        if (formatter != null) {
            Logger parent = logger.getParent();
            if (parent != null && logger.getUseParentHandlers()) {
                setFormatter(parent, formatter);
            } else {
                for (Handler handler : logger.getHandlers()) {
                    handler.setFormatter(formatter);
                }
            }
        }
    }


    private static class JavaMonitorDispatcher implements MonitorDispatcher {
        private final Logger logger;
        private String className;
        private String key;
        private final Level level;
        private final ResourceBundle bundle;
        private final int throwable;

        private JavaMonitorDispatcher(Logger logger, Level level, String methodName, ResourceBundle bundle, int throwable) {
            this.logger = logger;
            this.className = logger.getName();

            // construct the key for the resource bundle
            this.key = className + '#' + methodName;

            this.level = level;
            this.bundle = bundle;
            this.throwable = throwable;
        }

        public void invoke(Object[] args) {
            if (level == null || !logger.isLoggable(level)) {
                return;
            }

            LogRecord logRecord = new LogRecord(level, key);
            logRecord.setLoggerName(className);
            logRecord.setParameters(args);
            if (args != null && throwable >= 0) {
                logRecord.setThrown((Throwable) args[throwable]);
            }
            logRecord.setResourceBundle(bundle);
            logger.log(logRecord);
        }
    }

}