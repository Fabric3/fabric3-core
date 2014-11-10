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
package org.fabric3.monitor.impl.proxy;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.Map;

import org.fabric3.api.MonitorChannel;
import org.fabric3.api.annotation.monitor.MonitorLevel;
import org.fabric3.api.host.monitor.Monitorable;
import org.fabric3.monitor.spi.event.MonitorEventEntry;
import org.fabric3.monitor.impl.router.RingBufferDestinationRouter;
import org.fabric3.spi.monitor.DispatchInfo;

/**
 *
 */
public class JDKMonitorHandler implements InvocationHandler, MonitorChannel {
    private RingBufferDestinationRouter router;
    private boolean asyncEnabled;
    private int destinationIndex;
    private Monitorable monitorable;
    private String source;
    private Map<Method, DispatchInfo> infos;

    private MonitorLevel level;
    private String template;

    public JDKMonitorHandler(int destinationIndex,
                             Monitorable monitorable,
                             RingBufferDestinationRouter router,
                             Map<Method, DispatchInfo> infos,
                             boolean asyncEnabled) {
        this.destinationIndex = destinationIndex;
        this.monitorable = monitorable;
        this.router = router;
        this.asyncEnabled = asyncEnabled;
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
            // FIXME convert to index-based dispatch
            DispatchInfo info = infos.get(method);
            currentLevel = info.getLevel();
            currentMessage = info.getMessage();
        }
        if (currentLevel == null || currentLevel.intValue() < monitorable.getLevel().intValue()) {
            // monitoring is off
            return null;
        }
        long timestamp = System.currentTimeMillis();
        if (asyncEnabled) {
            send(currentLevel, timestamp, currentMessage, args);
        } else {
            router.send(currentLevel, destinationIndex, timestamp, source, currentMessage, true, args);
        }
        return null;

    }

    private void send(MonitorLevel level, long timestamp, String template, Object[] args) {
        MonitorEventEntry entry = null;
        try {
            long start = System.nanoTime();
            entry = router.get();
            entry.setDestinationIndex(destinationIndex);
            entry.setTimestampNanos(start);
            entry.setTemplate(template);
            entry.setParse(true);
            if (args != null) {
                for (int i = 0; i < args.length; i++) {
                    Object arg = args[i];
                    entry.getEntries()[i].setObjectValue(arg);
                }
            }
            entry.setLevel(level);
            entry.setLimit(args == null ? 0 : args.length);
            entry.setEntryTimestamp(timestamp);
        } finally {
            if (entry != null) {
                router.publish(entry);
            }
        }
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

    public void send(MonitorLevel level, long timestamp, String template, boolean parse, Object[] args) {
        if (args == null) {
            router.send(level, destinationIndex, timestamp, source, template, parse, template);
        } else {
            router.send(level, destinationIndex, timestamp, source, template, parse, args);
        }
    }

    private void checkAndSend(MonitorLevel level, String message, Object[] args) {
        if (level.intValue() < monitorable.getLevel().intValue()) {
            // monitoring is off
            return;
        }
        long timestamp = System.currentTimeMillis();
        send(level, timestamp, message, true, args);
    }

}
