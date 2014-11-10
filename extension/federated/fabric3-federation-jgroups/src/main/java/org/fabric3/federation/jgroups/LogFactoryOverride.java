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
package org.fabric3.federation.jgroups;

import org.fabric3.api.MonitorChannel;
import org.fabric3.api.annotation.monitor.Monitor;
import org.fabric3.api.annotation.monitor.MonitorLevel;
import org.fabric3.federation.jgroups.log.Fabric3LogFactory;
import org.fabric3.spi.monitor.MonitorService;
import org.jgroups.Global;
import org.jgroups.logging.CustomLogFactory;
import org.jgroups.logging.Log;
import org.oasisopen.sca.annotation.Constructor;
import org.oasisopen.sca.annotation.EagerInit;
import org.oasisopen.sca.annotation.Reference;

/**
 * Overrides the static JGroups log factory and redirects output to a Fabric3 monitor infrastructure.
 */
@EagerInit
public class LogFactoryOverride implements CustomLogFactory, Log {
    private MonitorLevel level = MonitorLevel.SEVERE;
    private MonitorChannel monitor;

    static {
        System.setProperty(Global.CUSTOM_LOG_FACTORY, Fabric3LogFactory.class.getName());
        Fabric3LogFactory.log = new LogFactoryOverride();
    }

    protected LogFactoryOverride() {
    }

    @Constructor
    public LogFactoryOverride(@Reference MonitorService monitorService, @Monitor MonitorChannel monitor) {
        this.monitor = monitor;
        LogFactoryOverride override = (LogFactoryOverride) Fabric3LogFactory.log;
        override.setMonitor(monitor);
        MonitorLevel level = monitorService.getProviderLevel("org.jgroups");
        if (level == null) {
            level = MonitorLevel.SEVERE;
        }
        override.setLevel(level);
    }

    public void setMonitor(MonitorChannel monitor) {
        this.monitor = monitor;
    }

    public void setLevel(MonitorLevel level) {
        this.level = level;
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

    public void debug(String msg, Object... args) {
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

    public void error(String format, Object... args) {
        if (!isFatalEnabled()) {
            return;
        }
        monitor.severe(format, args);
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

    public void fatal(String msg, Object... args) {
        if (!isFatalEnabled()) {
            return;
        }
        monitor.severe(msg, args);
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

    public void info(String msg, Object... args) {
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

    public void trace(String msg) {
        if (!isTraceEnabled()) {
            return;
        }
        monitor.trace(msg);
    }

    public void trace(String msg, Object... args) {
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

    public void warn(String msg, Object... args) {
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
