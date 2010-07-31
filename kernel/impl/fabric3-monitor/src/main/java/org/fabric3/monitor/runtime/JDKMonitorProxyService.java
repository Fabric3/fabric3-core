/*
 * Fabric3
 * Copyright (c) 2009 Metaform Systems
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
package org.fabric3.monitor.runtime;

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.URI;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import org.fabric3.api.annotation.monitor.MonitorLevel;
import org.fabric3.host.monitor.MonitorCreationException;
import org.fabric3.host.monitor.MonitorProxyService;
import org.fabric3.host.monitor.Monitorable;
import org.fabric3.spi.channel.Channel;
import org.fabric3.spi.channel.ChannelManager;
import org.fabric3.spi.channel.EventStreamHandler;
import org.fabric3.spi.channel.PassThroughHandler;

/**
 * The default MonitorProxyService that uses JDK proxies.
 * <p/>
 * Messages are formatted using an f3.properties resource bundle visible from the monitor interface classloader. Resource bundles will be searched
 * using a directory hierarchy derived from the monitor interface package.
 *
 * @version $Rev$ $Date$
 */
public class JDKMonitorProxyService implements MonitorProxyService {
    private ChannelManager channelManager;
    private Monitorable defaultMonitorable;

    public JDKMonitorProxyService(Monitorable monitorable, ChannelManager channelManager) {
        this.defaultMonitorable = monitorable;
        this.channelManager = channelManager;
    }

    public <T> T createMonitor(Class<T> type, URI channelUri) throws MonitorCreationException {
        return createMonitor(type, defaultMonitorable, channelUri);
    }

    public <T> T createMonitor(Class<T> type, Monitorable monitorable, URI channelUri) throws MonitorCreationException {
        Channel channel = channelManager.getChannel(channelUri);
        if (channel == null) {
            throw new ChannelNotFoundException("Channel not found: " + channelUri);
        }

        EventStreamHandler streamHandler = new PassThroughHandler();
        channel.attach(streamHandler);

        ClassLoader loader = type.getClassLoader();
        ResourceBundle bundle = locateBundle(type, "f3", loader);

        Map<String, DispatchInfo> levels = new HashMap<String, DispatchInfo>();
        for (Method method : type.getMethods()) {
            MonitorLevel level = MonitorLevel.getAnnotatedLogLevel(method);
            String key = type.getName() + "#" + method.getName();
            String message = null;
            if (bundle != null) {
                try {
                    message = bundle.getString(key);
                } catch (MissingResourceException e) {
                    // no resource, ignore
                }
            }
            if (message == null && method.getParameterTypes().length == 0) {
                // if there are no params, set the message to the key
                message = key;
            }
            levels.put(method.getName(), new DispatchInfo(level, message));
        }

        MonitorHandler handler = new MonitorHandler(monitorable, streamHandler, levels);
        return type.cast(Proxy.newProxyInstance(loader, new Class[]{type}, handler));
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
