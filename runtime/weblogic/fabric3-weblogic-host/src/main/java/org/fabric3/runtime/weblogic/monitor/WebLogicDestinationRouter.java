/*
 * Fabric3
 * Copyright (c) 2009-2015 Metaform Systems
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
 *
 * Portions originally based on Apache Tuscany 2007
 * licensed under the Apache 2.0 license.
 */
package org.fabric3.runtime.weblogic.monitor;

import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import org.fabric3.api.annotation.monitor.MonitorLevel;
import org.fabric3.api.host.monitor.DestinationRouter;
import org.fabric3.api.host.monitor.MessageFormatter;
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

    public void send(MonitorLevel level, int destinationIndex, long timestamp, String source, String message, boolean parse, Object... args) {
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