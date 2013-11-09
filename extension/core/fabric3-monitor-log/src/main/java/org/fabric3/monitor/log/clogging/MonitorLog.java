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
