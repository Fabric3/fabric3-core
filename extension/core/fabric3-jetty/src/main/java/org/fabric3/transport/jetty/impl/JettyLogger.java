/*
 * Fabric3
 * Copyright (c) 2009-2012 Metaform Systems
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
 *
 * ----------------------------------------------------
 *
 * Portions originally based on Apache Tuscany 2007
 * licensed under the Apache 2.0 license.
 *
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
