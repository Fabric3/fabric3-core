/*
 * Fabric3
 * Copyright (c) 2009-2012 Metaform Systems
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

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.Map;

import org.fabric3.host.monitor.MonitorCreationException;
import org.fabric3.host.monitor.Monitorable;
import org.fabric3.host.runtime.HostInfo;
import org.fabric3.monitor.impl.router.RingBufferDestinationRouter;
import org.fabric3.spi.monitor.DispatchInfo;
import org.oasisopen.sca.annotation.Reference;
import static org.fabric3.host.monitor.DestinationRouter.DEFAULT_DESTINATION;

/**
 * Performs bytecode generation at runtime to create a monitor proxy.
 */
public class JDKRingBufferMonitorProxyService extends AbstractMonitorProxyService {

    public JDKRingBufferMonitorProxyService(@Reference RingBufferDestinationRouter router, @Reference Monitorable monitorable, @Reference HostInfo info) {
        super(router, monitorable, info);
    }

    public <T> T createMonitor(Class<T> type, Monitorable monitorable, String destination) throws MonitorCreationException {
        if (destination == null) {
            destination = DEFAULT_DESTINATION;
        }
        int destinationIndex = router.getDestinationIndex(destination);
        ClassLoader loader = type.getClassLoader();
        Map<String, DispatchInfo> levels = new HashMap<String, DispatchInfo>();
        for (Method method : type.getMethods()) {
            DispatchInfo info = createDispatchInfo(type, loader, method);
            levels.put(method.getName(), info);
        }

        JDKMonitorHandler handler = new JDKMonitorHandler(destinationIndex, runtimeName, monitorable, router, levels, timestampWriter, enabled);
        return type.cast(Proxy.newProxyInstance(loader, new Class[]{type}, handler));
    }

}
