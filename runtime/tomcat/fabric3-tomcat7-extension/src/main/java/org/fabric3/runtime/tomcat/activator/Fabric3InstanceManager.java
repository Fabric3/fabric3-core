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
package org.fabric3.runtime.tomcat.activator;

import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Map;
import javax.naming.NamingException;

import org.apache.tomcat.InstanceManager;

import org.fabric3.spi.objectfactory.Injector;
import org.fabric3.spi.objectfactory.ObjectCreationException;

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
    private Object inject(Object instance) throws IllegalAccessException, InvocationTargetException, NamingException {
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
