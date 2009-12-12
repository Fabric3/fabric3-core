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
package com.bea.logging;

import java.util.logging.Level;

/**
 * @version $Rev$ $Date$
 */
public class LogLevel extends Level {
    private static final long serialVersionUID = -1528815140017548638L;

    public static LogLevel ALERT;

    public static int ALERT_INT;

    public static LogLevel CRITICAL;

    public static int CRITICAL_INT;

    public static LogLevel DEBUG;

    public static int DEBUG_INT;

    public static LogLevel EMERGENCY;

    public static int EMERGENCY_INT;

    public static LogLevel ERROR;

    public static int ERROR_INT;

    public static LogLevel INFO;

    public static int INFO_INT;

    public static LogLevel NOTICE;

    public static int NOTICE_INT;

    public static LogLevel OFF;

    public static int OFF_INT;

    public static LogLevel TRACE;

    public static int TRACE_INT;

    public static LogLevel WARNING;

    public static int WARNING_INT;

    public LogLevel(String name, int value) {
        super(name, value);
    }


    protected String getHeader(Level level) {
        return null;
    }

    public static Level getLevel(int severity) {
        return null;
    }

    public String getLocalizedName() {
        return null;
    }

    public int getSeverity() {
        return 0;
    }

    public static int getSeverity(Level level) {
        return 0;
    }
}
