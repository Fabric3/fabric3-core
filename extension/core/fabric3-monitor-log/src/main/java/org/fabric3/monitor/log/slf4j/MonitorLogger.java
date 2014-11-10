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
package org.fabric3.monitor.log.slf4j;

import org.fabric3.api.annotation.monitor.MonitorLevel;
import org.fabric3.spi.monitor.MonitorLocator;
import org.fabric3.spi.monitor.MonitorProxy;
import org.slf4j.Logger;
import org.slf4j.Marker;

/**
 * Logger that bridges to the Fabric3 monitor subsystem.
 */
public class MonitorLogger implements Logger {
    private String name;
    private int level;
    private MonitorProxy proxy;

    public MonitorLogger(String name, MonitorLevel level) {
        this.name = name;
        this.level = level.intValue();
        this.proxy = MonitorLocator.getProxy();
    }

    public String getName() {
        return name;
    }

    public boolean isTraceEnabled() {
        return level <= MonitorLevel.TRACE.intValue();

    }

    public void trace(String message) {
        if (!isTraceEnabled()) {
            return;
        }
        proxy.send(MonitorLevel.TRACE, System.currentTimeMillis(), message, false);
    }

    public void trace(String message, Object o) {
        if (!isTraceEnabled()) {
            return;
        }
        proxy.send(MonitorLevel.TRACE, System.currentTimeMillis(), message, false, o);
    }

    public void trace(String message, Object o, Object o2) {
        if (!isTraceEnabled()) {
            return;
        }
        proxy.send(MonitorLevel.TRACE, System.currentTimeMillis(), message, false, o, o2);
    }

    public void trace(String message, Object[] objects) {
        if (!isTraceEnabled()) {
            return;
        }
        proxy.send(MonitorLevel.TRACE, System.currentTimeMillis(), message, false, objects);
    }

    public void trace(String message, Throwable throwable) {
        if (!isTraceEnabled()) {
            return;
        }
        proxy.send(MonitorLevel.TRACE, System.currentTimeMillis(), message, false, throwable);
    }

    public boolean isTraceEnabled(Marker marker) {
        return level <= MonitorLevel.TRACE.intValue();
    }

    public void trace(Marker marker, String message) {
        if (!isTraceEnabled()) {
            return;
        }
        proxy.send(MonitorLevel.TRACE, System.currentTimeMillis(), message, false);
    }

    public void trace(Marker marker, String message, Object o) {
        if (!isTraceEnabled()) {
            return;
        }
        proxy.send(MonitorLevel.TRACE, System.currentTimeMillis(), message, false, o);
    }

    public void trace(Marker marker, String message, Object o, Object o2) {
        if (!isTraceEnabled()) {
            return;
        }
        proxy.send(MonitorLevel.TRACE, System.currentTimeMillis(), message, false, o, o2);
    }

    public void trace(Marker marker, String message, Object[] objects) {
        if (!isTraceEnabled()) {
            return;
        }
        proxy.send(MonitorLevel.TRACE, System.currentTimeMillis(), message, false, objects);
    }

    public void trace(Marker marker, String message, Throwable throwable) {
        if (!isTraceEnabled()) {
            return;
        }
        proxy.send(MonitorLevel.TRACE, System.currentTimeMillis(), message, false, throwable);
    }

    public boolean isDebugEnabled() {
        return level <= MonitorLevel.DEBUG.intValue();
    }

    public void debug(String message) {
        if (!isDebugEnabled()) {
            return;
        }
        proxy.send(MonitorLevel.DEBUG, System.currentTimeMillis(), message, false);
    }

    public void debug(String message, Object o) {
        if (!isDebugEnabled()) {
            return;
        }
        proxy.send(MonitorLevel.DEBUG, System.currentTimeMillis(), message, false, o);
    }

    public void debug(String message, Object o, Object o2) {
        if (!isDebugEnabled()) {
            return;
        }
        proxy.send(MonitorLevel.DEBUG, System.currentTimeMillis(), message, false, o, o2);
    }

    public void debug(String message, Object[] objects) {
        if (!isDebugEnabled()) {
            return;
        }
        proxy.send(MonitorLevel.DEBUG, System.currentTimeMillis(), message, false, objects);
    }

