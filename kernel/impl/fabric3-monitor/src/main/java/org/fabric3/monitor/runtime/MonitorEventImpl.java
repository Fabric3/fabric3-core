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
*/
package org.fabric3.monitor.runtime;

import java.util.Map;
import java.util.logging.Level;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.IThrowableProxy;
import ch.qos.logback.classic.spi.LoggerContextVO;
import ch.qos.logback.classic.spi.ThrowableProxy;
import org.slf4j.Marker;

import org.fabric3.host.monitor.MonitorEvent;
import org.fabric3.host.monitor.Monitorable;

/**
 * Default MonitorEvent implementation.
 * <p/>
 * The {@link ILoggingEvent} is also implemented to avoid creating an additional object when the event is dispatched to LogBack.
 *
 * @version $Rev$ $Date$
 */
public class MonitorEventImpl implements MonitorEvent, ILoggingEvent {
    private static final long serialVersionUID = 6943460067960488899L;
    private Level level;
    private ch.qos.logback.classic.Level logbackLevel;
    private String runtime;
    private String source;
    private long timeStamp;
    private String threadName;
    private String message;
    private Object[] data;

    /**
     * @param runtime    the runtime which originated the event
     * @param source     the name of the {@link Monitorable} which originated the event
     * @param level      the event level
     * @param timeStamp  the time the event was created
     * @param threadName the name of the executing thread when the event was created
     * @param message    the event message
     * @param data       any data (such as an exception) associated with the event. If this event will be broadcast outside the runtime VM, the data
     *                   should implement Serializable.
     */
    public MonitorEventImpl(String runtime, String source, Level level, long timeStamp, String threadName, String message, Object... data) {
        this.runtime = runtime;
        this.source = source;
        this.level = level;
        this.timeStamp = timeStamp;
        this.threadName = threadName;
        this.message = message;
        this.data = data;
        setLogbackLevel(level);
    }

    private void setLogbackLevel(Level level) {
        if (Level.ALL == level) {
            logbackLevel = ch.qos.logback.classic.Level.ALL;
        } else if (Level.CONFIG == level) {
            logbackLevel = ch.qos.logback.classic.Level.DEBUG;
        } else if (Level.FINE == level) {
            logbackLevel = ch.qos.logback.classic.Level.DEBUG;
        } else if (Level.FINER == level) {
            logbackLevel = ch.qos.logback.classic.Level.DEBUG;
        } else if (Level.FINEST == level) {
            logbackLevel = ch.qos.logback.classic.Level.TRACE;
        } else if (Level.INFO == level) {
            logbackLevel = ch.qos.logback.classic.Level.INFO;
        } else if (Level.OFF == level) {
            logbackLevel = ch.qos.logback.classic.Level.OFF;
        } else if (Level.SEVERE == level) {
            logbackLevel = ch.qos.logback.classic.Level.ERROR;
        } else if (Level.WARNING == level) {
            logbackLevel = ch.qos.logback.classic.Level.WARN;
        }
    }

    /**
     * Returns the runtime which originated the event.
     *
     * @return the runtime which originated the event
     */

    public String getRuntime() {
        return runtime;
    }

    /**
     * Returns the name of the {@link Monitorable} which originated the event.
     *
     * @return the Monitorable name
     */
    public String getSource() {
        return source;
    }

    /**
     * Returns the event level.
     *
     * @return the event level
     */
    public Level getMonitorLevel() {
        return level;
    }

    public long getTimeStamp() {
        return timeStamp;
    }

    public String getThreadName() {
        return threadName;
    }

    public String getMessage() {
        return message;
    }

    public Object[] getData() {
        return data;
    }

    public ch.qos.logback.classic.Level getLevel() {
        return logbackLevel;
    }

    public void prepareForDeferredProcessing() {

    }

    public Object[] getArgumentArray() {
        return data;
    }

    public String getFormattedMessage() {
        return message;
    }

    public String getLoggerName() {
        return source;
    }

    public LoggerContextVO getLoggerContextVO() {
        return null;
    }

    public IThrowableProxy getThrowableProxy() {
        for (Object o : data) {
            if (o instanceof Throwable) {
                return new ThrowableProxy((Throwable) o);
            }
        }
        return null;
    }

    public StackTraceElement[] getCallerData() {
        return null;
    }

    public boolean hasCallerData() {
        return false;
    }

    public Marker getMarker() {
        return null;
    }

    public Map<String, String> getMDCPropertyMap() {
        return null;
    }

}