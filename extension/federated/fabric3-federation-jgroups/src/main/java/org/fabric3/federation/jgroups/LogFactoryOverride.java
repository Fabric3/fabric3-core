/*
 * Fabric3
 * Copyright (c) 2009-2011 Metaform Systems
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
package org.fabric3.federation.jgroups;

import org.jgroups.Global;
import org.jgroups.logging.CustomLogFactory;
import org.jgroups.logging.Log;
import org.oasisopen.sca.annotation.Constructor;
import org.oasisopen.sca.annotation.EagerInit;
import org.oasisopen.sca.annotation.Init;
import org.oasisopen.sca.annotation.Property;
import org.oasisopen.sca.annotation.Reference;

import org.fabric3.api.MonitorChannel;
import org.fabric3.api.annotation.monitor.Monitor;
import org.fabric3.api.annotation.monitor.MonitorLevel;
import org.fabric3.federation.jgroups.log.Fabric3LogFactory;
import org.fabric3.spi.monitor.MonitorService;

/**
 * Overrides the static JGroups log factory and redirects output to a Fabric3 monitor channel.
 *
 * @version $Rev$ $Date$
 */
@EagerInit
public class LogFactoryOverride implements CustomLogFactory, Log {
    private MonitorLevel level = MonitorLevel.SEVERE;
    private MonitorService monitorService;
    private MonitorChannel monitor;

    static {
        System.setProperty(Global.CUSTOM_LOG_FACTORY, Fabric3LogFactory.class.getName());
        Fabric3LogFactory.log = new LogFactoryOverride();
    }

    public LogFactoryOverride() {
    }

    @Constructor
    public LogFactoryOverride(@Reference MonitorService monitorService, @Monitor MonitorChannel monitor) {
        this.monitorService = monitorService;
        this.monitor = monitor;
        ((LogFactoryOverride) Fabric3LogFactory.log).setMonitor(monitor);
    }

    public void setMonitor(MonitorChannel monitor) {
        this.monitor = monitor;
    }

    public void setLevel(MonitorLevel level) {
        this.level = level;
    }

    @Property(required = false)
    public void setLogLevel(String logLevel) {
        try {
            level = MonitorLevel.valueOf(logLevel.toUpperCase());
            ((LogFactoryOverride) Fabric3LogFactory.log).setLevel(level);
            monitorService.setDeployableLevel("{urn:fabric3.org}JGroupsFederationCommonExtension", level.toString());
        } catch (IllegalArgumentException e) {
            monitor.severe("Illegal log level: " + logLevel);
        }
    }

    @Init()
    public void inti() {

    }

    public Log getLog(Class clazz) {
        return Fabric3LogFactory.log;
    }

    public Log getLog(String category) {
        return Fabric3LogFactory.log;
    }

    public boolean isFatalEnabled() {
        return level.intValue() <= MonitorLevel.SEVERE.intValue();
    }

    public boolean isErrorEnabled() {
        return level.intValue() <= MonitorLevel.SEVERE.intValue();
    }

    public boolean isWarnEnabled() {
        return level.intValue() <= MonitorLevel.WARNING.intValue();
    }

    public boolean isInfoEnabled() {
        return level.intValue() <= MonitorLevel.INFO.intValue();
    }

    public boolean isDebugEnabled() {
        return level.intValue() <= MonitorLevel.DEBUG.intValue();
    }

    public boolean isTraceEnabled() {
        return level.intValue() <= MonitorLevel.TRACE.intValue();
    }

    public void debug(String msg) {
        if (!isDebugEnabled()) {
            return;
        }
        monitor.debug(msg);
    }

    public void debug(String msg, Throwable throwable) {
        if (!isDebugEnabled()) {
            return;
        }
        monitor.debug(msg, throwable);
    }

    public void error(String msg) {
        if (!isErrorEnabled()) {
            return;
        }
        monitor.severe(msg);
    }

    public void error(String msg, Throwable throwable) {
        if (!isErrorEnabled()) {
            return;
        }
        monitor.severe(msg, throwable);
    }

    public void fatal(String msg) {
        if (!isFatalEnabled()) {
            return;
        }
        monitor.severe(msg);
    }

    public void fatal(String msg, Throwable throwable) {
        if (!isFatalEnabled()) {
            return;
        }
        monitor.severe(msg, throwable);
    }

    public void info(String msg) {
        if (!isInfoEnabled()) {
            return;
        }
        monitor.info(msg);
    }

    public void info(String msg, Throwable throwable) {
        if (!isInfoEnabled()) {
            return;
        }
        monitor.info(msg, throwable);
    }

    public void trace(Object msg) {
        if (!isTraceEnabled() || msg == null) {
            return;
        }
        monitor.trace(msg.toString());
    }

    public void trace(Object msg, Throwable throwable) {
        if (!isTraceEnabled() || msg == null) {
            return;
        }
        monitor.trace(msg.toString(), throwable);
    }

    public void trace(String msg) {
        if (!isTraceEnabled()) {
            return;
        }
        monitor.trace(msg);
    }

    public void trace(String msg, Throwable throwable) {
        if (!isTraceEnabled()) {
            return;
        }
        monitor.trace(msg, throwable);
    }

    public void warn(String msg) {
        if (!isWarnEnabled()) {
            return;
        }
        monitor.warn(msg);
    }

    public void warn(String msg, Throwable throwable) {
        if (!isWarnEnabled()) {
            return;
        }
        monitor.warn(msg, throwable);
    }

    public void setLevel(String level) {
        // no op for now
    }

    public String getLevel() {
        return level.toString().toLowerCase();
    }
}