    public void debug(String message, Throwable throwable) {
        if (!isDebugEnabled()) {
            return;
        }
        proxy.send(MonitorLevel.DEBUG, System.currentTimeMillis(), message, false, throwable);
    }

    public boolean isDebugEnabled(Marker marker) {
        return level <= MonitorLevel.DEBUG.intValue();
    }

    public void debug(Marker marker, String message) {
        if (!isDebugEnabled()) {
            return;
        }
        proxy.send(MonitorLevel.DEBUG, System.currentTimeMillis(), message, false);
    }

    public void debug(Marker marker, String message, Object o) {
        if (!isDebugEnabled()) {
            return;
        }
        proxy.send(MonitorLevel.DEBUG, System.currentTimeMillis(), message, false, o);
    }

    public void debug(Marker marker, String message, Object o, Object o2) {
        if (!isDebugEnabled()) {
            return;
        }
        proxy.send(MonitorLevel.DEBUG, System.currentTimeMillis(), message, false, o, o2);
    }

    public void debug(Marker marker, String message, Object[] objects) {
        if (!isDebugEnabled()) {
            return;
        }
        proxy.send(MonitorLevel.DEBUG, System.currentTimeMillis(), message, false, objects);
    }

    public void debug(Marker marker, String message, Throwable throwable) {
        if (!isDebugEnabled()) {
            return;
        }
        proxy.send(MonitorLevel.DEBUG, System.currentTimeMillis(), message, false, throwable);
    }

    public boolean isInfoEnabled() {
        return level <= MonitorLevel.INFO.intValue();
    }

    public void info(String message) {
        if (!isInfoEnabled()) {
            return;
        }
        proxy.send(MonitorLevel.INFO, System.currentTimeMillis(), message, false);
    }

    public void info(String message, Object o) {
        if (!isInfoEnabled()) {
            return;
        }
        proxy.send(MonitorLevel.INFO, System.currentTimeMillis(), message, false, o);
    }

    public void info(String message, Object o, Object o2) {
        if (!isInfoEnabled()) {
            return;
        }
        proxy.send(MonitorLevel.INFO, System.currentTimeMillis(), message, false, o, o2);
    }

    public void info(String message, Object[] objects) {
        if (!isInfoEnabled()) {
            return;
        }
        proxy.send(MonitorLevel.INFO, System.currentTimeMillis(), message, false, objects);
    }

    public void info(String message, Throwable throwable) {
        if (!isInfoEnabled()) {
            return;
        }
        proxy.send(MonitorLevel.INFO, System.currentTimeMillis(), message, false, throwable);
    }

    public boolean isInfoEnabled(Marker marker) {
        return level <= MonitorLevel.INFO.intValue();
    }

    public void info(Marker marker, String message) {
        if (!isInfoEnabled()) {
            return;
        }
        proxy.send(MonitorLevel.INFO, System.currentTimeMillis(), message, false);
    }

    public void info(Marker marker, String message, Object o) {
        if (!isInfoEnabled()) {
            return;
        }
        proxy.send(MonitorLevel.INFO, System.currentTimeMillis(), message, false, o);
    }

    public void info(Marker marker, String message, Object o, Object o2) {
        if (!isInfoEnabled()) {
            return;
        }
        proxy.send(MonitorLevel.INFO, System.currentTimeMillis(), message, false, o, o2);
    }

    public void info(Marker marker, String message, Object[] objects) {
        if (!isInfoEnabled()) {
            return;
        }
        proxy.send(MonitorLevel.INFO, System.currentTimeMillis(), message, false, objects);
    }

    public void info(Marker marker, String message, Throwable throwable) {
        if (!isInfoEnabled()) {
            return;
        }
        proxy.send(MonitorLevel.INFO, System.currentTimeMillis(), message, false, throwable);
    }

    public boolean isWarnEnabled() {
        return level <= MonitorLevel.WARNING.intValue();
    }

    public void warn(String message) {
        if (!isWarnEnabled()) {
            return;
        }
        proxy.send(MonitorLevel.WARNING, System.currentTimeMillis(), message, false);
    }

    public void warn(String message, Object o) {
        if (!isWarnEnabled()) {
            return;
        }
        proxy.send(MonitorLevel.WARNING, System.currentTimeMillis(), message, false, o);
    }

