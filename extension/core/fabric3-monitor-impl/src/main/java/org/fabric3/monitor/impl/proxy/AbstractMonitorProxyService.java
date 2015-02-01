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
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.TimeZone;

import org.fabric3.api.host.Fabric3Exception;
import org.fabric3.api.host.monitor.MonitorProxyServiceExtension;
import org.fabric3.api.host.monitor.Monitorable;
import org.fabric3.monitor.impl.router.RingBufferDestinationRouter;
import org.fabric3.spi.monitor.DispatchInfo;
import org.fabric3.spi.monitor.MonitorUtil;
import org.oasisopen.sca.annotation.EagerInit;
import org.oasisopen.sca.annotation.Property;
import static org.fabric3.api.host.monitor.DestinationRouter.DEFAULT_DESTINATION;

/**
 *
 */
@EagerInit
public abstract class AbstractMonitorProxyService implements MonitorProxyServiceExtension {
    protected Monitorable defaultMonitorable;
    protected RingBufferDestinationRouter router;

    protected boolean enabled = false;
    protected String pattern = "%d:%m:%Y %H:%i:%s.%F";
    protected TimeZone timeZone = TimeZone.getDefault();

    public AbstractMonitorProxyService(RingBufferDestinationRouter router, Monitorable monitorable) {
        this.defaultMonitorable = monitorable;
        this.router = router;
    }

    @Property(required = false)
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public <T> T createMonitor(Class<T> type) throws Fabric3Exception {
        return createMonitor(type, defaultMonitorable, DEFAULT_DESTINATION);
    }

    protected <T> DispatchInfo createDispatchInfo(Class<T> type, ClassLoader loader, Method method) {
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
        ResourceBundle bundle = locateBundle(type, loader);
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

    protected <T> ResourceBundle locateBundle(Class<T> monitorInterface, ClassLoader loader) {
        Locale locale = Locale.getDefault();
        String packageName = monitorInterface.getPackage().getName();
        while (true) {
            try {
                return ResourceBundle.getBundle(packageName + ".f3", locale, loader);
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
            return ResourceBundle.getBundle("f3", locale, loader);
        } catch (Exception e) {
            return null;
        }
    }

}
