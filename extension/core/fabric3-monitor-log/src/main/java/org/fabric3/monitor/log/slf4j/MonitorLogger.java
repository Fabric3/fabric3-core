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
        proxy.send(MonitorLevel.TRACE, System.currentTimeMillis(), message);
    }

    public void trace(String message, Object o) {
        if (!isTraceEnabled()) {
            return;
        }
        proxy.send(MonitorLevel.TRACE, System.currentTimeMillis(), message, o);
    }

    public void trace(String message, Object o, Object o2) {
        if (!isTraceEnabled()) {
            return;
        }
        proxy.send(MonitorLevel.TRACE, System.currentTimeMillis(), message, o, o2);
    }

    public void trace(String message, Object[] objects) {
        if (!isTraceEnabled()) {
            return;
        }
        proxy.send(MonitorLevel.TRACE, System.currentTimeMillis(), message, objects);
    }

    public void trace(String message, Throwable throwable) {
        if (!isTraceEnabled()) {
            return;
        }
        proxy.send(MonitorLevel.TRACE, System.currentTimeMillis(), message, throwable);
    }

    public boolean isTraceEnabled(Marker marker) {
        return level <= MonitorLevel.TRACE.intValue();
    }

    public void trace(Marker marker, String message) {
        if (!isTraceEnabled()) {
            return;
        }
        proxy.send(MonitorLevel.TRACE, System.currentTimeMillis(), message);
    }

    public void trace(Marker marker, String message, Object o) {
        if (!isTraceEnabled()) {
            return;
        }
        proxy.send(MonitorLevel.TRACE, System.currentTimeMillis(), message, o);
    }

    public void trace(Marker marker, String message, Object o, Object o2) {
        if (!isTraceEnabled()) {
            return;
        }
        proxy.send(MonitorLevel.TRACE, System.currentTimeMillis(), message, o, o2);
    }

    public void trace(Marker marker, String message, Object[] objects) {
        if (!isTraceEnabled()) {
            return;
        }
        proxy.send(MonitorLevel.TRACE, System.currentTimeMillis(), message, objects);
    }

    public void trace(Marker marker, String message, Throwable throwable) {
        if (!isTraceEnabled()) {
            return;
        }
        proxy.send(MonitorLevel.TRACE, System.currentTimeMillis(), message, throwable);
    }

    public boolean isDebugEnabled() {
        return level <= MonitorLevel.DEBUG.intValue();
    }

    public void debug(String message) {
        if (!isDebugEnabled()) {
            return;
        }
        proxy.send(MonitorLevel.DEBUG, System.currentTimeMillis(), message);
    }

    public void debug(String message, Object o) {
        if (!isDebugEnabled()) {
            return;
        }
        proxy.send(MonitorLevel.DEBUG, System.currentTimeMillis(), message, o);
    }

    public void debug(String message, Object o, Object o2) {
        if (!isDebugEnabled()) {
            return;
        }
        proxy.send(MonitorLevel.DEBUG, System.currentTimeMillis(), message, o, o2);
    }

    public void debug(String message, Object[] objects) {
        if (!isDebugEnabled()) {
            return;
        }
        proxy.send(MonitorLevel.DEBUG, System.currentTimeMillis(), message, objects);
    }

    public void debug(String message, Throwable throwable) {
        if (!isDebugEnabled()) {
            return;
        }
        proxy.send(MonitorLevel.DEBUG, System.currentTimeMillis(), message, throwable);
    }

    public boolean isDebugEnabled(Marker marker) {
        return level <= MonitorLevel.DEBUG.intValue();
    }

    public void debug(Marker marker, String message) {
        if (!isDebugEnabled()) {
            return;
        }
        proxy.send(MonitorLevel.DEBUG, System.currentTimeMillis(), message);
    }

    public void debug(Marker marker, String message, Object o) {
        if (!isDebugEnabled()) {
            return;
        }
        proxy.send(MonitorLevel.DEBUG, System.currentTimeMillis(), message, o);
    }

    public void debug(Marker marker, String message, Object o, Object o2) {
        if (!isDebugEnabled()) {
            return;
        }
        proxy.send(MonitorLevel.DEBUG, System.currentTimeMillis(), message, o, o2);
    }

    public void debug(Marker marker, String message, Object[] objects) {
        if (!isDebugEnabled()) {
            return;
        }
        proxy.send(MonitorLevel.DEBUG, System.currentTimeMillis(), message, objects);
    }

    public void debug(Marker marker, String message, Throwable throwable) {
        if (!isDebugEnabled()) {
            return;
        }
        proxy.send(MonitorLevel.DEBUG, System.currentTimeMillis(), message, throwable);
    }

    public boolean isInfoEnabled() {
        return level <= MonitorLevel.INFO.intValue();
    }

    public void info(String message) {
        if (!isInfoEnabled()) {
            return;
        }
        proxy.send(MonitorLevel.INFO, System.currentTimeMillis(), message);
    }

    public void info(String message, Object o) {
        if (!isInfoEnabled()) {
            return;
        }
        proxy.send(MonitorLevel.INFO, System.currentTimeMillis(), message, o);
    }

    public void info(String message, Object o, Object o2) {
        if (!isInfoEnabled()) {
            return;
        }
        proxy.send(MonitorLevel.INFO, System.currentTimeMillis(), message, o, o2);
    }

    public void info(String message, Object[] objects) {
        if (!isInfoEnabled()) {
            return;
        }
        proxy.send(MonitorLevel.INFO, System.currentTimeMillis(), message, objects);
    }

    public void info(String message, Throwable throwable) {
        if (!isInfoEnabled()) {
            return;
        }
        proxy.send(MonitorLevel.INFO, System.currentTimeMillis(), message, throwable);
    }

    public boolean isInfoEnabled(Marker marker) {
        return level <= MonitorLevel.INFO.intValue();
    }

    public void info(Marker marker, String message) {
        if (!isInfoEnabled()) {
            return;
        }
        proxy.send(MonitorLevel.INFO, System.currentTimeMillis(), message);
    }

    public void info(Marker marker, String message, Object o) {
        if (!isInfoEnabled()) {
            return;
        }
        proxy.send(MonitorLevel.INFO, System.currentTimeMillis(), message, o);
    }

    public void info(Marker marker, String message, Object o, Object o2) {
        if (!isInfoEnabled()) {
            return;
        }
        proxy.send(MonitorLevel.INFO, System.currentTimeMillis(), message, o, o2);
    }

    public void info(Marker marker, String message, Object[] objects) {
        if (!isInfoEnabled()) {
            return;
        }
        proxy.send(MonitorLevel.INFO, System.currentTimeMillis(), message, objects);
    }

    public void info(Marker marker, String message, Throwable throwable) {
        if (!isInfoEnabled()) {
            return;
        }
        proxy.send(MonitorLevel.INFO, System.currentTimeMillis(), message, throwable);
    }

    public boolean isWarnEnabled() {
        return level <= MonitorLevel.WARNING.intValue();
    }

    public void warn(String message) {
        if (!isWarnEnabled()) {
            return;
        }
        proxy.send(MonitorLevel.WARNING, System.currentTimeMillis(), message);
    }

    public void warn(String message, Object o) {
        if (!isWarnEnabled()) {
            return;
        }
        proxy.send(MonitorLevel.WARNING, System.currentTimeMillis(), message, o);
    }

    public void warn(String message, Object[] objects) {
        if (!isWarnEnabled()) {
            return;
        }
        proxy.send(MonitorLevel.WARNING, System.currentTimeMillis(), message, objects);
    }

    public void warn(String message, Object o, Object o2) {
        if (!isWarnEnabled()) {
            return;
        }
        proxy.send(MonitorLevel.WARNING, System.currentTimeMillis(), message, o, o2);
    }

    public void warn(String message, Throwable throwable) {
        if (!isWarnEnabled()) {
            return;
        }
        proxy.send(MonitorLevel.WARNING, System.currentTimeMillis(), message, throwable);
    }

    public boolean isWarnEnabled(Marker marker) {
        return level <= MonitorLevel.WARNING.intValue();
    }

    public void warn(Marker marker, String message) {
        if (!isWarnEnabled()) {
            return;
        }
        proxy.send(MonitorLevel.WARNING, System.currentTimeMillis(), message);
    }

    public void warn(Marker marker, String message, Object o) {
        if (!isWarnEnabled()) {
            return;
        }
        proxy.send(MonitorLevel.WARNING, System.currentTimeMillis(), message, o);
    }

    public void warn(Marker marker, String message, Object o, Object o2) {
        if (!isWarnEnabled()) {
            return;
        }
        proxy.send(MonitorLevel.WARNING, System.currentTimeMillis(), message, o, o2);
    }

    public void warn(Marker marker, String message, Object[] objects) {
        if (!isWarnEnabled()) {
            return;
        }
        proxy.send(MonitorLevel.WARNING, System.currentTimeMillis(), message, objects);
    }

    public void warn(Marker marker, String message, Throwable throwable) {
        if (!isWarnEnabled()) {
            return;
        }
        proxy.send(MonitorLevel.WARNING, System.currentTimeMillis(), message, throwable);
    }

    public boolean isErrorEnabled() {
        return level <= MonitorLevel.SEVERE.intValue();
    }

    public void error(String message) {
        if (!isErrorEnabled()) {
            return;
        }
        proxy.send(MonitorLevel.SEVERE, System.currentTimeMillis(), message);
    }

    public void error(String message, Object o) {
        if (!isErrorEnabled()) {
            return;
        }
        proxy.send(MonitorLevel.SEVERE, System.currentTimeMillis(), message, o);
    }

    public void error(String message, Object o, Object o2) {
        if (!isErrorEnabled()) {
            return;
        }
        proxy.send(MonitorLevel.SEVERE, System.currentTimeMillis(), message, o, o2);
    }

    public void error(String message, Object[] objects) {
        if (!isErrorEnabled()) {
            return;
        }
        proxy.send(MonitorLevel.SEVERE, System.currentTimeMillis(), message, objects);
    }

    public void error(String message, Throwable throwable) {
        if (!isErrorEnabled()) {
            return;
        }
        proxy.send(MonitorLevel.SEVERE, System.currentTimeMillis(), message, throwable);
    }

    public boolean isErrorEnabled(Marker marker) {
        return level <= MonitorLevel.SEVERE.intValue();
    }

    public void error(Marker marker, String message) {
        if (!isErrorEnabled()) {
            return;
        }
        proxy.send(MonitorLevel.SEVERE, System.currentTimeMillis(), message);
    }

    public void error(Marker marker, String message, Object o) {
        if (!isErrorEnabled()) {
            return;
        }
        proxy.send(MonitorLevel.SEVERE, System.currentTimeMillis(), message, o);
    }

    public void error(Marker marker, String message, Object o, Object o2) {
        if (!isErrorEnabled()) {
            return;
        }
        proxy.send(MonitorLevel.SEVERE, System.currentTimeMillis(), message, o, o2);
    }

    public void error(Marker marker, String message, Object[] objects) {
        if (!isErrorEnabled()) {
            return;
        }
        proxy.send(MonitorLevel.SEVERE, System.currentTimeMillis(), message, objects);
    }

    public void error(Marker marker, String message, Throwable throwable) {
        if (!isErrorEnabled()) {
            return;
        }
        proxy.send(MonitorLevel.SEVERE, System.currentTimeMillis(), message, throwable);
    }
}
