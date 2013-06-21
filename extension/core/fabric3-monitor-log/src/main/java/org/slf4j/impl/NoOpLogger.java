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
package org.slf4j.impl;

import org.slf4j.helpers.MarkerIgnoringBase;

/**
 *
 */
public class NoOpLogger extends MarkerIgnoringBase {
    private static final long serialVersionUID = -4497232940636788072L;

    public String getName() {
        return "noop";
    }

    public boolean isTraceEnabled() {
        return false;
    }

    public void trace(String msg) {
        // no-op
    }

    public void trace(String format, Object arg) {
        // no-op
    }

    public void trace(String format, Object arg1, Object arg2) {
        // no-op
    }

    public void trace(String format, Object[] argArray) {
        // no-op
    }

    public void trace(String msg, Throwable t) {
        // no-op
    }

    public boolean isDebugEnabled() {
        return false;
    }

    public void debug(String msg) {
        // no-op
    }

    public void debug(String format, Object arg) {
        // no-op
    }

    public void debug(String format, Object arg1, Object arg2) {
        // no-op
    }

    public void debug(String format, Object[] argArray) {
        // no-op
    }

    public void debug(String msg, Throwable t) {
        // no-op
    }

    public boolean isInfoEnabled() {
        // no-op
        return false;
    }

    public void info(String msg) {
        // no-op
    }

    public void info(String format, Object arg1) {
        // no-op
    }

    public void info(String format, Object arg1, Object arg2) {
        // no-op
    }

    public void info(String format, Object[] argArray) {
        // no-op
    }

    public void info(String msg, Throwable t) {
        // no-op
    }

    public boolean isWarnEnabled() {
        return false;
    }

    public void warn(String msg) {
        // no-op
    }

    public void warn(String format, Object arg1) {
        // no-op
    }

    public void warn(String format, Object arg1, Object arg2) {
        // no-op
    }

    public void warn(String format, Object[] argArray) {
        // no-op
    }

    public void warn(String msg, Throwable t) {
        // no-op
    }

    public boolean isErrorEnabled() {
        return false;
    }

    public void error(String msg) {
        // no-op
    }

    public void error(String format, Object arg1) {
        // no-op
    }

    public void error(String format, Object arg1, Object arg2) {
        // no-op
    }

    public void error(String format, Object[] argArray) {
        // no-op
    }

    public void error(String msg, Throwable t) {
        // no-op
    }

}
