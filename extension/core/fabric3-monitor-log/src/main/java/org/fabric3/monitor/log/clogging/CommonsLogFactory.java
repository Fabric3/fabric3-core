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
package org.fabric3.monitor.log.clogging;

import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogConfigurationException;
import org.apache.commons.logging.LogFactory;
import org.fabric3.api.annotation.monitor.MonitorLevel;
import org.fabric3.spi.monitor.MonitorLocator;
import org.fabric3.spi.monitor.MonitorService;

/**
 *
 */
public class CommonsLogFactory extends LogFactory {
    private static final Pattern PATTERN = Pattern.compile("\\.");
    private static final String[] ATTRIBUTES = new String[0];

    public Object getAttribute(String name) {
        return null;
    }

    public String[] getAttributeNames() {
        return ATTRIBUTES;
    }

    public Log getInstance(Class clazz) throws LogConfigurationException {
        return getInstance(clazz.getName());
    }

    public Log getInstance(String name) throws LogConfigurationException {
        MonitorService service = MonitorLocator.getServiceInstance();
        String[] tokens = PATTERN.split(name);
        if (tokens.length == 0) {
            return new MonitorLog(MonitorLevel.INFO);
        } else if (tokens.length == 1) {
            MonitorLevel level = service.getProviderLevel(name);
            if (level != null) {
                return new MonitorLog(level);
            } else {
                return new MonitorLog(MonitorLevel.INFO);
            }
        } else {

            for (int i = tokens.length - 1; i >= 0; i--) {
                StringBuilder builder = new StringBuilder(tokens[0]);
                for (int n = 1; n <= i; n++) {
                    builder.append(".").append(tokens[n]);
                }
                MonitorLevel level = service.getProviderLevel(builder.toString());
                if (level != null) {
                    return new MonitorLog(level);
                }
            }
            return new MonitorLog(MonitorLevel.INFO);
        }
    }

    public void release() {
    }

    public void removeAttribute(String name) {
    }

    public void setAttribute(String name, Object value) {
    }
}
