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

import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import org.w3c.dom.Element;
import weblogic.logging.LoggingHelper;
import weblogic.logging.WLLevel;

import org.fabric3.host.monitor.MonitorEvent;
import org.fabric3.host.monitor.MonitorEventDispatcher;
import org.fabric3.api.annotation.monitor.MonitorLevel;

/**
 * A dispatcher that forwards events to the WebLogic logging service.
 *
 * @version $Rev: 8394 $ $Date: 2009-12-12 11:20:05 +0100 (Sat, 12 Dec 2009) $
 */
public class WebLogicMonitorEventDispatcher implements MonitorEventDispatcher {
    private Logger logger;

    public WebLogicMonitorEventDispatcher() {
        logger = LoggingHelper.getServerLogger();
    }

    public void onEvent(MonitorEvent event) {
        MonitorLevel level = event.getMonitorLevel();
        Level jdkLevel = convert(level);
        if (level == null || !logger.isLoggable(jdkLevel)) {
            return;
        }

        LogRecord logRecord = new LogRecord(jdkLevel, event.getMessage());
        // fixme this should be the application name
        logRecord.setLoggerName("fabric3");
        logRecord.setParameters(event.getData());
        for (Object data : event.getData()) {
            if (data instanceof Throwable) {
                logRecord.setThrown((Throwable) data);
                break;
            }
        }
        if (MonitorLevel.INFO == level) {
            // convert INFO to notice so it is displayed by default
            logRecord.setLevel(WLLevel.NOTICE);
        }
        logger.log(logRecord);
    }

    public void configure(Element element) {
        // no-op
    }

    public void start() {
        // no-op
    }

    public void stop() {
        // no-op
    }

    private Level convert(MonitorLevel level) {
        if (MonitorLevel.ERROR == level) {
            return Level.SEVERE;
        } else if (MonitorLevel.WARNING == level) {
            return Level.WARNING;
        } else if (MonitorLevel.INFO == level) {
            return Level.INFO;
        } else if (MonitorLevel.DEBUG == level) {
            return Level.FINE;
        } else if (MonitorLevel.TRACE == level) {
            return Level.FINEST;
        }
        return Level.FINEST;
    }

}