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
package org.fabric3.monitor.proxy;

import org.fabric3.api.host.monitor.MonitorCreationException;
import org.fabric3.api.host.monitor.MonitorProxyService;
import org.fabric3.api.host.monitor.MonitorProxyServiceExtension;
import org.fabric3.api.host.monitor.Monitorable;
import org.fabric3.spi.monitor.MonitorLocator;
import org.fabric3.spi.monitor.MonitorProxy;
import org.oasisopen.sca.annotation.Reference;

/**
 * Implementation that delegates to a default extension, which can be overridden via a wired extension service.
 */
public class MonitorProxyServiceImpl implements MonitorProxyService {
    private MonitorProxyServiceExtension extension;

    public MonitorProxyServiceImpl(JDKMonitorProxyService extension) {
        this.extension = extension;
        try {
            MonitorLocator.setMonitorProxy(extension.createMonitor(MonitorProxy.class));
        } catch (MonitorCreationException e) {
            throw new AssertionError(e);
        }
    }

    /**
     * Allows the default extension to overridden.
     *
     * @param extension the overriding extension
     */
    @Reference(required = false)
    public void setExtension(MonitorProxyServiceExtension extension) {
        this.extension = extension;
    }

    public <T> T createMonitor(Class<T> type) throws MonitorCreationException {
        return extension.createMonitor(type);
    }

    public <T> T createMonitor(Class<T> type, Monitorable monitorable, String destination) throws MonitorCreationException {
        return extension.createMonitor(type, monitorable, destination);
    }
}
