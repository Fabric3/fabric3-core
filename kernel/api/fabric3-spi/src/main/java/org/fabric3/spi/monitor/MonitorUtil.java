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

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.fabric3.api.annotation.monitor.MonitorEventType;
import org.fabric3.api.annotation.monitor.MonitorLevel;

/**
 *
 */
public class MonitorUtil {

    /**
     * Introspects a method and returns its {@link DispatchInfo}.
     *
     * @param method monitor method
     * @return the annotated <code>LogLevels</code> value
     */
    public static DispatchInfo getDispatchInfo(Method method) {

        MonitorLevel level = null;
        String message = "";
        MonitorEventType annotation = method.getAnnotation(MonitorEventType.class);
        if (annotation != null) {
            level = annotation.value();
        }

        if (level == null) {
            for (Annotation methodAnnotation : method.getDeclaredAnnotations()) {
                Class<? extends Annotation> annotationType = methodAnnotation.annotationType();

                MonitorEventType monitorEventType = annotationType.getAnnotation(MonitorEventType.class);
                if (monitorEventType != null) {
                    level = monitorEventType.value();
                    try {
                        Method valueMethod = methodAnnotation.getClass().getMethod("value");
                        message = (String) valueMethod.invoke(methodAnnotation);
                    } catch (IllegalAccessException | InvocationTargetException e) {
                        throw new AssertionError(e);
                    } catch (NoSuchMethodException e) {
                        // ignore
                    }
                    return new DispatchInfo(level, message);
                }
            }
        }
        // default to debug
        return new DispatchInfo(MonitorLevel.DEBUG, "");
    }

}
