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
package org.fabric3.implementation.spring.runtime.builder;

import java.lang.reflect.Method;
import java.net.URI;
import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;

import org.fabric3.api.host.Fabric3Exception;
import org.fabric3.implementation.pojo.builder.MethodUtils;
import org.fabric3.implementation.spring.provision.SpringWireTarget;
import org.fabric3.implementation.spring.runtime.component.SpringComponent;
import org.fabric3.implementation.spring.runtime.component.SpringInvoker;
import org.fabric3.spi.container.builder.TargetWireAttacher;
import org.fabric3.spi.container.component.ComponentManager;
import org.fabric3.spi.container.wire.InvocationChain;
import org.fabric3.spi.container.wire.Wire;
import org.fabric3.spi.model.physical.PhysicalOperation;
import org.fabric3.spi.model.physical.PhysicalWireSource;
import org.fabric3.spi.util.UriHelper;
import org.fabric3.spring.spi.WireListener;
import org.oasisopen.sca.annotation.EagerInit;
import org.oasisopen.sca.annotation.Reference;

/**
 * Attaches the target side of a wire to a Spring component.
 */
@EagerInit
public class SpringTargetWireAttacher implements TargetWireAttacher<SpringWireTarget> {
    private ComponentManager manager;
    private List<WireListener> listeners = Collections.emptyList();

    public SpringTargetWireAttacher(@Reference ComponentManager manager) {
        this.manager = manager;
    }

    @Reference(required = false)
    public void setListeners(List<WireListener> listeners) {
        this.listeners = listeners;
    }

    public void attach(PhysicalWireSource source, SpringWireTarget target, Wire wire) throws Fabric3Exception {
        String beanName = target.getBeanName();
        ClassLoader loader = target.getClassLoader();
        Class<?> interfaze;
        try {
            interfaze = loader.loadClass(target.getBeanInterface());
        } catch (ClassNotFoundException e) {
            throw new Fabric3Exception(e);
        }
        for (WireListener listener : listeners) {
            listener.onAttach(wire);
        }
        SpringComponent component = getComponent(target);
        for (InvocationChain chain : wire.getInvocationChains()) {
            PhysicalOperation operation = chain.getPhysicalOperation();
            Method beanMethod = MethodUtils.findMethod(source, target, operation, interfaze, loader);
            SpringInvoker invoker = new SpringInvoker(beanName, beanMethod, component);
            chain.addInterceptor(invoker);
        }
    }

    public Supplier<?> createSupplier(SpringWireTarget target) throws Fabric3Exception {
        throw new UnsupportedOperationException();
    }

    private SpringComponent getComponent(SpringWireTarget target) throws Fabric3Exception {
        URI uri = UriHelper.getDefragmentedName(target.getUri());
        SpringComponent component = (SpringComponent) manager.getComponent(uri);
        if (component == null) {
            throw new Fabric3Exception("Target not found: " + uri);
        }
        return component;
    }

}
