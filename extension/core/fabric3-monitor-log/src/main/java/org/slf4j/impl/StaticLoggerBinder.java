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

import java.util.regex.Pattern;

import org.fabric3.api.annotation.monitor.MonitorLevel;
import org.fabric3.monitor.log.slf4j.MonitorLogger;
import org.fabric3.spi.monitor.MonitorLocator;
import org.fabric3.spi.monitor.MonitorService;
import org.slf4j.ILoggerFactory;
import org.slf4j.Logger;
import org.slf4j.spi.LoggerFactoryBinder;

/**
 * Binds SLF4J to the Fabric3 monitor subsystem.
 */
public class StaticLoggerBinder implements LoggerFactoryBinder, ILoggerFactory {
    public static final StaticLoggerBinder SINGLETON = new StaticLoggerBinder();

    private static final NoOpLogger NOOP_LOGGGER = new NoOpLogger();

    private static final Pattern PATTERN = Pattern.compile("\\.");

    public static StaticLoggerBinder getSingleton() {
        return SINGLETON;
    }

    public ILoggerFactory getLoggerFactory() {
        return this;
    }

    public String getLoggerFactoryClassStr() {
        return getClass().getName();
    }

    public Logger getLogger(String name) {
        MonitorService service = MonitorLocator.getServiceInstance();
        if (service == null) {
            return NOOP_LOGGGER;
        }
        String[] tokens = PATTERN.split(name);
        if (tokens.length == 0) {
            return new MonitorLogger(name, MonitorLevel.WARNING);
        } else if (tokens.length == 1) {
            MonitorLevel level = service.getProviderLevel(name);
            if (level != null) {
                return new MonitorLogger(name, level);
            } else {
                return new MonitorLogger(name, MonitorLevel.WARNING);
            }
        } else {
            for (int i = tokens.length - 1; i >= 0; i--) {
                StringBuilder builder = new StringBuilder(tokens[0]);
                for (int n = 1; n <= i; n++) {
                    builder.append(".").append(tokens[n]);
                }
                MonitorLevel level = service.getProviderLevel(builder.toString());
                if (level != null) {
                    return new MonitorLogger(name, level);
                }
            }
            return new MonitorLogger(name, MonitorLevel.WARNING);
        }
    }
}
