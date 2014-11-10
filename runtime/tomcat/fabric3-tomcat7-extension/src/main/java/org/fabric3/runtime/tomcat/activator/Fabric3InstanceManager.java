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
package org.fabric3.runtime.tomcat.activator;

import javax.naming.NamingException;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Map;

import org.apache.tomcat.InstanceManager;
import org.fabric3.spi.container.objectfactory.Injector;
import org.fabric3.spi.container.objectfactory.ObjectCreationException;

/**
 * Manages reference injection on servlet instances.
 */
public class Fabric3InstanceManager implements InstanceManager {
    private Map<String, List<Injector<?>>> injectorMappings;
    private ClassLoader contextClassLoader;

    public Fabric3InstanceManager(Map<String, List<Injector<?>>> injectors, ClassLoader classLoader) {
        this.injectorMappings = injectors;
        this.contextClassLoader = classLoader;
    }

    public void destroyInstance(Object o) {
    }

    public Object newInstance(String className)
            throws IllegalAccessException, InvocationTargetException, NamingException, InstantiationException, ClassNotFoundException {
        return inject(newInstance(className, contextClassLoader));
    }

    public void newInstance(Object instance) throws IllegalAccessException, InvocationTargetException, NamingException {
        inject(instance);
    }

    public Object newInstance(String className, ClassLoader cl)
            throws IllegalAccessException, InvocationTargetException, NamingException, InstantiationException, ClassNotFoundException {
        if (className.startsWith("org.apache.catalina")) {
            return Class.forName(className, true, InstanceManager.class.getClassLoader()).newInstance();
        }
        return Class.forName(className, true, cl).newInstance();
    }

    @SuppressWarnings({"unchecked"})
    private Object inject(Object instance) throws InvocationTargetException {
        if (instance == null) {
            return null;
        }
        List<Injector<?>> injectors = injectorMappings.get(instance.getClass().getName());
        if (injectors != null) {
            for (Injector injector : injectors) {
                try {
                    injector.inject(instance);
                } catch (ObjectCreationException e) {
                    throw new InvocationTargetException(e);
                }
            }
        }
        return instance;
    }


}
