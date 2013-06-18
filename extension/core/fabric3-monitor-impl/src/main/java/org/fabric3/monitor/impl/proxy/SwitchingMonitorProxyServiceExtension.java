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
package org.fabric3.monitor.impl.proxy;

import org.fabric3.host.monitor.MonitorCreationException;
import org.fabric3.host.monitor.MonitorProxyServiceExtension;
import org.fabric3.host.monitor.Monitorable;
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

    public <T> T createMonitor(Class<T> type) throws MonitorCreationException {
        return delegate.createMonitor(type);
    }

    public <T> T createMonitor(Class<T> type, Monitorable monitorable, String destination) throws MonitorCreationException {
        return delegate.createMonitor(type, monitorable, destination);
    }
}
