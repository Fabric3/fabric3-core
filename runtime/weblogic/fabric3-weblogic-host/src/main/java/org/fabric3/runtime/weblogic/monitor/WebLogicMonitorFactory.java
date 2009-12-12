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
package org.fabric3.runtime.weblogic.monitor;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import weblogic.logging.LoggingHelper;
import weblogic.logging.WLLevel;

import org.fabric3.host.monitor.AbstractProxyMonitorFactory;
import org.fabric3.host.monitor.MonitorDispatcher;

/**
 * A MonitorFactory that forwards events to the WebLogic logging service.
 *
 * @version $Rev$ $Date$
 */
public class WebLogicMonitorFactory extends AbstractProxyMonitorFactory {

    public void readConfiguration(URL url) throws IOException {

    }

    protected MonitorDispatcher createDispatcher(Class<?> monitorInterface,
                                                 String methodName,
                                                 Level level,
                                                 ResourceBundle bundle,
                                                 int throwable) {
        String name = monitorInterface.getName();
        return new WebLogicMonitorDispatcher(level, name, methodName, bundle, throwable);
    }

    private static class WebLogicMonitorDispatcher implements MonitorDispatcher {
        private final Level level;
        private final ResourceBundle bundle;
        private final int throwable;
        private Logger logger;
        private String key;

        private WebLogicMonitorDispatcher(Level level, String className, String methodName, ResourceBundle bundle, int throwable) {
            this.level = level;
            this.bundle = bundle;
            this.throwable = throwable;
            // construct the key for the resource bundle
            this.key = className + '#' + methodName;
            logger = LoggingHelper.getServerLogger();
        }

        public void invoke(Object[] args) {
            if (level == null || !logger.isLoggable(level)) {
                return;
            }

            LogRecord logRecord = new LogRecord(level, key);
            // fixme this should be the application name
            logRecord.setLoggerName("fabric3");
            logRecord.setParameters(args);
            if (args != null && throwable >= 0) {
                logRecord.setThrown((Throwable) args[throwable]);
            }
            logRecord.setResourceBundle(bundle);
            if (Level.INFO == level) {
                // convert INFO to notice so it is displayed by default
                logRecord.setLevel(WLLevel.NOTICE);
            }
            logger.log(logRecord);
        }
    }
}