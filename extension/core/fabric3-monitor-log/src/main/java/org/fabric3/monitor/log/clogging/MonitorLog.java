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
 */
package org.fabric3.monitor.log.clogging;

import org.apache.commons.logging.Log;
import org.fabric3.api.annotation.monitor.MonitorLevel;
import org.fabric3.spi.monitor.MonitorLocator;
import org.fabric3.spi.monitor.MonitorProxy;

/**
 * Bridges from Commons Logging to the Fabric3 monitor subsystem.
 */
public class MonitorLog implements Log {
    private int level;
    private MonitorProxy proxy;

    public MonitorLog(MonitorLevel level) {
        this.level = level.intValue();
        this.proxy = MonitorLocator.getProxy();
    }

    public boolean isDebugEnabled() {
        return level <= MonitorLevel.DEBUG.intValue();
    }

    public boolean isErrorEnabled() {
        return level <= MonitorLevel.SEVERE.intValue();
    }

    public boolean isFatalEnabled() {
        return level <= MonitorLevel.SEVERE.intValue();
    }

    public boolean isInfoEnabled() {
        return level <= MonitorLevel.INFO.intValue();
    }

    public boolean isTraceEnabled() {
        return level <= MonitorLevel.TRACE.intValue();
    }

    public boolean isWarnEnabled() {
        return level <= MonitorLevel.WARNING.intValue();
    }

    public void trace(Object message) {
        if (!isTraceEnabled()) {
            return;
        }
        proxy.send(MonitorLevel.TRACE, System.currentTimeMillis(), message.toString(), false);
    }

    public void trace(Object message, Throwable t) {
        if (!isTraceEnabled()) {
            return;
        }
        proxy.send(MonitorLevel.TRACE, System.currentTimeMillis(), message.toString(), false, t);
    }

    public void debug(Object message) {
        if (!isDebugEnabled()) {
            return;
        }
        proxy.send(MonitorLevel.DEBUG, System.currentTimeMillis(), message.toString(), false);
    }

    public void debug(Object message, Throwable t) {
        if (!isDebugEnabled()) {
            return;
        }
        proxy.send(MonitorLevel.DEBUG, System.currentTimeMillis(), message.toString(), false, t);
    }

    public void info(Object message) {
        if (!isInfoEnabled()) {
            return;
        }
        proxy.send(MonitorLevel.INFO, System.currentTimeMillis(), message.toString(), false);
    }

    public void info(Object message, Throwable t) {
        if (!isInfoEnabled()) {
            return;
        }
        proxy.send(MonitorLevel.INFO, System.currentTimeMillis(), message.toString(), false, t);
    }

    public void warn(Object message) {
        if (!isWarnEnabled()) {
            return;
        }
        proxy.send(MonitorLevel.WARNING, System.currentTimeMillis(), message.toString(), false);
    }

    public void warn(Object message, Throwable t) {
        if (!isWarnEnabled()) {
            return;
        }
        proxy.send(MonitorLevel.WARNING, System.currentTimeMillis(), message.toString(), false, t);
    }

    public void error(Object message) {
        if (!isErrorEnabled()) {
            return;
        }
        proxy.send(MonitorLevel.SEVERE, System.currentTimeMillis(), message.toString(), false);
    }

    public void error(Object message, Throwable t) {
        if (!isErrorEnabled()) {
            return;
        }
        proxy.send(MonitorLevel.SEVERE, System.currentTimeMillis(), message.toString(), false, t);
    }

    public void fatal(Object message) {
        if (!isFatalEnabled()) {
            return;
        }
        proxy.send(MonitorLevel.SEVERE, System.currentTimeMillis(), message.toString(), false);
    }

    public void fatal(Object message, Throwable t) {
        if (!isFatalEnabled()) {
            return;
        }
        proxy.send(MonitorLevel.SEVERE, System.currentTimeMillis(), message.toString(), false, t);
    }
}
