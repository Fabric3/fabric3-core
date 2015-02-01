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
package org.fabric3.monitor.impl.proxy;

import org.fabric3.api.host.Fabric3Exception;
import org.fabric3.api.host.monitor.MonitorProxyServiceExtension;
import org.fabric3.api.host.monitor.Monitorable;
import org.fabric3.monitor.impl.router.RingBufferDestinationRouter;
import org.oasisopen.sca.annotation.Init;
import org.oasisopen.sca.annotation.Property;
import org.oasisopen.sca.annotation.Reference;

/**
 * Switches between JDK and bytecode-generation for creating monitor proxies.
 */
public class SwitchingMonitorProxyServiceExtension implements MonitorProxyServiceExtension {
    public static final String ASYNCHRONOUS_MODE = "asynchronous";

    private RingBufferDestinationRouter router;
    private Monitorable monitorable;

    private boolean bytecodeGeneration;
    private boolean enabled = false;

    private MonitorProxyServiceExtension delegate;

    @Property(required = false)
    public void setProxy(String proxy) {
        this.bytecodeGeneration = "bytecode".equalsIgnoreCase(proxy);
    }

    @Property(required = false)
    public void setMode(String mode) {
        this.enabled = ASYNCHRONOUS_MODE.equalsIgnoreCase(mode);
    }

    public SwitchingMonitorProxyServiceExtension(@Reference RingBufferDestinationRouter router, @Reference Monitorable monitorable) {
        this.router = router;
        this.monitorable = monitorable;
    }

    @Init
    public void init() {
        if (bytecodeGeneration) {
            BytecodeMonitorProxyService byteCodeDelegate = new BytecodeMonitorProxyService(router, monitorable);
            byteCodeDelegate.setEnabled(enabled);
            delegate = byteCodeDelegate;
        } else {
            JDKRingBufferMonitorProxyService jdkDelegate = new JDKRingBufferMonitorProxyService(router, monitorable);
            jdkDelegate.setEnabled(enabled);
            delegate = jdkDelegate;
        }

    }

    public <T> T createMonitor(Class<T> type) throws Fabric3Exception {
        return delegate.createMonitor(type);
    }

    public <T> T createMonitor(Class<T> type, Monitorable monitorable, String destination) throws Fabric3Exception {
        return delegate.createMonitor(type, monitorable, destination);
    }
}
