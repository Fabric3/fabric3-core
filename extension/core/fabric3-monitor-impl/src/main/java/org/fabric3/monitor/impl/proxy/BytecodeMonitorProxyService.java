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
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.TimeZone;

import org.fabric3.host.monitor.MonitorCreationException;
import org.fabric3.host.monitor.MonitorProxyServiceExtension;
import org.fabric3.host.monitor.Monitorable;
import org.fabric3.host.runtime.HostInfo;
import org.fabric3.monitor.impl.router.RingBufferDestinationRouter;
import org.fabric3.monitor.impl.writer.TimestampWriter;
import org.fabric3.spi.monitor.DispatchInfo;
import org.fabric3.spi.monitor.MonitorUtil;
import org.oasisopen.sca.annotation.Init;
import org.oasisopen.sca.annotation.Property;
import org.oasisopen.sca.annotation.Reference;
import static org.fabric3.host.monitor.DestinationRouter.DEFAULT_DESTINATION;

/**
 * Performs bytecode generation at runtime to create a monitor proxy.
 */
public class BytecodeMonitorProxyService implements MonitorProxyServiceExtension {
    private String runtimeName;
    private Monitorable defaultMonitorable;
    private RingBufferDestinationRouter router;
    private boolean enabled = true;

    private String pattern = "%d:%m:%Y %H:%i:%s.%F";
    private TimeZone timeZone = TimeZone.getDefault();

    private TimestampWriter timestampWriter;

    public BytecodeMonitorProxyService(@Reference RingBufferDestinationRouter router, @Reference Monitorable monitorable, @Reference HostInfo info) {
        this.runtimeName = info.getRuntimeName();
        this.defaultMonitorable = monitorable;
        this.router = router;
    }

    @Property(required = false)
    public void setPattern(String pattern) {
        this.pattern = pattern;
    }

    @Property(required = false)
    public void setTimeZone(String id) {
        this.timeZone = TimeZone.getTimeZone(id);
    }

    @Property(required = false)
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    @Init
    public void init() {
        this.timestampWriter = new TimestampWriter(pattern, timeZone);
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
        Map<String, DispatchInfo> levels = new HashMap<String, DispatchInfo>();
        for (Method method : type.getMethods()) {
            DispatchInfo info = createDispatchInfo(type, loader, method);
            levels.put(method.getName(), info);
        }

        AbstractMonitorHandler handler = new AbstractMonitorHandler(destinationIndex, runtimeName, monitorable, router, levels, timestampWriter, enabled);
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
