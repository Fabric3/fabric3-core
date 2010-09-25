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

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.text.MessageFormat;
import java.util.Map;

import org.fabric3.api.MonitorChannel;
import org.fabric3.api.annotation.monitor.MonitorLevel;
import org.fabric3.host.monitor.MonitorEvent;
import org.fabric3.host.monitor.Monitorable;
import org.fabric3.spi.channel.EventStreamHandler;

/**
 * Dispatches a monitor event from a proxy to a channel.
 *
 * @version $Rev$ $Date$
 */
public class MonitorHandler implements InvocationHandler, MonitorChannel {
    private static final Object[] EMPTY_DATA = new Object[0];
    private Monitorable monitorable;
    private String runtime;
    private String source;
    private EventStreamHandler streamHandler;
    private MonitorLevel level;
    private String message;
    private Map<String, DispatchInfo> infos;

    public MonitorHandler(Monitorable monitorable, EventStreamHandler streamHandler, Map<String, DispatchInfo> infos) {
        this.monitorable = monitorable;
        this.source = monitorable.getName();
        this.streamHandler = streamHandler;
        this.infos = infos;
        if (infos.size() == 1) {
            // optimization if the monitor interface has only one method: avoid a map lookup on dispatch
            DispatchInfo info = infos.values().iterator().next();
            this.level = info.getLevel();
            this.message = info.getMessage();
        }
    }

    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        MonitorLevel currentLevel;
        String currentMessage;
        if (level != null) {
            currentLevel = level;
            currentMessage = message;
        } else {
            DispatchInfo info = infos.get(method.getName());
            currentLevel = info.getLevel();
            currentMessage = info.getMessage();
        }
        if (currentLevel == null || currentLevel.intValue() < monitorable.getLevel().intValue()) {
            // monitoring is off
            return null;
        }
        dispatch(currentLevel, currentMessage, args);
        return null;
    }

    public void severe(String message, Object... args) {
        if (off(MonitorLevel.SEVERE)) {
            return;
        }
        dispatch(MonitorLevel.SEVERE, message, args);
    }

    public void warn(String message, Object... args) {
        if (off(MonitorLevel.WARNING)) {
            return;
        }
        dispatch(MonitorLevel.WARNING, message, args);
    }

    public void info(String message, Object... args) {
        if (off(MonitorLevel.INFO)) {
            return;
        }
        dispatch(MonitorLevel.INFO, message, args);
    }

    public void debug(String message, Object... args) {
        if (off(MonitorLevel.DEBUG)) {
            return;
        }
        dispatch(MonitorLevel.DEBUG, message, args);
    }

    public void trace(String message, Object... args) {
        if (off(MonitorLevel.TRACE)) {
            return;
        }
        dispatch(MonitorLevel.TRACE, message, args);
    }

    /**
     * Formats the monitor message by substituting placeholders with proxy parameters. If the message is null, the string values of the parameter
     * values will be concatenated.
     *
     * @param message the message
     * @param args    the monitor parameters
     * @return the formatted message
     */
    private String format(String message, Object[] args) {
        if (message == null) {
            StringBuilder builder = new StringBuilder();
            for (Object arg : args) {
                builder.append(arg).append(" ");
            }
            return builder.toString();
        }
        if (args != null && args.length != 0) {
            if (message.indexOf("{0") >= 0 || message.indexOf("{1") >= 0 || message.indexOf("{2") >= 0 || message.indexOf("{3") >= 0) {
                return MessageFormat.format(message, args);
            }
        }
        return message;
    }

    /**
     * Determines if monitoring is off for the current level
     *
     * @param currentLevel the level
     * @return true if off
     */
    private boolean off(MonitorLevel currentLevel) {
        return currentLevel.intValue() < monitorable.getLevel().intValue();
    }

    /**
     * Dispatches the monitor event to the channel
     *
     * @param currentLevel the current monitor level
     * @param message      the event message
     * @param args         the event arguments or null
     */
    private void dispatch(MonitorLevel currentLevel, String message, Object[] args) {
        String thread = Thread.currentThread().getName();
        message = format(message, args);
        long time = System.currentTimeMillis();
        if (args == null) {
            args = EMPTY_DATA;
        }
        MonitorEvent event = new MonitorEventImpl(runtime, source, currentLevel, time, thread, message, args);
        // events are passed as arrays
        Object[] param = new Object[]{event};
        streamHandler.handle(param);
    }

}