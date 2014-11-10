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

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.Map;

import org.fabric3.api.MonitorChannel;
import org.fabric3.api.host.monitor.MonitorCreationException;
import org.fabric3.api.host.monitor.Monitorable;
import org.fabric3.monitor.impl.router.RingBufferDestinationRouter;
import org.fabric3.spi.monitor.DispatchInfo;
import org.fabric3.spi.monitor.MonitorProxy;
import org.oasisopen.sca.annotation.Reference;
import static org.fabric3.api.host.monitor.DestinationRouter.DEFAULT_DESTINATION;

/**
 *
 */
public class JDKRingBufferMonitorProxyService extends AbstractMonitorProxyService {

    public JDKRingBufferMonitorProxyService(@Reference RingBufferDestinationRouter router, @Reference Monitorable monitorable) {
        super(router, monitorable);
    }

    public <T> T createMonitor(Class<T> type, Monitorable monitorable, String destination) throws MonitorCreationException {
        if (destination == null) {
            destination = DEFAULT_DESTINATION;
        }
        int destinationIndex = router.getDestinationIndex(destination);
        ClassLoader loader = type.getClassLoader();
        Map<Method, DispatchInfo> levels = new HashMap<>();
        for (Method method : type.getMethods()) {
            DispatchInfo info = createDispatchInfo(type, loader, method);
            levels.put(method, info);
        }

        JDKMonitorHandler handler = new JDKMonitorHandler(destinationIndex, monitorable, router, levels, enabled);
        if (MonitorChannel.class.isAssignableFrom(type) || MonitorProxy.class.isAssignableFrom(type)) {
            return type.cast(handler);
        }
        return type.cast(Proxy.newProxyInstance(loader, new Class[]{type}, handler));
    }

}