    public void warn(String message, Object[] objects) {
        if (!isWarnEnabled()) {
            return;
        }
        proxy.send(MonitorLevel.WARNING, System.currentTimeMillis(), message, false, objects);
    }

    public void warn(String message, Object o, Object o2) {
        if (!isWarnEnabled()) {
            return;
        }
        proxy.send(MonitorLevel.WARNING, System.currentTimeMillis(), message, false, o, o2);
    }

    public void warn(String message, Throwable throwable) {
        if (!isWarnEnabled()) {
            return;
        }
        proxy.send(MonitorLevel.WARNING, System.currentTimeMillis(), message, false, throwable);
    }

    public boolean isWarnEnabled(Marker marker) {
        return level <= MonitorLevel.WARNING.intValue();
    }

    public void warn(Marker marker, String message) {
        if (!isWarnEnabled()) {
            return;
        }
        proxy.send(MonitorLevel.WARNING, System.currentTimeMillis(), message, false);
    }

    public void warn(Marker marker, String message, Object o) {
        if (!isWarnEnabled()) {
            return;
        }
        proxy.send(MonitorLevel.WARNING, System.currentTimeMillis(), message, false, o);
    }

    public void warn(Marker marker, String message, Object o, Object o2) {
        if (!isWarnEnabled()) {
            return;
        }
        proxy.send(MonitorLevel.WARNING, System.currentTimeMillis(), message, false, o, o2);
    }

    public void warn(Marker marker, String message, Object[] objects) {
        if (!isWarnEnabled()) {
            return;
        }
        proxy.send(MonitorLevel.WARNING, System.currentTimeMillis(), message, false, objects);
    }

    public void warn(Marker marker, String message, Throwable throwable) {
        if (!isWarnEnabled()) {
            return;
        }
        proxy.send(MonitorLevel.WARNING, System.currentTimeMillis(), message, false, throwable);
    }

    public boolean isErrorEnabled() {
        return level <= MonitorLevel.SEVERE.intValue();
    }

    public void error(String message) {
        if (!isErrorEnabled()) {
            return;
        }
        proxy.send(MonitorLevel.SEVERE, System.currentTimeMillis(), message, false);
    }

    public void error(String message, Object o) {
        if (!isErrorEnabled()) {
            return;
        }
        proxy.send(MonitorLevel.SEVERE, System.currentTimeMillis(), message, false, o);
    }

    public void error(String message, Object o, Object o2) {
        if (!isErrorEnabled()) {
            return;
        }
        proxy.send(MonitorLevel.SEVERE, System.currentTimeMillis(), message, false, o, o2);
    }

    public void error(String message, Object[] objects) {
        if (!isErrorEnabled()) {
            return;
        }
        proxy.send(MonitorLevel.SEVERE, System.currentTimeMillis(), message, false, objects);
    }

    public void error(String message, Throwable throwable) {
        if (!isErrorEnabled()) {
            return;
        }
        proxy.send(MonitorLevel.SEVERE, System.currentTimeMillis(), message, false, throwable);
    }

    public boolean isErrorEnabled(Marker marker) {
        return level <= MonitorLevel.SEVERE.intValue();
    }

    public void error(Marker marker, String message) {
        if (!isErrorEnabled()) {
            return;
        }
        proxy.send(MonitorLevel.SEVERE, System.currentTimeMillis(), message, false);
    }

    public void error(Marker marker, String message, Object o) {
        if (!isErrorEnabled()) {
            return;
        }
        proxy.send(MonitorLevel.SEVERE, System.currentTimeMillis(), message, false, o);
    }

    public void error(Marker marker, String message, Object o, Object o2) {
        if (!isErrorEnabled()) {
            return;
        }
        proxy.send(MonitorLevel.SEVERE, System.currentTimeMillis(), message, false, o, o2);
    }

    public void error(Marker marker, String message, Object[] objects) {
        if (!isErrorEnabled()) {
            return;
        }
        proxy.send(MonitorLevel.SEVERE, System.currentTimeMillis(), message, false, objects);
    }

    public void error(Marker marker, String message, Throwable throwable) {
        if (!isErrorEnabled()) {
            return;
        }
        proxy.send(MonitorLevel.SEVERE, System.currentTimeMillis(), message, false, throwable);
    }
}
