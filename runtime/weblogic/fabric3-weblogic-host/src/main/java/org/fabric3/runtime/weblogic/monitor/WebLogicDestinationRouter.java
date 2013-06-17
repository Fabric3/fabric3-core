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

import org.fabric3.api.annotation.monitor.MonitorLevel;
import org.fabric3.host.monitor.DestinationRouter;
import org.fabric3.host.monitor.MessageFormatter;
import weblogic.logging.LoggingHelper;
import weblogic.logging.WLLevel;

/**
 * A router that forwards events to the WebLogic logging service.
 */
public class WebLogicDestinationRouter implements DestinationRouter {
    private Logger logger;

    public WebLogicDestinationRouter() {
        logger = LoggingHelper.getServerLogger();
    }

    public int getDestinationIndex(String name) {
        return 0;
    }

    public void send(MonitorLevel level, int destinationIndex, long timestamp, String source, String message, Object... args) {
        write(level, message, args);
    }

    private void write(MonitorLevel level, String message, Object[] args) {
        message = MessageFormatter.format(message, args);

        Level jdkLevel = convert(level);
        if (level == null || !logger.isLoggable(jdkLevel)) {
            return;
        }

        LogRecord logRecord = new LogRecord(jdkLevel, message);
        logRecord.setLoggerName("fabric3");
        logRecord.setParameters(args);
        for (Object data : args) {
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

    private Level convert(MonitorLevel level) {
        if (MonitorLevel.SEVERE == level) {
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