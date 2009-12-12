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
import java.net.URL;
import java.util.ResourceBundle;
import java.util.logging.Formatter;
import java.util.logging.Level;
import java.util.logging.LogRecord;

import org.apache.maven.plugin.logging.Log;

import org.fabric3.host.monitor.AbstractProxyMonitorFactory;
import org.fabric3.host.monitor.GenericFormatter;
import org.fabric3.host.monitor.MonitorDispatcher;

/**
 * @version $Rev$ $Date$
 */
public class MavenMonitorFactory extends AbstractProxyMonitorFactory {
    private Log log;
    private Formatter formatter = new GenericFormatter();

    public MavenMonitorFactory(Log log) {
        this.log = log;
    }

    public void readConfiguration(URL url) throws IOException {
        throw new UnsupportedOperationException();
    }

    protected MonitorDispatcher createDispatcher(Class<?> monitorInterface,
                                                 String methodName,
                                                 Level level,
                                                 ResourceBundle bundle,
                                                 int throwable) {
        return new MavenMonitorDispatcher(log, level, monitorInterface.getName(), methodName, bundle, throwable, formatter);
    }

    private class MavenMonitorDispatcher implements MonitorDispatcher {
        private Log log;
        private Level level;
        private String className;
        private String key;
        private ResourceBundle bundle;
        private int throwable;
        private Formatter formatter;

        private MavenMonitorDispatcher(Log log,
                                       Level level,
                                       String className,
                                       String methodName,
                                       ResourceBundle bundle,
                                       int throwable,
                                       Formatter formatter) {
            this.log = log;
            this.level = level;
            this.className = className;
            this.key = className + '#' + methodName;
            this.bundle = bundle;
            this.throwable = throwable;
            this.formatter = formatter;
        }

        public void invoke(Object[] args) {

            if (Level.SEVERE == level) {
                if (log.isErrorEnabled()) {
                    String message = createMessage((args));
                    log.error(message);
                }
            } else if (Level.WARNING == level) {
                if (log.isWarnEnabled()) {
                    String message = createMessage((args));
                    log.warn(message);
                }
            } else if (Level.INFO == level) {
                if (log.isInfoEnabled()) {
                    String message = createMessage((args));
                    log.info(message);
                }
            } else if (Level.FINE == level) {
                if (log.isDebugEnabled()) {
                    String message = createMessage((args));
                    log.debug(message);
                }
            } else if (Level.FINER == level) {
                if (log.isDebugEnabled()) {
                    String message = createMessage((args));
                    log.debug(message);
                }
            } else if (Level.FINEST == level) {
                if (log.isDebugEnabled()) {
                    String message = createMessage((args));
                    log.debug(message);
                }
            }
        }

        private String createMessage(Object args[]) {
            LogRecord logRecord = new LogRecord(level, key);
            logRecord.setLoggerName(className);
            logRecord.setParameters(args);
            if (args != null && throwable >= 0) {
                logRecord.setThrown((Throwable) args[throwable]);
            }
            logRecord.setResourceBundle(bundle);
            return formatter.format(logRecord);
        }
    }


}
