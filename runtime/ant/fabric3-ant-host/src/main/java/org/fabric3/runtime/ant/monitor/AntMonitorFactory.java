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
import java.net.URI;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.logging.Formatter;
import java.util.logging.Level;
import java.util.logging.LogRecord;

import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;

import org.fabric3.host.monitor.AbstractProxyMonitorFactory;
import org.fabric3.host.monitor.GenericFormatter;
import org.fabric3.host.monitor.MonitorDispatcher;

/**
 * A MonitorFactory that forwards events to the Ant logger.
 *
 * @version $Rev$ $Date$
 */
public class AntMonitorFactory extends AbstractProxyMonitorFactory {
    private Task task;
    private Formatter formatter = new GenericFormatter();

    public AntMonitorFactory(Task task) {
        this.task = task;
    }

    public <T> T getMonitor(Class<T> monitorInterface, URI componentId) {
        return getMonitor(monitorInterface);
    }

    public void readConfiguration(URL url) throws IOException {
    }

    protected MonitorDispatcher createDispatcher(Class<?> monitorInterface,
                                                 String methodName,
                                                 Level level,
                                                 ResourceBundle bundle,
                                                 int throwable) {
        String name = monitorInterface.getName();
        return new AntMonitorDispatcher(task, level, name, methodName, bundle, throwable, formatter);
    }


    private static class AntMonitorDispatcher implements MonitorDispatcher {
        private final Task task;
        private final Level level;
        private String className;
        private final ResourceBundle bundle;
        private final int throwable;
        private Formatter formatter;
        private String key;

        private AntMonitorDispatcher(Task task,
                                     Level level,
                                     String className,
                                     String methodName,
                                     ResourceBundle bundle,
                                     int throwable,
                                     Formatter formatter) {
            this.task = task;
            this.level = level;
            this.className = className;
            this.bundle = bundle;
            this.throwable = throwable;
            this.formatter = formatter;
            // construct the key for the resource bundle
            this.key = className + '#' + methodName;
        }

        public void invoke(Object[] args) {

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

}