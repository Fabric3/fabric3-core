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
package org.fabric3.implementation.junit.runtime;

import java.lang.reflect.Method;
import java.net.URI;

import org.fabric3.implementation.java.runtime.JavaComponent;
import org.fabric3.implementation.junit.provision.JUnitTargetDefinition;
import org.fabric3.implementation.pojo.builder.MethodUtils;
import org.fabric3.implementation.pojo.component.InvokerInterceptor;
import org.fabric3.implementation.pojo.provision.PojoSourceDefinition;
import org.fabric3.implementation.pojo.spi.reflection.ReflectionFactory;
import org.fabric3.implementation.pojo.spi.reflection.ServiceInvoker;
import org.fabric3.spi.builder.WiringException;
import org.fabric3.spi.builder.component.TargetWireAttacher;
import org.fabric3.spi.builder.component.WireAttachException;
import org.fabric3.spi.classloader.ClassLoaderRegistry;
import org.fabric3.spi.component.ComponentManager;
import org.fabric3.spi.component.Component;
import org.fabric3.spi.model.physical.PhysicalOperationDefinition;
import org.fabric3.spi.model.physical.PhysicalSourceDefinition;
import org.fabric3.spi.objectfactory.ObjectFactory;
import org.fabric3.spi.util.UriHelper;
import org.fabric3.spi.wire.InvocationChain;
import org.fabric3.spi.wire.Wire;
import org.junit.Test;
import org.oasisopen.sca.annotation.Reference;

/**
 * Attaches and detaches wires from Java components.
 */
public class JUnitTargetWireAttacher implements TargetWireAttacher<JUnitTargetDefinition> {

    private final ComponentManager manager;
    private ReflectionFactory reflectionFactory;
    private final ClassLoaderRegistry classLoaderRegistry;

    public JUnitTargetWireAttacher(@Reference ComponentManager manager,
                                   @Reference ReflectionFactory reflectionFactory,
                                   @Reference ClassLoaderRegistry classLoaderRegistry) {
        this.manager = manager;
        this.reflectionFactory = reflectionFactory;
        this.classLoaderRegistry = classLoaderRegistry;
    }

    public void attach(PhysicalSourceDefinition sourceDefinition, JUnitTargetDefinition targetDefinition, Wire wire) throws WireAttachException {
        URI targetName = UriHelper.getDefragmentedName(targetDefinition.getUri());
        Component component = manager.getComponent(targetName);
        if (component == null) {
            throw new WireAttachException("Target not found: " + targetName);
        }
        JavaComponent target = (JavaComponent) component;

        Class<?> implementationClass = target.getImplementationClass();
        ClassLoader loader = classLoaderRegistry.getClassLoader(targetDefinition.getClassLoaderId());

        // attach the invoker interceptor to forward invocation chains
        for (InvocationChain chain : wire.getInvocationChains()) {
            PhysicalOperationDefinition operation = chain.getPhysicalOperation();
            Method method = MethodUtils.findMethod(sourceDefinition, targetDefinition, operation, implementationClass, loader, classLoaderRegistry);
            ServiceInvoker invoker = reflectionFactory.createServiceInvoker(method);
            InvokerInterceptor interceptor;
            if (sourceDefinition instanceof PojoSourceDefinition && targetDefinition.getClassLoaderId().equals(sourceDefinition.getClassLoaderId())) {
                // if the source is Java and target classloaders are equal, do not set the TCCL
                interceptor = new InvokerInterceptor(invoker, target);
            } else {
                // If the source and target classloaders are not equal, configure the interceptor to set the TCCL to the target classloader
                // when dispatching to a target instance. This guarantees when application code executes, it does so with the TCCL set to the
                // target component's classloader.
                interceptor = new InvokerInterceptor(invoker, target, loader);
            }
            Test annotation = method.getAnnotation(Test.class);
            if (annotation != null) {
                Class<? extends Throwable> expected = annotation.expected();
                if (!expected.equals(Test.None.class)) {
                    ExpectedExceptionInterceptor exceptionInterceptor = new ExpectedExceptionInterceptor(expected);
                    chain.addInterceptor(exceptionInterceptor);
                }
            }
            chain.addInterceptor(interceptor);
        }
    }

    public void detach(PhysicalSourceDefinition source, JUnitTargetDefinition target) throws WiringException {
        // no-op
    }

    public ObjectFactory<?> createObjectFactory(JUnitTargetDefinition target) throws WiringException {
        URI targetId = UriHelper.getDefragmentedName(target.getUri());
        JavaComponent targetComponent = (JavaComponent) manager.getComponent(targetId);
        return targetComponent.createObjectFactory();
    }

}