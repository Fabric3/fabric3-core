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
