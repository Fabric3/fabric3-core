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
 *
 * Portions originally based on Apache Tuscany 2007
 * licensed under the Apache 2.0 license.
 */
package org.fabric3.transport.jetty.impl;

import java.text.MessageFormat;

import org.eclipse.jetty.util.log.Logger;

/**
 * Serves as a wrapper for a {@link TransportMonitor} to replace Jetty's logging mechanism
 */
public class JettyLogger implements Logger {

    private static TransportMonitor MONITOR;
    private static boolean DEBUG_ENABLED;

    public static void setMonitor(TransportMonitor monitor) {
        MONITOR = monitor;
    }

    public static void enableDebug() {
        DEBUG_ENABLED = true;
    }

    public String getName() {
        return "Logger";
    }

    public boolean isDebugEnabled() {
        return DEBUG_ENABLED;
    }

    public void setDebugEnabled(boolean debugEnabled) {
    }

    public void debug(String msg, Object... args) {
        if (DEBUG_ENABLED) {
            if (MONITOR != null) {
                MONITOR.debug(msg);
            } else {
                System.err.println(":DEBUG:  " + msg);
            }
        }
    }

    public void debug(String msg, long value) {
        if (DEBUG_ENABLED) {
            if (MONITOR != null) {
                MONITOR.debug(msg);
            } else {
                System.err.println(":DEBUG:  " + msg);
            }
        }
    }

    public void debug(Throwable thrown) {
        debug(thrown.getMessage(), thrown);
    }

    public void debug(String msg, Throwable th) {
        if (DEBUG_ENABLED) {
            if (MONITOR != null) {
                MONITOR.debug(msg, th);
            } else {
                System.err.println(":DEBUG:  " + msg);
                th.printStackTrace();
            }
        }
    }

    public void warn(String msg, Object... args) {
        if (MONITOR != null) {
            MONITOR.warn(msg, args);
        } else if (DEBUG_ENABLED) {
            System.err.println(":WARN: " + format(msg, args));
        }
    }

    public void warn(Throwable thrown) {
        warn(thrown.getMessage(), thrown);
    }

    public void warn(String msg, Throwable thrown) {
        if (MONITOR != null) {
            MONITOR.exception(msg, thrown);
        } else if (DEBUG_ENABLED) {
            System.err.println(":WARN: " + msg);
            thrown.printStackTrace();
        }
    }

    public void info(Throwable thrown) {
        info(thrown.getMessage(), thrown);
    }

    public void info(String msg, Object... args) {
        if (MONITOR != null) {
            MONITOR.debug(msg, args);
        } else if (DEBUG_ENABLED) {
            System.err.println(":INFO:  " + format(msg, args));
        }

    }

    public void info(String msg, Throwable thrown) {
        if (MONITOR != null) {
            MONITOR.debug(msg, thrown);
        } else if (DEBUG_ENABLED) {
            System.err.println(":INFO: " + msg);
            thrown.printStackTrace();
        }
    }

    public Logger getLogger(String name) {
        return this;
    }

    public void ignore(Throwable throwable) {

    }

    private String format(String msg, Object... args) {
        MessageFormat formatter = new MessageFormat(msg);
        return formatter.format(args);
    }
}
