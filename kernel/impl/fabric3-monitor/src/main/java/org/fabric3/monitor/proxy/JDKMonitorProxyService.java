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
package org.fabric3.monitor.proxy;

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import org.fabric3.api.MonitorChannel;
import org.fabric3.api.host.monitor.DestinationRouter;
import org.fabric3.api.host.monitor.MonitorCreationException;
import org.fabric3.api.host.monitor.MonitorProxyServiceExtension;
import org.fabric3.api.host.monitor.Monitorable;
import org.fabric3.spi.monitor.DispatchInfo;
import org.fabric3.spi.monitor.MonitorProxy;
import org.fabric3.spi.monitor.MonitorUtil;
import static org.fabric3.api.host.monitor.DestinationRouter.DEFAULT_DESTINATION;

/**
 * JDK-based implementation of the {@link MonitorProxyServiceExtension}.
 */
public class JDKMonitorProxyService implements MonitorProxyServiceExtension {
    private Monitorable defaultMonitorable;

    private DestinationRouter router;

    public JDKMonitorProxyService(Monitorable monitorable, DestinationRouter router) {
        this.defaultMonitorable = monitorable;
        this.router = router;
    }

    public <T> T createMonitor(Class<T> type) throws MonitorCreationException {
        return createMonitor(type, defaultMonitorable, DEFAULT_DESTINATION);
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

        JDKMonitorHandler handler = new JDKMonitorHandler(destinationIndex, monitorable, router, levels);
        if (MonitorChannel.class.isAssignableFrom(type) || MonitorProxy.class.isAssignableFrom(type)) {
            return type.cast(handler);
        }
        return type.cast(Proxy.newProxyInstance(loader, new Class[]{type}, handler));
    }

    private <T> DispatchInfo createDispatchInfo(Class<T> type, ClassLoader loader, Method method) {
        DispatchInfo info = MonitorUtil.getDispatchInfo(method);
        String message = info.getMessage();
        if (Locale.ENGLISH.getLanguage().equals(Locale.getDefault().getLanguage())) {
            if (message.length() > 0) {
                return info;
            } else if (message.length() == 0) {
                Class<?>[] types = method.getParameterTypes();
                if (types.length == 1 && String.class.equals(types[0])) {
                    // if there is no default message and the method takes a single String parameter, use the default formatting
                    info.setMessage("{0}");
                    return info;
                } else if (types.length == 2 && String.class.equals(types[0]) && Throwable.class.isAssignableFrom(types[1])) {
                    // if there is no default message and the method takes a String parameter and throwable, use the default formatting
                    info.setMessage("{0}");
                    return info;
                }
            }
        }
        String key = type.getName() + "#" + method.getName();
        ResourceBundle bundle = locateBundle(type, "f3", loader);
        if (bundle != null) {
            try {
                message = bundle.getString(key);
            } catch (MissingResourceException e) {
                // no resource, ignore
            }
        }
        if (message.length() == 0 && method.getParameterTypes().length == 0) {
            // if there are no params, set the message to the key
            message = key;
        }
        info.setMessage(message);
        return info;
    }

    private <T> ResourceBundle locateBundle(Class<T> monitorInterface, String bundleName, ClassLoader loader) {
        Locale locale = Locale.getDefault();
        String packageName = monitorInterface.getPackage().getName();
        while (true) {
            try {
                return ResourceBundle.getBundle(packageName + '.' + bundleName, locale, loader);
            } catch (MissingResourceException e) {
                //ok
            }
            int index = packageName.lastIndexOf('.');
            if (index == -1) {
                break;
            }
            packageName = packageName.substring(0, index);
        }
        try {
            return ResourceBundle.getBundle(bundleName, locale, loader);
        } catch (Exception e) {
            return null;
        }
    }

}
