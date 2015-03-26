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
 *
 * Portions originally based on Apache Tuscany 2007
 * licensed under the Apache 2.0 license.
 */
package org.fabric3.implementation.system.runtime;

import java.lang.reflect.Method;
import java.net.URI;
import java.util.List;
import java.util.function.Supplier;

import org.fabric3.api.host.Fabric3Exception;
import org.fabric3.implementation.system.provision.SystemWireTarget;
import org.fabric3.spi.container.builder.component.TargetWireAttacher;
import org.fabric3.spi.container.component.ComponentManager;
import org.fabric3.spi.container.wire.InvocationChain;
import org.fabric3.spi.container.wire.Wire;
import org.fabric3.spi.model.physical.PhysicalOperation;
import org.fabric3.spi.model.physical.PhysicalWireSource;
import org.fabric3.spi.util.UriHelper;
import org.oasisopen.sca.annotation.EagerInit;
import org.oasisopen.sca.annotation.Reference;

/**
 *
 */
@EagerInit
public class SystemTargetWireAttacher implements TargetWireAttacher<SystemWireTarget> {

    private final ComponentManager manager;

    public SystemTargetWireAttacher(@Reference ComponentManager manager) {
        this.manager = manager;
    }

    public void attach(PhysicalWireSource source, SystemWireTarget target, Wire wire) throws Fabric3Exception {
        URI targetId = UriHelper.getDefragmentedName(target.getUri());
        SystemComponent targetComponent = (SystemComponent) manager.getComponent(targetId);

        Class<?> implementationClass = targetComponent.getImplementationClass();

        for (InvocationChain chain : wire.getInvocationChains()) {
            PhysicalOperation operation = chain.getPhysicalOperation();

            List<Class<?>> params = operation.getSourceParameterTypes();
            Method method;
            try {
                method = implementationClass.getMethod(operation.getName(), params.toArray(new Class[params.size()]));
            } catch (NoSuchMethodException e) {
                throw new Fabric3Exception("No matching method found", e);
            }

            SystemInvokerInterceptor interceptor = new SystemInvokerInterceptor(method, targetComponent);
            chain.addInterceptor(interceptor);
        }
    }

    public Supplier<?> createSupplier(SystemWireTarget target) throws Fabric3Exception {
        URI targetId = UriHelper.getDefragmentedName(target.getUri());
        SystemComponent targetComponent = (SystemComponent) manager.getComponent(targetId);
        return targetComponent.createSupplier();
    }
}