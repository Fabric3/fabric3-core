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
