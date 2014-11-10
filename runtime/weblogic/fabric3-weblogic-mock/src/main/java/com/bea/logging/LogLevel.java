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
package com.bea.logging;

import java.util.logging.Level;

/**
 *
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
