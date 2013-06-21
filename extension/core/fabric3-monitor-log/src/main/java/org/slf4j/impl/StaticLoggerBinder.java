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
