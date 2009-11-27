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
 *
 * ----------------------------------------------------
 *
 * Portions originally based on Apache Tuscany 2007
 * licensed under the Apache 2.0 license.
 *
 */
package org.fabric3.transport.jetty.impl;

import org.mortbay.log.Logger;

/**
 * Serves as a wrapper for a {@link TransportMonitor} to replace Jetty's logging mechanism
 *
 * @version $Rev$ $Date$
 */
public class JettyLogger implements Logger {

    private TransportMonitor monitor;
    private boolean debugEnabled;

    public void setMonitor(TransportMonitor monitor) {
        this.monitor = monitor;
    }

    public boolean isDebugEnabled() {
        return debugEnabled;
    }

    public void setDebugEnabled(boolean debugEnabled) {
        this.debugEnabled = debugEnabled;
    }

    public void info(String msg, Object arg0, Object arg1) {
        if (monitor != null) {
            monitor.debug(msg, arg0, arg1);
        } else if (debugEnabled) {
            System.err.println(":INFO:  " + format(msg, arg0, arg1));
        }
    }

    public void debug(String msg, Throwable th) {
        if (debugEnabled) {
            if (monitor != null) {
                monitor.debug(msg, th);
            } else {
                System.err.println(":DEBUG:  " + msg);
                th.printStackTrace();
            }
        }
    }

    public void debug(String msg, Object arg0, Object arg1) {
        if (debugEnabled) {
            if (monitor != null) {
                monitor.debug(msg, arg0, arg1);
            } else {
                System.err.println(":DEBUG: " + format(msg, arg0, arg1));
            }
        }
    }

    public void warn(String msg, Object arg0, Object arg1) {
        if (monitor != null) {
            monitor.warn(msg, arg0, arg1);
        } else if (debugEnabled) {
            System.err.println(":WARN: " + format(msg, arg0, arg1));
        }
    }

    public void warn(String msg, Throwable th) {
        if (monitor != null) {
            monitor.exception(msg, th);
        } else if (debugEnabled) {
            System.err.println(":WARN: " + msg);
            th.printStackTrace();
        }
    }

    public Logger getLogger(String name) {
        return this;
    }

    private String format(String msg, Object arg0, Object arg1) {
        int i0 = msg.indexOf("{}");
        int i1 = i0 < 0 ? -1 : msg.indexOf("{}", i0 + 2);
        if (arg1 != null && i1 >= 0) {
            msg = msg.substring(0, i1) + arg1 + msg.substring(i1 + 2);
        }
        if (arg0 != null && i0 >= 0) {
            msg = msg.substring(0, i0) + arg0 + msg.substring(i0 + 2);
        }
        return msg;
    }
}
