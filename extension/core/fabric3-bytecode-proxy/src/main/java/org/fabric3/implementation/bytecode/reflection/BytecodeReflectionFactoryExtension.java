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
 *
 */
package org.fabric3.implementation.bytecode.reflection;

import java.lang.reflect.Constructor;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import org.fabric3.host.Names;
import org.fabric3.implementation.bytecode.proxy.common.BytecodeClassLoader;
import org.fabric3.implementation.pojo.spi.reflection.LifecycleInvoker;
import org.fabric3.implementation.pojo.spi.reflection.ReflectionFactoryExtension;
import org.fabric3.implementation.pojo.spi.reflection.TargetInvoker;
import org.fabric3.spi.builder.classloader.ClassLoaderListener;
import org.fabric3.spi.classloader.ClassLoaderRegistry;
import org.fabric3.spi.classloader.MultiParentClassLoader;
import org.fabric3.spi.objectfactory.ObjectFactory;
import org.oasisopen.sca.annotation.EagerInit;
import org.oasisopen.sca.annotation.Reference;

/**
 * Factory that uses bytecode generation to implement reflection capabilities.
 */
@EagerInit
public class BytecodeReflectionFactoryExtension implements ReflectionFactoryExtension, ClassLoaderListener {

    private TargetInvokerFactory targetInvokerFactory;
    private InjectorFactory injectorFactory;
    private LifecycleInvokerFactory lifecycleInvokerFactory;
    private ClassLoaderRegistry classLoaderRegistry;

    private Map<URI, BytecodeClassLoader> classLoaderCache = new HashMap<URI, BytecodeClassLoader>();

    public BytecodeReflectionFactoryExtension(@Reference TargetInvokerFactory targetInvokerFactory,
                                              @Reference InjectorFactory injectorFactory,
                                              @Reference LifecycleInvokerFactory lifecycleInvokerFactory,
                                              @Reference ClassLoaderRegistry classLoaderRegistry) {
        this.targetInvokerFactory = targetInvokerFactory;
        this.injectorFactory = injectorFactory;
        this.lifecycleInvokerFactory = lifecycleInvokerFactory;
        this.classLoaderRegistry = classLoaderRegistry;
    }

    public TargetInvoker createTargetInvoker(Method method) {
        BytecodeClassLoader classLoader = getClassLoader(method);
        return targetInvokerFactory.createTargetInvoker(method, classLoader);
    }

    public <T> ObjectFactory<T> createInstantiator(Constructor<T> constructor, ObjectFactory<?>[] parameterFactories) {
        return new ReflectiveObjectFactory(constructor, parameterFactories);
    }

    public LifecycleInvoker createLifecycleInvoker(Method method) {
        BytecodeClassLoader classLoader = getClassLoader(method);
        return lifecycleInvokerFactory.createLifecycleInvoker(method, classLoader);
    }

    public BytecodeInjector createInjector(Member member, ObjectFactory<?> parameterFactory) {
        BytecodeClassLoader classLoader = getClassLoader(member);
        return injectorFactory.createInjector(member, parameterFactory, classLoader);
    }

    public void onDeploy(ClassLoader classLoader) {
        // no-ip
    }

    public void onUndeploy(ClassLoader classLoader) {
        if (!(classLoader instanceof MultiParentClassLoader)) {
            return;
        }
        // remove cached classloader for the contribution on undeploy
        classLoaderCache.remove(((MultiParentClassLoader) classLoader).getName());
    }

    /**
     * Returns a classloader for loading the proxy class, creating one if necessary.
     *
     * @return the classloader
     */
    private BytecodeClassLoader getClassLoader(Member method) {

        URI classLoaderKey;
        ClassLoader classLoader = method.getDeclaringClass().getClassLoader();
        if (classLoader instanceof MultiParentClassLoader) {
            classLoaderKey = ((MultiParentClassLoader) classLoader).getName();
        } else {
            classLoaderKey = Names.BOOT_CONTRIBUTION;
        }

        ClassLoader parent = classLoaderRegistry.getClassLoader(classLoaderKey);
        BytecodeClassLoader generationClassLoader = classLoaderCache.get(classLoaderKey);
        if (generationClassLoader == null) {
            generationClassLoader = new BytecodeClassLoader(classLoaderKey, parent);
            generationClassLoader.addParent(getClass().getClassLoader()); // SPI classes need to be visible as well
            classLoaderCache.put(classLoaderKey, generationClassLoader);
        }
        return generationClassLoader;
    }
}
