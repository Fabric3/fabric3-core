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
package org.fabric3.spi.monitor;

/**
 *
 */
public class MonitorLocator {
    private static MonitorService MONITOR_SERVICE_INSTANCE;
    private static MonitorProxy MONITOR_PROXY;

    public static MonitorService getServiceInstance() {
        return MONITOR_SERVICE_INSTANCE;
    }

    public static void setInstance(MonitorService monitorService) {
        MONITOR_SERVICE_INSTANCE = monitorService;
    }

    public static MonitorProxy getProxy() {
        return MONITOR_PROXY;
    }

    public static void setMonitorProxy(MonitorProxy proxy) {
        MONITOR_PROXY = proxy;
    }
}
