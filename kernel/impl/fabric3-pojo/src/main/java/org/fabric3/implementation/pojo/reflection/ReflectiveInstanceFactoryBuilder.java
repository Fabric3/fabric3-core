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
 *
 * ----------------------------------------------------
 *
 * Portions originally based on Apache Tuscany 2007
 * licensed under the Apache 2.0 license.
 *
 */
package org.fabric3.implementation.pojo.reflection;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.osoa.sca.annotations.EagerInit;
import org.osoa.sca.annotations.Reference;

import org.fabric3.spi.model.type.java.ConstructorInjectionSite;
import org.fabric3.spi.model.type.java.Injectable;
import org.fabric3.spi.model.type.java.InjectionSite;
import org.fabric3.spi.model.type.java.Signature;
import org.fabric3.implementation.pojo.instancefactory.InstanceFactoryBuilder;
import org.fabric3.implementation.pojo.instancefactory.InstanceFactoryBuilderException;
import org.fabric3.implementation.pojo.provision.InstanceFactoryDefinition;
import org.fabric3.spi.classloader.ClassLoaderRegistry;

/**
 * Builds a reflection-based instance factory provider.
 *
 * @version $Date$ $Revision$
 */
@EagerInit
public class ReflectiveInstanceFactoryBuilder implements InstanceFactoryBuilder {
    private ClassLoaderRegistry classLoaderRegistry;

    public ReflectiveInstanceFactoryBuilder(@Reference ClassLoaderRegistry classLoaderRegistry) {
        this.classLoaderRegistry = classLoaderRegistry;
    }

    @SuppressWarnings("unchecked")
    public ReflectiveInstanceFactoryProvider build(InstanceFactoryDefinition definition, ClassLoader cl) throws InstanceFactoryBuilderException {

        try {
            String className = definition.getImplementationClass();
            Class<?> implClass = classLoaderRegistry.loadClass(cl, className);
            Constructor<?> ctr = getConstructor(implClass, definition.getConstructor());

            Map<InjectionSite, Injectable> injectionSites = definition.getConstruction();
            Injectable[] cdiSources = new Injectable[ctr.getParameterTypes().length];
            for (Map.Entry<InjectionSite, Injectable> entry : injectionSites.entrySet()) {
                InjectionSite site = entry.getKey();
                Injectable attribute = entry.getValue();
                ConstructorInjectionSite constructorSite = (ConstructorInjectionSite) site;
                cdiSources[constructorSite.getParam()] = attribute;
            }
            for (int i = 0; i < cdiSources.length; i++) {
                if (cdiSources[i] == null) {
                    String clazz = ctr.getName();
                    throw new InstanceFactoryBuilderException("No injection value for constructor parameter " + i + " in class " + clazz, clazz);
                }
            }

            Method initMethod = getMethod(implClass, definition.getInitMethod());
            Method destroyMethod = getMethod(implClass, definition.getDestroyMethod());

            Map<InjectionSite, Injectable> postConstruction = definition.getPostConstruction();
            List<Injectable> construction = Arrays.asList(cdiSources);
            boolean reinjectable = definition.isReinjectable();

            return new ReflectiveInstanceFactoryProvider(ctr, construction, postConstruction, initMethod, destroyMethod, reinjectable, cl);
        } catch (ClassNotFoundException ex) {
            throw new InstanceFactoryBuilderException(ex);
        } catch (NoSuchMethodException ex) {
            throw new InstanceFactoryBuilderException(ex);
        }
    }

    private Method getMethod(Class<?> implClass, Signature signature) throws NoSuchMethodException, ClassNotFoundException {
        return signature == null ? null : signature.getMethod(implClass);
    }

    private <T> Constructor<T> getConstructor(Class<T> implClass, Signature signature) throws ClassNotFoundException, NoSuchMethodException {
        Constructor<T> ctr = signature.getConstructor(implClass);
        ctr.setAccessible(true);
        return ctr;
    }


}
