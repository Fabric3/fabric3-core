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
package org.fabric3.monitor.proxy;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.Map;

import org.fabric3.api.MonitorChannel;
import org.fabric3.api.annotation.monitor.MonitorLevel;
import org.fabric3.host.monitor.DestinationRouter;
import org.fabric3.host.monitor.Monitorable;
import org.fabric3.spi.monitor.DispatchInfo;
import org.fabric3.spi.monitor.MonitorProxy;

/**
 * JDK-based dispatcher for monitor events.
 */
public class JDKMonitorHandler implements InvocationHandler, MonitorChannel, MonitorProxy {
    private DestinationRouter router;
    private int destinationIndex;
    private Monitorable monitorable;
    private String source;
    private Map<Method, DispatchInfo> infos;

    private MonitorLevel level;
    private String template;

    public JDKMonitorHandler(int destinationIndex, Monitorable monitorable, DestinationRouter router, Map<Method, DispatchInfo> infos) {
        this.destinationIndex = destinationIndex;
        this.monitorable = monitorable;
        this.router = router;
        this.source = monitorable.getName();
        this.infos = infos;
        if (infos.size() == 1) {
            // optimization if the monitor interface has only one method: avoid a map lookup on dispatch
            DispatchInfo info = infos.values().iterator().next();
            this.level = info.getLevel();
            this.template = info.getMessage();
        }
    }

    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        if (args != null && args.length > 10) {
            throw new UnsupportedOperationException("Unsupported number of monitor arguments: " + args.length);
        }
        MonitorLevel currentLevel;
        String currentMessage;
        if (level != null) {
            currentLevel = level;
            currentMessage = template;
        } else {
            DispatchInfo info = infos.get(method);
            currentLevel = info.getLevel();
            currentMessage = info.getMessage();
        }
        if (currentLevel == null || currentLevel.intValue() < monitorable.getLevel().intValue()) {
            // monitoring is off
            return null;
        }
        long timestamp = System.currentTimeMillis();
        send(currentLevel, timestamp, currentMessage, args);
        return null;

    }

    public void severe(String message, Object... args) {
        checkAndSend(MonitorLevel.SEVERE, message, args);
    }

    public void warn(String message, Object... args) {
        checkAndSend(MonitorLevel.WARNING, message, args);
    }

    public void info(String message, Object... args) {
        checkAndSend(MonitorLevel.INFO, message, args);
    }

    public void debug(String message, Object... args) {
        checkAndSend(MonitorLevel.DEBUG, message, args);
    }

    public void trace(String message, Object... args) {
        checkAndSend(MonitorLevel.TRACE, message, args);
    }

    public void send(MonitorLevel level, long timestamp, String template, Object[] args) {
        if (args == null) {
            router.send(level, destinationIndex, timestamp, source, template);
        } else {
            router.send(level, destinationIndex, timestamp, source, template, args);
        }
    }

    private void checkAndSend(MonitorLevel level, String message, Object[] args) {
        if (level.intValue() < monitorable.getLevel().intValue()) {
            // monitoring is off
            return;
        }
        long timestamp = System.currentTimeMillis();
        send(level, timestamp, message, args);
    }

}
