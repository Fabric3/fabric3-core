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
package org.fabric3.implementation.web.runtime;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.fabric3.implementation.pojo.reflection.FieldInjector;
import org.fabric3.implementation.pojo.reflection.MethodInjector;
import org.fabric3.implementation.web.provision.WebContextInjectionSite;
import org.fabric3.spi.model.type.java.FieldInjectionSite;
import org.fabric3.spi.model.type.java.InjectionSite;
import org.fabric3.spi.model.type.java.MethodInjectionSite;
import org.fabric3.spi.objectfactory.Injector;
import org.fabric3.spi.objectfactory.ObjectFactory;

/**
 * Default implementation of the InjectorFactory.
 */
public class InjectorFactoryImpl implements InjectorFactory {

    public void createInjectorMappings(Map<String, List<Injector<?>>> injectors,
                                       Map<String, Map<String, InjectionSite>> siteMappings,
                                       Map<String, ObjectFactory<?>> factories,
                                       ClassLoader classLoader) throws InjectionCreationException {
        for (Map.Entry<String, ObjectFactory<?>> entry : factories.entrySet()) {
            String siteName = entry.getKey();
            ObjectFactory<?> factory = entry.getValue();
            Map<String, InjectionSite> artifactMapping = siteMappings.get(siteName);
            if (artifactMapping == null) {
                throw new InjectionCreationException("Injection site not found for: " + siteName);
            }
            for (Map.Entry<String, InjectionSite> siteEntry : artifactMapping.entrySet()) {
                String artifactName = siteEntry.getKey();
                InjectionSite site = siteEntry.getValue();
                List<Injector<?>> injectorList = injectors.get(artifactName);
                if (injectorList == null) {
                    injectorList = new ArrayList<Injector<?>>();
                    injectors.put(artifactName, injectorList);
                }
                Injector<?> injector;
                if (site instanceof WebContextInjectionSite) {
                    injector = createInjector(siteName, factory, (WebContextInjectionSite) site);
                } else if (site instanceof FieldInjectionSite) {
                    injector = createInjector(factory, artifactName, (FieldInjectionSite) site, classLoader);
                } else if (site instanceof MethodInjectionSite) {
                    injector = createInjector(factory, artifactName, (MethodInjectionSite) site, classLoader);
                } else {
                    throw new UnsupportedOperationException("Unsupported injection site type: " + site.getClass());
                }
                injectorList.add(injector);
            }
        }
    }

    private Injector<?> createInjector(ObjectFactory<?> factory, String artifactName, MethodInjectionSite site, ClassLoader classLoader)
            throws InjectionCreationException {
        try {
            return new MethodInjector(getMethod(site, artifactName, classLoader), factory);
        } catch (ClassNotFoundException e) {
            throw new InjectionCreationException(e);
        } catch (NoSuchMethodException e) {
            throw new InjectionCreationException(e);
        }
    }

    private Injector<?> createInjector(ObjectFactory<?> factory, String artifactName, FieldInjectionSite site, ClassLoader classLoader)
            throws InjectionCreationException {
        try {
            return new FieldInjector(getField(site, artifactName, classLoader), factory);
        } catch (NoSuchFieldException e) {
            throw new InjectionCreationException(e);
        } catch (ClassNotFoundException e) {
            throw new InjectionCreationException(e);
        }
    }

    private Injector<?> createInjector(String referenceName, ObjectFactory<?> factory, WebContextInjectionSite site) {
        if (site.getContextType() == WebContextInjectionSite.ContextType.SERVLET_CONTEXT) {
            Injector<?> injector = new ServletContextInjector();
            injector.setObjectFactory(factory, referenceName);
            return injector;
        } else {
            Injector<?> injector = new HttpSessionInjector();
            injector.setObjectFactory(factory, referenceName);
            return injector;
        }
    }

    private Method getMethod(MethodInjectionSite methodSite, String implementationClass, ClassLoader classLoader)
            throws ClassNotFoundException, NoSuchMethodException {
        Class<?> clazz = classLoader.loadClass(implementationClass);
        return methodSite.getSignature().getMethod(clazz);
    }

    private Field getField(FieldInjectionSite site, String implementationClass, ClassLoader classLoader)
            throws NoSuchFieldException, ClassNotFoundException {
        Class<?> clazz = classLoader.loadClass(implementationClass);
        String name = site.getName();
        while (clazz != null) {
            try {
                return clazz.getDeclaredField(name);
            } catch (NoSuchFieldException e) {
                clazz = clazz.getSuperclass();
            }
        }
        throw new NoSuchFieldException(name);
    }

}
